package org.aksw.mssw;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MsswPreferenceActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	
}
