package org.aksw.natanael.msw;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

/**
 * The triple Provider is a simple Android ContentProvider, which stores
 * and retrieves semantic datasets according to the Linked Data principle. 
 * @author natanael
 *
 */
public class TripleProvider extends ContentProvider{
	
	public static final Uri CONTENT_URI =
		Uri.parse("content://org.aksw.msw/tripleStore");
	public static final String DISPLAY_NAME = "";
	
	/**
	 * The model contains all triples stored on the device.
	 */
	private Model model;
	
	/**
	 * The cache-Model contains some triples, which are not stored permanently
	 * on the device, but are needed for the current work.
	 */
	private Model cache;
	
	public Resource getResource(String uri) {
		Resource res;
		res = model.getResource(uri);//lol
		if (res == null) {
			res = cache.getResource(uri);
		}
		if (res == null) {
			res = cacheResource(uri);
		}
		return res;
	}
	
	private void importResource(String uri) {
		
	}
	
	public Resource cacheResource(String uri) {
		Model tmp = ModelFactory.createDefaultModel();
		tmp.read(uri);
		Resource subj = new ResourceImpl(uri);
		SimpleSelector selector = new SimpleSelector(subj, (Property)null, (RDFNode)null);
		cache.union(tmp.query(selector));
		
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
		return null;
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
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
