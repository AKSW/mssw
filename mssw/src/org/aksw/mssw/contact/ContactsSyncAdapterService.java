package org.aksw.mssw.contact;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

		// 1: the both diffs could maybe be improved by something like a
		// diff-bitmap (see Bernhard Schandl mobisem)
		// find out, which persons are missing and which are common (Person
		// Diff) [1]
		String uri;
		while (rc.moveToNext()) {
			uri = rc.getString(rc.getColumnIndex("object"));
			if (localContacts.get(uri) == null) {
				// add missing persons to contacts
				addContact(account, uri);
			} else {
				// for each already existing Person find out, which data has
				// changed (Data Diff) [1]
				updateContact(operationList, uri, localContacts.get(uri));
			}
		}

		try {
			if (operationList.size() > 0) {
				content.applyBatch(ContactsContract.AUTHORITY, operationList);
			}
		} catch (Exception e) {
			Log.e(TAG, "error:", e);
		}
	}

	private static void addContact(Account account, String uri) {

		Log.i(TAG, "Adding contact: " + uri);

		try {
			String enc = "UTF-8";
			/*
			 * Uri contentUri = Uri.parse(CONTENT_URI + "/person/" +
			 * URLEncoder.encode(uri, enc));
			 * 
			 * String[] projection = { "http://xmlns.com/foaf/0.1/name" };
			 * Cursor rc = content.query(contentUri, projection, null, null,
			 * null);
			 */

			// 1. query hasData Properties
			// 2. for each hasData Property
			// 2.1 query hasData Properties object
			// 2.2 check rdf:type (?s a ?o) an set as Mime-Type
			// 2.3 for each hasData Property
			// 2.3.1 check uri of property and set as column name (as in 2.)
			// 2.3.2 check whether ?o is a literal or resource
			// 2.3.2.1 literal: insert as data
			// 2.3.2.2 resource: get object and insert (as in 2.)

			HashMap<String, ContentProviderOperation.Builder> builderList = new HashMap<String, ContentProviderOperation.Builder>();

			ContentProviderOperation.Builder builder = ContentProviderOperation
					.newInsert(RawContacts.CONTENT_URI);
			builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
			builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
			builder.withValue(RawContacts.SYNC1, uri);
			builderList.put(uri, builder);

			Uri contentUri = Uri.parse(CONTACT_URI + "/data/"
					+ URLEncoder.encode(uri, enc));

			// String[] projection = { "http://xmlns.com/foaf/0.1/name" };
			Cursor rc = content.query(contentUri, null, null, null, null);

			if (rc != null) {
				String subject, predicat, object;
				while (rc.moveToNext()) {
					predicat = rc.getString(rc.getColumnIndex("predicat"));

					if (predicat.equals(ContactProvider.PROP_hasData)) {
						object = rc.getString(rc.getColumnIndex("object"));

						if (!builderList.containsKey(object)) {

							builder = ContentProviderOperation
									.newInsert(ContactsContract.Data.CONTENT_URI);
							builder.withValueBackReference(
									ContactsContract.RawContactsEntity.RAW_CONTACT_ID,
									0);
							builderList.put(object, builder);
						} else {
							Log.i(TAG, "Builder List already has this data.");
						}
					}
				}

				// reset cursor
				rc.moveToPosition(-1);

				while (rc.moveToNext()) {
					predicat = rc.getString(rc.getColumnIndex("predicat"));

					if (!predicat.equals(ContactProvider.PROP_hasData)) {
						subject = rc.getString(rc.getColumnIndex("subject"));
						object = rc.getString(rc.getColumnIndex("object"));
						builder = builderList.get(subject);

						try {
							if (predicat.equals(ContactProvider.PROP_rdfType)) {
								// this is the place of magic
								String type = (String) forUri(object, false)
										.getField("CONTENT_ITEM_TYPE")
										.get(null);

								builder.withValue(
										ContactsContract.Data.MIMETYPE, type);
							} else {
								boolean isResource;
								if (rc.getString(rc.getColumnIndex("oIsResource")).equals("true")) {
									isResource = true;
								} else {
									isResource = false;
								}
								// this is the place of magic
								String fieldName = extractFieldName(predicat);
								String column = (String) forUri(predicat, true)
										.getField(fieldName).get(null);

								if (isResource) {
									// Type is int Protocol is string

									// this is the place of magic
									fieldName = extractFieldName(object);
									Field valueField = forUri(object, true)
											.getField(object);

									if (valueField.getType().getName()
											.equalsIgnoreCase("int")) {
										int value = (Integer) valueField
												.get(null);
										builder.withValue(column, value);
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
							}

						} catch (Exception e) {
							Log.e(TAG,
									"Couldn't interpres the Triple subject = '"
											+ subject
											+ "', predicat = '"
											+ predicat
											+ "', object = '"
											+ object
											+ "' I'll ignorr it and am proceding with next Property.");
						}

						builderList.put(subject, builder);
					}
				}

				// builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
				// givName);
				// builder.withValue(ContactsContract.CommonDataKinds.StructuredName.PREFIX,
				// prefix);
				/*
				 * builder =
				 * ContentProviderOperation.newInsert(ContactsContract.
				 * Data.CONTENT_URI);
				 * builder.withValueBackReference(ContactsContract
				 * .CommonDataKinds.Phone.RAW_CONTACT_ID, 0);
				 * builder.withValue(ContactsContract.Data.MIMETYPE,
				 * ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				 * builder
				 * .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
				 * nummer1);
				 * builder.withValue(ContactsContract.CommonDataKinds.Phone
				 * .TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
				 * operationList.add(builder.build());
				 * 
				 * 
				 * builder =
				 * ContentProviderOperation.newInsert(ContactsContract.
				 * Data.CONTENT_URI);
				 * builder.withValueBackReference(ContactsContract
				 * .CommonDataKinds.Phone.RAW_CONTACT_ID, 0);
				 * builder.withValue(ContactsContract.Data.MIMETYPE,
				 * ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				 * builder
				 * .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
				 * nummer2);
				 * builder.withValue(ContactsContract.CommonDataKinds.Phone
				 * .TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_TELEX);
				 * operationList.add(builder.build());
				 */

			} else {
				Log.e(TAG,
						"Contentprovider returned an empty Cursor, couldn't add Data to contact '"
								+ uri + "'.");
			}

			ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
			Iterator<String> builderIterator = builderList.keySet().iterator();
			// ContentProviderOperation.Builder builder;

			while (builderIterator.hasNext()) {
				builder = builderList.get(builderIterator.next());
				operationList.add(builder.build());
			}

			content.applyBatch(ContactsContract.AUTHORITY, operationList);

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
		} catch (Exception e) {
			Log.e(TAG, "An other error occured.", e);
		}

	}

	private static void updateContact(
			ArrayList<ContentProviderOperation> operationList, String uri,
			long rawContactId) {

		Log.i(TAG, "updating contact: " + uri);

		try {
			String enc = "UTF-8";

			Uri contentUri = Uri.parse(CONTENT_URI + "/person/"
					+ URLEncoder.encode(uri, enc));

			String[] projection = { "http://xmlns.com/foaf/0.1/name" };
			Cursor rc = content.query(contentUri, projection, null, null, null);

			if (rc != null) {
				// for one value kinds:
				// should get the current value of a field
				// compare it to the value from FoafProvider
				// if different set the FoafProvider value

				// or for multivalue kinds:
				// get all values of these fields
				// compare all of these values with all values from FoafProvider
				// if one of FoafProvider is missing in Contacts, add it
				// else do nothing

				/**
				 * Many of the following code is from
				 * http://developer.android.com
				 * /reference/android/provider/ContactsContract.RawContacts.html
				 */

				Uri rawContactUri;
				Uri entityUri;
				Cursor cc;

				while (rc.moveToNext()) {
					rawContactUri = ContentUris.withAppendedId(
							RawContacts.CONTENT_URI, rawContactId);
					entityUri = Uri.withAppendedPath(rawContactUri,
							Entity.CONTENT_DIRECTORY);
					cc = content.query(entityUri, null, null, null, null);
					try {
						while (cc.moveToNext()) {
							String sourcId = cc.getString(cc
									.getColumnIndex(RawContacts.SOURCE_ID));
							if (!cc.isNull(cc.getColumnIndex(Entity.DATA_ID))) {
								String mimeType = cc.getString(cc
										.getColumnIndex(Entity.MIMETYPE));

								if (mimeType
										.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
									String ccName = cc
											.getString(cc
													.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
									String rcName = rc.getString(rc
											.getColumnIndex("objectReadable"));
									if (!ccName.equals(rcName)) {
										Log.v(TAG, "Updating name from "
												+ ccName + " to " + rcName
												+ " of " + uri);

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
										Log.v(TAG, "Doesn't need to update "
												+ uri);
									}
								}
							}
						}
					} finally {
						cc.close();
					}
				}
			} else {
				Log.e(TAG,
						"Contentprovider returned an empty Cursor, don't know what to do.");
			}

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
		} catch (Exception e) {
			Log.e(TAG, "An other error occured.", e);
		}
	}

	private static Class forUri(String uri, boolean isField)
			throws ClassNotFoundException {
		String className;
		Uri uri_ = Uri.parse(uri);

		ArrayList<String> path = new ArrayList<String>(uri_.getPathSegments());

		if (isField) {
			String fieldName = path.get(path.size() - 1);
			className = fieldName.substring(0, fieldName.lastIndexOf("."));
		} else {
			className = path.get(path.size() - 1);
		}

		Log.v(TAG, "Classname ist: '" + className + "'.");

		return Class.forName(className);
	}

	private static String extractFieldName(String uri) {
		Uri uri_ = Uri.parse(uri);

		ArrayList<String> path = new ArrayList<String>(uri_.getPathSegments());

		String fullFieldName = path.get(path.size() - 1);
		String fieldName = fullFieldName.substring(fullFieldName
				.lastIndexOf("."));
		Log.v(TAG, "Fieldname ist: '" + fieldName + "'.");

		return fieldName;
	}

}
