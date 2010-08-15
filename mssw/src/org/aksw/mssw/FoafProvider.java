/**
 * 
 */
package org.aksw.mssw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author Natanael Arndt <arndtn@gmx.de>
 * 
 */
public class FoafProvider extends ContentProvider {

	private static final String TAG = "FoafProvider";
	public static final String DISPLAY_NAME = "FoafProvider";
	public static final String AUTHORITY = "org.aksw.mssw.foafprovider";
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
	private static final int WORLD = 1;
	private static final int ME = 100;
	private static final int ME_MECARD = 110;
	private static final int ME_FRIENDS = 120;
	private static final int ME_FRIEND_ADD = 121;
	private static final int PERSON = 20;
	private static final int PERSON_NAME = 21;
	private static final int PERSON_PICTURE = 22;
	private static final int PERSON_MECARD = 23;
	private static final int PERSON_FRIENDS = 24;

	/**
	 * The UriMatcher, which parses the incoming querie-uris
	 */
	private static final UriMatcher uriMatcher = new UriMatcher(WORLD);

	static {
		uriMatcher.addURI(AUTHORITY, "me/friend/add/*", ME_FRIEND_ADD);
		uriMatcher.addURI(AUTHORITY, "me/friends", ME_FRIENDS);
		uriMatcher.addURI(AUTHORITY, "me/mecard", ME_MECARD);
		uriMatcher.addURI(AUTHORITY, "me", ME);
		uriMatcher.addURI(AUTHORITY, "person/friends/*", PERSON_FRIENDS);
		uriMatcher.addURI(AUTHORITY, "person/mecard/*", PERSON_MECARD);
		uriMatcher.addURI(AUTHORITY, "person/name/*", PERSON_NAME);
		uriMatcher.addURI(AUTHORITY, "person/picture/*", PERSON_PICTURE);
		uriMatcher.addURI(AUTHORITY, "person/*", PERSON);
	}

	/**
	 * The WebID of the user
	 */
	private String me;

	/**
	 * The Application Context in wich the ContentProvider is running and the
	 * SharedPreferences of all Applications in this Context
	 */
	private static Context context;
	private static SharedPreferences sharedPreferences;
	private static ContentResolver contentResolver;

	/**
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

	/**
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

	/**
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 *      java.lang.String[], java.lang.String, java.lang.String[],
	 *      java.lang.String)
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
		case ME:
			return getMe();
		case PERSON:
			if (path.size() > 1) {
				return getPerson(path.get(1));
			}
			break;
		case ME_MECARD:
			return getMeCard();
		case PERSON_MECARD:
			if (path.size() > 2) {
				return getMeCard(path.get(2));
			}
			break;
		case ME_FRIENDS:
			return getFriends();
		case PERSON_FRIENDS:
			if (path.size() > 2) {
				return getFriends(path.get(2));
			}
			break;
		default:
			return null;
		}

		Log.v(TAG, "Size of path (" + path.size() + ") to short. <" + uri + ">");
		return null;
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
		default:
			return 0;
		}
	}

	/*------------- private ----------------*/

	private String[] relations = { "http://xmlns.com/foaf/0.1/knows",
			"http://purl.org/vocab/relationship/acquaintanceOf",
			"http://purl.org/vocab/relationship/ambivalentOf",
			"http://purl.org/vocab/relationship/ancestorOf",
			"http://purl.org/vocab/relationship/antagonistOf",
			"http://purl.org/vocab/relationship/apprenticeTo",
			"http://purl.org/vocab/relationship/childOf",
			"http://purl.org/vocab/relationship/closeFriendOf",
			"http://purl.org/vocab/relationship/collaboratesWith",
			"http://purl.org/vocab/relationship/colleagueOf",
			"http://purl.org/vocab/relationship/descendantOf",
			"http://purl.org/vocab/relationship/employedBy",
			"http://purl.org/vocab/relationship/employerOf",
			"http://purl.org/vocab/relationship/enemyOf",
			"http://purl.org/vocab/relationship/engagedTo",
			"http://purl.org/vocab/relationship/friendOf",
			"http://purl.org/vocab/relationship/grandchildOf",
			"http://purl.org/vocab/relationship/grandparentOf",
			"http://purl.org/vocab/relationship/hasMet",
			"http://purl.org/vocab/relationship/influencedBy",
			"http://purl.org/vocab/relationship/knowsByReputation",
			"http://purl.org/vocab/relationship/knowsInPassing",
			"http://purl.org/vocab/relationship/knowsOf",
			"http://purl.org/vocab/relationship/lifePartnerOf",
			"http://purl.org/vocab/relationship/livesWith",
			"http://purl.org/vocab/relationship/lostContactWith",
			"http://purl.org/vocab/relationship/mentorOf",
			"http://purl.org/vocab/relationship/neighborOf",
			"http://purl.org/vocab/relationship/parentOf",
			"http://purl.org/vocab/relationship/participant",
			"http://purl.org/vocab/relationship/participantIn",
			"http://purl.org/vocab/relationship/Relationship",
			"http://purl.org/vocab/relationship/siblingOf",
			"http://purl.org/vocab/relationship/spouseOf",
			"http://purl.org/vocab/relationship/worksWith",
			"http://purl.org/vocab/relationship/wouldLikeToKnow" };

	private String[] meCard = { "http://xmlns.com/foaf/0.1/name",
			"http://xmlns.com/foaf/0.1/jabberID" };

	private Cursor getMe() {
		if (me == null) {
			me = getConfiguration().getString("me", null);
		}
		return getPerson(me);
	}

	private Cursor getPerson(String uri) {
		Log.v(TAG, "getPerson: <" + uri + ">");

		try {
			String enc = "UTF-8";

			Uri contentUri;
			contentUri = Uri.parse(TRIPLE_CONTENT_URI + "/resource/"
					+ URLEncoder.encode(uri, enc));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");
			Cursor rc = getContentResolver().query(contentUri, null, null, null,
					null);

			return rc;
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
		Log.v(TAG, "getMeCard: <" + uri + ">");

		try {
			String enc = "UTF-8";

			Uri contentUri;
			contentUri = Uri.parse(TRIPLE_CONTENT_URI + "/resource/"
					+ URLEncoder.encode(uri, enc));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");
			Cursor rc = getContentResolver().query(contentUri, relations, "complement", null,
					null);

			return rc;
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
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
		Log.v(TAG, "getMeCard: <" + uri + ">");

		try {
			String enc = "UTF-8";

			Uri contentUri;
			contentUri = Uri.parse(TRIPLE_CONTENT_URI + "/resource/"
					+ URLEncoder.encode(uri, enc));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");
			Cursor rc = getContentResolver().query(contentUri, relations, null, null,
					null);

			return rc;
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
			return null;
		}
	}
	
	/*---- private ----*/

	private SharedPreferences getConfiguration() {

		if (context == null) {
			context = getContext();
		}

		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		return sharedPreferences;
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

	/*------------ the following methods are not supported by this ContentProvider -----------*/

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
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
