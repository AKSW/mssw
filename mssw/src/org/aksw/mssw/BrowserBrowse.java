package org.aksw.mssw;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TabHost.OnTabChangeListener;

public class BrowserBrowse extends Activity implements OnTabChangeListener {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_browse);
	}
	
	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub

	}

}
