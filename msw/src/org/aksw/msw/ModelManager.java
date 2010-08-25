package org.aksw.msw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.aksw.msw.foafssl.FoafsslURLConnection;
import org.aksw.msw.foafssl.TrustManagerFactory;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.shared.DoesNotExistException;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class ModelManager {

	private static final String TAG = "MswModelManager";

	public static HashMap<String, String> namespaces = new HashMap<String, String>();
	static {
		namespaces.put("rel", "http://purl.org/vocab/relationship/");
		namespaces.put("foaf", "http://xmlns.com/foaf/0.1/");
		namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-shema#");
	}

	private static File webModelsFiles;
	private static File infModelsFiles;
	private static File localModelsFiles;
	// private static File cacheModelsFiles;

	private static HashMap<String, Model> models = new HashMap<String, Model>();

	private static HashMap<String, ModelMaker> modelMakers;
	private static ModelMaker webModels;
	private static ModelMaker infModels;
	private static ModelMaker localModels;
	private static ModelMaker cacheModels;

	private static Context context;

	private static FoafMapper fm;

	public ModelManager(Context context) {
		fm = new FoafMapper(context);

		File storage = Environment.getExternalStorageDirectory();
		webModelsFiles = new File(storage, Constants.WEB_MODELS_DIR);
		infModelsFiles = new File(storage, Constants.INF_MODELS_DIR);
		localModelsFiles = new File(storage, Constants.LOCAL_MODELS_DIR);
		// cacheModelsFiles = new File(storage, Constants.CACHE_MODELS_DIR);

		if (initModelMakers()) {
			Log.v(TAG, "ModelManager initiated ModelMakers successfully.");
		} else {
			Log.v(TAG, "ModelManager couldn't initiate ModelMakers.");
		}

	}

	private boolean initModelMakers() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// read and write access
			webModelsFiles.mkdirs();
			infModelsFiles.mkdirs();
			localModelsFiles.mkdirs();
			
			modelMakers = new HashMap<String, ModelMaker>();
			modelMakers.put("web", ModelFactory
					.createFileModelMaker(webModelsFiles.getAbsolutePath()));
			modelMakers.put("inf", ModelFactory
					.createFileModelMaker(infModelsFiles.getAbsolutePath()));
			modelMakers.put("local", ModelFactory
					.createFileModelMaker(localModelsFiles.getAbsolutePath()));
			modelMakers.put("cache", ModelFactory.createMemModelMaker());

			return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			return false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			return false;
		}

	}

	public Model getModel() {
		return cacheModels.createDefaultModel();
	}

	public Model getModel(String uri, boolean persistant) {
		Model model = null;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) && uri != null) {
			// read and write access
			if (persistant) {
				boolean has = modelMakers.get("web").hasModel(uri);
				model = modelMakers.get("web").openModel(uri);
				if (!has) {
					model = readSSL(uri, model);
				}

			} else {
				boolean has = modelMakers.get("cache").hasModel(uri);
				model = modelMakers.get("cache").openModel(uri);
				if (!has) {
					model = readSSL(uri, null);
				}
			}

			model.setNsPrefixes(namespaces);

			if (modelMakers.get("local").hasModel(uri)) {
				model.add(modelMakers.get("local").openModel(uri));
			}
		} else if (uri == null) {
			Log.v(TAG,
					"You have to give an uri, to get a Model, that is the deal. Returning 'null'");
		} else {
			Log.v(TAG,
					"ExternalStorrage not mounted, couldn't get model, returning 'null'");
		}
		return model;
	}

	public void updateResources() {

		// should check if a internet connection is possible before

		List<String> webModels = listModels("web");
		
		Model model;
		String modelName;
		
		Iterator<String> webModelIterator = webModels.iterator();
		
		while (webModelIterator.hasNext()) {
			modelName = webModelIterator.next();
			Log.v(TAG, "webModelMaker knows: " + modelName);
			if (modelName != null) {
				model = modelMakers.get("web").openModel(modelName);
				try {
					if (model.supportsTransactions()) {
						model.begin();
					}
					model.removeAll();
					readSSL(modelName, model);
					if (model.supportsTransactions()) {
						model.commit();
					}
				} catch (JenaException e) {
					Log.e(TAG, "Exception on updating model. (rollback)", e);
					if (model.supportsTransactions()) {
						model.abort();
					}
				}
				model.close();
			} else {
				Log.v(TAG, "webModelMaker knows model without name.");
			}
		}
	}

	public void updateResource(String uri) {

	}

	public void clearCache() {

	}

	protected Model readSSL(String url, Model model) {

		String state = Environment.getExternalStorageState();

		String certPath = Constants.CERT_DIR + File.separator
				+ "privatekey.p12";

		if (model == null) {
			model = modelMakers.get("cache").createDefaultModel();
		}
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			try {
				// We can read and write the media
				File storage = Environment.getExternalStorageDirectory();

				File keyFile = new File(storage, certPath);
				// storage.getAbsolutePath();
				if (keyFile.isFile()) {

					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(context);

					SSLSocketFactory socketFactory = TrustManagerFactory
							.getFactory(keyFile,
									prefs.getString("privatekey_password", ""));

					try {
						FoafsslURLConnection
								.setDefaultSSLSocketFactory(socketFactory);
						HttpsURLConnection conn = new FoafsslURLConnection(
								new URL(url));

						read(url, model, conn.getInputStream());
					} catch (FileNotFoundException e) {
						Log.e(TAG, "Couldn't find File.", e);
					} catch (IOException e) {
						Log.e(TAG,
								"Input/Output Error while creating or using Socket.",
								e);
						read(url, model, null);
					}

				} else {
					Log.i(TAG,
							"Couldn't get private Key, reading without FOAF+SSL features.");
				}
				read(url, model, null);
			} catch (DoesNotExistException e) {
				Log.e(TAG, "Jena couldn't find the model: '" + url + "'.'", e);
			}
		}

		return model;
	}

	private Model read(String uri, Model model, InputStream inputstream) {

		if (model == null) {
			model = modelMakers.get("cache").openModel(uri);
		}
		Model tmp = modelMakers.get("cache").createFreshModel();
		try {
			if (inputstream == null) {
				tmp.read(uri);
			} else {
				tmp.read(inputstream, uri);
			}
		} catch (JenaException e) {
			Log.e(TAG, "Error on reading <" + uri + "> into temp model.", e);
		}

		Resource subj = new ResourceImpl(uri);
		SimpleSelector selector = new SimpleSelector(subj, (Property) null,
				(RDFNode) null);

		try {
			if (model.supportsTransactions()) {
				model.abort();
				model.begin();
			}
			model.add(tmp.query(selector));
			if (model.supportsTransactions()) {
				model.commit();
			}
		} catch (JenaException e) {
			Log.e(TAG, "Exception on query for resource. (rollback)", e);
			if (model.supportsTransactions()) {
				model.abort();
			}
		}
		tmp.removeAll();
		tmp.close();

		return model;
	}

	private static final String INDEX = "http://ns.aksw.org/Android/SysOnt/index";
	private static final String CONTAINS = "http://ns.aksw.org/Android/SysOnt/contains";

	private ArrayList<String> listModels(String makerKey) {

		ArrayList<String> modelList = new ArrayList<String>();

		List<ModelMaker> makers = new ArrayList<ModelMaker>();

		if (makerKey == null || !makerKey.equals("cache")) {
			// are FileModels
			if (makerKey == null) {
				makers.add(modelMakers.get("web"));
				makers.add(modelMakers.get("inf"));
				makers.add(modelMakers.get("local"));
			} else if (modelMakers.get(makerKey) != null) {
				makers.add(modelMakers.get(makerKey));
			} else {
				Log.v(TAG, "There is no Modeltype: '" + makerKey + "'.");
			}

			Iterator<ModelMaker> makerIterator = makers.iterator();

			while (makerIterator.hasNext()) {
				ModelMaker maker = makerIterator.next();
				Model indexModel = maker.openModel("index");

				Resource index = indexModel.getResource(INDEX);

				StmtIterator models = index.listProperties(indexModel
						.getProperty(CONTAINS));

				while (models.hasNext()) {
					Statement modelEntry = models.next();
					if (modelEntry.getObject().isURIResource()) {
						Resource object = (Resource) modelEntry.getObject();
						modelList.add(object.getURI());
					}
				}
			}
		}

		if (makerKey == null || makerKey.equals("cache")) {
			// is MemModel
			Iterator<String> models = modelMakers.get("cache").listModels();
			while (models.hasNext()) {
				modelList.add(models.next());
			}
		}

		return modelList;
	}

	private boolean modelExists(String uri, String makerKey) {

		return listModels(makerKey).contains(uri);

	}
}