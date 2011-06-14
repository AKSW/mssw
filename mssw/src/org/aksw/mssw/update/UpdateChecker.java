package org.aksw.mssw.update;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

/**
 * should be singleton, but how to manage the context?
 * @author natanael
 * TODO: should be merged with UpdateManager
 *
 */
public class UpdateChecker {

	private static String TAG = "UpdateChecker";
	private List<UpdateCheck> registeredChecks;

	public UpdateChecker(Context context) {
		Log.v(TAG, "instantiating UpdateChecker");
		registeredChecks = new ArrayList<UpdateCheck>();
		RulesCheck rc = new RulesCheck();
		rc.setContext(context);
		registeredChecks.add(rc);
	}
	
	public void registerCheck (UpdateCheck check) {
		this.registeredChecks.add(check);
	}
	
	public boolean isConsistent () {
		for (UpdateCheck check : this.registeredChecks) {
			if (check.isConsistent()) {
				
			} else {
				Log.v(TAG, "UpdateChecker is not Consistent");
				return false;
			}
		}
		Log.v(TAG, "UpdateChecker is Consistent");
		return true;
	}
	
	public void configure () {
		Log.v(TAG, "configuring UpdateChecker");
		for (UpdateCheck check : this.registeredChecks) {
			if (!check.isConsistent()) {
				try {
					check.configure();
				} catch (MswUpdateException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
