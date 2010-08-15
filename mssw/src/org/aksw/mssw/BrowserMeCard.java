package org.aksw.mssw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.zip.Inflater;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class BrowserMeCard extends Activity {

	private static final String TAG = "msswBrowserMeCard";

	private static final String CONTENT_AUTHORITY = "org.aksw.mssw.foafprovider";
	private static final Uri CONTENT_URI = Uri.parse("content://"
			+ CONTENT_AUTHORITY);

	private static final String DEFAULT_ME = "http://people.comiles.eu/example";
	
	/**
	 * should be replaced by something saved in the Application Context to use it also in Contacts
	 * @deprecated
	 */
	private static String selectedWebID;

	private TextView name;
	private ImageView photo;
	private TableLayout properties;
	private TextView empty;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/**
		 * Load View for MeCard
		 */
		setContentView(R.layout.browser_mecard);
		empty = (TextView) this.findViewById(android.R.id.empty);
		empty.setText("");

		/**
		 * retrieve WebID first from savedInstanceState than from SharedPreferences
		 */
		if (selectedWebID == null) {
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			selectedWebID = sharedPreferences.getString("me", DEFAULT_ME);
		}

		try {
			String enc = "UTF-8";

			Uri contentUri = Uri.parse(CONTENT_URI + "/person/mecard/"
					+ URLEncoder.encode(selectedWebID, enc));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");

			Cursor rc = managedQuery(contentUri, null, null, null, null);

			Resources res = getResources(); // Resource object to get Drawables
			this.properties = (TableLayout) this.findViewById(R.id.mecard_properties);
			
			TableRow row;
			TextView key;
			TextView value;
			Log.v(TAG, "Preparing Cursor Loop.");
			for (rc.moveToFirst(); !rc.isLast(); rc.moveToNext()) {
				Log.v(TAG, "Starting Cursor Loop.");
				row = (TableRow) getLayoutInflater().inflate(
						R.layout.mecard_properties, null);
				key = (TextView) row.findViewById(R.id.key);
				value = (TextView) row.findViewById(R.id.value);
				
				key.setText(rc.getString(rc.getColumnIndex("predicatReadable")));
				value.setText(rc.getString(rc.getColumnIndex("objectReadable")));
				this.properties.addView(row);
			}
			
			/*
			this.name = (TextView) this.findViewById(R.id.mecard_name);
			this.name.setText("Your Name");

			this.photo = (ImageView) this.findViewById(R.id.mecard_picture);
			this.photo.setImageDrawable(res.getDrawable(R.drawable.icon));
			*/

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,
					"Could not encode URI and so couldn't get Resource from "
							+ CONTENT_AUTHORITY + ".", e);
			empty.setText("Could not encode URI and so couldn't get Resource from "
					+ CONTENT_AUTHORITY + ".");
		}

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
		Intent i;
		switch (item.getItemId()) {
		case R.id.itemPref:
			i = new Intent(this, MsswPreferenceActivity.class);
			startActivity(i);
			return true;
		case R.id.itemMe:
			i = new Intent(this, BrowserMeCard.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*----------------- private -------------------*/

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
