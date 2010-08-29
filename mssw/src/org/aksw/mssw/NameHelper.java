package org.aksw.mssw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class NameHelper {

	private static final String TAG = "MsswNameHelper";

	private static HashMap<String, String> names;
	private static LinkedList<String> projection;
	private static Context context;

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
		return getNames(uris.toArray(new String[]{}));
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
		
		batchGetNames((String[]) batchNames.toArray());
		
		for (int i = 0; i < uris.length; i++) {
			String uri = uris[i];
			namesOut.put(uri, names.get(uri));
		}

		return namesOut;
	}

	public String getName(String uri) {
		if (!names.containsKey(uri)) {
			batchGetNames(new String[]{uri});
		}
		return names.get(uri);
	}

	private void batchGetNames(String[] uris) {
		ContentResolver cr = context.getContentResolver();

		Uri contentUri;
		String uri;
		for (int i = 0; i < uris.length; i++) {
			uri = uris[i];
			try {
				contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI
						+ "/resource/tmp/" + URLEncoder.encode(uri, Constants.ENC));

				Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
						+ ">.");

				Cursor rc = cr.query(contentUri, projection.toArray(new String[]{}), null, null, null);

				if (rc != null) {
					String predicat;
					String name = "";
					int guefak = projection.size();
					while (rc.moveToNext()) {
						predicat = rc.getString(rc.getColumnIndex("predicat"));
						if (projection.indexOf(predicat) < guefak) {
							guefak = projection.indexOf(predicat);
							name = rc.getString(rc.getColumnIndex("object"));
						}
					}
					if (guefak < projection.size()) {
						names.put(uri, name);
					} else {
						names.put(uri, uri);
					}
				}

			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Could not encode uri for query. Skipping <" + uri
						+ ">", e);
			}
		}
	}
}
