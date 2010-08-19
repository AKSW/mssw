package org.aksw.mssw.contact;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

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

			Uri contentUri = Uri.parse(CONTENT_URI + "/person/"
					+ URLEncoder.encode(uri, enc));

			String[] projection = { "http://xmlns.com/foaf/0.1/name" };
			Cursor rc = content.query(contentUri, projection, null, null, null);

			if (rc != null) {
				rc.moveToFirst();
				String name = rc.getString(rc.getColumnIndex("object"));

				ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

				ContentProviderOperation.Builder builder = ContentProviderOperation
						.newInsert(RawContacts.CONTENT_URI);
				builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
				builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
				builder.withValue(RawContacts.SYNC1, uri);
				operationList.add(builder.build());

				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
				builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
				builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
				//builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, givName);
				//builder.withValue(ContactsContract.CommonDataKinds.StructuredName.PREFIX, prefix);
				operationList.add(builder.build());
/*
				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, 0);
				builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, nummer1);
				builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
				operationList.add(builder.build());
				

				builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, 0);
				builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, nummer2);
				builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_TELEX);
				operationList.add(builder.build());
	*/			
				content.applyBatch(ContactsContract.AUTHORITY, operationList);
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

}
