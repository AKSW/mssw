package org.aksw.mssw.triplestore;

import java.io.File;
import java.util.HashMap;

import android.net.Uri;

public final class Constants {

	public static final String TRIPLE_AUTHORITY = "org.aksw.mssw.triplestore.tripleprovider";
	public static final Uri TRIPLE_CONTENT_URI = Uri.parse("content://"
			+ TRIPLE_AUTHORITY);

	public static final String FILES_PATH = "Android" + File.separator + "data"
			+ File.separator + "org.aksw.mssw.triplestore" + File.separator + "files";

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
	public static final String PROP_updateEndpoint = "http://ns.aksw.org/update/queryEndpoint";

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
