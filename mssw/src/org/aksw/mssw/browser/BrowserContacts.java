package org.aksw.mssw.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.aksw.mssw.Constants;
import org.aksw.mssw.R;
import org.aksw.mssw.triplestore.PersonCursor;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
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
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BrowserContacts extends ListActivity implements OnSharedPreferenceChangeListener {

	private static final String TAG = "msswBrowserContacts";

	/**
	 * should be replaced by something saved in the Application Context to use
	 * it also in MeCard
	 */
	private String selectedWebID;

	private ResourceCursorAdapter rca;

	private MenuManager menuManager;
	
	// link to self from threads
	private ListActivity self;
	
	// progress dialog
	private ProgressDialog pd;
	
	// handler for callbacks to the UI thread
    private final Handler mHandler = new Handler();
    
    // data vars for threads
	private Cursor rc;
    private String[] from;
    private int[] to;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_contacts);
		
		self = this;

		/*
		Intent intent = getIntent();
		if (intent != null) {
			String data = intent.getDataString();
			if (data != null) {
				selectedWebID = data;
			}
		}
		*/

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

		menuManager = new MenuManager();

		selectionChanged(selectedWebID);
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor rc = rca.getCursor();
		if (rc.moveToPosition(position)) {
			String uri = rc.getString(rc.getColumnIndex("webid"));
			Intent i = new Intent(Constants.INTENT_VIEW_WEBID, Uri.parse(uri));
			startActivity(i);
		} else {
			Log.v(TAG, "Error on finding selected item at position: '"
					+ position + "' with id: '" + id + "'");
		}
		super.onListItemClick(l, v, position, id);
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
			Log.v(TAG, "Back-Button pressed in Contacts");
			Intent i = new Intent(Constants.INTENT_BACK);
			i.putExtra("keyCode", keyCode);
			i.putExtra("event", event);
			startActivity(i);
			return true;
		}

        // Otherwise fall through to parent
        return super.onKeyDown(keyCode, event);
	}
	
	// Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
        	updateList();
        }
    };
    
	public void updateList(){		
		rca = new SimpleCursorAdapter(getApplicationContext(),R.layout.contact_row, rc, from, to);
		
		getNames();

		ListView list = (ListView) self.findViewById(android.R.id.list);
		list.setAdapter(rca);
		
		pd.dismiss();
	}
	
	private class webIDGetter extends Thread {
		public void run() {
			try {
				//Uri contentUri = Uri.parse(Constants.FOAF_CONTENT_URI
					//	+ "/person/friends/"
					//	+ URLEncoder.encode(selectedWebID, Constants.ENC));
				Log.v(TAG, "getFriends: <" + selectedWebID + ">");
				
				Uri contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI + "/resource/" + 
						URLEncoder.encode(selectedWebID, Constants.ENC));

				Log.v(TAG, "Starting Query with uri: <" + contentUri.toString() + ">.");
				
				rc = getContentResolver().query(contentUri, Constants.PROPS_relations, null, null, null);
				PersonCursor pc = new PersonCursor();
				
				if (rc != null) {
					String relation;
					String relationReadable;
					String uri;
					while (rc.moveToNext()) {
						int objectType = Integer.parseInt(rc.getString(rc.getColumnIndex("objectType"))); 
						Log.v(TAG, "foaf:knows objectType: "+objectType);
						switch(objectType){
							case 0: // if it's literal url 
								uri = rc.getString(rc.getColumnIndex("object"));
								Log.v(TAG, "foaf:knows object: "+uri);
								relation = rc.getString(rc.getColumnIndex("predicate"));
								relationReadable = rc.getString(rc.getColumnIndex("predicateReadable"));
								pc.addPerson(uri, relation, uri, relationReadable, null);
								break;
							case 1: // if it's blank node
								// TODO parse blank nodes
								break;
						}
					}
				}
				
				rc = pc;

				from = new String[] { "name", "relationReadable" };
				to = new int[] { R.id.firstLine, R.id.secondLine };
				
				mHandler.post(mUpdateResults);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG,
						"Could not encode URI and so couldn't get Resource from "
								+ Constants.TRIPLE_AUTHORITY + ".", e);
				TextView empty = (TextView) self.findViewById(android.R.id.empty);
				empty.setText("Could not encode URI and so couldn't get Resource from "
						+ Constants.TRIPLE_AUTHORITY + ".");
			}
		}
	}
	
	// name request
	private String[] names;
	private NameGetter nameGetter;
	
	private void getNames(){
		if(nameGetter != null){
			try{
				nameGetter.interrupt();
				nameGetter.stop();
			}catch(Exception e){
				Log.e(TAG, "nameGetter kill exception", e);
			}
		}
		
		nameGetter = new NameGetter();
		names = new String[rc.getCount()];
		String[] uris = new String[rc.getCount()];
		
		rc.moveToPosition(-1);
		while(rc.moveToNext()){
			uris[rc.getPosition()] = rc.getString(1);
		}
		
		nameGetter = new NameGetter();
		nameGetter.setUris(uris);
		nameGetter.start();
	}
	
	private class NameGetter extends Thread {
		private String[] _uri;
		
		public void setUris(String[] uri){
			_uri = uri;
		}
		
		public void run() {
			ContentResolver cr = getContentResolver();
			String uri;
			Uri contentUri;
			Cursor rc;
			String predicate;
            String name;
			
			for(int i = 0; i < _uri.length; i++){
				uri = _uri[i];
				try {
	                contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI + "/resource/tmp/"
	                                + URLEncoder.encode(uri, Constants.ENC));
	                Log.v(TAG, "Starting Query with uri: <" + contentUri.toString() + ">.");
	                rc = cr.query(contentUri, Constants.projection.toArray(new String[] {}), null, null,null);
	                
	                if (rc != null) {
	                	name = "";
                        /**
                         * quality is a measure of the quality of the resulting string for a name
                         * the less the better. The worst is no name in this case we will use the uri. 
                         */
                        int quality = Constants.projection.size();
                        while (rc.moveToNext()) {
                                predicate = rc.getString(rc.getColumnIndex("predicate"));
                                Log.v(TAG,"Got name '" + rc.getString(
                                		rc.getColumnIndex("object"))+ "' with güfak: "+ 
                                		Constants.projection.indexOf(predicate)
                                );
                                if (Constants.projection.indexOf(predicate) < quality) {
                                        quality = Constants.projection.indexOf(predicate);
                                        name = rc.getString(rc.getColumnIndex("object"));
                                }
                        }
                        if (quality < Constants.projection.size()) {
                        	names[i] = name;
                        }
	                }
	                Log.v(TAG, "Ready with getting Name: " + names[i] + ".");
				} catch (UnsupportedEncodingException e) {
	                Log.e(TAG, "Could not encode uri for query. Skipping <" + _uri + ">", e);
				}
			}
			
			mHandler.post(mUpdateNames);
		}
	}
	// Create runnable for posting
    final Runnable mUpdateNames = new Runnable() {
        public void run() {
        	updateNames();
        }
    };
	public void updateNames(){
		Log.v(TAG, "updating names list");
		
		PersonCursor pc = new PersonCursor();
		rc.moveToPosition(-1);
		while(rc.moveToNext()){
			if(names[rc.getPosition()] == null){
				pc.addPerson(rc.getString(1), rc.getString(2), rc.getString(3), rc.getString(4), null);
			}else{
				pc.addPerson(rc.getString(1), rc.getString(2), names[rc.getPosition()], rc.getString(4), null);
			}
		}
		rc = pc;
		rca.changeCursor(rc);
		
		/*rca = new SimpleCursorAdapter(getApplicationContext(),R.layout.contact_row, rc, from, to);
		ListView list = (ListView) self.findViewById(android.R.id.list);
		list.setAdapter(rca);*/
	}


	public boolean selectionChanged(String webid) {
		Log.v(TAG, "selectionChanged: <" + webid + ">");

		pd = ProgressDialog.show(this, "Working..", "Getting WebID contacts..", true, false);
		
		selectedWebID = webid;
		
		webIDGetter wig = new webIDGetter();
		wig.start();
		
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key == "selectedWebID") {
			String selectedWebIDnew = sharedPreferences.getString(key,
					Constants.EXAMPLE_webId);
			if (selectedWebID == null
					|| !selectedWebID.equals(selectedWebIDnew)) {
				selectedWebID = selectedWebIDnew;
				selectionChanged(selectedWebID);
				// TODO change view
			}
		}
	}

}
