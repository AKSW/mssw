package org.aksw.msw.foafssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import android.util.Log;


public class TrustManagerFactory {
	
	private static String TAG = "MswTrustManagerFactory";
	private static String KEY_ALGORITHM = "X509";
	private static String KEYSTORE_TYPE = "PKCS12";
	private static String SSL_PROTOCOL = "SSL";
	
	// TODO need to import the CA somewhere
	
	public static SSLSocketFactory getFactory(File keyFile, String keyPassword) {
		try {
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KEY_ALGORITHM);
			KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
			
			InputStream keyInput = new FileInputStream(keyFile);
			keyStore.load(keyInput, keyPassword.toCharArray());
			keyInput.close();
			
			keyManagerFactory.init(keyStore, keyPassword.toCharArray());
			
			SSLContext context = SSLContext.getInstance(SSL_PROTOCOL);
			context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
			
			return context.getSocketFactory();
			
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "There is no support for " + KEY_ALGORITHM + " or " + SSL_PROTOCOL + " key algorithm available.", e);
		} catch (KeyStoreException e) {
			Log.e(TAG, "There is no support for " + KEYSTORE_TYPE + " keystore type available.", e);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "The given keyfile: '" + keyFile.getAbsolutePath() + "' could not be found.", e);
		} catch (CertificateException e) {
			Log.e(TAG, "Error on loading key into the keystore.", e);
		} catch (IOException e) {
			Log.e(TAG, "Problem while reading from keyfile: '" + keyFile.getAbsolutePath() + "'.", e);
		} catch (UnrecoverableKeyException e) {
			Log.e(TAG, "Could not recover key.", e);
		} catch (KeyManagementException e) {
			Log.e(TAG, "Could not initialize SSLContext.", e);
		}
		
		Log.i(TAG, "Sorry could only return 'null'.");
		return null;
	}

}
