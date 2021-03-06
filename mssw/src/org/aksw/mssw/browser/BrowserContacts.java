package org.aksw.mssw.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.aksw.mssw.Constants;
import org.aksw.mssw.R;
import org.aksw.mssw.triplestore.PersonCursor;

import android.app.ListActivity;
import android.app.ProgressDialog;
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
    
    private String defaultResource; 
    
    // data vars for threads
	private Cursor rc;
    private String[] from;
    private int[] to;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_contacts);
		
		self = this;
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		defaultResource = sharedPreferences.getString("defaultResource", null);

		/**
		 * retrieve WebID first from savedInstanceState than from
		 * SharedPreferences
		 */
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
		from = new String[] { "name", "relationReadable" };
		to = new int[] { R.id.firstLine, R.id.secondLine };
		
		rca = new SimpleCursorAdapter(getApplicationContext(),R.layout.contact_row, rc, from, to);

		ListView list = (ListView) self.findViewById(android.R.id.list);
		list.setAdapter(rca);
		
		if(pd != null && pd.isShowing()){
			pd.dismiss();
		}
	}
	
	private class webIDGetter extends Thread {
		public void run() {
			synchronized (Constants.CONTENT_THREAD) {
				try {
					//Uri contentUri = Uri.parse(Constants.FOAF_CONTENT_URI
						//	+ "/person/friends/"
						//	+ URLEncoder.encode(selectedWebID, Constants.ENC));
					Log.v(TAG, "getFriends: <" + selectedWebID + ">");
					
					Uri contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI + "/resource/" + 
							URLEncoder.encode(selectedWebID, Constants.ENC));
	
					Log.v(TAG, "Starting Query with uri: <" + contentUri.toString() + ">.");
					
					rc = getContentResolver().query(contentUri, Constants.PROPS_relations, null, null, null);
					PersonCursor pc = new PersonCursor(new refreshList());
					
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
					
					//get names
					pc.requestNames(getApplicationContext(), defaultResource);
					
					rc = pc;
					
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
	}


	public boolean selectionChanged(String webid) {
		Log.v(TAG, "selectionChanged: <" + webid + ">");
		
		try{
			pd = ProgressDialog.show( self , "Working..", "Getting WebID contacts..", true, false);
		}catch(Exception e){
			pd = null;
		}
		
		selectedWebID = webid;
		
		if( rca != null ){
			try{
				PersonCursor pc = (PersonCursor)rca.getCursor();
				pc.killThreads();
			}catch(Exception e){}
		}
		
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
	
	public interface refreshCallback{
		void refreshInterface();
	}
	
	private class refreshList implements refreshCallback{
		public void refreshInterface(){
			Log.v(TAG, "refreshing list");
			mHandler.post(mUpdateList);
		}
	}
	
	// Create runnable for posting
    final Runnable mUpdateList = new Runnable() {
        public void run() {
        	refreshListData();
        }
    };
    
	public void refreshListData(){		
		rca.notifyDataSetChanged();
	}

}
