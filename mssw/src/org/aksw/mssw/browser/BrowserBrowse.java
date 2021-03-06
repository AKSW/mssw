package org.aksw.mssw.browser;

import org.aksw.mssw.Constants;
import org.aksw.mssw.R;
import org.aksw.mssw.search.SindiceSearch;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class BrowserBrowse extends ListActivity implements OnSharedPreferenceChangeListener {

	private static final String TAG = "msswBrowserBrowse";

	private ListView results;
	private Button scan;
	private Button go;
	
	private BrowserBrowse self;

	private ResourceCursorAdapter rca;
	private Cursor rc;

	private MenuManager menuManager;

	private String searchTerm;
	
	// progress dialog
	private ProgressDialog pd;
	
	// handler for callbacks to the UI thread
    private final Handler mHandler = new Handler();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		self = this;

		setContentView(R.layout.browser_browse);

		searchTerm = null;

		menuManager = new MenuManager();

		scan = (Button) findViewById(R.id.scan_code);
		go = (Button) findViewById(R.id.go_btn);
		results = (ListView) this.findViewById(android.R.id.list);

		scan.setOnClickListener(new scanClickListener());
		go.setOnClickListener(new goClickListener());

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
/*
		Intent intent = getIntent();
		if (intent != null) {
			String data = intent.getDataString();
			if (data != null) {
				searchTerm = data;
				search();
			}
		}
		*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.browse, menu);
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
		Log.i(TAG, "Starting search for: " + searchTerm);
		if (searchTerm.length() > 0) {
			// TODO: put this into separate thread
			try{
				pd = ProgressDialog.show( self , "Working..", "Searching Sindice..", true, false);
			}catch(Exception e){
				pd = null;
			}
			
			SindiceSearcher ssearch = new SindiceSearcher();
			ssearch.start();
		}else{
			Log.i(TAG, "Searchterm WebID <" + searchTerm + "> is not http or https or null.");
		}
	}
	
	// Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
        	updateList();
        }
    };
    
	public void updateList(){
		String[] from = new String[] { "name", "webid" };
		int[] to = { R.id.firstLine, R.id.secondLine };
		
		rca = new SimpleCursorAdapter(getApplicationContext(),R.layout.contact_row, rc, from, to);
		results.setAdapter(rca);
		
		if(pd != null && pd.isShowing()){
			pd.dismiss();
		}
	}
	
	private class SindiceSearcher extends Thread {
		public void run() {
			try {
				Log.i(TAG, "Trying sindice search for: " + searchTerm);
				
				SindiceSearch sc = new SindiceSearch();
				
				rc = sc.findTerm(searchTerm);
				
				if (rc != null) {
					mHandler.post(mUpdateResults);
				}
			} catch (Exception e) {
				Log.e(TAG, "Something went wrong during search.", e);
				TextView empty = (TextView) self.findViewById(android.R.id.empty);
				empty.setText("Error during search.");
			}
		}
	}
	
	class goClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			TextView input = (TextView) self.findViewById(R.id.webid_url);
			String uri = input.getText().toString();
			
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromInputMethod(input.getWindowToken(), 0);
			
			input.setText("");

			if (uri.length() > 7) {
				// uri = "http://sebastian.tramp.name";
				Intent i = new Intent(Constants.INTENT_VIEW_WEBID, Uri.parse(uri));
				startActivity(i);
			} else {
				Log.v(TAG, "Error opening uri (to short): "+uri);
			}
		}
	}

	class scanClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			try {
				startActivityForResult(intent, 0);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(BrowserBrowse.this, R.string.install_zxing,
						Toast.LENGTH_LONG);
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				// String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Handle successful scan
				if (contents != null) {
					TextView t = (TextView) self.findViewById(R.id.webid_url);
					t.setText(contents);
					//searchTerm = contents;
					//search();
				}
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String uri;

		Cursor rc = rca.getCursor();
		if (rc.moveToPosition(position)) {
			uri = rc.getString(rc.getColumnIndex("webid"));

			// uri = "http://sebastian.tramp.name";
			Intent i = new Intent(Constants.INTENT_VIEW_WEBID, Uri.parse(uri));
			startActivity(i);
		} else {
			Log.v(TAG, "Error on finding selected item at position: '"
					+ position + "' with id: '" + id + "'");
		}
		super.onListItemClick(l, v, position, id);
	}

	public boolean searchTermChanged(String sTerm) {
		searchTerm = sTerm;
		
		Log.v(TAG, "searchTermChanged: '" + searchTerm + "'");

		self.search();
		return false;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyDown()
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * should manage a stack with the history of browsed users
		 */
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.v(TAG, "Back-Button pressed in Browser");
			Intent i = new Intent(Constants.INTENT_BACK);
			i.putExtra("keyCode", keyCode);
			i.putExtra("event", event);
			startActivity(i);
			return true;
		}

        // Otherwise fall through to parent
        return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key == "searchTerm") {
			String searchTermNew = sharedPreferences.getString(key,
					Constants.EXAMPLE_webId);
			if (searchTerm == null || !searchTerm.equals(searchTermNew)) {
				searchTerm = searchTermNew;
				searchTermChanged(searchTerm);
			}
		}

	}
}
