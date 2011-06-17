package org.aksw.mssw.update;

import org.aksw.mssw.Constants;
import org.aksw.mssw.R;
import org.aksw.mssw.FirstRun.OkClickListener;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * This update check check if a webid is configured
 * 
 * @author natanael
 * 
 */
public class WebIdCheck extends UpdateCheck {
	public static final String TAG = "WebIdCheck";

	Activity context;

	@Override
	public void setContext(Context contextIn) {
		// the problem is, that this is no Activity
		context = (Activity) contextIn;
	}

	@Override
	public boolean isConsistent() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		String webid = pref.getString("me", null);
		if (webid != null) {
			return true;
		}
		return false;
	}

	// maybe the UpdateChecks should return a view which will be enqueued in
	// something like the first run wizard
	@Override
	public void configure() throws MswUpdateException {
		// TODO show a first run activity to give the user the possibility to
		// configure a webid
		context.setContentView(R.layout.firstrun_webid);

		EditText webidInput = (EditText) context.findViewById(R.id.webid);
		Button okButton = (Button) context.findViewById(R.id.next_button);
		if (okButton != null) {
			// there has to be something that evaluates the content of the input
			// field on pressing ok
			okButton.setOnClickListener(new OkClickListener(context, webidInput));
		} else {
			Log.e(TAG, "Couldn't find 'next' button in view.");
		}
	}

	class OkClickListener implements OnClickListener {

		private Context context;
		private EditText value;

		public OkClickListener(Context contextIn, EditText valueIn) {
			value = valueIn;
			context = contextIn;
		}

		@Override
		public void onClick(View v) {
			String webid = value.getText().toString();

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			Editor editor = prefs.edit();
			editor.putString("me", webid);
			editor.commit();
		}
	}

}
