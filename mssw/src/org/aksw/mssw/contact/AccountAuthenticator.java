package org.aksw.mssw.contact;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class AccountAuthenticator extends AbstractAccountAuthenticator {
	
	private static String TAG = "MsswAccountAuthenticator";

	private Context context;
	private SharedPreferences sharedPreferences;
	
	public AccountAuthenticator(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
			String authTokenType, String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {
		
		Bundle bundle = new Bundle();
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String me = sharedPreferences.getString("me", null);
		
		if (me == null) {
			// special authentication method
			// show preferences and message, for the user to enter his WebID 
		}
		
		Account account = new Account(me, accountType);
		AccountManager am = AccountManager.get(context);
		if(am.getAccountsByType(accountType).length > 0) {
			am.removeAccount(am.getAccountsByType(accountType)[0], null, null);
		}
		if (am.addAccountExplicitly(account, "", null)) {
			Log.i(TAG, "The Account '" + me + "' was created successfully.");
		} else {
			Log.i(TAG, "The Account '" + me + "' was not created successfully. This could be because it already exist or because there was a problem with account creation.");
		}
		
		bundle.putString(AccountManager.KEY_ACCOUNT_NAME, me);
		bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,
			Account account, Bundle options) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response,
			String accountType) {
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		return null;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response,
			Account account, String[] features) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		return null;
	}

}
