package org.aksw.msw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.aksw.msw.foafssl.TrustManagerFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

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

public class ModelManager {

	private static final String TAG = "MswModelManager";

	private static File webModelsFiles;
	private static File infModelsFiles;
	private static File localModelsFiles;
	// private static File cacheModelsFiles;

	private static HashMap<String, ModelMaker> modelMakers;

	private Context context;
	private SharedPreferences sharedPreferences;

	private static FoafMapper fm;

	public ModelManager(Context contextIn) {
		context = contextIn;

		File storage = Environment.getExternalStorageDirectory();

		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			fm = new FoafMapper(storage, Constants.RULE_FILE, context);
		} else {
			Log.v(TAG,
					"Can not get ruleset file, because external storrage is not mounted.");
		}

		webModelsFiles = new File(storage, Constants.WEB_MODELS_DIR);
		infModelsFiles = new File(storage, Constants.INF_MODELS_DIR);
		localModelsFiles = new File(storage, Constants.LOCAL_MODELS_DIR);

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
		return modelMakers.get("cache").createDefaultModel();
	}

	public Model getModel(String uri, String makerKey) {
		Model model = null;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) && uri != null) {
			if (modelExists(uri, makerKey)) {
				model = modelMakers.get(makerKey).openModel(uri);
			} else {
				model = createModel(uri, makerKey);
				if (model != null && makerKey.equals("web")) {
					readSSL(uri, model);
				}
			}
		} else if (uri == null) {
			Log.v(TAG,
					"You have to give an uri, to get a Model, that is the deal. Returning 'null'.");
		} else {
			Log.v(TAG,
					"ExternalStorrage not mounted, couldn't get model, returning 'null'.");
		}
		return model;
	}

	public Model getModel(String uri, boolean persistant, boolean inferenced) {
		Model model = null;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) && uri != null) {
			// read and write access
			if (persistant) {
				if (modelExists(uri, "web")) {
					model = modelMakers.get("web").openModel(uri);
				} else {
					model = createModel(uri, "web");
					if (model != null) {
						readSSL(uri, model);
					}
				}
			} else {
				if (modelExists(uri, "cache")) {
					model = modelMakers.get("cache").openModel(uri);
				} else {
					model = modelMakers.get("cache").openModel(uri);
					model = readSSL(uri, null);
				}
			}

			model.setNsPrefixes(Constants.namespaces);

			if (modelExists(uri, "local")) {
				model.add(modelMakers.get("local").openModel(uri));
			}

			// add the according inference model
			if (inferenced) {
				Model infModel;
				if (modelExists(uri, "inf")
						&& !modelMakers.get("inf").openModel(uri).isEmpty()) {
					infModel = modelMakers.get("inf").openModel(uri);
				} else {
					infModel = createModel(uri, "inf");
					// TODO have to check if infModel != null
					// TODO move the model mapping out to just add ist
					Model mappedModel = fm.map(model);
					synchronized (this) {
						try {
							if (infModel.supportsTransactions()) {
								infModel.begin();
							}
							infModel.add(mappedModel);
							if (infModel.supportsTransactions()) {
								infModel.commit();
							}
						} catch (JenaException e) {
							Log.e(TAG,
									"Exception on updating model. (rollback)",
									e);
							if (infModel.supportsTransactions()) {
								infModel.abort();
							}
						}
					}
				}
				model.add(infModel);
			}
		} else if (uri == null) {
			Log.v(TAG,
					"You have to give an uri, to get a Model, that is the deal. Returning 'null'.");
		} else {
			Log.v(TAG,
					"ExternalStorrage not mounted, couldn't get model, returning 'null'.");
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

				synchronized (this) {
					try {
						if (model.supportsTransactions()) {
							model.begin();
						}
						model.removeAll();
						if (model.supportsTransactions()) {
							model.commit();
						}
						readSSL(modelName, model);
					} catch (JenaException e) {
						Log.e(TAG, "Exception on updating model. (rollback)", e);
						if (model.supportsTransactions()) {
							model.abort();
						}
					}
				}
				model.close();

				// remove the according inference model
				if (modelExists(modelName, "inf")) {
					model = modelMakers.get("inf").openModel(modelName);

					synchronized (this) {
						try {
							if (model.supportsTransactions()) {
								model.begin();
							}
							model.removeAll();
							if (model.supportsTransactions()) {
								model.commit();
							}
						} catch (JenaException e) {
							Log.e(TAG,
									"Exception on updating model. (rollback)",
									e);
							if (model.supportsTransactions()) {
								model.abort();
							}

						}
					}
					model.close();
				}
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

		if (model == null) {
			model = modelMakers.get("cache").createDefaultModel();
		}
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			try {
				// We can read and write the media
				File storage = Environment.getExternalStorageDirectory();

				File keyFile = new File(storage, Constants.CERT_FILE);
				// storage.getAbsolutePath();
				if (keyFile.isFile()) {

					SharedPreferences prefs = getConfiguration();

					SSLSocketFactory socketFactory = TrustManagerFactory
							.getFactory(keyFile,
									prefs.getString("privatekey_password", ""));

					HostnameVerifier hostNameVerifier = TrustManagerFactory
							.getVerifier();

					if (socketFactory != null) {
						try {

							HttpsURLConnection
									.setDefaultSSLSocketFactory(socketFactory);
							HttpsURLConnection
									.setDefaultHostnameVerifier(hostNameVerifier);

							URLConnection conn = new URL(url).openConnection();

							/**
							 * Set the Accept-Header
							 */
							conn.setRequestProperty("accept",
									Constants.REQUEST_PROPERTY);
							conn.setDoOutput(true);
							conn.setDoInput(true);
							conn.setUseCaches(true);

							if (conn instanceof HttpsURLConnection) {
								Log.v(TAG, "HTTPS Connection.");
							} else {
								Log.v(TAG, "HTTP/URL Connection.");
							}

							conn.connect();
							InputStream iStream = conn.getInputStream();

							read(url, model, iStream);

							iStream.close();

							if (conn instanceof HttpsURLConnection) {
								((HttpsURLConnection) conn).disconnect();
							}
						} catch (MalformedURLException e) {
							Log.e(TAG, "URL not formed correctly", e);
						} catch (FileNotFoundException e) {
							Log.e(TAG, "Couldn't find File.", e);
						} catch (ConnectException e) {
							Log.e(TAG,
									"Jena couldn't connect to the server for: '"
											+ url + "'.'", e);
						} catch (IOException e) {
							Log.e(TAG,
									"Input/Output Error while creating or using Socket.",
									e);
						}
					} else {
						Log.v(TAG, "Socket Factory is null.");
						read(url, model, null);
					}

				} else {
					Log.i(TAG,
							"Couldn't get private Key, reading without FOAF+SSL features.");
					read(url, model, null);
				}
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
		} catch (DoesNotExistException e) {
			Log.e(TAG, "Could not get <" + uri + "> into temp model,"
					+ "check the existence with your webbrowser.", e);
		} catch (JenaException e) {
			Log.e(TAG, "Error on reading <" + uri + "> into temp model.", e);
		}

		// TODO should include also all blanknodes in the connected graph
		Resource subj = new ResourceImpl(uri);
		SimpleSelector selector = new SimpleSelector(subj, (Property) null,
				(RDFNode) null);
		
		//String queryString = "";
		
		//Query query = QueryFactory.create(queryString);
		//query.addDescribeNode(subj);

		synchronized (this) {
			try {
				if (model.supportsTransactions()) {
					// model.abort();
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
		}
		tmp.removeAll();
		tmp.close();

		return model;
	}

	private static final String INDEX = "http://ns.aksw.org/Android/SysOnt/index";
	private static final String MODEL = "http://ns.aksw.org/Android/SysOnt/Model";
	private static final String CONTAINS = "http://ns.aksw.org/Android/SysOnt/contains";
	private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

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

	private Model createModel(String uri, String makerKey) {
		if (modelMakers.get(makerKey) != null) {
			if (makerKey == "cache" || modelExists(uri, makerKey)) {
				return modelMakers.get(makerKey).openModel(uri);
			} else {
				Model indexMod = modelMakers.get(makerKey).openModel("index");
				Resource index = indexMod.getResource(INDEX);
				Property contains = indexMod.getProperty(CONTAINS);
				Property rdf_type = indexMod.getProperty(RDF_TYPE);
				Resource modelClass = indexMod.getResource(MODEL);
				Resource model = indexMod.getResource(uri);

				try {
					synchronized (this) {
						indexMod.begin();
						model.addProperty(rdf_type, modelClass);
						index.addProperty(contains, model);
						indexMod.commit();
					}

					return modelMakers.get(makerKey).openModel(uri);
				} catch (JenaException e) {
					Log.e(TAG, "Could not write new Model <" + uri
							+ "> to index.", e);
					indexMod.abort();
					return null;
				}
			}
		} else {
			return null;
		}
	}

	private SharedPreferences getConfiguration() {

		if (context == null) {
			Log.v(TAG, "Context is null");
		}
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		return sharedPreferences;
	}
}
