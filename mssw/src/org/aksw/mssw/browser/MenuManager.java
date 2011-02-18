package org.aksw.mssw.browser;

import org.aksw.mssw.Constants;
import org.aksw.mssw.MsswPreferenceActivity;
import org.aksw.mssw.R;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

public class MenuManager {
	
	public MenuManager() {
		
	}
	
	public boolean itemSelected (Activity context, MenuItem item, String selectedWebID) {
		Intent i;
		switch (item.getItemId()) {
			case R.id.itemPref:
				i = new Intent(context, MsswPreferenceActivity.class);
				context.startActivity(i);
				return true;
			case R.id.itemAdd:
				i = new Intent(Constants.INTENT_ADD_WEBID, Uri.parse(selectedWebID));
				context.startActivity(i);
				return true;
			case R.id.itemMe:
				// i = new Intent(this, BrowserMeCard.class);
				SharedPreferences sharedPreferences = PreferenceManager
						.getDefaultSharedPreferences(context.getApplicationContext());
				String webId = sharedPreferences.getString("me",
						Constants.EXAMPLE_webId);
				i = new Intent(Constants.INTENT_VIEW_WEBID, Uri.parse(webId));
				context.startActivity(i);
				return true;
			case R.id.itemSearch:
				context.onSearchRequested();
				return true;
			case R.id.itemScan:
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				try {
					context.startActivityForResult(intent, 0);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(context.getApplicationContext(), R.string.install_zxing, Toast.LENGTH_LONG);
				}
				return true;
			default:
				return false;
		}
	}
}
