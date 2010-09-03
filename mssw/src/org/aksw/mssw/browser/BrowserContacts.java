package org.aksw.mssw.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.aksw.mssw.Constants;
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
import android.view.View;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BrowserContacts extends ListActivity implements OnSharedPreferenceChangeListener {

	private static final String TAG = "msswBrowserContacts";

	/**
	 * should be replaced by something saved in the Application Context to use it also in MeCard
	 */
	private String selectedWebID;
	
	private ResourceCursorAdapter rca; 

	private MenuManager menuManager;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_contacts);
		
		Intent intent = getIntent();
		if (intent != null) {
			String data = intent.getDataString();
			if (data != null) {
				selectedWebID = data;
			}
		}

		/**
		 * retrieve WebID first from savedInstanceState than from SharedPreferences
		 */
		if (selectedWebID == null) {
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			selectedWebID = sharedPreferences.getString("me", Constants.EXAMPLE_webId);
		}

		menuManager = new MenuManager();
		
		try {
			Uri contentUri = Uri.parse(Constants.FOAF_CONTENT_URI + "/person/friends/"
					+ URLEncoder.encode(selectedWebID, Constants.ENC));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");

			Cursor rc = managedQuery(contentUri, null, null, null, null);
			
			String[] from = new String[]{"name", "relationReadable"};
			int[] to = {R.id.firstLine,R.id.secondLine};
			rca = new SimpleCursorAdapter(getApplicationContext(), R.layout.contact_row, rc, from, to);
			
			ListView list = (ListView) this.findViewById(android.R.id.list);
			list.setAdapter(rca);

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,
					"Could not encode URI and so couldn't get Resource from "
							+ Constants.FOAF_AUTHORITY + ".", e);
			TextView empty = (TextView) this.findViewById(android.R.id.empty);
			empty.setText("Could not encode URI and so couldn't get Resource from "
					+ Constants.FOAF_AUTHORITY + ".");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contacts, menu);
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
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key == "me") {
			selectedWebID = sharedPreferences.getString(key, Constants.EXAMPLE_webId);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String uri;
		
		Cursor rc = rca.getCursor();
		if (rc.moveToPosition(position)) {
		uri = rc.getString(rc.getColumnIndex("webid"));
		
		//uri = "http://sebastian.tramp.name";
		Intent i = new Intent(Constants.INTENT_VIEW_WEBID, Uri.parse(uri));
		startActivity(i);
		} else {
			Log.v(TAG, "Error on finding selected item at position: '" + position + "' with id: '" + id + "'");
		}
		super.onListItemClick(l, v, position, id);
	}
}
