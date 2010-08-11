/**
 * 
 */
package org.aksw.mssw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Resource;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author natanael
 * 
 */
public class FoafProvider extends ContentProvider {

	private static final String TAG = "FoafProvider";
	public static final String AUTHORITY = "org.aksw.mssw.foafprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	private static final String TRIPLE_AUTHORITY = "org.aksw.msw.tripleprovider";
	private static final Uri TRIPLE_CONTENT_URI = Uri.parse("content://"
			+ TRIPLE_AUTHORITY);

	public static final String DISPLAY_NAME = "FoafProvider";

	/**
	 * content://org.aksw.mssw.foafprovider returns nothing
	 * content://org.aksw.mssw.foafprovider/me returns the resource representing
	 * you content://org.aksw.mssw.foafprovider/me/friends returns a list of
	 * resources of my friends
	 * content://org.aksw.mssw.foafprovider/me/friend/add/_uri_ add friends to
	 * the friends list content://org.aksw.mssw.foafprovider/person/_uri_
	 * returns a specific person
	 * content://org.aksw.mssw.foafprovider/person/_uri_/friends returns a list
	 * of resources of my friends of a specific person
	 * content://org.aksw.mssw.foafprovider/config get the current configuration
	 * content://org.aksw.mssw.foafprovider/config/me/_uri_ set my own webid
	 */

	private static final int WORLD = 1;

	private static final int ME = 100;
	private static final int ME_MECARD = 110;
	private static final int ME_FRIENDS = 120;
	private static final int ME_FRIEND_ADD = 121;

	private static final int PERSON = 20;
	private static final int PERSON_MECARD = 21;
	private static final int PERSON_FRIENDS = 22;

	private static final UriMatcher uriMatcher = new UriMatcher(WORLD);

	static {
		uriMatcher.addURI(AUTHORITY, "me/friend/add/*", ME_FRIEND_ADD);
		uriMatcher.addURI(AUTHORITY, "me/friends", ME_FRIENDS);
		uriMatcher.addURI(AUTHORITY, "me/mecard", ME_MECARD);
		uriMatcher.addURI(AUTHORITY, "me", ME);
		uriMatcher.addURI(AUTHORITY, "person/friends/*", PERSON_FRIENDS);
		uriMatcher.addURI(AUTHORITY, "person/mecard/*", PERSON_MECARD);
		uriMatcher.addURI(AUTHORITY, "person/*", PERSON);
	}

	private String me;

	private static Context context;
	private static SharedPreferences sharedPreferences;

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

		// String mimeTypeResItm =
		// "vnd.android.cursor.item/vnd.aksw.msw.resource";
		// String mimeTypeResDir =
		// "vnd.android.cursor.dir/vnd.aksw.msw.resource";
		// String mimeTypeTriple = "vnd.android.cursor.dir/vnd.aksw.msw.triple";
		String mimeTypeResItm = "vnd.android.cursor.item/vnd.com.hp.hpl.jena.rdf.model.resource";
		String mimeTypeResDir = "vnd.android.cursor.dir/vnd.com.hp.hpl.jena.rdf.model.resource";
		// String mimeTypeTriple =
		// "vnd.android.cursor.dir/vnd.com.hp.hpl.jena.rdf.model.statement";

		int match = uriMatcher.match(uri);
		switch (match) {
		case ME:
		case PERSON:
		case ME_FRIEND_ADD:
			return mimeTypeResItm;
		case WORLD:
		case ME_MECARD:
		case ME_FRIENDS:
		case PERSON_MECARD:
		case PERSON_FRIENDS:
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
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {

		if (sharedPreferences == null) {
			getConfiguration();
		}

		this.me = sharedPreferences.getString("me", null);
		if (this.me == null) {
			Log.i(TAG,
					"No URI for \"me\" specified in FoafProvider, please set a URI in configuration.");
		} else {
			Log.v(TAG, "URI for \"me\" is: " + this.me);
		}

		return true;
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
		// TODO Auto-generated method stub

		Log.v(TAG, "Starting query");

		ArrayList<String> path = new ArrayList<String>(uri.getPathSegments());

		Log.v(TAG, "path(1/" + path.size() + "): " + path.get(1) + ".");
		if (path.size() > 2) {
			Log.v(TAG, "path(2/" + path.size() + "): " + path.get(2) + ".");
		}

		int match = uriMatcher.match(uri);

		Log.v(TAG, "Matching URI <" + uri + "> match: (" + match + ").");

		switch (match) {
		case ME:
			return getMe();
		case PERSON:
			if (path.size() > 1) {
				return getPerson(path.get(1));
			} else {
				Log.v(TAG, "Size of path (" + path.size() + ") to short. <"
						+ uri + ">");
				return null;
			}
		case ME_MECARD:
			return getMeCard();
		case PERSON_MECARD:
			return getMeCard(path.get(2));
		case ME_FRIENDS:
			return getFriends();
		case PERSON_FRIENDS:
			return getFriends(path.get(2));
		case WORLD:
		case ME_FRIEND_ADD:
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

		int match = uriMatcher.match(uri);
		switch (match) {
		case ME_FRIEND_ADD:
			Log.i(TAG,
					"Adding new friends to your WebID is not yet implemented.");
			// return 1;
		case ME:
		case PERSON:
		case WORLD:
		case ME_FRIENDS:
		case PERSON_FRIENDS:
		default:
			return 0;
		}
	}

	private Cursor getMe() {
		if (me == null) {
			me = getConfiguration().getString("me", null);
		}
		return getPerson(me);
	}

	private Cursor getPerson(String uri) {
		Log.v(TAG, "getPerson: <" + uri + ">");

		if (context == null) {
			context = getContext();
		}
		ContentResolver contentResolver = context.getContentResolver();
		try {
			String enc = "UTF-8";

			Uri contentUri;
			contentUri = Uri.parse(TRIPLE_CONTENT_URI + "/resource/"
					+ URLEncoder.encode(uri, enc));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");

			return contentResolver.query(contentUri, null, null, null, null);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
			return null;
		}
	}

	private Cursor getMeCard() {
		if (me == null) {
			me = getConfiguration().getString("me", null);
		}
		return getMeCard(me);
	}

	private Cursor getMeCard(String uri) {
		if (uri == null) {
			return null;
		} else {

			return null;
		}
	}

	private Cursor getFriends() {
		if (me == null) {
			me = getConfiguration().getString("me", null);
		}
		return getFriends(me);
	}

	private Cursor getFriends(String uri) {
		if (uri == null) {
			return null;
		} else {

			return null;
		}
	}

	private SharedPreferences getConfiguration() {

		if (context == null) {
			context = getContext();
		}

		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		return sharedPreferences;

	}
}
