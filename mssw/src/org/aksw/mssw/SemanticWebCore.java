package org.aksw.msw;

import org.aksw.msw.update.UpdateChecker;

import android.app.Application;
import android.util.Log;

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
			 *  TODO: what to do it not consistent and the configuration fails?
			 *  in this case the UpdateChecker should start a configuration activity
			 *  like the FirstRun wizard.
			 *  Is it good to start an Activity even if the TripleProvider is called?
			 */
		}
		super.onCreate();
	}

}
