package org.aksw.msw;

import android.content.ContentProvider;
import android.content.ContentValues;
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
 * The triple Provider is a simple Android ContentProvider, which stores
 * and retrieves semantic datasets according to the Linked Data principle. 
 * @author natanael
 *
 */
public class TripleProvider extends ContentProvider{
	
	public static final Uri CONTENT_URI =
		Uri.parse("content://org.aksw.msw.tripleprovider");
	public static final String DISPLAY_NAME = "";
	public static final String LOG_TAG = "test";
	
	/**
	 * The model contains all triples stored on the device.
	 */
	private Model model;
	
	/**
	 * The cache-Model contains some triples, which are not stored permanently
	 * on the device, but are needed for the current work.
	 */
	private Model cache;
	
	public TripleProvider(){
		ModelMaker models = ModelFactory.createFileModelMaker("");
		ModelMaker caches = ModelFactory.createMemModelMaker();
		cache = caches.openModel("cache");
		model = models.openModel("model");
	}
	
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
		 * villeicht named-graphes verwenden
		 */
		Model tmp = ModelFactory.createDefaultModel();
		tmp.read(uri);
		Resource subj = new ResourceImpl(uri);
		SimpleSelector selector = new SimpleSelector(subj, (Property)null, (RDFNode)null);
		cache.union(tmp.query(selector));
		
		
	}
	
	public Resource cacheResource(String uri) {
		cache.read(uri);
		/*Model tmp = ModelFactory.createDefaultModel();
		tmp.read(uri);
		Resource subj = new ResourceImpl(uri);
		SimpleSelector selector = new SimpleSelector(subj, (Property)null, (RDFNode)null);
		cache.union(tmp.query(selector));*/
		
		Property lastUpdata = cache.createProperty("http://msw.aksw.org/lastUpdate/");
		
		cache.getResource(uri).addProperty(lastUpdata, "heute");
		
		return cache.getResource(uri);
	}
	
	private Model getModel(String uri) {
		Resource res;
		res = model.getResource(uri);
		if (res != null) {
			return model;
		}
		res = cache.getResource(uri);
		if (res != null) {
			return cache;
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) { 
		// TODO Auto-generated method stub
		
		/**
		 * content://org.aksw.msw.tripleprovider
		 *	returns nothing, because the whole web is to much.
		 * content://org.aksw.msw.tripleprovider/resource/_uri_
		 * 	returns all triples with the given _uri_ as subject
		 * content://org.aksw.msw.tripleprovider/resource/tmp/_uri_
		 * 	returns all triples with the given _uri_ as subject if it is not in the triplestore
		 * 	import it with Linked Data
		 * content://org.aksw.msw.tripleprovider/resource/inverse/_uri_
		 * 	returns all triples with the given _uri_ as object
		 * content://org.aksw.msw.tripleprovider/class/
		 * content://org.aksw.msw.tripleprovider/type/
		 * 	returns a list of all classes/types
		 * content://org.aksw.msw.tripleprovider/class/_uri_
		 * content://org.aksw.msw.tripleprovider/type/_uri_
		 * 	returns all members of a class/type
		 * content://org.aksw.msw.tripleprovider/sparql/_sparql_
		 */ 
		
		String mimeTypeItm = "vnd.android.cursor.item/vnd.aksw.msw.resource";
		String mimeTypeDir = "vnd.android.cursor.dir/vnd.aksw.msw.resource";
		return mimeTypeItm;
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
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		ResourceCursor rc = new ResourceCursor();
		return rc;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
