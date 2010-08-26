/**
 * 
 */
package org.aksw.mssw.contact;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.aksw.mssw.Constants;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * @author natanael
 * 
 */
public class ContactProvider extends ContentProvider {

	private static final String TAG = "ContactProvider";
	public static final String DISPLAY_NAME = "ContactProvider";
	public static final String AUTHORITY = "org.aksw.mssw.contact.contactprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	/**
	 * Static values for querying the TripleProvider
	 */
	private static final String TRIPLE_AUTHORITY = "org.aksw.msw.tripleprovider";
	private static final Uri TRIPLE_CONTENT_URI = Uri.parse("content://"
			+ TRIPLE_AUTHORITY);

	/**
	 * Static values which represent the different pathes of the query uris
	 */
	private static final int WORLD = 42;
	private static final int DATA = 1337;

	/**
	 * The UriMatcher, which parses the incoming querie-uris
	 */
	private static final UriMatcher uriMatcher = new UriMatcher(WORLD);

	static {
		uriMatcher.addURI(AUTHORITY, "data/*", DATA);
	}

	/**
	 * The Application Context in which the ContentProvider is running and the
	 * SharedPreferences of all Applications in this Context
	 */
	private static Context context;
	private static ContentResolver contentResolver;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		String mimeTypeResDir = "vnd.android.cursor.dir/vnd.aksw.mssw.contact.data";

		int match = uriMatcher.match(uri);
		switch (match) {
		case DATA:
		case WORLD:
			return mimeTypeResDir;
		default:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#insert(android.net.Uri,
	 * android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 * java.lang.String[], java.lang.String, java.lang.String[],
	 * java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		Log.v(TAG, "Starting query");

		/**
		 * The segments of the query-path
		 */
		ArrayList<String> path = new ArrayList<String>(uri.getPathSegments());

		for (int i = 0; i < path.size(); i++) {
			Log.v(TAG, "path(" + i + "/" + path.size() + "): " + path.get(i)
					+ ".");
		}

		/**
		 * The determined URI-format
		 */
		int match = uriMatcher.match(uri);

		Log.v(TAG, "Matching URI <" + uri + "> match: (" + match + ").");

		switch (match) {
		case DATA:
			if (path.size() > 1) {
				return getData(path.get(1));
			} else {
				Log.e(TAG, "Request URI too short.");
			}
		default:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#update(android.net.Uri,
	 * android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	private Cursor getData(String uri) {
		Log.v(TAG, "getPerson: <" + uri + ">");
		
		try {
			Uri contentUri;
			contentUri = Uri.parse(TRIPLE_CONTENT_URI + "/resource/"
					+ URLEncoder.encode(uri, Constants.ENC));

			String[] projection = new String[] { Constants.PROP_hasData };

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");

			// get hasData properties of this resource
			Cursor rc = getContentResolver().query(contentUri, projection,
					null, null, null);

			if (rc != null && rc.getCount() > 0) {
				rc.moveToPrevious();
				ContactCursor cc = new ContactCursor();
				// foreach hasData propertiy get object
				String subject, predicat, object;
				boolean oIsResource, oIsBlankNode;
				while (rc.moveToNext()) {
					object = rc.getString(rc.getColumnIndex("object"));
					oIsBlankNode = oIsResource = false;
					if (rc.getString(rc.getColumnIndex("oIsResource")).equals(
							"true")) {
						oIsResource = true;
					}

					if (oIsResource) {
						if (rc.getString(rc.getColumnIndex("oIsBlankNode")).equals("true")) {
							oIsBlankNode = true;
						}
						
						cc.addTriple(uri, Constants.PROP_hasData, object, true,
								oIsBlankNode);

						subject = object;
						// query for objects properties
						if (oIsBlankNode) {
							contentUri = Uri.parse(TRIPLE_CONTENT_URI
									+ "/bnode/"
									+ URLEncoder.encode(uri, Constants.ENC) + "/"
									+ URLEncoder.encode(subject, Constants.ENC));
						} else {
							contentUri = Uri.parse(TRIPLE_CONTENT_URI
									+ "/resource/"
									+ URLEncoder.encode(subject, Constants.ENC));
						}
						Log.v(TAG,
								"Starting Query with uri: <"
										+ contentUri.toString() + ">.");
						Cursor rc2 = getContentResolver().query(contentUri,
								null, null, null, null);

						while (rc2.moveToNext()) {
							// get object properties
							predicat = rc2.getString(rc
									.getColumnIndex("predicat"));
							object = rc2.getString(rc.getColumnIndex("object"));

							oIsBlankNode = oIsResource = false;
							if (rc2.getString(rc2.getColumnIndex("oIsResource"))
									.equals("true")) {
								oIsResource = true;
							}
							if (rc2.getString(
									rc2.getColumnIndex("oIsBlankNode")).equals(
									"true")) {
								oIsBlankNode = true;
							}

							if (!oIsBlankNode) {
								cc.addTriple(subject, predicat, object,
										oIsResource, false);
							} else {
								Log.e(TAG,
										"Got data with blanknode as object, ignorring.");
							}
						}

					} else {
						Log.e(TAG,
								"Found hasData Property without Resource in range: object = '"
										+ object + "', ignorring.");
					}
				}

				// return ...
				// cc.checkData(); // only debug output
				return cc;
			} else {
				Log.v(TAG,
						"Triple Provider gave me nothing, so I can't give you anything. Returning null.");
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
			return null;
		}
	}

	private ContentResolver getContentResolver() {
		if (contentResolver == null) {
			if (context == null) {
				context = getContext();
			}
			contentResolver = context.getContentResolver();
		}

		return contentResolver;
	}

}
