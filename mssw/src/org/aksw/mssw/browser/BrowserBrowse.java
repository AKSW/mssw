package org.aksw.mssw.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.aksw.mssw.Constants;
import org.aksw.mssw.R;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BrowserBrowse extends ListActivity {

	private static final String TAG = "msswBrowserMeCard";

	private ListView results;
	private Button search;
	private Button scan;
	
	private ResourceCursorAdapter rca;

	private MenuManager menuManager;
	
	private String searchTerm;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.browser_browse);

		searchTerm = null;

		
		menuManager = new MenuManager();

		search = (Button) findViewById(R.id.search);
		scan = (Button) findViewById(R.id.scan_code);
		results = (ListView) this.findViewById(android.R.id.list);
		
		search.setOnClickListener(new searchClickListener());
		scan.setOnClickListener(new scanClickListener());
		
		
		Intent intent = getIntent();
		if (intent != null) {
			String data = intent.getDataString();
			if (data != null) {
				searchTerm = data;
				search();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		boolean ret = menuManager.itemSelected(this, item, "");
		if (ret) {
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void search() {
		
		try {
			Uri contentUri = Uri.parse(Constants.FOAF_CONTENT_URI + "/search/"
					+ URLEncoder.encode(searchTerm, Constants.ENC));

			Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
					+ ">.");

			Cursor rc = managedQuery(contentUri, null, null, null, null);
			
			String[] from = new String[]{"name", "webid"};
			int[] to = {R.id.firstLine,R.id.secondLine};
			rca = new SimpleCursorAdapter(getApplicationContext(), R.layout.contact_row, rc, from, to);
			
			results.setAdapter(rca);

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,
					"Could not encode searchterm and so couldn't get Resource from "
							+ Constants.FOAF_AUTHORITY + ".", e);
			TextView empty = (TextView) this.findViewById(android.R.id.empty);
			empty.setText("Could not encode Searchterm and so couldn't get Resource from "
					+ Constants.FOAF_AUTHORITY + ".");
		}
	}
	
	class searchClickListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			onSearchRequested();
		}
	}
	
	class scanClickListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			
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
