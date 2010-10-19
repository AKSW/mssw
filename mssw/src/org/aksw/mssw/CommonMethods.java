package org.aksw.mssw;

import android.content.ContentResolver;

public class CommonMethods {

	public static boolean checkForTripleProvider(ContentResolver cr) {
		if (cr.acquireContentProviderClient(
				Constants.TRIPLE_AUTHORITY) == null) {
			return false;
		} else {
			return true;
		}
	}
	
}
