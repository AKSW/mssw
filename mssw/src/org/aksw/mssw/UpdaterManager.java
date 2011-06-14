package org.aksw.mssw;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UpdaterManager {
	private Context context;
	
	public UpdaterManager (Context contextIn) {
		context = contextIn;
	}
	
	public boolean needUpdate() {

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		int version = sharedPreferences.getInt("version", -1);
		
		int versionInstalled = 0;
		
		if (version < versionInstalled) {
			return true;
		}
		return false;
	}
	
	public void update() {
		
	}
}
