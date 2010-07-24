/**
 * 
 */
package org.aksw.mssw;

import java.net.URLEncoder;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
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
	private static final Uri TRIPLE_CONTENT_URI = Uri.parse("content://" + TRIPLE_AUTHORITY);
	
	public static final String DISPLAY_NAME = "FoafProvider";
	
	/**
	 * content://org.aksw.mssw.foafprovider
	 * 		returns nothing
	 * content://org.aksw.mssw.foafprovider/me
	 * 		returns the resource representing you
	 * content://org.aksw.mssw.foafprovider/me/friends
	 * 		returns a list of resources of my friends
	 * content://org.aksw.mssw.foafprovider/me/friend/add/_uri_
	 * 		add friends to the friends list
	 * content://org.aksw.mssw.foafprovider/person/_uri_
	 * 		returns a specific person
	 * content://org.aksw.mssw.foafprovider/person/_uri_/friends
	 * 		returns a list of resources of my friends of a specific person
	 * content://org.aksw.mssw.foafprovider/config
	 * 		get the current configuration
	 * content://org.aksw.mssw.foafprovider/config/me
	 * 		set my own webid
	 */

	private static final int WORLD = 1;

	private static final int ME = 100;
	private static final int ME_FRIENDS = 110;
	private static final int ME_FRIEND_ADD = 111;

	private static final int PERSON = 20;
	private static final int PERSON_FRIENDS = 21;

	private static final int CONFIG = 30;
	private static final int CONFIG_ME = 31;

	private static final UriMatcher uriMatcher = new UriMatcher(WORLD);

	static {
		uriMatcher.addURI(AUTHORITY, "me/friend/add/*", ME_FRIEND_ADD);
		uriMatcher.addURI(AUTHORITY, "me/friends", ME_FRIENDS);
		uriMatcher.addURI(AUTHORITY, "me", ME);
		uriMatcher.addURI(AUTHORITY, "person/friends", PERSON_FRIENDS);
		uriMatcher.addURI(AUTHORITY, "person/*", PERSON);
		uriMatcher.addURI(AUTHORITY, "config/me", CONFIG_ME);
		uriMatcher.addURI(AUTHORITY, "config/", CONFIG);
	}

	private String me;
	
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {

		//String mimeTypeResItm = "vnd.android.cursor.item/vnd.aksw.msw.resource";
		//String mimeTypeResDir = "vnd.android.cursor.dir/vnd.aksw.msw.resource";
		//String mimeTypeTriple = "vnd.android.cursor.dir/vnd.aksw.msw.triple";
		String mimeTypeResItm = "vnd.android.cursor.item/vnd.com.hp.hpl.jena.rdf.model.resource";
		String mimeTypeResDir = "vnd.android.cursor.dir/vnd.com.hp.hpl.jena.rdf.model.resource";
		//String mimeTypeTriple = "vnd.android.cursor.dir/vnd.com.hp.hpl.jena.rdf.model.statement";

		int match = uriMatcher.match(uri);
		switch (match) {
		case ME:
		case PERSON:
		case CONFIG_ME:
		case ME_FRIEND_ADD:
			return mimeTypeResItm;
		case WORLD:
		case ME_FRIENDS:
		case PERSON_FRIENDS:
		case CONFIG:
			return mimeTypeResDir;
		default:
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		
		String config_uri = "http://" + AUTHORITY + "/config/me"; 
		
		String enc = "UTF-8";
		Uri contentUri;
		contentUri = Uri.parse(TRIPLE_CONTENT_URI
				+ "/resource/" + URLEncoder.encode(config_uri, enc) + "/");

		Cursor rc = managedQuery(contentUri, null, null, null, null);
		
		return false;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		int match = uriMatcher.match(uri);
		switch (match) {
		case ME:
		case PERSON:
		case CONFIG_ME:
		case ME_FRIEND_ADD:
			return mimeTypeResItm;
		case WORLD:
		case ME_FRIENDS:
		case PERSON_FRIENDS:
		case CONFIG:
			return mimeTypeResDir;
		default:
			return null;
		}
		return 0;
	}

}
