package org.aksw.mssw.update;

import android.content.Context;

public abstract class UpdateCheck {

	protected Context context;
	
	/**
	 * Gives the context to the test
	 * @param context
	 */
	public abstract void setContext (Context context); 
	
	/**
	 * Checks if the specified test is consistent and up-to-date.
	 * @return
	 */
	public abstract boolean isConsistent ();

	/**
	 * In this function the test configures its new environment to be up to date
	 */
	public abstract void configure () throws MswUpdateException;
}
