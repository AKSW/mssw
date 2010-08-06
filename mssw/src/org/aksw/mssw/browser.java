package org.aksw.mssw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class browser extends Activity {
	/** Called when the activity is first created. */

	private static final String TAG = "msswBrowser";
	
	//private static final String CONTENT_AUTHORITY = "org.aksw.msw.tripleprovider";
	private static final String CONTENT_AUTHORITY = "org.aksw.mssw.foafprovider";
	//private static final Uri CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	private static final Uri CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	private final ArrayList<Property> items = new ArrayList<Property>();

	private PropertiesAdapter aa;

	private ListView properties;
	private TextView status;
	private EditText uriInput;
	private Button loadButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);
		
		aa = new PropertiesAdapter(this,
				android.R.layout.simple_list_item_1, items);

		properties = (ListView) findViewById(R.id.Properties);
		status = (TextView) findViewById(R.id.Status);
		uriInput = (EditText) findViewById(R.id.UriInput);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		properties.setAdapter(aa);
		status.setText("");
		uriInput.setText(sharedPreferences.getString("me", "noPref"));

		this.loadButton = (Button) this.findViewById(R.id.Load);

		this.loadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadRes("tmp");
			}
		});

		loadRes("offline");
	}

	public void loadRes() {
		loadRes("tmp");
	}

	public void loadRes(String mode) {

		aa.clear();

		// String uri = "http://comiles.eu/~natanael/foaf.rdf#me";
		String uri = uriInput.getText().toString();
		

		if (uri.length() > 0) {

			status.setText("Loading (" + mode + ") URI: <" + uri + ">.");

			try {
				String enc = "UTF-8";
				
				Uri contentUri;
				contentUri = Uri.parse(CONTENT_URI
						+ "/person/"
						+ URLEncoder.encode(uri, enc));

				// ResourceCursor rc = (ResourceCursor) managedQuery(contentUri,
				// null,
				// null, null, null);

				Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
						+ ">.");

				Cursor rc = managedQuery(contentUri, null, null, null, null);

				if (rc != null) {
					if (!rc.isFirst()) {
						rc.moveToFirst();
					}

					String[] predicates = rc.getColumnNames();

					for (int i = 0; i < predicates.length; i++) {
						Property prop = new Property();
						prop.setPredicat(predicates[i]);
						prop.setObject(rc.getString(i));
						items.add(prop);
						Log.v(TAG, "Added new Triple ?s <" + predicates[i] + "> '" + rc.getString(i) + "' to List.");
					}
				} else {
					if(mode == "offline") {
						status.setText("No resource found, try to cache or import this resource.");
					} else {
						status.setText("No resource found.");
					}
				}

			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Problem with encoding uri for the query.", e);
				status.append("Error retriving Data from Contentprovider.");
			}
		} else {
			status.append("No URI inserted.");
		}

		aa.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.browser, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	  switch (item.getItemId()) {
	  case R.id.itemPref:
		Intent i = new Intent(this, MsswPreferenceActivity.class);
		startActivity(i);
	    return true;
	  default:
	    return super.onOptionsItemSelected(item);
	  }
	}
	
	/*----------------- private -------------------*/
	
	private class PropertiesAdapter extends ArrayAdapter<Property> {

		private ArrayList<Property> items;

		public PropertiesAdapter(Context context, int textViewResourceId,
				ArrayList<Property> objects) {
			super(context, textViewResourceId, objects);
			this.items = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.properties_row, null);
			}
			Property p = items.get(position);
			if (p != null) {
				TextView fl = (TextView) v.findViewById(R.id.firstLine);
				TextView sl = (TextView) v.findViewById(R.id.secondLine);
				if (fl != null) {
					fl.setText("o: " + p.getObject());
					Log.v(TAG,"TextView firstLine found and filled with '" + p.getObject() + "'.");
				} else {
					Log.v(TAG,"TextView firstLine not found.");
				}
				if (sl != null) {
					sl.setText("p: " + p.getPredicat());
					Log.v(TAG,"TextView secondLine found and filled with '" + p.getPredicat() + "'.");
				} else {
					Log.v(TAG,"TextView secondLine not found.");
				}
			}
			return v;
		}

	}

	private class Property {
		private String predicat;
		private String object;
		
		public void setPredicat(String predicat) {
			this.predicat = predicat;
		}
		public String getPredicat() {
			return predicat;
		}
		public void setObject(String object) {
			this.object = object;
		}
		public String getObject() {
			return object;
		}
		
		
	}

}