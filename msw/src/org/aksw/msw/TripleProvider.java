package org.aksw.msw;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
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
import com.hp.hpl.jena.shared.JenaException;

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

	private static final int SPARQL = 50;

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

		//String mimeTypeResItm = "vnd.android.cursor.item/vnd.aksw.msw.resource";
		//String mimeTypeResDir = "vnd.android.cursor.dir/vnd.aksw.msw.resource";
		//String mimeTypeTriple = "vnd.android.cursor.dir/vnd.aksw.msw.triple";
		String mimeTypeResItm = "vnd.android.cursor.item/vnd.com.hp.hpl.jena.rdf.model.resource";
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

		if(initModels()) {
			Log.v(TAG, "Created TripleProvider");
			return true;
		} else {
			Log.e(TAG, "The models couln't be initiated.");
			return false;
		}
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.v(TAG, "TripleProvider gets toled about low memory. Should destroy Memmodels and so on.");
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

		Resource res = null;

		// Debugoutput
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

		// Debugoutput
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

		ResourceCursor rc;
		if (res != null) {
			rc = new ResourceCursor(res);
			Log.v(TAG, "Cursor created");
		} else {
			Log.v(TAG, "Return null because I couldn't get a Resource.");
			return null;
		}

		return rc;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The TripleProvider is not capable of updating Resources, sorry.");
		//return 0;
	}

	// ---------------------------- private --------------------

	/**
	 * r - read permission
	 * w - write permission (means can request to model to import missing data from the web)
	 * 				cache 	model
	 * TMP-Mode		 rw		 r-
	 * SAV-Mode		 r-		 rw
	 * OFF-Mode		 --		 r- 
	 */
	
	private static final int TMP = RESOURCE_TMP;
	private static final int SAV = RESOURCE_SAVE;
	private static final int OFF = RESOURCE_OFFLINE;

	private Resource getResource(String uri, int mode) {
		switch (mode) {
		case TMP:
			return cacheResource(uri);
		case SAV:
			return importResource(uri);
		case OFF:
			return queryResource(uri);
		default:
			return null;
		}
	}

	/**
	 * Read this resource from the model.
	 * @param uri the URI of the resource you want to get
	 * @return a jena-Resource-Object, or null if this resource is not available
	 */
	private Resource queryResource(String uri) {
		// maybe it is better if this method would query the union of model and
		// cache, but I don't know. The future will tell me, what's right.
		return queryResource(uri, model);
	}
	
	private Resource queryResource(String uri, Model model) {
		// 1. check if resource exists
		if (resourceExists(uri, model)) {
			// 2a. get and return resource
			return model.getResource(uri);
		} else {
			// 2b. or null if resource doesn't exist
			return null;
		}
	}

	private Resource importResource(String uri) {
		// 1. check if resource exists in model
		if (!resourceExists(uri, model)) {
			// 2. if not 1, then check if resource exists in cache
			if (resourceExists(uri, cache)) {
				// 3a. if 2 import resource from cache to model
				Resource subj = new ResourceImpl(uri);
				SimpleSelector selector = new SimpleSelector(subj, (Property) null,
						(RDFNode) null);
				
				model.add(cache.query(selector));
				model.commit();
			} else {
				// 3b. if not 2, then import resource to model from the web (Linked Data)
				Model tmp = ModelFactory.createDefaultModel();
				try {
					tmp.read(uri);

					Resource subj = new ResourceImpl(uri);
					SimpleSelector selector = new SimpleSelector(subj,
							(Property) null, (RDFNode) null);

					// on every update of a resource from the web I should add
					// some
					// information about the last update, for versioning and
					// sync

					// Property lastUpdate = cache.createProperty(PROPNAMESPACE,
					// "lastUpdate");
					// tmp.getResource(uri).addProperty(lastUpdate, "heute");

					model.add(tmp.query(selector));
					tmp.close();
					model.commit();
				} catch (JenaException e) {
					Log.v(TAG, "An Exception occured whyle querying uri <" + uri + ">", e);
				}
			}
		} else {
			// 2b. the resource exists in model, so we can query it
		}

		// 4. return resource from model
		return queryResource(uri, model);
	}

	private Resource cacheResource(String uri) {
		// 1. check if resource exists in model
		// 2. if not 1, then check if resource exists in cache
		// 3. if not 2, then import resource to cache
		// 4. return resource from cache

		// 1. check if resource exists in model
		if (!resourceExists(uri, model)) {
			// 2. if not 1, then check if resource exists in cache
			if (!resourceExists(uri, cache)) {
				// 3b. if not 2, then import resource to cache from the web (Linked Data)
				Model tmp = ModelFactory.createDefaultModel();
				try {
					tmp.read(uri);

					Resource subj = new ResourceImpl(uri);
					SimpleSelector selector = new SimpleSelector(subj,
							(Property) null, (RDFNode) null);

					// on every update of a resource from the web I should add
					// some
					// information about the last update, for versioning and
					// sync

					// Property lastUpdate = cache.createProperty(PROPNAMESPACE,
					// "lastUpdate");
					// tmp.getResource(uri).addProperty(lastUpdate, "heute");

					cache.add(tmp.query(selector));
					tmp.close();
				} catch (JenaException e) {
					Log.v(TAG, "An Exception occured whyle querying uri <" + uri + ">", e);
				}
			}
		} else {
			// 2b. the resource exists in model, so we can query it
			return queryResource(uri, model);
		}

		// 4. return resource from cache
		return queryResource(uri, cache);
	}
	
	/**
	 * Check whether a triple with the given uri exists as subject or object in the model
	 * or not.
	 * 
	 * @param uri
	 *            the uri of the subject-resource
	 * @param model
	 *            the model in which you want to check for the resource
	 * @param asObject check also if the resource exists in a tripel as object
	 * @return whether the resource occures as subject in a triple or not
	 */
	private boolean resourceExists(String uri, Model model, boolean asObject) {

		Resource res = model.getResource(uri);
		
		if (model.contains(res, null, (RDFNode)null)) {
			Log.v(TAG, "The resource <" + uri + "> does exist as Subject in the given model.");
			return true;
		} else if (asObject && model.contains(null, null, res)) {
			Log.v(TAG, "The resource <" + uri + "> does exist as Object in the given model.");
			return true;
		} else {
			Log.v(TAG, "The resource <" + uri + "> doesn't exist in the given model.");
			return false;
		}

		/*
		StmtIterator si = res.listProperties();

		if (!si.hasNext()) {
			Log.v(TAG, "The resource <" + uri + "> has no properties in the given model.");
			return false;
		} else {
			Log.v(TAG, "The resource <" + uri + "> has at leased one property in the given model.");
			return true;
		}
		*/

	}
	
	/**
	 * Check whether a triple with the given uri as subject exists in the model
	 * or not.
	 * The asObject parameter is defaulted to false.
	 * 
	 * @param uri
	 *            the uri of the subject-resource
	 * @param model
	 *            the model in which you want to check for the resource
	 * @return whether the resource occures as subject in a triple or not
	 */
	private boolean resourceExists(String uri, Model model) {
		return this.resourceExists(uri, model, false);
	}
	
	private boolean initModels() {
		String state = Environment.getExternalStorageState();

		String path = "/Android/data/org.aksw.msw/files/models/";

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
			File storage = Environment.getExternalStorageDirectory();
			storage.getAbsolutePath();
			if (storage.isDirectory()) {
				File modelsPath = new File(storage, path);
				modelsPath.mkdirs();
				ModelMaker models = ModelFactory.createFileModelMaker(modelsPath.getAbsolutePath());
				ModelMaker caches = ModelFactory.createMemModelMaker();
				cache = caches.openModel("cache");
				model = models.openModel("model");
				model.begin();
				
				return true;
			} else {
				return false;
			}

		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
			return false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
			return false;
		}
		
	}

}
