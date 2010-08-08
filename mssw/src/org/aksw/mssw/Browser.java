package org.aksw.mssw;

import java.util.ArrayList;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class Browser extends TabActivity implements OnTabChangeListener {
	/** Called when the activity is first created. */

	private static final String TAG = "msswBrowser";
	
	private TabHost tabHost;
	private int lastManuallySelectedTab;
	private ArrayList<String> tabnames = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);
		
		tabHost = getTabHost();
		tabHost.setOnTabChangedListener(this);

		TabHost.TabSpec spec;	// Reusable TabSpec for each tab
		Intent intent;			// Reusable Intent for each tab

		/* This is bad, because I repeat very similar code tree times */
		intent = new Intent().setClass(this, BrowserMeCard.class);
		spec = tabHost.newTabSpec("meCard");
		spec.setIndicator(getString(R.string.me));
		spec.setContent(intent);
		tabHost.addTab(spec);
		
		/* This is bad, because I repeat very similar code tree times */
		intent = new Intent().setClass(this, BrowserContacts.class);
		spec = tabHost.newTabSpec("Contacts");
		spec.setIndicator(getString(R.string.contacts));
		spec.setContent(intent);
		tabHost.addTab(spec);
		
		/* This is bad, because I repeat very similar code tree times */
		intent = new Intent().setClass(this, BrowserBrowse.class);
		spec = tabHost.newTabSpec("Browser");
		spec.setIndicator(getString(R.string.browse));
		spec.setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
	}

	@Override
	public void onTabChanged(String tabId) {
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

	//	lastManuallySelectedTab = tabHost.getCurrentTab();
	}

}