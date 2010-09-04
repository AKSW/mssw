package org.aksw.mssw;

import android.net.Uri;

public final class Constants {
	public static final String ENC = "UTF-8";
	public static final String DATA_KINDS_PREFIX = "http://ns.aksw.org/Android/ContactsContract.";

	public static final String PROP_hasData = "http://ns.aksw.org/Android/hasData";
	public static final String PROP_rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public static final String PROP_knows = "http://xmlns.com/foaf/0.1/knows";
	public static final String TYPE_Person = "http://xmlns.com/foaf/0.1/Person";

	public static final String EXAMPLE_webId = "http://people.comiles.eu/example";

	/**
	 * Static values for querying the FoafProvider
	 */
	public static final String FOAF_AUTHORITY = "org.aksw.mssw.content.foafprovider";
	public static final Uri FOAF_CONTENT_URI = Uri.parse("content://"
			+ FOAF_AUTHORITY);

	/**
	 * Static values for querying the Mssw-ContactProvider
	 */
	public static final String CONTACT_AUTHORITY = "org.aksw.mssw.contact.contactprovider";
	public static final Uri CONTACT_CONTENT_URI = Uri.parse("content://"
			+ CONTACT_AUTHORITY);

	/**
	 * Static values for querying the TripleProvider
	 */
	public static final String TRIPLE_AUTHORITY = "org.aksw.msw.tripleprovider";
	public static final Uri TRIPLE_CONTENT_URI = Uri.parse("content://"
			+ TRIPLE_AUTHORITY);
	
	public static final String INTENT_ADD_WEBID = "org.aksw.mssw.ADD_WEBID";
	public static final String INTENT_VIEW_WEBID = "org.aksw.mssw.VIEW_WEBID";
	public static final String INTENT_WEBID_SELECTED = "org.aksw.mssw.WEBID_SELECTED";
	public static final String INTENT_WEBID_SEARCH = "org.aksw.mssw.VIEW_WEBID_SEARCH";
	
	public static final int TIME_SHORT = 1000;
	public static final int TIME_MIDDLE = 5000;
	public static final int TIME_LONG = 10000;
}
