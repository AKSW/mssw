package org.aksw.msw;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

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
	public static final String DISPLAY_NAME = "";
	public static final String LOG_TAG = "test";

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
	private static final int RESOURCE_INV = 15;

	private static final int CLASS = 20;
	private static final int CLASS_OVERVIEW = 21;

	private static final int TYPE = 30;
	private static final int TYPE_OVERVIEW = 31;

	private static final int SPARQL = 50;

	private static final UriMatcher uriMatcher = new UriMatcher(WORLD);

	static {
		uriMatcher.addURI(AUTHORITY, "resource/*", RESOURCE);
		uriMatcher.addURI(AUTHORITY, "resource/tmp/*", RESOURCE_TMP);
		uriMatcher.addURI(AUTHORITY, "resource/save/*", RESOURCE_SAVE);
		uriMatcher.addURI(AUTHORITY, "resource/inverse/*", RESOURCE_INV);
		uriMatcher.addURI(AUTHORITY, "class/*", CLASS);
		uriMatcher.addURI(AUTHORITY, "class/", CLASS_OVERVIEW);
		uriMatcher.addURI(AUTHORITY, "type/*", TYPE);
		uriMatcher.addURI(AUTHORITY, "type/", TYPE_OVERVIEW);
		uriMatcher.addURI(AUTHORITY, "sparql/*", SPARQL);
	}

	/**
	 * some often used properties
	 */
	public static final String PROPNAMESPACE = "http://msw.aksw.org/";
	public static final Property PROP_LASTUPDATE = new PropertyImpl(
			PROPNAMESPACE, "lastUpdate");

	/**
	 * The model contains all triples stored on the device.
	 */
	private Model model;

	/**
	 * The cache-Model contains some triples, which are not stored permanently
	 * on the device, but are needed for the current work.
	 */
	private Model cache;

	// ---------------------------- methods --------------------
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub

		String mimeTypeResItm = "vnd.android.cursor.item/vnd.aksw.msw.resource";
		String mimeTypeResDir = "vnd.android.cursor.dir/vnd.aksw.msw.resource";
		String mimeTypeTriple = "vnd.android.cursor.dir/vnd.aksw.msw.triple";

		int match = uriMatcher.match(uri);
		switch (match) {
		case RESOURCE:
		case RESOURCE_TMP:
		case RESOURCE_SAVE:
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
			 */
			return mimeTypeTriple;
		default:
			return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		ModelMaker models = ModelFactory.createFileModelMaker("");
		ModelMaker caches = ModelFactory.createMemModelMaker();
		cache = caches.openModel("cache");
		model = models.openModel("model");
		Log.v(TAG, "Created TripleProvider");
		return true;
	}

	/**
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 *      java.lang.String[], java.lang.String, java.lang.String[],
	 *      java.lang.String)
	 * @param projection
	 *            is not used in this provider
	 * @param selection
	 *            specify a WHERE clause if you use spqrql, else it is ignorred
	 * @param selectionArgs
	 *            some substitutions for the selection, i don't use this
	 * @param sortOrder
	 *            specify the sort order like in sparql
	 */
	@Override
	public ResourceCursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		Log.v(TAG, "Starting query");

		Resource res;
		ResourceCursor rc;
		String resourceUri = null;

		ArrayList<String> path = new ArrayList<String>(uri.getPathSegments());
		Log.v(TAG, "path(1): " + path.get(1) + ".");

		int match = uriMatcher.match(uri);
		Log.v(TAG, "Matching URI <" + uri + "> match: (" + match + ").");
		switch (match) {
		case RESOURCE:
			if(path.size() > 1) {
				resourceUri = path.get(1);
			} else {
				Log.v(TAG, "Size of path (" + path.size() + ") to short. <" + uri + ">");
			}
		case RESOURCE_TMP:
			if (resourceUri == null && path.size() > 2) {
				resourceUri = path.get(2);
			}

			Log.v(TAG, "getResource: <" + resourceUri + ">");
			res = getResource(resourceUri);
			break;
		case RESOURCE_SAVE:
			if(path.size() > 2) {
				resourceUri = path.get(2);
			} else {
				Log.v(TAG, "Size of path (" + path.size() + ") to short. <" + uri + ">");
			}

			res = getResource(resourceUri);
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
			Log.v(TAG, "Return null because unimplemented URI was queried: (" + match + ")");
			return null;
		}

		rc = new ResourceCursor(res);
		
		Log.v(TAG, "Cursor created");
		
		return rc;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	// ---------------------------- private --------------------

	/*
	 * public TripleProvider(){ ModelMaker models =
	 * ModelFactory.createFileModelMaker(""); ModelMaker caches =
	 * ModelFactory.createMemModelMaker(); cache = caches.openModel("cache");
	 * model = models.openModel("model"); }
	 */

	public Resource getResource(String uri) {
		Model tmp = model.union(cache);

		Resource res = tmp.getResource(uri);

		StmtIterator si;

		si = res.listProperties();

		Log.v(LOG_TAG, "1. Resource in getResource " + si.toString());
		if (!si.hasNext()) {
			res = cacheResource(uri);
			Log.v(LOG_TAG, "2. Resource in getResource " + si.toString());
		}
		return res;
	}

	private void importResource(String uri) {
		/**
		 * vielleicht named-graphes verwenden
		 */
		Model tmp = ModelFactory.createDefaultModel();
		tmp.read(uri);
		Resource subj = new ResourceImpl(uri);
		SimpleSelector selector = new SimpleSelector(subj, (Property) null,
				(RDFNode) null);
		cache.union(tmp.query(selector));

	}

	public Resource cacheResource(String uri) {
		cache.read(uri);
		/*
		 * Model tmp = ModelFactory.createDefaultModel(); tmp.read(uri);
		 * Resource subj = new ResourceImpl(uri); SimpleSelector selector = new
		 * SimpleSelector(subj, (Property)null, (RDFNode)null);
		 * cache.union(tmp.query(selector));
		 */

		Property lastUpdate = cache.createProperty(PROPNAMESPACE, "lastUpdate");
		cache.getResource(uri).addProperty(lastUpdate, "heute");

		return cache.getResource(uri);
	}

}
