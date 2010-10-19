package org.aksw.mssw.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.aksw.mssw.Constants;
import org.aksw.mssw.NameHelper;
import org.aksw.mssw.R;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BrowserMeCard extends ListActivity implements OnSharedPreferenceChangeListener {

	private static final String TAG = "msswBrowserMeCard";

	/**
	 * should be replaced by something saved in the Application Context to use
	 * it also in Contacts
	 * 
	 */
	private String selectedWebID;

	private TextView name;
	private TextView empty;

	private ResourceCursorAdapter rca;

	private MenuManager menuManager;
	private NameHelper nh;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/**
		 * Load View for MeCard
		 */
		setContentView(R.layout.browser_mecard);
		// empty = (TextView) this.findViewById(android.R.id.empty);
		// empty.setText("");

		Intent intent = getIntent();
		if (intent != null) {
			String data = intent.getDataString();
			if (data != null) {
				selectedWebID = data;
			}
		}

		/**
		 * retrieve WebID first from savedInstanceState than from
		 * SharedPreferences
		 */

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		if (selectedWebID == null) {
			selectedWebID = sharedPreferences.getString("selectedWebID", null);
			if (selectedWebID == null) {
				selectedWebID = sharedPreferences.getString("me",
						Constants.EXAMPLE_webId);
			}
		}

		nh = new NameHelper(getApplicationContext());

		name = (TextView) this.findViewById(R.id.mecard_name);

		menuManager = new MenuManager();

		selectionChanged(selectedWebID);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		// don't show addWebId if the actualy selecte WebId is already a friend
		// don't show the me button of you show my WenId
		inflater.inflate(R.menu.mecard, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		boolean ret = menuManager.itemSelected(this, item, selectedWebID);
		if (ret) {
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	public boolean selectionChanged(String webid) {
		Log.v(TAG, "selectionChanged: <" + webid + ">");
		
		selectedWebID = webid;
		

		name.setText(nh.getName(selectedWebID));

		try {
			Uri contentUri = Uri.parse(Constants.FOAF_CONTENT_URI
					+ "/person/mecard/"
					+ URLEncoder.encode(selectedWebID, Constants.ENC));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");

			Cursor rc = managedQuery(contentUri, null, null, null, null);

			String[] from = new String[] { "predicateReadable", "objectReadable" };
			int[] to = { R.id.key, R.id.value };
			rca = new SimpleCursorAdapter(getApplicationContext(),
					R.layout.mecard_properties, rc, from, to);

			ListView list = (ListView) this.findViewById(android.R.id.list);
			list.setAdapter(rca);

			// Resources res = getResources(); // Resource object to get
			// Drawables

			/*
			 * this.photo = (ImageView) this.findViewById(R.id.mecard_picture);
			 * this.photo.setImageDrawable (res.getDrawable(R.drawable.icon));
			 */

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,
					"Could not encode URI and so couldn't get Resource from "
							+ Constants.FOAF_AUTHORITY + ".", e);
			empty.setText("Could not encode URI and so couldn't get Resource from "
					+ Constants.FOAF_AUTHORITY + ".");
		}
		
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.v(TAG, "SharedPreference changed.");
		if (key == "selectedWebID") {
			String selectedWebIDnew = sharedPreferences.getString(key,
					Constants.EXAMPLE_webId);
			Log.v(TAG, "selectedWebID changed to <" + selectedWebIDnew + ">.");
			if (selectedWebID == null || !selectedWebID.equals(selectedWebIDnew)) {
				selectedWebID = selectedWebIDnew;
				// TODO change view
				selectionChanged(selectedWebID);
			}
		}
	}
}
