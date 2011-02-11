package org.aksw.mssw.foafssl;

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
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

public class TrustManagerFactory {

	private static String TAG = "MswTrustManagerFactory";
	private static String KEY_ALGORITHM = "X509";
	private static String KEYSTORE_TYPE = "PKCS12";
	private static String SSL_PROTOCOL = "TLS";

	// TODO need to import the CA somewhere

	public static SSLSocketFactory getFactory(File keyFile, String keyPassword) {

		TrustManager[] trustEverybody = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		} };

		try {
			KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KEY_ALGORITHM);
			KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);

			InputStream keyInput = new FileInputStream(keyFile);
			keyStore.load(keyInput, keyPassword.toCharArray());
			keyInput.close();

			keyManagerFactory.init(keyStore, keyPassword.toCharArray());

			SSLContext context = SSLContext.getInstance(SSL_PROTOCOL);

			context.init(keyManagerFactory.getKeyManagers(), trustEverybody,
					new SecureRandom());

			return context.getSocketFactory();

		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "There is no support for " + KEY_ALGORITHM + " or "
					+ SSL_PROTOCOL + " key algorithm available.", e);
		} catch (KeyStoreException e) {
			Log.e(TAG, "There is no support for " + KEYSTORE_TYPE
					+ " keystore type available.", e);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "The given keyfile: '" + keyFile.getAbsolutePath()
					+ "' could not be found.", e);
		} catch (CertificateException e) {
			Log.e(TAG, "Error on loading key into the keystore.", e);
		} catch (IOException e) {
			if (e.getMessage().contains("wrong password")) {
				Log.e(TAG,
						"Problem while reading from keyfile, maybe Password is wrong.");
			}
			Log.e(TAG,
					"Problem while reading from keyfile: '"
							+ keyFile.getAbsolutePath() + "'.", e);
		} catch (UnrecoverableKeyException e) {
			Log.e(TAG, "Could not recover key.", e);
		} catch (KeyManagementException e) {
			Log.e(TAG, "Could not initialize SSLContext.", e);
		}

		Log.i(TAG, "Sorry could only return 'null'.");
		return null;
	}
	
	public static HostnameVerifier getVerifier() {
		return new HostnameVerifier() {
			
			@Override
			public boolean verify(String hostname, SSLSession session) {
				Log.v(TAG, "Hostname is: " + hostname + ", peerHost is: " + session.getPeerHost() + ", I'll accept all hosts ;-)");
				return true;
			}
		};
	}
}
