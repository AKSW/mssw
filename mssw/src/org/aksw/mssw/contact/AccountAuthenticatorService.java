package org.aksw.mssw.contact;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AccountAuthenticatorService extends Service {

	private static AccountAuthenticator authenticator;
	
	@Override
	public IBinder onBind(Intent intent) {
		IBinder binder = null;
		if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
			if(authenticator == null) {
				authenticator = new AccountAuthenticator(this.getApplicationContext());
			}
			binder = authenticator.getIBinder();
		}
		return binder;
	}

}
