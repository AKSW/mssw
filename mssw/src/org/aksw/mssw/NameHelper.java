package org.aksw.mssw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class NameHelper {

	private static final String TAG = "MsswNameHelper";

	private static HashMap<String, String> names;
	private static LinkedList<String> projection;
	private Context context;
	
	private ReentrantLock rLock = new ReentrantLock();

	static {
		projection = new LinkedList<String>();
		projection.add("http://xmlns.com/foaf/0.1/name");
		projection.add("http://rdfs.org/sioc/ns#name");
		projection.add("http://www.w3.org/2000/01/rdf-schema#label");
		projection.add("http://xmlns.com/foaf/0.1/nick");
		projection.add("http://xmlns.com/foaf/0.1/surname");
	}

	public NameHelper(Context contextIn) {
		context = contextIn;
		if (names == null) {
			names = new HashMap<String, String>();
		}
	}

	public HashMap<String, String> getNames(ArrayList<String> uris) {
		return getNames(uris.toArray(new String[] {}));
	}

	public HashMap<String, String> getNames(String[] uris) {

		HashMap<String, String> namesOut = new HashMap<String, String>();
		ArrayList<String> batchNames = new ArrayList<String>();
		for (int i = 0; i < uris.length; i++) {
			String uri = uris[i];
			if (!names.containsKey(uri)) {
				batchNames.add(uri);
			}
		}

		batchGetNames(batchNames.toArray(new String[batchNames.size()]));

		for (int i = 0; i < uris.length; i++) {
			String uri = uris[i];
			if (names.containsKey(uri) && names.get(uri).trim().length() > 0) {
				namesOut.put(uri, names.get(uri));
			} else {
				namesOut.put(uri, uri);
			}
		}

		return namesOut;
	}

	public String getName(String uri) {
		if (!names.containsKey(uri)) {
			batchGetNames(new String[] { uri });
		}

		if (names.containsKey(uri)) {
			if (names.containsKey(uri) && names.get(uri).trim().length() > 0) {
				return names.get(uri);
			} else {
				return uri;
			}
		} else {
			return uri;
		}
	}

	private void batchGetNames(String[] uris) {
		GetNameThread[] threads = new GetNameThread[uris.length];
		for (int i = 0; i < uris.length; i++) {
			threads[i] = new GetNameThread();
			threads[i].setUri(uris[i]);
			threads[i].start();
		}
		Thread mt = new MetaThread(threads);
		mt.start();
		try {
			mt.join(Constants.TIME_MIDDLE);
			Log.v(TAG, "Ready with waiting");
		} catch (InterruptedException e) {
			Log.v(TAG, "MetaThread was interrupted");
		}
	}

	private class GetNameThread extends Thread {

		private String uri;

		public void setUri(String uriIn) {
			uri = uriIn;
		}

		public void run() {
			rLock.lock();
			ContentResolver cr = context.getContentResolver();
			try {
				Uri contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI
						+ "/resource/tmp/"
						+ URLEncoder.encode(uri, Constants.ENC));

				Log.v(TAG,
						"Starting Query with uri: <"
								+ contentUri.toString() + ">.");

				Cursor rc = cr.query(contentUri,
						projection.toArray(new String[] {}), null, null,
						null);

				if (rc != null) {
					String predicate;
					String name = "";
					
					/**
					 * quality is a measure of the quality of the resulting string for a name
					 * the less the better. The worst is no name in this case we will use the uri. 
					 */
					int quality = projection.size();
					while (rc.moveToNext()) {
						predicate = rc.getString(rc
								.getColumnIndex("predicate"));
						Log.v(TAG,
								"Got name '"
										+ rc.getString(rc
												.getColumnIndex("object"))
										+ "' with güfak: "
										+ projection.indexOf(predicate));
						if (projection.indexOf(predicate) < quality) {
							quality = projection.indexOf(predicate);
							name = rc
									.getString(rc.getColumnIndex("object"));
						}
					}
					if (quality < projection.size()) {
						names.put(uri, name);
					} else {
						names.put(uri, uri);
					}
				}
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Could not encode uri for query. Skipping <" + uri
						+ ">", e);
			} finally {
				rLock.unlock();
			}
			Log.v(TAG, "Ready with getting Name: " + names.get(uri) + ".");
		}
	}

	private class MetaThread extends Thread {
		Thread[] threads;

		public MetaThread(Thread[] threadsIn) {
			threads = threadsIn;
		}

		public void run() {
			for (int i = 0; i < threads.length; i++) {
				try {
					threads[i].join(Constants.TIME_LONG);
				} catch (InterruptedException e) {
					Log.e(TAG,
							"The thread was interrupted, while waitung for its end.",
							e);
				}
			}
		}
	}
}
