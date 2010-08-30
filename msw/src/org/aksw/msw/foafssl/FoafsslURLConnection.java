/*
 * Some parts are copied from http://docjar.net/html/api/org/apache/harmony/luni/internal/net/www/protocol/https/HttpsURLConnectionImpl.java.html
 * 
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.aksw.msw.foafssl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpConnection;

import android.net.Uri;

public class FoafsslURLConnection extends HttpsURLConnection {

	private static final String MSG_NOT_CONNECTED = "This connection is not yet connected.";

	private SSLSocket sslSocket;
	
	private HttpConnection connection;
	
	private URI uri;
	
	public FoafsslURLConnection(URL url) {
		super(url);
		this.setRequestProperty(
				"accept",
				"application/rdf+xml, application/xml; q=0.8, text/xml; q=0.7, application/rss+xml; q=0.3, */*; q=0.2");

		// super.getSSLSocketFactory();
	}

	@Override
	public String getCipherSuite() {
		if (sslSocket == null) {
			throw new IllegalStateException(MSG_NOT_CONNECTED);
		} else {
			return sslSocket.getSession().getCipherSuite();
		}
	}

	@Override
	public Certificate[] getLocalCertificates() {
		if (sslSocket == null) {
			throw new IllegalStateException(MSG_NOT_CONNECTED);
		} else {
			return sslSocket.getSession().getLocalCertificates();
		}
	}

	@Override
	public Certificate[] getServerCertificates()
			throws SSLPeerUnverifiedException {
		if (sslSocket == null) {
			throw new IllegalStateException(MSG_NOT_CONNECTED);
		} else {
			return sslSocket.getSession().getPeerCertificates();
		}
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean usingProxy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void connect() throws IOException {
		getSSLSocketFactory().createSocket();
		if (connected) {
			return;
		} else {
			try {
				uri = url.toURI();
			} catch (URISyntaxException e) {
				// TODO: handle exception
			}
			
			connection = null;
			
		}
		
		sslSocket = (SSLSocket) getSSLSocketFactory().createSocket(url.getHost(), url.getPort());

	}

	public boolean isConnected() {
		return connected;
	}

}
