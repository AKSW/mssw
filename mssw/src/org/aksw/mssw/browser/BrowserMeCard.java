package org.aksw.mssw.browser;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.aksw.mssw.Constants;
import org.aksw.mssw.R;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
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

		self = this;

		/**
		 * Load View for MeCard
		 */
		setContentView(R.layout.browser_mecard);
		// empty = (TextView) this.findViewById(android.R.id.empty);
		// empty.setText("");
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
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyDown()
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * should manage a stack with the history of browsed users
		 */
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.v(TAG, "Back-Button pressed in MeCard");
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor rc = rca.getCursor();
		if (rc.moveToPosition(position)) {
			String predicate = rc.getString(2);
			String uri = rc.getString(3);
			if(uri != null && predicate != null){
				Log.v(TAG, predicate);
				Log.v(TAG, uri);
				for(int index = 0; index < Constants.PROPS_webactive.length; index++) {            
			        if (Constants.PROPS_webactive[index].equals(predicate)) {
			        	Intent i = new Intent(Intent.ACTION_VIEW);
					    i.setData(Uri.parse(uri));
					    startActivity(i);
			        	break;
			        }
			    } 
			}else{
				Log.v(TAG, "uri == null");
			}
		}
		
		super.onListItemClick(l, v, position, id);
	}


	public boolean selectionChanged(String webid) {
		Log.v(TAG, "selectionChanged: <" + webid + ">");
		
		pd = ProgressDialog.show( self , "Working..", "Getting WebID data..", true, false);
		
		selectedWebID = webid;
		
		webIDGetter wig = new webIDGetter();
		wig.start();
		
		return true;
	}

    // Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
        	updateList();
        }
    };
    
	public void updateList(){
		// get data
		rca = new SimpleCursorAdapter(self.getApplicationContext(),
				R.layout.mecard_properties, rc, from, to);
		// assign data to list
		ListView list = (ListView) self.findViewById(android.R.id.list);
		list.setAdapter(rca);
		
		// find name in data
		String predicate;
		String webIDName = "";
		String webIDPic = "";
		int index = 0;
		int quality = Constants.PROPS_nameProps.length;
		while (rc.moveToNext()) {
			predicate = rc.getString(rc.getColumnIndex("predicate"));
			for(index = 0; index < Constants.PROPS_nameProps.length; index++) {            
                if (Constants.PROPS_nameProps[index].equals(predicate)) {
                        Log.v(TAG, "found name: "+predicate);
                        if (index < quality) {
            				quality = index;
            				webIDName = rc.getString(rc.getColumnIndex("object"));
            			}
                }
            }
			for(index = 0; index < Constants.PROPS_pictureProps.length; index++) {            
                if (Constants.PROPS_pictureProps[index].equals(predicate)) {
                        Log.v(TAG, "found pic: "+predicate);
                        webIDPic = rc.getString(rc.getColumnIndex("object"));
            			break;
                }
            }
		}
		if(webIDName.length() > 1) name.setText(webIDName);
		
		// set image
		Drawable bm;
		if(webIDPic.length() > 1){
	        bm = loadImageFromWeb(webIDPic);
	        if(bm == null) bm = getResources().getDrawable(R.drawable.icon); 
		}else{
			bm = getResources().getDrawable(R.drawable.icon);
		}
		ImageView img = (ImageView) self.findViewById(R.id.mecard_icon);
		img.setImageDrawable(bm);
		
		// remove loader
		pd.dismiss();
	}
	
	private Drawable loadImageFromWeb(String url){
        Log.i("IMGLOAD", "Fetching image");
        try{
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src");
            Log.i("IMGLOAD", "Created image from stream");
            return d;
        }catch (Exception e) {
            //TODO handle error
            Log.e("IMGLOAD", "Error fetching image");
            return null;
        }
	}
	
	private class webIDGetter extends Thread {
		public void run() {	
			synchronized (Constants.CONTENT_THREAD) {
				try {
					//Uri contentUri = Uri.parse(Constants.FOAF_CONTENT_URI
						//	+ "/person/mecard/"
						//	+ URLEncoder.encode(selectedWebID, Constants.ENC));
					
					Log.v(TAG, "getMeCard: <" + selectedWebID + ">");
					
					Uri contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI + "/resource/" 
							+ URLEncoder.encode(selectedWebID, Constants.ENC));
	
					Log.v(TAG, "Starting Query with uri: <" + contentUri.toString() + ">.");
	
					String[] projection = new String[Constants.PROPS_relations.length+1];
					System.arraycopy(Constants.PROPS_relations, 0, projection, 0, Constants.PROPS_relations.length);
					projection[Constants.PROPS_relations.length] = Constants.PROP_hasData;
					String selection = "complement";
					
					rc = getContentResolver().query(contentUri, projection, selection, null, null);
	
					from = new String[] { "predicateReadable", "objectReadable" };
					to = new int[] { R.id.key, R.id.value };
					
					mHandler.post(mUpdateResults);
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG,
							"Could not encode URI and so couldn't get Resource from "
									+ Constants.TRIPLE_AUTHORITY + ".", e);
					empty.setText("Could not encode URI and so couldn't get Resource from "
							+ Constants.TRIPLE_AUTHORITY + ".");
				}
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
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
