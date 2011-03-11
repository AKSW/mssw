package org.aksw.mssw.browser;

import java.net.URLEncoder;
import java.util.Stack;

import org.aksw.mssw.Constants;
import org.aksw.mssw.R;

import android.app.Activity;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class Browser extends TabActivity implements OnTabChangeListener,
		OnSharedPreferenceChangeListener {
	/** Called when the activity is first created. */

	private static final String TAG = "msswBrowser";

	private TabHost tabHost;

	protected String selectedWebID;
	protected String searchTerm;
	protected int selectedTab = 0;

	private Stack<String> historyStack = new Stack<String>(); //  && !historyStack.empty()

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "================ onCreate Browser =====================");
		// important!! don't set the content because else no tab-content would
		// be displayed.

		setContentView(R.layout.browser);

		tabHost = getTabHost();
		tabHost.setOnTabChangedListener(this);

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		/**
		 * Variable me is needed to check if a webID is set, else start first-run-wizard
		 */
		String me = sharedPreferences.getString("me", null);
		String meSPAQRL = sharedPreferences.getString("meSPAQRL", null);

		if (me != null) {
			if(meSPAQRL != null){
				selectedWebID = sharedPreferences.getString("selectedWebID", null);
				if (selectedWebID == null) selectedWebID = me;
			}else{
				selectedWebID = me;
			}
			
			selectionChanged();
			
			handleIntent(getIntent());
			
			Resources res = getResources(); // Resource object to get
											// Drawables

			/* add meCard tab */
			addTab(
					new Intent().setClass(this, BrowserMeCard.class), 
					getString(R.string.profile), 
					res.getDrawable(R.drawable.ic_tab_mecard)
			);

			/* add contacts tab */
			addTab(
					new Intent().setClass(this, BrowserContacts.class),
					getString(R.string.contacts),
					res.getDrawable(R.drawable.ic_tab_contacts)
			);

			/* add browse tab */
			addTab(
					new Intent().setClass(this, BrowserBrowse.class),
					getString(R.string.browse),
					res.getDrawable(R.drawable.ic_tab_browse)
			);
			
			tabHost.setCurrentTab(selectedTab);
		} else {
			// Start first run wizard
			Intent intent = new Intent(Constants.INTENT_FIRSTRUN);
			intent.putExtra("sender", "browser.onCreate");
			intent.putExtra("progress", 0);
			startActivity(intent);
			finish();
		}
	}
	
	private void addTab(Intent intent, String tabName, Drawable icon){
		TabHost.TabSpec spec = tabHost.newTabSpec(tabName);
		spec.setIndicator(tabName,icon);
		spec.setContent(intent);
		tabHost.addTab(spec);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.v(TAG, "New intent");
		setIntent(intent);
		handleIntent(intent);
		tabHost.setCurrentTab(selectedTab);
		tabHost.getCurrentView();
	}

	private void handleIntent(Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			String data;
			if (action == null) {
				Log.v(TAG, "No Action");
			} else if (action.equals(Constants.INTENT_ADD_WEBID)) {
				data = intent.getDataString();
				Log.v(TAG, "Add WebId <" + data + "> Intent.");
				if (data != null) {
					addWebID(data);
					selectedTab = 1;
					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext());
					selectedWebID = sp.getString("me", Constants.EXAMPLE_webId);
					selectionChanged();
				}
			} else if (action.equals(Constants.INTENT_VIEW_WEBID)) {
				data = intent.getDataString();
				Log.v(TAG, "View WebId <" + data + "> Intent.");
				if (data != null) {
					selectedWebID = data;
					selectedTab = 0;
					selectionChanged();
				}
			} else if (action.equals(Constants.INTENT_BACK)) {
				if (historyStack.size() > 1) {
					historyStack.pop(); // removing the last WebID
					selectedWebID = historyStack.pop(); // setting the new WebID 
					selectedTab = 0;
					Log.v(TAG, "Go back in History to WebId <" + selectedWebID + "> Intent.");
					selectionChanged();
				} else {
					Log.v(TAG, "History is empty, giving back call to super.");
					// TODO handle the case that nothing is on the stack
					// maybe call some system function to go back in stack
					super.onKeyDown(intent.getIntExtra("keyCode", 0), (KeyEvent)intent.getParcelableExtra("event"));
					super.onBackPressed();
				}
			} else if (action.equals(Intent.ACTION_SEARCH)) {
				data = intent.getStringExtra(SearchManager.QUERY);
				
				// TODO: hide keyboard
				
				Log.v(TAG, "Search WebId <" + data + "> Intent.");
				if (data != null) {
					searchTerm = data;
					selectedTab = 2;
					searchTermChanged();
				}
			} else {
				Log.v(TAG, "Unsupported Action");
				if (intent.getBooleanExtra("reCreate", false)) {
					onCreate(null);
				}
			}
		}

		setTitle(selectedWebID);
	}

	private void selectionChanged() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		Log.v(TAG, "Selection changed <" + selectedWebID + ">.");
		// selectedWebID != null || 
		if (!selectedWebID.equals(sp.getString("selectedWebID", null))) {
			Log.v(TAG, "Writing selectedWebID <" + selectedWebID
					+ "> to config.");
			Editor spEdit = sp.edit();
			spEdit.putString("selectedWebID", selectedWebID);
			spEdit.commit();
			historyStack.push(selectedWebID);
		}
	}

	private void searchTermChanged() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		Log.v(TAG, "SearchTerm changed <" + searchTerm + ">.");
		if (searchTerm != null
				|| !searchTerm.equals(sp.getString("searchTerm", null))) {
			Log.v(TAG, "Writing searchTerm <" + searchTerm + "> to config.");
			Editor spEdit = sp.edit();
			spEdit.putString("searchTerm", searchTerm);
			spEdit.commit();
		}

	}

	private void addWebID(String webid) {
		AddWebIdTread awt = new AddWebIdTread();
		awt.setWebId(webid);
		awt.start();
	}

	private class AddWebIdTread extends Thread {
		private String webid;

		public void setWebId(String webidIn) {
			webid = webidIn;
		}

		public void run() {
			try {
				//Log.v(TAG, "Starting Query with uri: <" + contentUri.toString() + ">.");
				String relation = Constants.PROP_knows;
				
				Uri contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI + "/resource/addTriple/"
										+ URLEncoder.encode(selectedWebID, Constants.ENC));
				
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String updateEndpoint = sharedPreferences.getString("meSPAQRL", null);
				String login = sharedPreferences.getString("ep_login", null);
				String pass = sharedPreferences.getString("ep_pass", null);
				
				ContentValues values = new ContentValues();
				values.put("subject", selectedWebID);
				values.put("predicate", relation);
				values.put("object", webid);
				values.put("updateEndpoint", updateEndpoint);
				values.put("login", login);
				values.put("pass", pass);

				Log.i(TAG, "Adding new friend");
				Log.i(TAG,  "You <" + selectedWebID + "> will know <" + relation + "> a new Person <" + webid + ">.");
 
				Uri result = getContentResolver().insert(contentUri, values);
				
				if (result != null) {
					// return true;
				} else {
					// return false;
				}
			} catch (Exception e) {
				Log.e(TAG, "Error on adding new Friend.", e);
				// return false;
			}
		}
	}

	@Override
	public void onTabChanged(String tabId) {
		Log.v(TAG, "onTabChange id: " + tabId);
		// Because we're using Activities as our tab children, we trigger
		// onWindowFocusChanged() to let them know when they're active. This may
		// seem to duplicate the purpose of onResume(), but it's needed because
		// onResume() can't reliably check if a keyguard is active.
		Activity activity = getLocalActivityManager().getActivity(tabId);
		if (activity != null) {
			activity.onWindowFocusChanged(true);
		}

		// Remember this tab index. This function is also called, if the tab is
		// set automatically
		// in which case the setter (setCurrentTab) has to set this to its old
		// value afterwards

		// lastManuallySelectedTab = tabHost.getCurrentTab();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		if (key == "me") {
			String me = sharedPreferences.getString(key,
					Constants.EXAMPLE_webId);
			if (!selectedWebID.equals(me)) {
				selectedWebID = me;
				selectionChanged();
			}

		}
	}
}