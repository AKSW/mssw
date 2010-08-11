package org.aksw.mssw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;

public class BrowserMeCard extends Activity implements OnTabChangeListener {

	private static final String TAG = "msswBrowserMeCard";

	// private static final String CONTENT_AUTHORITY =
	// "org.aksw.msw.tripleprovider";
	private static final String CONTENT_AUTHORITY = "org.aksw.mssw.foafprovider";
	// private static final Uri CONTENT_URI = Uri.parse("content://" +
	// CONTENT_AUTHORITY);
	private static final Uri CONTENT_URI = Uri.parse("content://"
			+ CONTENT_AUTHORITY);

	private static final String DEFAULT_ME = "http://people.comiles.eu/example";

	private TextView name;
	private ImageView photo;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_mecard);

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		Resources res = getResources(); // Resource object to get Drawables

		try {
			String meuri = sharedPreferences.getString("me", DEFAULT_ME);

			String enc = "UTF-8";

			Uri contentUri = Uri.parse(CONTENT_URI + "/person/mecard/"
					+ URLEncoder.encode(meuri, enc));

			// ResourceCursor rc = (ResourceCursor) managedQuery(contentUri,
			// null,
			// null, null, null);

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");

			Cursor rc = managedQuery(contentUri, null, null, null, null);

			this.name = (TextView) this.findViewById(R.id.mecard_name);
			this.name.setText("Natanael Arndt");

			this.photo = (ImageView) this.findViewById(R.id.mecard_picture);
			this.photo.setImageDrawable(res.getDrawable(R.drawable.icon));
			
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,
					"Could not encode URI and so couldn't get Resource from "
							+ CONTENT_AUTHORITY + ".", e);
			TextView empty = (TextView) this.findViewById(android.R.id.empty);
			empty.setText("Could not encode URI and so couldn't get Resource from "
					+ CONTENT_AUTHORITY + ".");
		}

	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		Log.v(TAG, "onTabChange id: " + tabId);
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
