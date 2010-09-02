package org.aksw.mssw.contact;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Stack;

import org.aksw.mssw.Constants;
import org.aksw.mssw.tools.Base64;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
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
import android.provider.ContactsContract.RawContactsEntity;
import android.util.Log;

public class ContactsSyncAdapterService extends Service {

	private static String TAG = "MsswContactsSyncAdapterService";
	private static SyncAdapter syncAdapter;

	private static Context context;
	private static ContentResolver content;

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
		Uri contentUri = Uri.parse(Constants.FOAF_CONTENT_URI + "/me/friends/");
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
			uri = rc.getString(rc.getColumnIndex("webid"));
			if (localContacts.get(uri) == null) {
				// add missing persons to contacts
				addContact(uri, account);
			} else {
				// for each already existing Person find out, which data has
				// changed (Data Diff) [1]
				updateContact(uri, localContacts.get(uri));
			}
			// TODO maybe find also contacts in ContactsContract which are not
			// in foaf and get whose uri to fetch there data from web. (would
			// maybe happen in two sync cycles)
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
			LinkedHashMap<String, Boolean> builderHasMimeTypeList;
			builderHasMimeTypeList = new LinkedHashMap<String, Boolean>();

			ContentProviderOperation.Builder builder = ContentProviderOperation
					.newInsert(RawContacts.CONTENT_URI);
			builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
			builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
			builder.withValue(RawContacts.SYNC1, uri);
			builderList.put(uri, builder);
			builderHasMimeTypeList.put(uri, Boolean.TRUE);

			if (uri != null) {
				Uri contentUri = Uri.parse(Constants.CONTACT_CONTENT_URI
						+ "/data/" + URLEncoder.encode(uri, Constants.ENC));

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
								builderHasMimeTypeList.put(object,
										Boolean.FALSE);
							} else {
								Log.i(TAG,
										"Builder List already has this data: "
												+ object);
							}
						}
					}

					// reset cursor before first element, as if it would be new
					rc.moveToPosition(-1);

					Log.i(TAG,
							"== Initializing propertys of hasData objects. ==");
					/**
					 * get the properties of the nodes, which are the in the
					 * range of a hasData property
					 */
					while (rc.moveToNext()) {
						predicat = rc.getString(rc.getColumnIndex("predicat"));
						Log.v(TAG, "predicat = " + predicat);

						if (predicat != null
								&& !predicat.equals(Constants.PROP_hasData)) {
							subject = rc
									.getString(rc.getColumnIndex("subject"));
							object = rc.getString(rc.getColumnIndex("object"));
							builder = builderList.get(subject);

							try {
								if (predicat.equals(Constants.PROP_rdfType)
										&& object
												.startsWith(Constants.DATA_KINDS_PREFIX)) {
									Log.v(TAG, "rdf:type = " + object + ".");
									// this is the place of magic
									String type = (String) forUri(object, false)
											.getField("CONTENT_ITEM_TYPE").get(
													null);

									Log.v(TAG, "Goind to Add Mimetype: " + type
											+ ".");
									builder.withValue(
											ContactsContract.Data.MIMETYPE,
											type);
									builderHasMimeTypeList.put(subject,
											Boolean.TRUE);

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
									 * this is the place of magic extract the
									 * last part of the URI, which determines a
									 * property of the given class and get a
									 * Class object representing the class given
									 * in the uri. than get the specified
									 * property from this class.
									 */
									String fieldName = extractFieldName(predicat);
									String column = (String) forUri(predicat,
											true).getField(fieldName).get(null);

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
													+ " to column: " + column
													+ ".");
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
										if (column
												.equals(RawContactsEntity.DATA15)) {

											builder.withValue(column,
													Base64.decode(object));
											Log.v(TAG, "wrote blob.");
										} else {
											builder.withValue(column, object);
										}

									}
								} else if (predicat
										.equals(Constants.PROP_rdfType)) {
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
					Log.v(TAG,
							"Destroy builders, to avoid '(Unknown)'-Contacts.");
					builderList.clear();
				}
			}

			if (builderList.size() > 1) {
				ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
				Iterator<String> builderIterator = builderList.keySet()
						.iterator();

				while (builderIterator.hasNext()) {
					String key = builderIterator.next();
					if (builderHasMimeTypeList.get(key)) {
						builder = builderList.get(key);
						operationList.add(builder.build());
					} else {
						Log.v(TAG, "Found dataset <" + key
								+ "> without mimetype.");
					}
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

		Log.i(TAG, "updating contact <" + uri + "> with rawId: '" + rawId
				+ "'.");

		try {

			Uri contactUri = Uri.parse(Constants.CONTACT_CONTENT_URI + "/data/"
					+ URLEncoder.encode(uri, Constants.ENC));

			Cursor rc = content.query(contactUri, null, null, null, null);

			Uri rawUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
					rawId);
			Uri entityUri = Uri.withAppendedPath(rawUri,
					Entity.CONTENT_DIRECTORY);
			String[] projection = { RawContacts._ID, RawContacts.CONTACT_ID,
					RawContactsEntity.DATA_ID, RawContactsEntity.MIMETYPE,
					RawContactsEntity.DATA1, RawContactsEntity.DATA2,
					RawContactsEntity.DATA3, RawContactsEntity.DATA4,
					RawContactsEntity.DATA5, RawContactsEntity.DATA6,
					RawContactsEntity.DATA7, RawContactsEntity.DATA8,
					RawContactsEntity.DATA9, RawContactsEntity.DATA10,
					RawContactsEntity.DATA11, RawContactsEntity.DATA12,
					RawContactsEntity.DATA13, RawContactsEntity.DATA14,
					RawContactsEntity.DATA15 };
			Cursor cc = content.query(entityUri, projection, null, null, null);

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
						if (!dataList.containsKey(subject)) {
							dataList.put(subject, new HashMap<String, String>());
						}

						Log.v(TAG, "rdf:type = " + object + ".");
						// this is the place of magic
						try {
							String type = (String) forUri(object, false)
									.getField("CONTENT_ITEM_TYPE").get(null);

							data = dataList.get(subject);
							data.put(Entity.MIMETYPE, type);
							dataList.put(subject, data);
						} catch (ClassNotFoundException e) {
							Log.v(TAG,
									"Could not find Class '"
											+ object
											+ "' (ignoring), check the file 'rules.n3' on your sdcard if everything is spelled right.");
							dataList.remove(subject);
						}
					} else if (predicat.startsWith(Constants.DATA_KINDS_PREFIX)) {

						String fieldName = extractFieldName(predicat);
						String column = (String) forUri(predicat, true)
								.getField(fieldName).get(null);

						if (isResource
								&& object
										.startsWith(Constants.DATA_KINDS_PREFIX)) {

							if (dataList.containsKey(subject)) {
								data = dataList.get(subject);
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

							dataList.put(subject, data);
						} else {
							if (dataList.containsKey(subject)) {
								data = dataList.get(subject);
							} else {
								data = new HashMap<String, String>();
							}
							data.put(column, object);
							dataList.put(subject, data);
						}
					} else {
						Log.v(TAG, "Unknown predicat <" + predicat
								+ "> or unknown object <" + object + ">.");
					}

				}

				Iterator<String> dataListIterator;
				String key;
				Iterator<String> dataIterator;
				String column;
				String contactsData;
				String myData;

				HashMap<String, HashMap<String, String>> changeList = new HashMap<String, HashMap<String, String>>();

				while (cc.moveToNext()) {

					if (!cc.isNull(cc.getColumnIndex(Entity.DATA_ID))) {

						boolean isSame = false;

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

						// compare
						dataListIterator = dataList.keySet().iterator();

						// search dataList for entries, which could fit
						while (dataListIterator.hasNext()) {

							try {
								key = dataListIterator.next();
								data = dataList.get(key);

								if (data.get(Entity.DATA_ID) == null
										&& data.get(Entity.MIMETYPE) != null) {
									dataIterator = data.keySet().iterator();

									isSame = true;

									while (dataIterator.hasNext()) {
										column = dataIterator.next();
										if (column.equals(Entity.DATA15)) {
											byte[] contactsDataBlob = cc
													.getBlob(cc
															.getColumnIndex(column));
											if (contactsDataBlob != null) {
												contactsData = Base64
														.encodeBytes(contactsDataBlob);
											} else {
												contactsData = null;
											}
										} else {
											contactsData = cc.getString(cc
													.getColumnIndex(column));
										}
										myData = data.get(column);

										if (!myData.equals(contactsData)) {
											isSame = false;
											break;
										}
									}

									if (data.size() < 1) {
										isSame = false;
									}

									if (isSame) {
										Log.v(TAG,
												"Found same rows, dataList ("
														+ key
														+ ") and ContactContract ("
														+ cc.getString(cc
																.getColumnIndex(Entity.DATA_ID))
														+ ")");
										/*
										 * for (int i = 0; i <
										 * cc.getColumnCount(); i++) {
										 * Log.v(TAG, "contactRows: (" + i +
										 * ", " + cc.getColumnName(i) + ") " +
										 * cc.getString(i) + ", dataRow: " +
										 * data.get(cc .getColumnName(i)) +
										 * "."); }
										 */

										data.put(
												Entity.DATA_ID,
												cc.getString(cc
														.getColumnIndex(Entity.DATA_ID)));
										dataList.put(key, data);
										break;
									}

									// if not the same, than procede with next
									// dataset
								}
							} catch (Exception e) {
								Log.v(TAG,
										"An error occured while trying to compare datasets.",
										e);
								isSame = false;
							}
						}

						// if the while-loop ran through without success there
						// is no fitting dataset in the webid so save them to
						// the local model
						if (!isSame) {
							try {

								data = new HashMap<String, String>();

								Log.v(TAG, "New Data found: ");

								if (cc.getString(
										cc.getColumnIndex(Entity.MIMETYPE))
										.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
									Log.v(TAG,
											"But is Photo, will not save it back");
								} else {
									for (int i = 0; i < cc.getColumnCount(); i++) {

										if (cc.getColumnName(i).equals(
												Entity.DATA15)) {
											Log.v(TAG,
													"Don't save BLOB (DATA15).");
										} else {
											data.put(cc.getColumnName(i),
													cc.getString(i));

											Log.v(TAG,
													"contactRows: ("
															+ i
															+ ", "
															+ cc.getColumnName(i)
															+ ") "
															+ cc.getString(i)
															+ ".");
										}
									}

									changeList.put(uri, data);
								}
							} catch (Exception e) {
								Log.e(TAG,
										"An Exception occured while trying to get a changed dataset.",
										e);
							}
						}

					}
				}

				cc.close();
				// reset cc
				// cc.moveToPosition(-1);

				// now write the data sets, which didn't fit a contacts data set
				// (with data_id = null) to ContactsContract
				Log.v(TAG, "=== Now write new datasets for <" + uri + "> to contacts. ===");

				dataListIterator = dataList.keySet().iterator();

				ContentProviderOperation.Builder builder;

				while (dataListIterator.hasNext()) {

					key = dataListIterator.next();
					data = dataList.get(key);

					if (data.get(Entity.DATA_ID) == null) {

						dataIterator = data.keySet().iterator();

						// write to ContactContract
						String mimetype = data
								.get(ContactsContract.Data.MIMETYPE);
						if (mimetype
								.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
							Log.v(TAG, "Special treatment for Structured Name");

							rawUri = ContentUris.withAppendedId(
									RawContacts.CONTENT_URI, rawId);
							entityUri = Uri.withAppendedPath(rawUri,
									Entity.CONTENT_DIRECTORY);
							projection = new String[] { RawContacts._ID };
							String[] selectionArgs = {
									String.valueOf(rawId),
									ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
							cc = content.query(entityUri, projection,
									RawContacts.CONTACT_ID + "=? AND "
											+ ContactsContract.Data.MIMETYPE
											+ "=?", selectionArgs, null);
							if (cc != null) {
								if (cc.getCount() > 1) {
									Log.v(TAG,
											"Contact <"
													+ uri
													+ "> has more than one StructuredName, thats not so good.");
									// maybe remove all except one
								}

								if (cc.moveToFirst()) {

									builder = ContentProviderOperation
											.newUpdate(ContactsContract.Data.CONTENT_URI);
									builder.withValue(
											ContactsContract.Data._ID,
											cc.getString(cc
													.getColumnIndex(RawContacts._ID)));
								} else {
									builder = ContentProviderOperation
											.newInsert(ContactsContract.Data.CONTENT_URI);
								}
							} else {
								builder = ContentProviderOperation
										.newInsert(ContactsContract.Data.CONTENT_URI);
							}
						} else if (mimetype
								.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
							Log.v(TAG, "Special treatment for Photo");

							rawUri = ContentUris.withAppendedId(
									RawContacts.CONTENT_URI, rawId);
							entityUri = Uri.withAppendedPath(rawUri,
									Entity.CONTENT_DIRECTORY);
							projection = new String[] { RawContacts._ID };
							String[] selectionArgs = {
									String.valueOf(rawId),
									ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE };
							cc = content.query(entityUri, projection,
									RawContacts.CONTACT_ID + "=? AND "
											+ ContactsContract.Data.MIMETYPE
											+ "=?", selectionArgs, null);
							if (cc != null) {
								if (cc.getCount() > 1) {
									Log.v(TAG,
											"Contact <"
													+ uri
													+ "> has more than one Photo, thats not so good.");
									// maybe remove all except one
								}

								if (cc.moveToFirst()) {

									builder = ContentProviderOperation
											.newUpdate(ContactsContract.Data.CONTENT_URI);
									builder.withValue(
											ContactsContract.Data._ID,
											cc.getString(cc
													.getColumnIndex(RawContacts._ID)));
								} else {
									builder = ContentProviderOperation
											.newInsert(ContactsContract.Data.CONTENT_URI);
								}
							} else {
								builder = ContentProviderOperation
										.newInsert(ContactsContract.Data.CONTENT_URI);
							}
						} else {
							builder = ContentProviderOperation
									.newInsert(ContactsContract.Data.CONTENT_URI);
						}

						Log.v(TAG, "New insert to <"
								+ ContactsContract.Data.CONTENT_URI + ">");

						builder.withValue(ContactsContract.Data.RAW_CONTACT_ID,
								rawId);

						Log.v(TAG, "with value "
								+ ContactsContract.Data.RAW_CONTACT_ID + " = "
								+ rawId + ".");

						while (dataIterator.hasNext()) {
							column = dataIterator.next();
							myData = data.get(column);

							if (column.equals(RawContactsEntity.DATA15)) {
								try {
									builder.withValue(column,
											Base64.decode(myData));
									Log.v(TAG, "with value " + column
											+ " = <blob>.");
								} catch (IOException e) {
									Log.e(TAG, "Error on decoding blob: "
											+ myData, e);
								}
							} else {
								builder.withValue(column, myData);

								Log.v(TAG, "with value " + column + " = "
										+ myData + ".");
							}

							// ContentProviderOperation.Builder builder =
							// ContentProviderOperation
							// .newUpdate(ContactsContract.Data.CONTENT_URI);

							// builder.withSelection(
							// BaseColumns._ID
							// + " = '"
							// + cc.getLong(cc
							// .getColumnIndex(Entity.DATA_ID))
							// + "'", null);

						}

						operationList.add(builder.build());
					}
				}

				// apply changes to ContactsContract
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

				try {
					// TODO write changeList to foaf
					dataListIterator = changeList.keySet().iterator();
					ContentValues values;
					while (dataListIterator.hasNext()) {
						key = dataListIterator.next();
						data = changeList.get(key);
						values = new ContentValues();

						dataIterator = data.keySet().iterator();
						while (dataIterator.hasNext()) {
							column = dataIterator.next();
							myData = data.get(column);

							values.put(column, myData);
						}

						int result = content.update(contactUri, values, null,
								null);

						if (result > 0) {
							Log.v(TAG, "Updated contact <" + uri
									+ "> sucessfully.");
						} else {
							Log.v(TAG, "Error on updating contact <" + uri
									+ ">.");
						}
					}
				} catch (Exception e) {
					Log.e(TAG,
							"An error occured on writing changes back to foaf.",
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

	/**
	 * Get a Class object, from the class which is represented by the given uri.
	 * 
	 * @param uri
	 *            the uri, which represents the class you want to get. This uri
	 *            has to start with Constants.DATA_KINDS_PREFIX.
	 * @param isField
	 *            set to true if the given uri is not a class, but a field of
	 *            this class, so the part after the last Point "." will be
	 *            ignored.
	 * @return a Class object of a final class
	 * @throws ClassNotFoundException
	 *             if the given uri doesn't start with
	 *             Constants.DATA_KINDS_PREFIX or could not be found.
	 */
	private static Class<? extends Object> forUri(String uri, boolean isField)
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
			Class<? extends Object> klasse;
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
}
