package org.aksw.mssw.triplestore;

import org.aksw.mssw.Constants;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class MswPreferenceActivity extends PreferenceActivity {

	private static final String TAG = "MswPreferenceActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(org.aksw.mssw.R.xml.preferences);
		
		Preference sync = (Preference) findPreference("sync");
		sync.setOnPreferenceClickListener(new syncClickListener());
	}
	
	private class syncClickListener implements Preference.OnPreferenceClickListener {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			Toast.makeText(getApplicationContext(), org.aksw.mssw.R.string.syncing_toast, Toast.LENGTH_LONG).show();
			
			Uri contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI + "/update/");
			
			ContentResolver cr = getContentResolver();
			
			Log.v(TAG, "Starting query with <" + contentUri.toString() + ">.");
			int result = cr.update(contentUri, new ContentValues(), null, null);
			if (result > 0) {
				Toast.makeText(getApplicationContext(), org.aksw.mssw.R.string.syncing_toast_success, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), org.aksw.mssw.R.string.syncing_toast_failed, Toast.LENGTH_LONG).show();
			}
			
			// could save last result or date to shared Preferences
			return true;
		}
	}
	
}
