package org.aksw.mssw;

import java.net.URLEncoder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class MsswPreferenceActivity extends PreferenceActivity {

	private static final String TAG = "MswPreferenceActivity";
	
	private static final String CONTENT_AUTHORITY = "org.aksw.msw.tripleprovider";
	private static final Uri CONTENT_URI = Uri.parse("content://"
			+ CONTENT_AUTHORITY);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		Preference sync = (Preference) findPreference("sync");
		sync.setOnPreferenceClickListener(new syncClickListener());
	}
	
	private class syncClickListener implements Preference.OnPreferenceClickListener {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			Toast.makeText(getApplicationContext(), R.string.syncing_toast, Toast.LENGTH_LONG).show();
			
			Uri contentUri = Uri.parse(CONTENT_URI + "/update/");
			
			ContentResolver cr = getContentResolver();
			
			Log.v(TAG, "Starting query with <" + contentUri.toString() + ">.");
			int result = cr.update(contentUri, new ContentValues(), null, null);
			if (result > 0) {
				Toast.makeText(getApplicationContext(), R.string.syncing_toast_success, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), R.string.syncing_toast_failed, Toast.LENGTH_LONG).show();
			}
			
			// could save last result or date to shared Preferences
			return true;
		}
	}
	
}
