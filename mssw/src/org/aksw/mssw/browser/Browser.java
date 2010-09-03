package org.aksw.mssw.browser;

import org.aksw.mssw.Constants;
import org.aksw.mssw.R;

import android.app.Activity;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
		selectedWebID = sharedPreferences.getString("me",
				Constants.EXAMPLE_webId);

		Resources res = getResources(); // Resource object to get Drawables
		TabHost.TabSpec spec; // Reusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		handleIntent(getIntent());

		setTitle(selectedWebID);

		/* This is bad, because I repeat very similar code three times */
		intent = new Intent().setClass(this, BrowserMeCard.class);
		intent.setData(Uri.parse(selectedWebID));
		spec = tabHost.newTabSpec("meCard");
		spec.setIndicator(getString(R.string.profile),
				res.getDrawable(android.R.drawable.ic_menu_myplaces));
		spec.setContent(intent);
		tabHost.addTab(spec);

		/* This is bad, because I repeat very similar code three times */
		intent = new Intent().setClass(this, BrowserContacts.class);
		intent.setData(Uri.parse(selectedWebID));
		spec = tabHost.newTabSpec("Contacts");
		spec.setIndicator(getString(R.string.contacts),
				res.getDrawable(android.R.drawable.ic_menu_help));
		spec.setContent(intent);
		tabHost.addTab(spec);

		/* This is bad, because I repeat very similar code three times */
		intent = new Intent().setClass(this, BrowserBrowse.class);
		if (searchTerm != null) {
			intent.setData(Uri.parse(searchTerm));
		}
		spec = tabHost.newTabSpec("Browser");
		spec.setIndicator(getString(R.string.browse),
				res.getDrawable(android.R.drawable.ic_menu_compass));
		spec.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(selectedTab);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
		tabHost.setCurrentTab(selectedTab);
		tabHost.getCurrentView();
	}

	private void handleIntent(Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			String data;
			if (action.equals(Constants.INTENT_ADD_WEBID)) {
				data = intent.getDataString();
				if (data != null) {
					try {
						Uri contentUri = Uri.parse(Constants.FOAF_CONTENT_URI
								+ "/me/friend/add");

						Log.v(TAG,
								"Starting Query with uri: <"
										+ contentUri.toString() + ">.");

						ContentValues values = new ContentValues();
						values.put("webid", data);

						getContentResolver().insert(contentUri, values);
					} catch (Exception e) {
						Log.e(TAG, "Error on adding new Friend.", e);
					}
				}
				selectedTab = 1;
			} else if (action.equals(Constants.INTENT_VIEW_WEBID)) {
				data = intent.getDataString();
				if (data != null) {
					selectedWebID = data;
				}
				selectedTab = 0;
			} else if (action.equals(Intent.ACTION_SEARCH)) {
				data = intent.getStringExtra(SearchManager.QUERY);
				if (data != null) {
					searchTerm = data;
				}
				selectedTab = 2;
			}
		}
	}

	@Override
	public void onTabChanged(String tabId) {
		Log.v(TAG, "onTabChange id: " + tabId);
		// TODO Auto-generated method stub
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
		/*
		 * if (key == "me") { selectedWebID = sharedPreferences.getString(key,
		 * Constants.EXAMPLE_webId);
		 * 
		 * }
		 */
	}
}