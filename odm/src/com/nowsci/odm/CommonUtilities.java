package com.nowsci.odm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

public final class CommonUtilities extends Activity {

	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "CommonUtilities";

	static String gSERVER_URL = "";
	static String gNAME = "";
	static String gUSERNAME = "";
	static String gENC_KEY = "";
	static String gREG_ID = "";
	static String gVALID_SSL = "";
	static String gDEBUG = "";
	static String gSENDER_ID = "590633583092";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	static String getVAR(String s) {
		if (s.equals("SERVER_URL"))
			return gSERVER_URL;
		if (s.equals("NAME"))
			return gNAME;
		if (s.equals("USERNAME"))
			return gUSERNAME;
		if (s.equals("ENC_KEY"))
			return gENC_KEY;
		if (s.equals("REG_ID"))
			return gREG_ID;
		if (s.equals("VALID_SSL"))
			return gVALID_SSL;
		if (s.equals("DEBUG"))
			return gDEBUG;
		if (s.equals("SENDER_ID"))
			return gSENDER_ID;
		return "";
	}

	static void setVAR(String s, String v) {
		if (s.equals("SERVER_URL"))
			gSERVER_URL = v;
		if (s.equals("NAME"))
			gNAME = v;
		if (s.equals("USERNAME"))
			gUSERNAME = v;
		if (s.equals("ENC_KEY"))
			gENC_KEY = v;
		if (s.equals("REG_ID"))
			gREG_ID = v;
		if (s.equals("VALID_SSL"))
			gVALID_SSL = v;
		if (s.equals("DEBUG"))
			gDEBUG = v;
	}

	static void Logd(String inTAG, String message) {
		if (gDEBUG.equals("true"))
			Log.d(inTAG, message);
	}

	/**
	 * Notifies UI to display a message.
	 * 
	 * This method is defined in the common helper because it's used both by the
	 * UI and the background service.
	 * 
	 * @param context
	 *            application's context.
	 * @param message
	 *            message to be displayed.
	 */
	static void displayMessage(Context context, String message) {
		Intent intent = new Intent("com.nowsci.odm.DISPLAY_MESSAGE");
		intent.putExtra("message", message);
		context.sendBroadcast(intent);
	}

	static void setBasicAuthentication(HttpURLConnection conn, URL url) {
		String userInfo = url.getUserInfo();
		if (userInfo != null && userInfo.length() > 0) {
			String authString = Base64.encodeToString(userInfo.getBytes(), Base64.DEFAULT);
			conn.setRequestProperty("Authorization", "Basic " + authString);
		}
	}

	/**
	 * Issue a POST request to the server.
	 * 
	 * @param endpoint
	 *            POST address.
	 * @param params
	 *            request parameters.
	 * 
	 * @throws IOException
	 *             propagated from POST.
	 */
	static String post(String endpoint, Map<String, String> params) throws IOException {
		URL url;
		String html = "";
		Logd(TAG, "Starting post...");
		Boolean cont = true;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Invalid url: " + endpoint);
			cont = false;
			throw new IllegalArgumentException("Invalid url: " + endpoint);
		}
		if (cont) {
			StringBuilder bodyBuilder = new StringBuilder();
			StringBuilder bodyBuilderDebug = new StringBuilder();
			Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
			// constructs the POST body using the parameters
			while (iterator.hasNext()) {
				Entry<String, String> param = iterator.next();
				bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
				if (param.getKey().equals("username"))
					bodyBuilderDebug.append("username").append('=').append("HIDDEN").append(param.getValue().length());
				else if (param.getKey().equals("password"))
					bodyBuilderDebug.append("password").append('=').append("HIDDEN").append(param.getValue().length());
				else
					bodyBuilderDebug.append(param.getKey()).append('=').append(param.getValue());
				if (iterator.hasNext()) {
					bodyBuilder.append('&');
					bodyBuilderDebug.append('&');
				}
			}
			String body = bodyBuilder.toString();
			// Only to be used for major debug purposes as it reveals usernames and passwords.
			Logd(TAG, "Posting '" + bodyBuilderDebug.toString() + "' to " + url);

			// The below is for v2.
			// In v2, we will encrypt/decrypt completely on client in Android and Javascript
			// This will keep everything on the server anonymous, too.
			/*
			// If it's not regId, username, or password, encrypt it.
			MCrypt mcrypt = new MCrypt();
			for (Map.Entry<String, String> param : params.entrySet()) {
				if (!param.getKey().equals("regId") && !param.getKey().equals("username") && !param.getKey().equals("password")) {
					String value = param.getValue();
					try {
						String encrypted = new String(mcrypt.encrypt(value));
						param.setValue(encrypted);
					} catch (Exception e) {
						Logd(TAG, e.getMessage());
					}
				}
			}
			*/

			byte[] bytes = body.getBytes();
			if (!endpoint.startsWith("https") || gVALID_SSL.equals("true")) {
				Logd(TAG, "Posting (valid) to " + url);
				html = validPost(params, bytes, url);
			} else {
				Logd(TAG, "Posting to " + url);
				html = invalidPost(params, bytes, url);
			}
			Logd(TAG, "Completed post");
		} else {
			html = "Bad url";
		}
		Logd(TAG, "Server returned:" + html);
		return html;
	}

	static String validPost(Map<String, String> params, byte[] bytes, URL url) throws IOException {
		String html = "";
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		// TODO ensure HttpsURLConnection.setDefaultSSLSocketFactory isn't required to be reset like hostnames are
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			setBasicAuthentication(conn, url);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			// handle the response
			int status = conn.getResponseCode();
			//Logd(TAG, "Post status (" + status + ")");
			if (status != 200) {
				Logd(TAG, "Failed to post to server, returned code: " + status);
				throw new IOException("Post failed with error code " + status);
			} else {
				InputStreamReader in = new InputStreamReader(conn.getInputStream());
				BufferedReader br = new BufferedReader(in);
				String decodedString;
				while ((decodedString = br.readLine()) != null) {
					html += decodedString;
				}
				in.close();
			}
		} catch (IOException e) {
			Logd(TAG, "Failed to post to server: " + e.getMessage());
		}
		if (conn != null) {
			conn.disconnect();
		}
		return html;
	}

	static String invalidPost(Map<String, String> params, byte[] bytes, URL url) throws IOException {
		String html = "";
		HttpsURLConnection conn = null;
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					// TODO Auto-generated method stub					
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					// TODO Auto-generated method stub
				}
			} };
			// Install the all-trusting trust manager
			SSLContext sc;
			sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			setBasicAuthentication(conn, url);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			// handle the response
			int status = conn.getResponseCode();
			//Logd(TAG, "Post status (" + status + ")");
			if (status != 200) {
				Logd(TAG, "Failed to post to server, returned code: " + status);
				throw new IOException("Post failed with error code " + status);
			} else {
				InputStreamReader in = new InputStreamReader(conn.getInputStream());
				BufferedReader br = new BufferedReader(in);
				String decodedString;
				while ((decodedString = br.readLine()) != null) {
					html += decodedString;
				}
				in.close();
			}
		} catch (IOException e) {
			Logd(TAG, "Failed to post to server: " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			Logd(TAG, "Failed to post to server: " + e.getMessage());
		} catch (KeyManagementException e) {
			Logd(TAG, "Failed to post to server: " + e.getMessage());
		}
		if (conn != null) {
			conn.disconnect();
		}
		return html;
	}
}
