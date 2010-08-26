package org.aksw.msw;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * The triple Provider is a simple Android ContentProvider, which stores and
 * retrieves semantic datasets according to the Linked Data principle.
 * 
 * @author natanael
 * 
 */
public class TripleProvider extends ContentProvider {

	private static final String TAG = "TripleProvider";
	public static final String AUTHORITY = "org.aksw.msw.tripleprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final String DISPLAY_NAME = "TripleProvider";

	/**
	 * content://org.aksw.msw.tripleprovider returns nothing, because the whole
	 * web is to much. content://org.aksw.msw.tripleprovider/resource/_uri_
	 * returns all triples with the given _uri_ as subject
	 * content://org.aksw.msw.tripleprovider/resource/save/_uri_ returns all
	 * triples with the given _uri_ as subject if it is not in the triplestore
	 * import it with Linked Data into the persistent model
	 * content://org.aksw.msw.tripleprovider/resource/tmp/_uri_ returns all
	 * triples with the given _uri_ as subject if it is not in the triplestore
	 * import it with Linked Data into the cache model
	 * content://org.aksw.msw.tripleprovider/resource/offline/_uri_ returns all
	 * triples with the given _uri_ as subject from the triplestore
	 * content://org.aksw.msw.tripleprovider/resource/inverse/_uri_ returns all
	 * triples with the given _uri_ as object
	 * content://org.aksw.msw.tripleprovider/class/
	 * content://org.aksw.msw.tripleprovider/type/ returns a list of all
	 * classes/types content://org.aksw.msw.tripleprovider/class/_uri_
	 * content://org.aksw.msw.tripleprovider/type/_uri_ returns all members of a
	 * class/type content://org.aksw.msw.tripleprovider/sparql/_sparql_
	 */

	private static final int WORLD = 1;

	private static final int RESOURCE = 10;
	private static final int RESOURCE_SAVE = 11;
	private static final int RESOURCE_TMP = 12;
	private static final int RESOURCE_OFFLINE = 13;
	private static final int RESOURCE_INV = 15;

	private static final int CLASS = 20;
	private static final int CLASS_OVERVIEW = 21;

	private static final int TYPE = 30;
	private static final int TYPE_OVERVIEW = 31;

	private static final int BNODE = 40;

	private static final int SPARQL = 50;

	private static final int UPDATE_ALL = 60;
	private static final int UPDATE_THIS = 61;

	private static final UriMatcher uriMatcher = new UriMatcher(WORLD);

	static {
		uriMatcher.addURI(AUTHORITY, "resource/tmp/*", RESOURCE_TMP);
		uriMatcher.addURI(AUTHORITY, "resource/save/*", RESOURCE_SAVE);
		uriMatcher.addURI(AUTHORITY, "resource/offline/*", RESOURCE_OFFLINE);
		uriMatcher.addURI(AUTHORITY, "resource/inverse/*", RESOURCE_INV);
		uriMatcher.addURI(AUTHORITY, "resource/*", RESOURCE);
		uriMatcher.addURI(AUTHORITY, "class/*", CLASS);
		uriMatcher.addURI(AUTHORITY, "class/", CLASS_OVERVIEW);
		uriMatcher.addURI(AUTHORITY, "type/*", TYPE);
		uriMatcher.addURI(AUTHORITY, "type/", TYPE_OVERVIEW);
		uriMatcher.addURI(AUTHORITY, "bnode/*", BNODE);
		uriMatcher.addURI(AUTHORITY, "sparql/*", SPARQL);
		uriMatcher.addURI(AUTHORITY, "update/*", UPDATE_THIS);
		uriMatcher.addURI(AUTHORITY, "update/", UPDATE_ALL);
	}

	private static ModelManager mm;

	// ---------------------------- methods --------------------

	@Override
	public boolean onCreate() {
		mm = new ModelManager(getContext());
		return true;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.v(TAG,
				"TripleProvider gets toled about low memory. Should destroy Memmodels and so on.");
		mm.clearCache();
	}

	@Override
	public String getType(Uri uri) {

		// String mimeTypeResItm =
		// "vnd.android.cursor.item/vnd.aksw.msw.resource";
		// String mimeTypeResDir =
		// "vnd.android.cursor.dir/vnd.aksw.msw.resource";
		// String mimeTypeTriple = "vnd.android.cursor.dir/vnd.aksw.msw.triple";
		String mimeTypeResItm = "vnd.android.cursor.dir/vnd.aksw.msw.triple";
		String mimeTypeResDir = "vnd.android.cursor.dir/vnd.com.hp.hpl.jena.rdf.model.resource";
		String mimeTypeTriple = "vnd.android.cursor.dir/vnd.com.hp.hpl.jena.rdf.model.statement";

		int match = uriMatcher.match(uri);
		switch (match) {
		case RESOURCE:
		case RESOURCE_TMP:
		case RESOURCE_SAVE:
		case RESOURCE_OFFLINE:
		case CLASS:
		case TYPE:
			return mimeTypeResItm;
		case WORLD:
		case RESOURCE_INV:
		case CLASS_OVERVIEW:
		case TYPE_OVERVIEW:
			return mimeTypeResDir;
		case SPARQL:
			/**
			 * sparql is not implemented, because androjena doesn't include ARQ
			 * 2010-08-19 androjena works on ARQ support, but I don't need it
			 * now
			 */
		case BNODE:
			return mimeTypeTriple;
		default:
			return null;
		}
	}

	/**
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 *      java.lang.String[], java.lang.String, java.lang.String[],
	 *      java.lang.String)
	 * @param projection
	 *            An array of property URIs. If empty return all properties.
	 * @param selection
	 *            if this string is null nothing special happens, if it is not
	 *            null the projection will be interpreted as complement to all
	 *            existing properties
	 * @param selectionArgs
	 *            unused
	 * @param sortOrder
	 *            unused
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.v(TAG, "Starting query");

		Resource res = null;
		String[] properties = projection;

		boolean complement;

		if (selection == null) {
			complement = false;
		} else {
			complement = true;
		}

		ArrayList<String> path = new ArrayList<String>(uri.getPathSegments());

		Log.v(TAG, "path.size() = " + path.size() + ".");
		if (path.size() > 0) {
			Log.v(TAG, "path(0/" + path.size() + "): " + path.get(0) + ".");
		}
		if (path.size() > 1) {
			Log.v(TAG, "path(1/" + path.size() + "): " + path.get(1) + ".");
		}
		if (path.size() > 2) {
			Log.v(TAG, "path(2/" + path.size() + "): " + path.get(2) + ".");
		}

		int match = uriMatcher.match(uri);

		Log.v(TAG, "Matching URI <" + uri + "> match: (" + match + ").");

		switch (match) {
		case RESOURCE:
			if (path.size() > 1) {
				Log.v(TAG, "getResource: <" + path.get(1) + ">");
				res = getResource(path.get(1), match);
			} else {
				Log.v(TAG, "Size of path (" + path.size() + ") to short. <"
						+ uri + ">");
			}
			break;
		case RESOURCE_TMP:
		case RESOURCE_SAVE:
		case RESOURCE_OFFLINE:
			if (path.size() > 2) {
				Log.v(TAG, "getResource: <" + path.get(2) + ">");
				res = getResource(path.get(2), match);
			} else {
				Log.v(TAG, "Size of path (" + path.size() + ") to short. <"
						+ uri + ">");
			}
			break;
		case BNODE:
			if (path.size() > 1) {
				Log.v(TAG, "getBlankNode: <" + path.get(1) + ">");
				res = getBlankNode(path.get(1));
			} else {
				Log.v(TAG, "Size of path (" + path.size() + ") to short. <"
						+ uri + ">");
			}
			break;
		/**
		 * The following cases are not implemented at the moment
		 */
		case WORLD:
		case RESOURCE_INV:
		case CLASS:
		case CLASS_OVERVIEW:
		case TYPE:
		case TYPE_OVERVIEW:
		case SPARQL:
		default:
			Log.v(TAG, "Return null because unimplemented URI was queried: ("
					+ match + ")");
			return null;
		}

		if (res != null) {
			TripleCursor tc = new TripleCursor(res, properties, complement);
			Log.v(TAG, "Cursor created");
			return tc;
		} else {
			Log.v(TAG, "Return null because I couldn't get a Resource.");
			return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException(
				"The TripleProvider is not capable of inserting Resources, sorry.");
		// return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException(
				"The TripleProvider is not capable of deleting Resources, sorry.");
		// return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int match = uriMatcher.match(uri);
		switch (match) {
		case UPDATE_ALL:
			mm.updateResources();
			return 2;
		case UPDATE_THIS:
			ArrayList<String> path = new ArrayList<String>(
					uri.getPathSegments());

			if (path.size() > 1) {
				mm.updateResource(path.get(1));
				return 1;
			} else {
				Log.v(TAG, "Size of path (" + path.size() + ") to short. <"
						+ uri + ">");
				return 0;
			}
		default:
			return 0;
		}
	}

	// ---------------------------- private --------------------

	/**
	 * r - read permission w - write permission (means can request to model to
	 * import missing data from the web) cache model TMP-Mode rw r- SAV-Mode r-
	 * rw OFF-Mode -- r-
	 */

	private static final int TMP = RESOURCE_TMP;
	private static final int SAV = RESOURCE_SAVE;
	private static final int OFF = RESOURCE_OFFLINE;

	private Resource getBlankNode(String id) {
		Resource resource = mm.getModel().createResource(new AnonId(id));
		StmtIterator iterator = resource.listProperties();
		while (iterator.hasNext()) {
			String triple = iterator.next().asTriple().toString();
			Log.v(TAG, "BNode (" + id + ") has triple: " + triple);
		}
		return resource;
	}

	private Resource getResource(String uri, int mode) {
		switch (mode) {
		case TMP:
			return queryResource(uri, false);
		case SAV:
			return queryResource(uri, true);
		case OFF:
			return queryResource(uri, true);
		default:
			return queryResource(uri, true);
		}
	}

	/**
	 * Read this resource from the model.
	 * 
	 * @param uri
	 *            the URI of the resource you want to get
	 * @return a jena-Resource-Object, or null if this resource is not available
	 */
	private Resource queryResource(String uri, boolean persistant) {
		boolean inferenced = true;

		Resource resource = mm.getModel(uri, persistant, inferenced).getResource(uri);

		StmtIterator iterator = resource.listProperties();
		while (iterator.hasNext()) {
			String triple = iterator.next().asTriple().toString();
			Log.v(TAG, "Resource (" + uri + ") has triple: " + triple);
		}
		
		return resource;
	}

	/**
	 * Check whether a triple with the given uri exists as subject or object in
	 * the model or not.
	 * 
	 * @param uri
	 *            the uri of the subject-resource
	 * @param model
	 *            the model in which you want to check for the resource
	 * @param asObject
	 *            check also if the resource exists in a triple as object
	 * @return whether the resource occures as subject in a triple or not
	 */
	private boolean resourceExists(String uri, Model model, boolean asObject) {

		Resource res = model.getResource(uri);

		if (model.contains(res, null, (RDFNode) null)) {
			Log.v(TAG, "The resource <" + uri
					+ "> does exist as Subject in the given model.");
			return true;
		} else if (asObject && model.contains(null, null, res)) {
			Log.v(TAG, "The resource <" + uri
					+ "> does exist as Object in the given model.");
			return true;
		} else {
			Log.v(TAG, "The resource <" + uri
					+ "> doesn't exist in the given model.");
			return false;
		}

	}

	public static String getName(Resource person) {
		return person.getLocalName();
	}

	public static String getLable(Resource resource) {
		return resource.getLocalName();
	}

}
