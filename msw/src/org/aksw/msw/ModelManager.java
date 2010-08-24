package org.aksw.msw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;

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
	private static File cacheModelsFiles;

	private static HashMap<String, Model> models = new HashMap<String, Model>();

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
		cacheModelsFiles = new File(storage, Constants.CACHE_MODELS_DIR);

		initModelMakers();

	}

	private boolean initModelMakers() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// read and write access
			webModelsFiles.mkdirs();
			webModels = ModelFactory.createFileModelMaker(webModelsFiles
					.getAbsolutePath());
			cacheModels = ModelFactory.createMemModelMaker();
			infModels = ModelFactory.createFileModelMaker(infModelsFiles
					.getAbsolutePath());
			localModels = ModelFactory.createFileModelMaker(localModelsFiles
					.getAbsolutePath());

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
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// read and write access
			if (persistant) {
				boolean has = webModels.hasModel(uri);
				model = webModels.openModel(uri);
				if(model.supportsTransactions()) {
					model.begin();
				}
				if (!has) {
					model = readSSL(uri, model);
				}

			} else {
				boolean has = cacheModels.hasModel(uri);
				model = cacheModels.openModel(uri);
				if (!has) {
					model = readSSL(uri, null);
				}
			}

			model.setNsPrefixes(namespaces);
			
			if (localModels.hasModel(uri)) {
				model.add(localModels.openModel(uri));
			}
		}
		return model;
	}

	public void updateResources() {
		ExtendedIterator<String> webModelIterator = webModels.listModels();
		Model model;
		String modelName;
		while (webModelIterator.hasNext()) {
			modelName = webModelIterator.next();
			Log.v(TAG, "webModelMaker knows: " + modelName);
			model = webModels.openModel(modelName);
			model.begin();
			model.removeAll();
			model = readSSL(modelName, model);
			model.commit();
			model.close();
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
			model = cacheModels.createDefaultModel();
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

						model = read(url, model, conn.getInputStream());
					} catch (FileNotFoundException e) {
						Log.e(TAG, "Couldn't find File.", e);
					} catch (IOException e) {
						Log.e(TAG,
								"Input/Output Error while creating or using Socket.",
								e);
						model = read(url, model, null);
					}

				} else {
					Log.i(TAG,
							"Couldn't get private Key, reading without FOAF+SSL features.");
				}
				model = read(url, model, null);
			} catch (DoesNotExistException e) {
				Log.e(TAG, "Jena couldn't find the model: '" + url + "'.'", e);
			}
		}
		if (model.supportsTransactions()) {
			model.commit();
		}
		
		return model;
	}

	private Model read(String uri, Model model, InputStream inputstream) {

		if (model == null) {
			model = cacheModels.openModel(uri);
		}
		Model tmp = cacheModels.createDefaultModel();

		if (inputstream == null) {
			tmp.read(uri);
		} else {
			tmp.read(inputstream, uri);
		}

		Resource subj = new ResourceImpl(uri);
		SimpleSelector selector = new SimpleSelector(subj, (Property) null,
				(RDFNode) null);

		try {
			model.add(tmp.query(selector));
		} catch (JenaException e) {
			Log.e(TAG, "Exception on query for resource.", e);
		}
		tmp.close();

		return model;
	}
}
