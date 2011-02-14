package org.aksw.mssw;

import java.io.File;
import java.util.HashMap;

import android.net.Uri;

public final class Constants {
	public static final String ENC = "UTF-8";

	public static final String FILES_PATH = "Android" + File.separator + "data"
			+ File.separator + "org.aksw.mssw" + File.separator + "files";

	public static final String CERT_FILE = FILES_PATH + File.separator
			+ "privatekey.p12";
	public static final String RULE_FILE = FILES_PATH + File.separator
			+ "rules.n3";

	public static final String WEB_MODELS_DIR = FILES_PATH + File.separator
			+ "models" + File.separator + "web";

	/**
	 * Directory for cached infered Models
	 */
	public static final String INF_MODELS_DIR = FILES_PATH + File.separator
			+ "models" + File.separator + "inf";
	public static final String LOCAL_MODELS_DIR = FILES_PATH + File.separator
			+ "models" + File.separator + "local";
	public static final String CACHE_MODELS_DIR = FILES_PATH + File.separator
			+ "models" + File.separator + "cache";


	public static HashMap<String, String> namespaces = new HashMap<String, String>();
	static {
		namespaces.put("rel", "http://purl.org/vocab/relationship/");
		namespaces.put("foaf", "http://xmlns.com/foaf/0.1/");
		namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-shema#");
	}
	
	public static final String PROP_hasData = "http://ns.aksw.org/Android/hasData";
	public static final String PROP_rdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public static final String PROP_knows = "http://xmlns.com/foaf/0.1/knows";
	public static final String TYPE_Person = "http://xmlns.com/foaf/0.1/Person";
	public static final String PROP_updateEndpoint = "http://ns.aksw.org/update/queryEndpoint";

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
	public static final String TRIPLE_AUTHORITY = "org.aksw.mssw.triplestore.tripleprovider";
	public static final Uri TRIPLE_CONTENT_URI = Uri.parse("content://"
			+ TRIPLE_AUTHORITY);
	
	public static final String INTENT_ADD_WEBID = "org.aksw.mssw.ADD_WEBID";
	public static final String INTENT_VIEW_WEBID = "org.aksw.mssw.VIEW_WEBID";
	public static final String INTENT_BACK = "org.aksw.mssw.BACK";
	public static final String INTENT_ERROR = "org.aksw.mssw.ERROR";
	public static final String INTENT_FIRSTRUN = "org.aksw.mssw.FIRSTRUN";
	
	public static final int TIME_SHORT = 1000;
	public static final int TIME_MIDDLE = 5000;
	public static final int TIME_LONG = 10000;
	
	public static String[] PROPS_webactive = {
		"http://xmlns.com/foaf/0.1/depiction",
		"http://xmlns.com/foaf/0.1/workplaceHomepage",
		"http://xmlns.com/foaf/0.1/workInfoHomepage",
		"http://xmlns.com/foaf/0.1/schoolHomepage",
		"http://xmlns.com/foaf/0.1/curretProject",
		"http://xmlns.com/foaf/0.1/weblog"
	};

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
			"http://rdfs.org/sioc/ns#name",
			"http://xmlns.com/foaf/0.1/givenName",
			"http://xmlns.com/foaf/0.1/familyName",
			"http://xmlns.com/foaf/0.1/nick",
			"http://xmlns.com/foaf/0.1/surname" };

	public static String[] PROPS_pictureProps = { "http://xmlns.com/foaf/0.1/depiction" };
	

	public static final String DATA_KINDS_PREFIX = "http://ns.aksw.org/Android/ContactsContract.";
	public static final String COMMON_DATA_KINDS_PREFIX = DATA_KINDS_PREFIX
			+ "CommonDataKinds.";
	public static final String DATA_COLUMNS_PREFIX = DATA_KINDS_PREFIX
			+ "DataColumns.";

	public static final String REQUEST_PROPERTY = "application/rdf+xml, application/xml; q=0.8, text/xml; q=0.7, application/rss+xml; q=0.3, */*; q=0.2";

	/**
	 * Because the fields android.provider.ContactsContract.DataColumns is not
	 * available in API < 5
	 */

	public static final HashMap<String, String> DATA_COLUMNS = new HashMap<String, String>();

	static {
		DATA_COLUMNS.put("mimetype", PROP_rdfType);
		DATA_COLUMNS.put("data1", DATA_COLUMNS_PREFIX + "DATA1");
		DATA_COLUMNS.put("data2", DATA_COLUMNS_PREFIX + "DATA2");
		DATA_COLUMNS.put("data3", DATA_COLUMNS_PREFIX + "DATA3");
		DATA_COLUMNS.put("data4", DATA_COLUMNS_PREFIX + "DATA4");
		DATA_COLUMNS.put("data5", DATA_COLUMNS_PREFIX + "DATA5");
		DATA_COLUMNS.put("data6", DATA_COLUMNS_PREFIX + "DATA6");
		DATA_COLUMNS.put("data7", DATA_COLUMNS_PREFIX + "DATA7");
		DATA_COLUMNS.put("data8", DATA_COLUMNS_PREFIX + "DATA8");
		DATA_COLUMNS.put("data9", DATA_COLUMNS_PREFIX + "DATA9");
		DATA_COLUMNS.put("data10", DATA_COLUMNS_PREFIX + "DATA10");
		DATA_COLUMNS.put("data11", DATA_COLUMNS_PREFIX + "DATA11");
		DATA_COLUMNS.put("data12", DATA_COLUMNS_PREFIX + "DATA12");
		DATA_COLUMNS.put("data13", DATA_COLUMNS_PREFIX + "DATA13");
		DATA_COLUMNS.put("data14", DATA_COLUMNS_PREFIX + "DATA14");
		DATA_COLUMNS.put("data15", DATA_COLUMNS_PREFIX + "DATA15");
	}

	/**
	 * Because there is no possibilitiy for MIME-Type "reverselookup"
	 */

	public static final HashMap<String, String> MIME_TYPES = new HashMap<String, String>();

	static {
		MIME_TYPES.put("vnd.android.cursor.item/email",
				COMMON_DATA_KINDS_PREFIX + "Email");
		MIME_TYPES.put("vnd.android.cursor.item/email_v2",
				COMMON_DATA_KINDS_PREFIX + "Email");
		MIME_TYPES.put("vnd.android.cursor.item/contact_event",
				COMMON_DATA_KINDS_PREFIX + "Event");
		MIME_TYPES.put("vnd.android.cursor.item/group_membership",
				COMMON_DATA_KINDS_PREFIX + "GroupMembership");
		MIME_TYPES.put("vnd.android.cursor.item/im", COMMON_DATA_KINDS_PREFIX
				+ "Im");
		MIME_TYPES.put("vnd.android.cursor.item/nickname",
				COMMON_DATA_KINDS_PREFIX + "Nickname");
		MIME_TYPES.put("vnd.android.cursor.item/note", COMMON_DATA_KINDS_PREFIX
				+ "Note");
		MIME_TYPES.put("vnd.android.cursor.item/organization",
				COMMON_DATA_KINDS_PREFIX + "Organization");
		MIME_TYPES.put("vnd.android.cursor.item/phone",
				COMMON_DATA_KINDS_PREFIX + "Phone");
		MIME_TYPES.put("vnd.android.cursor.item/phone_v2",
				COMMON_DATA_KINDS_PREFIX + "Phone");
		MIME_TYPES.put("vnd.android.cursor.item/photo",
				COMMON_DATA_KINDS_PREFIX + "Photo");
		MIME_TYPES.put("vnd.android.cursor.item/relation",
				COMMON_DATA_KINDS_PREFIX + "Relation");
		MIME_TYPES.put("vnd.android.cursor.item/name", COMMON_DATA_KINDS_PREFIX
				+ "StructuredName");
		MIME_TYPES.put("vnd.android.cursor.item/postal-address",
				COMMON_DATA_KINDS_PREFIX + "StructuredPostal");
		MIME_TYPES.put("vnd.android.cursor.item/postal-address_v2",
				COMMON_DATA_KINDS_PREFIX + "StructuredPostal");
		MIME_TYPES.put("vnd.android.cursor.item/website",
				COMMON_DATA_KINDS_PREFIX + "Website");
	}
}
