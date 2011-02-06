/**
 * 
 */
package org.aksw.mssw.content;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.aksw.mssw.Constants;
import org.aksw.mssw.NameHelper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author Natanael Arndt <arndtn@gmx.de>
 * 
 */
public class FoafProvider extends ContentProvider implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = "FoafProvider";
	public static final String DISPLAY_NAME = "FoafProvider";
	public static final String AUTHORITY = "org.aksw.mssw.content.foafprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	/**
	 * Static values which represent the different paths of the query uris
	 */
	private static final int WORLD = 1;
	private static final int ME = 100;
	private static final int ME_MECARD = 110;
	private static final int ME_FRIENDS = 120;
	private static final int ME_FRIEND_ADD = 121;
	private static final int PERSON = 200;
	private static final int PERSON_PICTURE = 210;
	private static final int PERSON_MECARD = 220;
	private static final int PERSON_FRIENDS = 230;
	private static final int SEARCH = 300;

	/**
	 * The UriMatcher, which parses the incoming query-uris
	 */
	private static final UriMatcher uriMatcher = new UriMatcher(WORLD);

	static {
		uriMatcher.addURI(AUTHORITY, "me/friend/add", ME_FRIEND_ADD);
		uriMatcher.addURI(AUTHORITY, "me/friends", ME_FRIENDS);
		uriMatcher.addURI(AUTHORITY, "me/mecard", ME_MECARD);
		uriMatcher.addURI(AUTHORITY, "me", ME);
		uriMatcher.addURI(AUTHORITY, "person/friends/*", PERSON_FRIENDS);
		uriMatcher.addURI(AUTHORITY, "person/mecard/*", PERSON_MECARD);
		uriMatcher.addURI(AUTHORITY, "person/picture/*", PERSON_PICTURE);
		uriMatcher.addURI(AUTHORITY, "person/*", PERSON);
		uriMatcher.addURI(AUTHORITY, "search/*", SEARCH);
	}

	/**
	 * The WebID of the user
	 */
	private static String me;

	/**
	 * The Application Context in which the ContentProvider is running and the
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

		me = sharedPreferences.getString("me", null);
		if (me == null) {
			Log.i(TAG,
					"No URI for \"me\" specified in FoafProvider, please set a URI in configuration.");
			me = Constants.EXAMPLE_webId;
		} else {
			Log.v(TAG, "URI for \"me\" is: " + me);
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
			return getMe(projection);
		case PERSON:
			if (path.size() > 1) {
				return getPerson(path.get(1), projection);
			}
			break;
		case ME_MECARD:
			return getMeCard(projection);
		case PERSON_MECARD:
			if (path.size() > 2) {
				return getMeCard(path.get(2), projection);
			}
			break;
		case ME_FRIENDS:
			return getFriends(projection);
		case PERSON_FRIENDS:
			if (path.size() > 2) {
				return getFriends(path.get(2), projection);
			}
			break;
		case PERSON_PICTURE:
			if (path.size() > 2) {
				return getPicture(path.get(2), projection);
			}
			break;
		case SEARCH:
			if (path.size() > 1) {
				return search(path.get(1));
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
			String webid = values.getAsString("webid");
			String relation = values.getAsString("relation");
			addFriend(webid, relation);
			return 1;
		default:
			return 0;
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

		int match = uriMatcher.match(uri);
		switch (match) {
		case ME_FRIEND_ADD:
			String webid = values.getAsString("webid");
			String relation = values.getAsString("relation");
			return addFriend(webid, relation);
			// return Uri.parse((String) values.get("webid"));
		default:
			return null;
		}
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.v(TAG, "Shared Preference Changed key = \"" + key + "\".");
		if (key == "me") {
			me = sharedPreferences.getString(key, Constants.EXAMPLE_webId);
		}
	}
	
	/*------------- private ----------------*/

	private Cursor getMe(String[] projection) {
		return getPerson(me, projection);
	}

	private Cursor getPerson(String uri, String[] projection) {
		Log.v(TAG, "getPerson: <" + uri + ">");

		try {
			Uri contentUri;
			contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI + "/resource/"
					+ URLEncoder.encode(uri, Constants.ENC));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");
			Cursor rc = getContentResolver().query(contentUri, projection,
					null, null, null);

			return rc;
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
			return null;
		}
	}

	private Cursor getMeCard(String[] projection) {
		return getMeCard(me, projection);
	}

	private Cursor getMeCard(String uri, String[] projection) {
		Log.v(TAG, "getMeCard: <" + uri + ">");

		try {
			Uri contentUri;
			contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI + "/resource/"
					+ URLEncoder.encode(uri, Constants.ENC));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");

			String selection = null;
			if (projection == null) {
				int length = Constants.PROPS_relations.length;
				projection = new String[length+1];
				System.arraycopy(Constants.PROPS_relations, 0, projection, 0, length);
				projection[length] = Constants.PROP_hasData;
				selection = "complement";
			}
			Cursor rc = getContentResolver().query(contentUri, projection,
					selection, null, null);

			return rc;
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
			return null;
		}
	}

	private Cursor getPicture(String uri, String[] projection) {
		Log.v(TAG, "getPicture: <" + uri + ">");

		try {

			Uri contentUri;
			contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI + "/resource/"
					+ URLEncoder.encode(uri, Constants.ENC));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");

			if (projection != null) {
				Log.i(TAG, "projection not supported for getPicture()");
			}
			Cursor rc = getContentResolver().query(contentUri, Constants.PROPS_pictureProps,
					null, null, null);

			return rc;
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
			return null;
		}
	}

	private Cursor getFriends(String[] projection) {
		return getFriends(me, projection);
	}

	private Cursor getFriends(String uri, String[] projection) {
		Log.v(TAG, "getFriends: <" + uri + ">");

		try {
			Uri contentUri;
			contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI + "/resource/"
					+ URLEncoder.encode(uri, Constants.ENC));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");

			if (projection == null) {
				projection = Constants.PROPS_relations;
			}

			Cursor rc = getContentResolver().query(contentUri, Constants.PROPS_relations, null,
					null, null);
			if (rc != null) {
				String relation;
				String relationReadable;
				boolean isResource;
				NameHelper nh = new NameHelper(getContext());
				ArrayList<String> uris = new ArrayList<String>();
				while (rc.moveToNext()) {
					int objectType = Integer.parseInt(rc.getString(rc.getColumnIndex("objectType"))); 
					//isResource = rc.getString(rc.getColumnIndex("oIsResource"))
					//		.equals("true");
					isResource = objectType < 2 ? true : false;
					if (isResource) {
						uris.add(rc.getString(rc.getColumnIndex("object")));
					}
				}
				rc.moveToPosition(-1);
				HashMap<String, String> names = nh.getNames(uris);
				PersonCursor pc = new PersonCursor();
				while (rc.moveToNext()) {
					int objectType = Integer.parseInt(rc.getString(rc.getColumnIndex("objectType"))); 
					//isResource = rc.getString(rc.getColumnIndex("oIsResource"))
					//		.equals("true");
					isResource = objectType < 2 ? true : false;
					if (isResource) {
						uri = rc.getString(rc.getColumnIndex("object"));
						relation = rc.getString(rc.getColumnIndex("predicate"));
						relationReadable = rc.getString(rc
								.getColumnIndex("predicateReadable"));
						pc.addPerson(uri, relation, names.get(uri),
								relationReadable, null);
					}
				}
				return pc;
			} else {
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Problem with encoding uri for the query.", e);
			return null;
		}
	}

	private Uri addFriend(String webid, String relation) {

		if (webid == null) {
			Log.i(TAG, "No webid to add specified, returning null");
			return null;
		}
		
		if (relation == null) {
			relation = Constants.PROP_knows;
		}

		try {
			Uri contentUri;
			contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI
					+ "/resource/addTriple/"
					+ URLEncoder.encode(me, Constants.ENC));

			ContentValues values = new ContentValues();
			values.put("subject", me);
			values.put("predicate", relation);
			values.put("object", webid);

			Log.i(TAG, "Adding new friend");
			Log.i(TAG,  "You <" + me + "> will know <" + relation + "> a new Person <" + webid + ">.");

			return getContentResolver().insert(contentUri, values);

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error on adding Friend to your WebID.", e);
			return null;
		}
	}

	private Cursor search(String searchTerm) {
		// explicit search
		if (searchTerm.startsWith("http:") || searchTerm.startsWith("https:")) {
			try {
				Uri contentUri;
				contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI
						+ "/resource/tmp/"
						+ URLEncoder.encode(searchTerm, Constants.ENC));

				Log.i(TAG, "Getting WebID <" + searchTerm + ">.");

				String[] projection = { Constants.PROP_rdfType };

				Cursor rc = getContentResolver().query(contentUri, projection,
						null, null, null);

				if (rc != null) {
					NameHelper nh = new NameHelper(getContext());
					PersonCursor pc = new PersonCursor();
					String webid;
					while (rc.moveToNext()) {

						webid = rc.getString(rc.getColumnIndex("subject"));
						if (webid.equals(searchTerm)) {
							pc.addPerson(webid, null, nh.getName(webid), null,
									null);
							break;
						}
					}
					return pc;
				} else {
					return null;
				}
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Error on adding Friend to your WebID.", e);
				return null;
			}
		} else {
			Log.v(TAG, "Free search not yet supported");

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
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
