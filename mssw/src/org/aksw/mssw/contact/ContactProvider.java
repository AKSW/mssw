/**
 * 
 */
package org.aksw.mssw.contact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.aksw.mssw.Constants;
import org.aksw.mssw.tools.Base64;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
		return true;
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

		/**
		 * The segments of the query-path
		 */
		ArrayList<String> path = new ArrayList<String>(uri.getPathSegments());

		int match = uriMatcher.match(uri);

		Log.v(TAG, "Matching URI <" + uri + "> match: (" + match + ").");

		switch (match) {
		case DATA:
			if (path.size() > 1) {
				return addData(path.get(1), values);
			} else {
				Log.e(TAG, "Request URI too short.");
			}
		default:
			return 0;
		}
	}

	private Cursor getData(String uri) {
		Log.v(TAG, "getPerson: <" + uri + ">");

		try {
			Uri contentUri;
			contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI + "/resource/"
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
						if (rc.getString(rc.getColumnIndex("oIsBlankNode"))
								.equals("true")) {
							oIsBlankNode = true;
						}

						cc.addTriple(uri, Constants.PROP_hasData, object, true,
								oIsBlankNode);

						subject = object;
						// query for objects properties
						if (oIsBlankNode) {
							contentUri = Uri
									.parse(Constants.TRIPLE_CONTENT_URI
											+ "/bnode/"
											+ URLEncoder.encode(uri,
													Constants.ENC)
											+ "/"
											+ URLEncoder.encode(subject,
													Constants.ENC));
						} else {
							contentUri = Uri
									.parse(Constants.TRIPLE_CONTENT_URI
											+ "/resource/"
											+ URLEncoder.encode(subject,
													Constants.ENC));
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
								if (predicat.equals(Constants.DATA_KINDS_PREFIX
										+ "CommonDataKinds.Phone.NUMBER")
										&& object.startsWith("tel:")) {
									object = object.substring(4);
									oIsResource = false;
								} else if (predicat
										.equals(Constants.DATA_KINDS_PREFIX
												+ "CommonDataKinds.Photo.PHOTO")) {
									// TODO get photo and convert it in the
									// right format
									if (oIsResource) {
										try {
											Log.v(TAG, "Reading Photo from <" + object + ">.");
											URLConnection photoConnection = new URL(
													object).openConnection();
											InputStream photoStream = photoConnection.getInputStream();
														
											InputStream photo64Stream = new Base64.InputStream(photoStream, Base64.ENCODE);

											StringBuilder sb = new StringBuilder();
											BufferedReader reader = new BufferedReader(new InputStreamReader(photo64Stream));
											String line;
											
											while ((line = reader.readLine()) != null) {
												sb.append(line);
											}
											
											object = sb.toString();
										} catch (MalformedURLException e) {
											Log.e(TAG,
													"The given Photoresource <"
															+ object
															+ "> is not valide.",
													e);
											object = null;
										} catch (IOException e) {
											Log.e(TAG, "Could not read from <"
													+ object + ">.", e);
											object = null;
										} finally {
											oIsResource = false;
										}
									} else {
										Log.v(TAG,
												"Using unsupported Datatype for Photo");
										object = null;
									}
								}
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

	private int addData(String uri, ContentValues data) {
		Uri contentUri;
		try {
			contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI
					+ "/resource/addData/"
					+ URLEncoder.encode(uri, Constants.ENC));

			return getContentResolver().update(contentUri, data, null, null);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error on sending resourceupdates to TripleProvider", e);
			return 0;
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
