package org.aksw.mssw;

import org.aksw.mssw.update.UpdateChecker;

import android.app.Application;
import android.util.Log;

/**
 * This Class is the main class, which is always instantiate when the App is running.
 * So we can do basic stuff like checking if the local configuration is up to date and fits to the installed version.
 * 
 * @author natanael
 *
 */
public class SemanticWebCore extends Application {

	private static String TAG = "SemanticWebCore Application";
	
	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		Log.v(TAG, "================ Creating SemanticWebCore ================");
		UpdateChecker updateChecker = new UpdateChecker(getApplicationContext());
		if (!updateChecker.isConsistent()) {
			updateChecker.configure();
			/**
			 *  TODO: what to do if not consistent and the configuration fails?
			 *  in this case the UpdateChecker should start a configuration activity
			 *  like the FirstRun wizard.
			 *  Is it good to start an Activity even if the TripleProvider is called?
			 *  Maybe the TripleProvider should throw an exception if the system is not configured.
			 *  And than we should give the possibility to run the configuration.
			 */
		}
		super.onCreate();
	}

}
