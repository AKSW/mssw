package org.aksw.mssw.contact;

import java.util.ArrayList;
import java.util.HashMap;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
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


	public static void performSync(Context contextIn, Account account, Bundle extras,
			String authority, ContentProviderClient provider,
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

	private static void addContact(Account account, String name) {
		
		Log.i(TAG, "Adding contact: " + name);
/*		
		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(RawContacts.CONTENT_URI);
		builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
		builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
		builder.withValue(RawContacts.SYNC1, name);
		operationList.add(builder.build());

		builder = ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(
				ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
				0);
		builder.withValue(
				ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		builder.withValue(
				ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
				name);
		operationList.add(builder.build());

		builder = ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
		builder.withValue(ContactsContract.Data.MIMETYPE,
				"vnd.android.cursor.item/vnd.org.c99.SyncProviderDemo.profile");
		builder.withValue(ContactsContract.Data.DATA1, name);
		builder.withValue(ContactsContract.Data.DATA2,
				"SyncProviderDemo Profile");
		builder.withValue(ContactsContract.Data.DATA3, "View profile");
		operationList.add(builder.build());

		try {
			content.applyBatch(ContactsContract.AUTHORITY, operationList);
		} catch (Exception e) {
			Log.e(TAG, "error:", e);
		}
		*/
	}

	private static void updateContact(
			ArrayList<ContentProviderOperation> operationList, String uri,
			long rawContactId) {
		
		Log.i(TAG, "updating contact: " + uri);
/*		
		Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
				rawContactId);
		Uri entityUri = Uri.withAppendedPath(rawContactUri,
				Entity.CONTENT_DIRECTORY);
		Cursor c = content.query(entityUri, new String[] {
				RawContacts.SOURCE_ID, Entity.DATA_ID, Entity.MIMETYPE,
				Entity.DATA1 }, null, null, null);
		try {
			while (c.moveToNext()) {
				if (!c.isNull(1)) {
					String mimeType = c.getString(2);

					if (mimeType
							.equals("vnd.android.cursor.item/vnd.org.c99.SyncProviderDemo.profile")) {
						ContentProviderOperation.Builder builder = ContentProviderOperation
								.newInsert(ContactsContract.StatusUpdates.CONTENT_URI);
						builder.withValue(
								ContactsContract.StatusUpdates.DATA_ID,
								c.getLong(1));
						builder.withValue(
								ContactsContract.StatusUpdates.STATUS, status);
						builder.withValue(
								ContactsContract.StatusUpdates.STATUS_RES_PACKAGE,
								"org.c99.SyncProviderDemo");
						builder.withValue(
								ContactsContract.StatusUpdates.STATUS_LABEL,
								R.string.app_name);
						builder.withValue(
								ContactsContract.StatusUpdates.STATUS_ICON,
								R.drawable.icon);
						builder.withValue(
								ContactsContract.StatusUpdates.STATUS_TIMESTAMP,
								System.currentTimeMillis());
						operationList.add(builder.build());

						builder = ContentProviderOperation
								.newUpdate(ContactsContract.Data.CONTENT_URI);
						builder.withSelection(
								BaseColumns._ID + " = '" + c.getLong(1) + "'",
								null);
						builder.withValue(ContactsContract.Data.DATA3, status);
						operationList.add(builder.build());
					}
				}
			}
		} finally {
			c.close();
		}
		*/
	}

	
}
