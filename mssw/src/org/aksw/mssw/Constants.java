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
	public static final String INTENT_ERROR = "org.aksw.mssw.ERROR";
	
	public static final int TIME_SHORT = 1000;
	public static final int TIME_MIDDLE = 5000;
	public static final int TIME_LONG = 10000;

	public static String[] PROPS_relations = { "http://xmlns.com/foaf/0.1/knows",
			"http://purl.org/vocab/relationship/acquaintanceOf",
			"http://purl.org/vocab/relationship/ambivalentOf",
			"http://purl.org/vocab/relationship/ancestorOf",
			"http://purl.org/vocab/relationship/antagonistOf",
			"http://purl.org/vocab/relationship/apprenticeTo",
			"http://purl.org/vocab/relationship/childOf",
			"http://purl.org/vocab/relationship/closeFriendOf",
			"http://purl.org/vocab/relationship/collaboratesWith",
			"http://purl.org/vocab/relationship/colleagueOf",
			"http://purl.org/vocab/relationship/descendantOf",
			"http://purl.org/vocab/relationship/employedBy",
			"http://purl.org/vocab/relationship/employerOf",
			"http://purl.org/vocab/relationship/enemyOf",
			"http://purl.org/vocab/relationship/engagedTo",
			"http://purl.org/vocab/relationship/friendOf",
			"http://purl.org/vocab/relationship/grandchildOf",
			"http://purl.org/vocab/relationship/grandparentOf",
			"http://purl.org/vocab/relationship/hasMet",
			"http://purl.org/vocab/relationship/influencedBy",
			"http://purl.org/vocab/relationship/knowsByReputation",
			"http://purl.org/vocab/relationship/knowsInPassing",
			"http://purl.org/vocab/relationship/knowsOf",
			"http://purl.org/vocab/relationship/lifePartnerOf",
			"http://purl.org/vocab/relationship/livesWith",
			"http://purl.org/vocab/relationship/lostContactWith",
			"http://purl.org/vocab/relationship/mentorOf",
			"http://purl.org/vocab/relationship/neighborOf",
			"http://purl.org/vocab/relationship/parentOf",
			"http://purl.org/vocab/relationship/participant",
			"http://purl.org/vocab/relationship/participantIn",
			"http://purl.org/vocab/relationship/Relationship",
			"http://purl.org/vocab/relationship/siblingOf",
			"http://purl.org/vocab/relationship/spouseOf",
			"http://purl.org/vocab/relationship/worksWith",
			"http://purl.org/vocab/relationship/wouldLikeToKnow" };

	public static String[] PROPS_nameProps = { "http://xmlns.com/foaf/0.1/name",
			"http://xmlns.com/foaf/0.1/givenName",
			"http://xmlns.com/foaf/0.1/familyName",
			"http://xmlns.com/foaf/0.1/nick" };

	public static String[] PROPS_pictureProps = { "http://xmlns.com/foaf/0.1/depiction" };
}
