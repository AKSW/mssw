package org.aksw.mssw;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FirstRun extends Activity {
	private static String TAG = "MsswFirstRunWinzard";
	
	public void onCreate() {
		Log.v(TAG, "create");
		go();

	}
	

	@Override
	protected void onNewIntent(Intent intent) {
		Log.v(TAG, "New intent");
		setIntent(intent);
		go();
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "resume");
		go();
	}


	private void go() {
		Log.v(TAG, "go");
		Intent intent = getIntent();
		if (intent == null) {
			Log.v(TAG, "No Intent, starting first run wizard.");

			setContentView(R.layout.error);
			TextView textView;
			textView = (TextView) findViewById(R.id.error_titel);
			textView.setText("First Run Wizard");
			textView = (TextView) findViewById(R.id.error_message);
			textView.setText("Bitte tragen Sie ihre WebID ein.");
		} else {
			String action = intent.getAction();
			Log.v(TAG, "Intent with action " + action + " found.");
			if (action.equals(Constants.INTENT_ERROR)) {
				String titel = intent.getStringExtra("error_titel");
				String message = intent.getStringExtra("error_message");

				setContentView(R.layout.error);
				TextView textView;
				textView = (TextView) findViewById(R.id.error_titel);
				textView.setText(titel);
				textView = (TextView) findViewById(R.id.error_message);
				textView.setText(message);
				Button okButton = (Button) findViewById(R.id.ok_button);
				okButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent i = new Intent(getApplicationContext(), org.aksw.mssw.browser.Browser.class);
						startActivity(i);
					}
				});
			} else {

			}
		}
	}
}
