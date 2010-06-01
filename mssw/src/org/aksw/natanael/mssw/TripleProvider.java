package org.aksw.natanael.mssw;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * The triple Provider is a simple Android ContentProvider, which stores
 * and retrieves semantic datasets according to the Linked Data principle. 
 * @author natanael
 *
 */
public class TripleProvider {
	/**
	 * The model contains all triples stored on the device.
	 */
	private Model model;
	
	/**
	 * The cache-Model contains some triples, which are not stored permanently
	 * on the device, but are needed for the current work.
	 */
	private Model cache;

	public TripleProvider() {
		ModelMaker models = ModelFactory.createFileModelMaker("");
		ModelMaker caches = ModelFactory.createMemModelMaker();
		cache = caches.openModel("cache");
		model = models.openModel("model");
	}
	
	public Resource getResource(String uri) {
		Resource res;
		res = model.getResource(uri);
		if (res == null) {
			res = cache.getResource(uri);
		}
		if (res == null) {
			res = cacheResource(uri);
		}
		return res;
	}
	
	public void importResource(String uri) {
		
	}
	
	public Resource cacheResource(String uri) {
		Model tmp = ModelFactory.createDefaultModel();
		tmp.read(uri);
		Resource res = tmp.getResource(uri);
		cache.add(res);
		return res;
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
}
