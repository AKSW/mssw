package org.aksw.mssw.update;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This update check check if a webid is configured
 * 
 * @author natanael
 *
 */
public class WebIdCheck extends UpdateCheck {

	Context context;
	
	@Override
	public void setContext(Context contextIn) {
		context = contextIn;
	}

	@Override
	public boolean isConsistent() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String webid = pref.getString("me", null);
		if (webid != null) {
			return true;
		}
		return false;
	}

	@Override
	public void configure() throws MswUpdateException {
		// TODO show a first run activity to give the user the possibility to configure a webid
		
	}

}
