package org.aksw.msw;

import java.io.File;

public final class Constants {

	public static final String FILES_PATH = "Android" + File.separator + "data" + File.separator + "org.aksw.msw" + File.separator + "files";
	
	public static final String CERT_DIR = FILES_PATH + File.separator + "certs";
	public static final String WEB_MODELS_DIR = FILES_PATH + File.separator + "models" + File.separator + "web";
	
	/**
	 * Directory for cached infered Models
	 */
	public static final String INF_MODELS_DIR = FILES_PATH + File.separator + "models" + File.separator + "inf";
	public static final String LOCAL_MODELS_DIR = FILES_PATH + File.separator + "models" + File.separator + "local";
	public static final String CACHE_MODELS_DIR = FILES_PATH + File.separator + "models" + File.separator + "cache";

}
