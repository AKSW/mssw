package org.aksw.mssw;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class FirstRun extends Activity {
	private static String TAG = "MsswFirstRunWinzard";
	private static int poke = 0;

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
		if (intent == null && poke < 3) {
			Log.v(TAG, "No Intent, starting first run wizard.");
			intent = new Intent(Constants.INTENT_FIRSTRUN);
			setIntent(intent);
			poke++;
			go();
		} else {
			String action = intent.getAction();
			Log.v(TAG, "Intent with action " + action + " found.");
			if (action != null && action.equals(Constants.INTENT_ERROR)) {
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
						Intent i = new Intent(getApplicationContext(),
								org.aksw.mssw.browser.Browser.class);
						startActivity(i);
					}
				});
			} else if (action != null
					&& action.equals(Constants.INTENT_FIRSTRUN)) {
				int progress = intent.getIntExtra("progress", 0);
				// int ready = 3;
				if (progress < 1) {
					// WebID
					setContentView(R.layout.firstrun_webid);

					EditText webidInput = (EditText) findViewById(R.id.webid);
					Button okButton = (Button) findViewById(R.id.next_button);
					if (okButton != null) {

						okButton.setOnClickListener(new OkClickListener(
								progress + 1, "me", webidInput));
					} else {
						Log.e(TAG, "Couldn't find 'next' button in view.");
					}
				} else if (progress < 2) {
					// certificat

					Uri contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI
							+ "/config/foafssl/");
					Log.v(TAG,
							"Starting Query with uri: <"
									+ contentUri.toString() + ">.");
					Cursor ret = getContentResolver().query(contentUri, null,
							null, null, null);
					if (ret == null) {

						setContentView(R.layout.firstrun_nocert);

						Button okButton = (Button) findViewById(R.id.next_button);
						if (okButton != null) {
							okButton.setOnClickListener(new OkClickListener(
									progress + 1));
						} else {
							Log.e(TAG, "Couldn't find 'next' button in view.");
						}

					} else {

						setContentView(R.layout.firstrun_cert);

						EditText passwordInput = (EditText) findViewById(R.id.password);
						CheckBox showPassword = (CheckBox) findViewById(R.id.show_password);
						Button okButton = (Button) findViewById(R.id.next_button);
						if (okButton != null && passwordInput != null) {
							okButton.setOnClickListener(new OkClickListener(
									progress + 1, "password", passwordInput));
						} else {
							Log.e(TAG, "Couldn't find 'next' button in view.");
						}

						if (showPassword != null && passwordInput != null) {
							showPassword.setOnCheckedChangeListener(new PasswordCheckedChangeListener(passwordInput));
						}
					}
				} else {
					// ready
					Intent i = new Intent(getApplicationContext(),
							org.aksw.mssw.browser.Browser.class);
					startActivity(i);
				}

			} else if (poke < 3) {
				setIntent(null);
				poke++;
				go();
			} else {
				Log.v(TAG, "Intent error.");
			}
		}
	}

	class PasswordCheckedChangeListener implements OnCheckedChangeListener {

		private EditText passwordInput;

		public PasswordCheckedChangeListener(EditText passwordInputIn) {
			passwordInput = passwordInputIn;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			if (isChecked) {
				passwordInput
						.setTransformationMethod(android.text.method.SingleLineTransformationMethod
								.getInstance());
			} else {
				passwordInput
						.setTransformationMethod(android.text.method.PasswordTransformationMethod
								.getInstance());
			}
		}
	}

	class OkClickListener implements OnClickListener {

		private int progress;
		private String key;
		private EditText value;

		public OkClickListener(int progressIn) {
			progress = progressIn;
		}

		public OkClickListener(int progressIn, String keyIn, EditText valueIn) {
			key = keyIn;
			value = valueIn;
			progress = progressIn;
		}

		@Override
		public void onClick(View v) {
			String valueText = value.getText().toString();
			boolean goOn = true;
			if (key == null) {

			} else if (key.equals("password")) {
				Uri contentUri = Uri.parse(Constants.TRIPLE_CONTENT_URI
						+ "/config/foafssl/");

				Log.v(TAG, "Starting Query with uri: <" + contentUri.toString()
						+ ">.");

				ContentValues values = new ContentValues();
				values.put("password", valueText);

				int ret = getContentResolver().update(contentUri, values, null,
						null);
				if (ret > 0) {
					goOn = true;
				} else {
					Intent intent = new Intent(Constants.INTENT_ERROR);
					intent.putExtra("error_titel",
							getString(R.string.error_write_password_titel));
					intent.putExtra("error_message",
							getString(R.string.error_write_password_message));
					startActivity(intent);
				}
			} else {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				Editor editor = prefs.edit();
				editor.putString(key, valueText);
				if (editor.commit()) {
					goOn = true;
				} else {
					Intent intent = new Intent(Constants.INTENT_ERROR);
					intent.putExtra("error_titel",
							getString(R.string.error_write_webid_titel));
					intent.putExtra("error_message",
							getString(R.string.error_write_webid_message));
					startActivity(intent);
				}
			}
			if (goOn) {
				Intent i = new Intent(Constants.INTENT_FIRSTRUN);
				i.putExtra("progress", progress);
				startActivity(i);
			}
		}
	}
}
