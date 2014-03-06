package com.nowsci.odm;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;

public final class CommonUtilities extends Activity {

	/**
	 * Tag used on log messages.
	 */
	static final String TAG= "ODMCommonUtilities";

	static String gSERVER_URL = "";
	static String gNAME = "";
	static String gUSERNAME = "";
	static String gENC_KEY = "";
	static String gREG_ID = "";
	static String gVALID_SSL = "";
	static String gDEBUG = "";
	static String gSENDER_ID = "590633583092";
	static String gTOKEN = "";
	static String gVERSION = "";
	static String gINTERVAL = "0";
	static String gNETWORK_ONLY = "false";
	static String gHIDE_ICON = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	static void loadVARs(Context context) {
		SharedPreferences mPrefs = context.getSharedPreferences("usersettings", 0);
		String su = mPrefs.getString("SERVER_URL", "");
		setVAR("SERVER_URL", su);
		setVAR("NAME", mPrefs.getString("NAME", ""));
		setVAR("USERNAME", mPrefs.getString("USERNAME", ""));
		setVAR("ENC_KEY", mPrefs.getString("ENC_KEY", ""));
		setVAR("REG_ID", mPrefs.getString("REG_ID", ""));
		setVAR("VALID_SSL", mPrefs.getString("VALID_SSL", ""));
		setVAR("DEBUG", mPrefs.getString("DEBUG", ""));
		setVAR("TOKEN", mPrefs.getString("TOKEN", ""));
		setVAR("VERSION", mPrefs.getString("VERSION", "true"));
		setVAR("INTERVAL", mPrefs.getString("INTERVAL", "0"));
		setVAR("NETWORK_ONLY", mPrefs.getString("NETWORK_ONLY", "false"));
		setVAR("HIDE_ICON", mPrefs.getString("HIDE_ICON", "false"));
		Logd(TAG, "Loaded variables.");
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
		if (s.equals("TOKEN"))
			return gTOKEN;
		if (s.equals("VERSION"))
			return gVERSION;
		if (s.equals("INTERVAL"))
			return gINTERVAL;
		if (s.equals("NETWORK_ONLY"))
			return gNETWORK_ONLY;
		if (s.equals("HIDE_ICON"))
			return gHIDE_ICON;
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
		if (s.equals("TOKEN"))
			gTOKEN = v;
		if (s.equals("VERSION"))
			gVERSION = v;
		if (s.equals("INTERVAL"))
			gINTERVAL = v;
		if (s.equals("NETWORK_ONLY"))
			gNETWORK_ONLY = v;
		if (s.equals("HIDE_ICON"))
			gHIDE_ICON = v;
	}

	static void Logd(String inTAG, String message) {
		if (gDEBUG.equals("true"))
			Log.d(inTAG, message);
	}

	public static SharedPreferences myGetSharedPreferences(Context ctxt) {
		return ctxt.getSharedPreferences("usersettings", 0);
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

	@SuppressWarnings("resource")
	public static String post(String targetUrl, Map<String, String> params, String file, byte[] data) {
		Logd(TAG, "Starting post...");
	    String html = "";
		Boolean cont = true;
		URL url = null;
		try {
			url = new URL(targetUrl);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Invalid url: " + targetUrl);
			cont = false;
			throw new IllegalArgumentException("Invalid url: " + targetUrl);
		}
		if (cont) {		
			if (!targetUrl.startsWith("https") || gVALID_SSL.equals("true")) {
				HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
				HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
			} else {
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
				try {
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
				} catch (NoSuchAlgorithmException e) {
					Logd(TAG, "Error: " + e.getLocalizedMessage());
				} catch (KeyManagementException e) {
					Logd(TAG, "Error: " + e.getLocalizedMessage());
				}
			}
		    Logd(TAG, "Filename: " + file);
		    Logd(TAG, "URL: " + targetUrl);
		    HttpURLConnection connection = null;
		    DataOutputStream outputStream = null;
		    String pathToOurFile = file;
		    String lineEnd = "\r\n";
		    String twoHyphens = "--";
		    String boundary = "*****";
		    int bytesRead, bytesAvailable, bufferSize;
		    byte[] buffer;
		    int maxBufferSize = 1 * 1024;
		    try {
		        connection = (HttpURLConnection) url.openConnection();
		        // Allow Inputs & Outputs
		        connection.setDoInput(true);
		        connection.setDoOutput(true);
		        connection.setUseCaches(false);
		        connection.setChunkedStreamingMode(1024);
		        // Enable POST method
		        connection.setRequestMethod("POST");
				setBasicAuthentication(connection, url);
		        connection.setRequestProperty("Connection", "Keep-Alive");
		        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		        outputStream = new DataOutputStream(connection.getOutputStream());
		        //outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, String> param = iterator.next();
					outputStream.writeBytes(twoHyphens + boundary + lineEnd);
					outputStream.writeBytes("Content-Disposition: form-data;" + "name=\"" + param.getKey() + "\"" + lineEnd + lineEnd);
					outputStream.write(param.getValue().getBytes("UTF-8"));
					outputStream.writeBytes(lineEnd);
				}
		        String connstr = null;
		        if (!file.equals("")) {
			        FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile));
					outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			        connstr = "Content-Disposition: form-data; name=\"upfile\";filename=\"" + pathToOurFile + "\"" + lineEnd;
			        outputStream.writeBytes(connstr);
			        outputStream.writeBytes(lineEnd);
			        bytesAvailable = fileInputStream.available();
			        bufferSize = Math.min(bytesAvailable, maxBufferSize);
			        buffer = new byte[bufferSize];
			        // Read file
			        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			        Logd(TAG, "File length: " + bytesAvailable);
			        try {
			            while (bytesRead > 0) {
			                try {
			                    outputStream.write(buffer, 0, bufferSize);
			                } catch (OutOfMemoryError e) {
			                    e.printStackTrace();
			                    html = "Error: outofmemoryerror";
			                    return html;
			                }
			                bytesAvailable = fileInputStream.available();
			                bufferSize = Math.min(bytesAvailable, maxBufferSize);
			                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			            }
			        } catch (Exception e) {
			            Logd(TAG, "Error: " + e.getLocalizedMessage());
			            html = "Error: Unknown error";
			            return html;
			        }
			        outputStream.writeBytes(lineEnd);
			        fileInputStream.close();
		        } else if (data != null) {
					outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			        connstr = "Content-Disposition: form-data; name=\"upfile\";filename=\"tmp\"" + lineEnd;
			        outputStream.writeBytes(connstr);
			        outputStream.writeBytes(lineEnd);
			        bytesAvailable = data.length;
			        Logd(TAG, "File length: " + bytesAvailable);
			        try {
	                    outputStream.write(data, 0, data.length);
	                } catch (OutOfMemoryError e) {
	                    e.printStackTrace();
	                    html = "Error: outofmemoryerror";
	                    return html;
			        } catch (Exception e) {
			            Logd(TAG, "Error: " + e.getLocalizedMessage());
			            html = "Error: Unknown error";
			            return html;
			        }
			        outputStream.writeBytes(lineEnd);
		        }
		        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
		        // Responses from the server (code and message)
		        int serverResponseCode = connection.getResponseCode();
		        String serverResponseMessage = connection.getResponseMessage();
		        Logd(TAG, "Server Response Code " + serverResponseCode);
		        Logd(TAG, "Server Response Message: " + serverResponseMessage);
		        if (serverResponseCode == 200) {
					InputStreamReader in = new InputStreamReader(connection.getInputStream());
					BufferedReader br = new BufferedReader(in);
					String decodedString;
					while ((decodedString = br.readLine()) != null) {
						html += decodedString;
					}
					in.close();
		        }
		        outputStream.flush();
		        outputStream.close();
		        outputStream = null;
		    } catch (Exception ex) {
		        // Exception handling
		        html = "Error: Unknown error";
		        Logd(TAG, "Send file Exception: " + ex.getMessage());
		    }
		}
		if (html.startsWith("success:"))
			Logd(TAG, "Server returned: success:HIDDEN");
		else
			Logd(TAG, "Server returned: " + html);
	    return html;
	}
	
	public static String post(String targetUrl, Map<String, String> params, String file) {
		return post(targetUrl, params, file, null);
	}
	
	public static String post(String targetUrl, Map<String, String> params, byte[] data) {
		return post(targetUrl, params, "", data);
	}
	
	public static String post(String targetUrl, Map<String, String> params) {
			return post(targetUrl, params, "", null);
	}

	static String get(String endpoint) throws IOException {
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
			html = validGet(url);
		} else {
			html = "Bad url";
		}
		return html;
	}

	static String validGet(URL url) throws IOException {
		String html = "";
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		// TODO ensure HttpsURLConnection.setDefaultSSLSocketFactory isn't
		// required to be reset like hostnames are
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false);
			conn.setRequestMethod("GET");
			setBasicAuthentication(conn, url);
			int status = conn.getResponseCode();
			if (status != 200) {
				Logd(TAG, "Failed to get from server, returned code: " + status);
				throw new IOException("Get failed with error code " + status);
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
			Logd(TAG, "Failed to get from server: " + e.getMessage());
		}
		if (conn != null) {
			conn.disconnect();
		}
		return html;
	}

	public static String getFileName(String extUrl) {
		URL url = null;
		String path = extUrl;
		try {
			url = new URL(extUrl);
			path = url.getPath();
		} catch (MalformedURLException e) {
			path = extUrl;
		}
		String filename = "";
		String[] pathContents = path.split("[\\\\/]");
		if (pathContents != null) {
			int pathContentsLength = pathContents.length;
			System.out.println("Path Contents Length: " + pathContentsLength);
			String lastPart = pathContents[pathContentsLength - 1];
			String[] lastPartContents = lastPart.split("\\.");
			if (lastPartContents != null && lastPartContents.length > 1) {
				int lastPartContentLength = lastPartContents.length;
				String name = "";
				for (int i = 0; i < lastPartContentLength; i++) {
					if (i < (lastPartContents.length - 1)) {
						name += lastPartContents[i];
						if (i < (lastPartContentLength - 2)) {
							name += ".";
						}
					}
				}
				String extension = lastPartContents[lastPartContentLength - 1];
				filename = name + "." + extension;
			}
		}
		return filename;
	}
	
	public static void checkStorageDir() {
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Android");
        if (!storageDir.exists())
        	storageDir.mkdir();		
        storageDir = new File(Environment.getExternalStorageDirectory() + "/Android/data");
        if (!storageDir.exists())
        	storageDir.mkdir();		
        storageDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/com.nowsci.odm");
        if (!storageDir.exists())
        	storageDir.mkdir();		
        storageDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/com.nowsci.odm/.storage");
        if (!storageDir.exists())
        	storageDir.mkdir();		
	}

	public static void DownloadFile(String u) {
		try {
			Logd(TAG, "Starting download of: " + u);
			URL url = new URL(u);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.connect();
			//File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			checkStorageDir();
	        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/com.nowsci.odm/.storage");
			File file = new File(storageDir, getFileName(u));
			Logd(TAG, "Storage directory: " + storageDir.toString());
			Logd(TAG, "File name: " + file.toString());
			FileOutputStream fileOutput = new FileOutputStream(file);
			InputStream inputStream = urlConnection.getInputStream();
			byte[] buffer = new byte[1024];
			int bufferLength = 0;
			while ((bufferLength = inputStream.read(buffer)) > 0) {
				fileOutput.write(buffer, 0, bufferLength);
			}
			fileOutput.close();
			Logd(TAG, "File written");
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
