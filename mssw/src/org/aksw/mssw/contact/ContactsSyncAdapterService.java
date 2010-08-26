package org.aksw.mssw.contact;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Stack;

import org.aksw.mssw.Constants;

import dalvik.system.PathClassLoader;

import android.R.integer;
import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Entity;
import android.util.Log;

public class ContactsSyncAdapterService extends Service {

	private static String TAG = "MsswContactsSyncAdapterService";
	private static SyncAdapter syncAdapter;

	private static Context context;
	private static ContentResolver content;

	private static final String CONTENT_AUTHORITY = "org.aksw.mssw.content.foafprovider";
	private static final Uri CONTENT_URI = Uri.parse("content://"
			+ CONTENT_AUTHORITY);

	private static final String CONTACT_AUTHORITY = "org.aksw.mssw.contact.contactprovider";
	private static final Uri CONTACT_URI = Uri.parse("content://"
			+ CONTACT_AUTHORITY);

	@Override
	public IBinder onBind(Intent intent) {
		if (syncAdapter == null) {
			syncAdapter = new SyncAdapter(getApplicationContext(), true);
		}
		return syncAdapter.getSyncAdapterBinder();
	}

	public static void performSync(Context contextIn, Account account,
			Bundle extras, String authority, ContentProviderClient provider,
			SyncResult syncResult) throws OperationCanceledException {

		Log.i(TAG, "perform Sync :-) " + account.toString());

		context = contextIn;

		if (content == null) {
			content = context.getContentResolver();
		}

		// maybe should also tell Foaf/TripleProvider to pull the latest
		// versions from the Web
		// get the friend list from FoafProvider
		Uri contentUri = Uri.parse(CONTENT_URI + "/me/friends/");
		Log.v(TAG, "Starting Query with uri: <" + contentUri.toString() + ">.");
		Cursor rc = content.query(contentUri, null, null, null, null);

		// get the contact list from contacts data table
		Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name)
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, account.type)
				.build();
		Cursor cc = content.query(rawContactUri, new String[] {
				BaseColumns._ID, RawContacts.SYNC1 }, null, null, null);

		HashMap<String, Long> localContacts = new HashMap<String, Long>();
		while (cc.moveToNext()) {
			localContacts.put(cc.getString(1), cc.getLong(0));
		}

		// 1: the both diffs could maybe be improved by something like a
		// diff-bitmap (see Bernhard Schandl mobisem)
		// find out, which persons are missing and which are common (Person
		// Diff) [1]
		String uri;
		while (rc.moveToNext()) {
			uri = rc.getString(rc.getColumnIndex("object"));
			if (localContacts.get(uri) == null) {
				// add missing persons to contacts
				addContact(uri, account);
			} else {
				// for each already existing Person find out, which data has
				// changed (Data Diff) [1]
				updateContact(uri, localContacts.get(uri));
			}
		}

	}

	/**
	 * Add a contact to the Android system addressbook
	 * 
	 * @param uri
	 * @param account
	 */
	private static void addContact(String uri, Account account) {

		Log.i(TAG, "Adding contact: " + uri);

		try {
			// 1. query hasData Properties
			// 2. for each hasData Property
			// 2.1 query hasData Properties object
			// 2.2 check rdf:type (?s a ?o) an set as Mime-Type
			// 2.3 for each hasData Property
			// 2.3.1 check uri of property and set as column name (as in 2.)
			// 2.3.2 check whether ?o is a literal or resource
			// 2.3.2.1 literal: insert as data
			// 2.3.2.2 resource: get object and insert (as in 2.)

			// have to check if any data was added to the builder.

			LinkedHashMap<String, ContentProviderOperation.Builder> builderList;
			builderList = new LinkedHashMap<String, ContentProviderOperation.Builder>();

			ContentProviderOperation.Builder builder = ContentProviderOperation
					.newInsert(RawContacts.CONTENT_URI);
			builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
			builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
			builder.withValue(RawContacts.SYNC1, uri);
			builderList.put(uri, builder);

			Uri contentUri = Uri.parse(CONTACT_URI + "/data/"
					+ URLEncoder.encode(uri, Constants.ENC));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");
			Cursor rc = content.query(contentUri, null, null, null, null);

			if (rc != null) {
				String subject, predicat, object;

				Log.i(TAG, "== Initializing hasData properties. ==");
				/**
				 * Initializing all hasData Properties of this contact
				 */
				while (rc.moveToNext()) {
					predicat = rc.getString(rc.getColumnIndex("predicat"));
					Log.v(TAG, "predicat = " + predicat);

					if (predicat != null
							&& predicat.equals(Constants.PROP_hasData)) {
						object = rc.getString(rc.getColumnIndex("object"));

						if (!builderList.containsKey(object)) {
							builder = ContentProviderOperation
									.newInsert(ContactsContract.Data.CONTENT_URI);
							builder.withValueBackReference(
									ContactsContract.RawContactsEntity.RAW_CONTACT_ID,
									0);
							builderList.put(object, builder);
						} else {
							Log.i(TAG, "Builder List already has this data: "
									+ object);
						}
					}
				}

				// reset cursor before first element, as if it would be new
				rc.moveToPosition(-1);

				Log.i(TAG, "== Initializing propertys of hasData objects. ==");
				/**
				 * get the properties of the nodes, which are the in the range
				 * of a hasData property
				 */
				while (rc.moveToNext()) {
					predicat = rc.getString(rc.getColumnIndex("predicat"));
					Log.v(TAG, "predicat = " + predicat);

					if (predicat != null
							&& !predicat.equals(Constants.PROP_hasData)) {
						subject = rc.getString(rc.getColumnIndex("subject"));
						object = rc.getString(rc.getColumnIndex("object"));
						builder = builderList.get(subject);

						try {
							if (predicat.equals(Constants.PROP_rdfType)
									&& object
											.startsWith(Constants.DATA_KINDS_PREFIX)) {
								Log.v(TAG, "rdf:type = " + object + ".");
								// this is the place of magic
								String type = (String) forUri(object, false)
										.getField("CONTENT_ITEM_TYPE")
										.get(null);

								Log.v(TAG, "Goind to Add Mimetype: " + type
										+ ".");
								builder.withValue(
										ContactsContract.Data.MIMETYPE, type);

							} else if (predicat
									.startsWith(Constants.DATA_KINDS_PREFIX)) {
								boolean isResource;
								if (rc.getString(
										rc.getColumnIndex("oIsResource"))
										.equals("true")) {
									isResource = true;
								} else {
									isResource = false;
								}

								/**
								 * this is the place of magic extract the last
								 * part of the URI, which determines a property
								 * of the given class and get a Class object
								 * representing the class given in the uri. than
								 * get the specified property from this class.
								 */
								String fieldName = extractFieldName(predicat);
								String column = (String) forUri(predicat, true)
										.getField(fieldName).get(null);

								if (isResource
										&& object
												.startsWith(Constants.DATA_KINDS_PREFIX)) {
									// this is the place of magic
									fieldName = extractFieldName(object);
									Field valueField = forUri(object, true)
											.getField(fieldName);

									// Type is int
									// Protocol is string
									if (valueField.getType().getName()
											.equalsIgnoreCase("int")) {
										int value = (Integer) valueField
												.get(null);
										builder.withValue(column, value);

										Log.v(TAG, "Added value: " + value
												+ " to column: " + column + ".");
									} else if (valueField
											.getType()
											.getName()
											.equalsIgnoreCase(
													"java.lang.String")) {
										String value = (java.lang.String) valueField
												.get(null);
										builder.withValue(column, value);
									} else {
										Log.e(TAG,
												"I don't know the Type of the field: '"
														+ object + "'.");
									}

								} else {
									builder.withValue(column, object);
								}
							} else if (predicat.equals(Constants.PROP_rdfType)) {
								Log.v(TAG,
										"The given object <"
												+ object
												+ "> is not a valide type. (ignorring this triple)");
							} else {
								Log.v(TAG,
										"The given predicat <"
												+ predicat
												+ "> is not valide. (ignorring this triple)");
							}

						} catch (ClassNotFoundException e) {
							Log.e(TAG,
									"There was a error to find the according Class. (ignorring this triple)",
									e);
						} catch (Exception e) {
							Log.e(TAG,
									"Couldn't interpres the Triple subject = '"
											+ subject
											+ "', predicat = '"
											+ predicat
											+ "', object = '"
											+ object
											+ "' I'll ignorre it and am proceding with next Property.",
									e);
						}

						builderList.put(subject, builder);
					}
				}

			} else {
				Log.e(TAG,
						"Contentprovider returned an empty Cursor, couldn't add Data to contact '"
								+ uri + "'.");
				Log.v(TAG, "Destroy builders, to avoid '(Unknown)'-Contacts.");
				builderList.clear();
			}

			if (builderList.size() > 1) {
				ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
				Iterator<String> builderIterator = builderList.keySet()
						.iterator();

				while (builderIterator.hasNext()) {
					builder = builderList.get(builderIterator.next());
					operationList.add(builder.build());
				}

				content.applyBatch(ContactsContract.AUTHORITY, operationList);
			}

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
		} catch (Exception e) {
			Log.e(TAG, "An other error occured.", e);
		}

	}

	private static void updateContact(String uri, long rawId) {

		Log.i(TAG, "updating contact: " + uri);

		try {

			Uri contactUri = Uri.parse(CONTACT_URI + "/data/"
					+ URLEncoder.encode(uri, Constants.ENC));

			Cursor rc = content.query(contactUri, null, null, null, null);

			Uri rawUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
					rawId);
			Uri entityUri = Uri.withAppendedPath(rawUri,
					Entity.CONTENT_DIRECTORY);
			Cursor cc = content.query(entityUri, null, null, null, null);

			if (rc != null && cc != null) {

				ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
				HashMap<String, HashMap<String, String>> dataList = new HashMap<String, HashMap<String, String>>();

				HashMap<String, String> data;

				while (rc.moveToNext()) {

					String subject = rc.getString(rc.getColumnIndex("subject"));
					String predicat = rc.getString(rc
							.getColumnIndex("predicat"));
					String object = rc.getString(rc.getColumnIndex("object"));
					boolean isResource = rc.getString(
							rc.getColumnIndex("oIsResource")).equals("true");

					if (predicat.equals(Constants.PROP_hasData)) {
						if (!dataList.containsKey(object)) {
							dataList.put(object, new HashMap<String, String>());
						}
					} else if (predicat.equals(Constants.PROP_rdfType)
							&& object.startsWith(Constants.DATA_KINDS_PREFIX)) {
						// check mimetype
						if (!dataList.containsKey(object)) {
							dataList.put(object, new HashMap<String, String>());
						}

						Log.v(TAG, "rdf:type = " + object + ".");
						// this is the place of magic
						String type = (String) forUri(object, false).getField(
								"CONTENT_ITEM_TYPE").get(null);

						data = dataList.get(object);
						data.put(Entity.MIMETYPE, type);
						dataList.put(object, data);
					} else if (predicat.startsWith(Constants.DATA_KINDS_PREFIX)) {

						String fieldName = extractFieldName(predicat);
						String column = (String) forUri(predicat, true)
								.getField(fieldName).get(null);

						if (isResource
								&& object
										.startsWith(Constants.DATA_KINDS_PREFIX)) {

							if (dataList.containsKey(object)) {
								data = dataList.get(object);
							} else {
								data = new HashMap<String, String>();
							}

							// this is the place of magic
							fieldName = extractFieldName(object);
							Field valueField = forUri(object, true).getField(
									fieldName);

							// Type is int
							// Protocol is string
							if (valueField.getType().getName()
									.equalsIgnoreCase("int")) {
								int value = (Integer) valueField.get(null);

								data.put(column, Integer.toString(value));
							} else if (valueField.getType().getName()
									.equalsIgnoreCase("java.lang.String")) {
								object = (java.lang.String) valueField
										.get(null);

								data.put(column, object);
							} else {
								Log.e(TAG,
										"I don't know the Type of the field: '"
												+ object + "'.");
							}

							dataList.put(object, data);
						} else {
							if (dataList.containsKey(object)) {
								data = dataList.get(object);
							} else {
								data = new HashMap<String, String>();
							}
							data.put(column, object);
							dataList.put(object, data);
						}
					} else {
						Log.v(TAG, "Unknown predicat <" + predicat
								+ "> or unknown object <" + object + ">.");
					}

				}

				while (cc.moveToNext()) {

					String sourcId = cc.getString(cc
							.getColumnIndex(RawContacts.SOURCE_ID));

					if (!cc.isNull(cc.getColumnIndex(Entity.DATA_ID))) {

						// for one value kinds:
						// should get the current value of a field
						// compare it to the value from ContactProvider
						// if different set the ContactProvider value

						// or for multivalue kinds:
						// get all values of these fields
						// compare all of these values with all values from
						// ContactProvider
						// if one of ContactProvider is missing in Contacts, add
						// it
						// else do nothing

						String mimeType = cc.getString(cc.getColumnIndex(Entity.MIMETYPE));
						

						// search dataList for entries, which could fit

						if (mimeType
								.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
							String ccName = cc
									.getString(cc
											.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
							String rcName = rc.getString(rc
									.getColumnIndex("objectReadable"));
							if (!ccName.equals(rcName)) {
								Log.v(TAG, "Updating name from " + ccName
										+ " to " + rcName + " of " + uri);

								ContentProviderOperation.Builder builder = ContentProviderOperation
										.newUpdate(ContactsContract.Data.CONTENT_URI);
								builder.withSelection(
										BaseColumns._ID
												+ " = '"
												+ cc.getLong(cc
														.getColumnIndex(Entity.DATA_ID))
												+ "'", null);
								builder.withValue(
										ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
										rcName);
								operationList.add(builder.build());

							} else {
								Log.v(TAG, "Doesn't need to update " + uri);
							}
						}
					}
				}
				// reset cc
				cc.moveToPosition(-1);

				cc.close();

				try {
					if (operationList.size() > 0) {
						content.applyBatch(ContactsContract.AUTHORITY,
								operationList);
					}
				} catch (Exception e) {
					Log.e(TAG,
							"Error on batch applying changes on the contacts list.",
							e);
				}
			} else {
				Log.e(TAG,
						"ContentProvider or ContactsContract returned an empty Cursor, don't know what to do.");
			}

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
		} catch (Exception e) {
			Log.e(TAG, "An other error occured.", e);
		}
	}

	private static Class forUri(String uri, boolean isField)
			throws ClassNotFoundException {
		if (uri.startsWith(Constants.DATA_KINDS_PREFIX)) {
			String classNamePrefix = "android.provider.";
			String className;
			Uri uri_ = Uri.parse(uri);

			ArrayList<String> path = new ArrayList<String>(
					uri_.getPathSegments());

			if (isField) {
				String fullFieldName = path.get(path.size() - 1);
				className = fullFieldName.substring(0,
						fullFieldName.lastIndexOf("."));
			} else {
				className = path.get(path.size() - 1);
			}

			className = classNamePrefix + className;

			Stack<String> nestedPath = new Stack<String>();
			Class klasse;
			while (className.lastIndexOf(".") > 0) {
				try {
					klasse = Class.forName(className);
					break;
				} catch (ClassNotFoundException e) {
					nestedPath.add(className.substring(className
							.lastIndexOf(".") + 1));
					className = className.substring(0,
							className.lastIndexOf("."));
				}
			}

			while (!nestedPath.empty()) {
				className += "$" + nestedPath.pop();
			}

			// Log.v(TAG, "Classname is: '" + className + "'.");

			klasse = Class.forName(className);

			// return commonDataKinds.get(className);
			return klasse;
		} else {
			Log.v(TAG, "The given Uri <" + uri
					+ "> is not in the correct domain <"
					+ Constants.DATA_KINDS_PREFIX + " ...>.");
			throw new ClassNotFoundException("The given Uri <" + uri
					+ "> is not in the correct domain <"
					+ Constants.DATA_KINDS_PREFIX + " ...>.");
		}
	}

	private static String extractFieldName(String uri) {
		Uri uri_ = Uri.parse(uri);

		ArrayList<String> path = new ArrayList<String>(uri_.getPathSegments());

		if (path.size() > 0) {
			String fullFieldName = path.get(path.size() - 1);
			String fieldName = fullFieldName.substring(fullFieldName
					.lastIndexOf(".") + 1);
			// Log.v(TAG, "Fieldname ist: '" + fieldName + "'.");
			return fieldName;
		} else {
			Log.v(TAG, "Couldn't extract fieldname  from uri <" + uri + ">.");
			return null;
		}

	}

	private static void testMethod() {

		// public static final class
		// the last two points are replaced by $ because they are nested classes
		// :-S
		String className = "android.provider.ContactsContract$CommonDataKinds$StructuredName";
		// String className = "android.provider.CallLog";

		Log.v(TAG, "Now I try some magic.");
		try {
			Class myClass = context.getClassLoader().loadClass(className);
			String mimetype = (String) myClass.getField("CONTENT_ITEM_TYPE")
					.get(null);
			// String mimetype = (String)
			// myClass.getField("AUTHORITY").get(null);
			Log.v(TAG, "Yes it works. mimetype = " + mimetype);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Classe wurde wieder nicht gefunden.", e);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Falsches argument.", e);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "sicherheits ausnahme.", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "ittegaler zutritt.", e);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "feld nicht gefunden.", e);
		}

	}

}
