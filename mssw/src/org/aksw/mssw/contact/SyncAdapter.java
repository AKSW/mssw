package org.aksw.mssw.contact;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.Html.TagHandler;
import android.util.Log;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

	private static String TAG = "MsswSyncAdapter";
	private Context context;
	
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		this.context = context;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		try {
			ContactsSyncAdapterService.performSync(context, account, extras, authority, provider, syncResult);
		} catch (OperationCanceledException e) {
			Log.v(TAG, "The SyncAdapter was canceld.", e);
		}

	}

}
