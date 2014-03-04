package com.nowsci.odm.misc;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.nowsci.odm.misc.CommonUtilities.Logd;

//http://www.codenet.ru/webmast/php/HTTP-POST.php
public class MultipartPost {

	private final String TAG = "MultipartPost";
	@SuppressWarnings("rawtypes")
	private List<PostParameter> params;
	private static final String CRLF = "\r\n";
	private static final String BOUNDARY = "**AaB03xZTSs**";

	@SuppressWarnings("rawtypes")
	public MultipartPost(List<PostParameter> params) {
		this.params = params;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String send(String urlString, boolean checkSSL) throws Exception {
	    
	    HttpsURLConnection conn = null;
	    DataOutputStream dos = null;
	    String response = null;
	    InputStream is = null;
	    HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
	    
	    if(checkSSL == false) {
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
			hostnameVerifier = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
	    }
	    
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
			conn = (HttpsURLConnection) new URL(urlString).openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
			dos = new DataOutputStream(conn.getOutputStream());
			
			for(PostParameter param : params) {
				Logd(TAG, "Processing param: " + param.getParamName());
				if(param.getValue() == null) {
				    param.setValue("");
				}
				if(param.getValue().getClass() == File.class) {
					postFileParameter(dos, param.getParamName(), (File) param.getValue(), param.getContentType());
				} 
				else {
					postStringParameter(dos, param.getParamName(), param.getValue().toString());
				}
			}

			dos.writeBytes(closeBoundary());
			dos.flush();

			is = conn.getInputStream();
			int ch;

			StringBuffer b = new StringBuffer();
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
			response = b.toString();
		} 
		finally {
		    if(dos != null) try { dos.close(); } catch(IOException ioe) { /* that's it */ }
		    if(is  != null) try { is .close(); } catch(IOException ioe) { /* that's it */ }
		}
		
		return response;
	}

	private void postStringParameter(DataOutputStream dos, String paramName, String paramValue) throws IOException {
		dos.writeBytes(boundary() + CRLF);
		dos.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\"" + CRLF + CRLF);
		dos.writeBytes(paramValue + CRLF);
	}

	private void postFileParameter(DataOutputStream dos, String paramName, File file, String contentType) throws IOException {
		dos.writeBytes(boundary() + CRLF);
		dos.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + file.getName() + "\"" + CRLF);
		dos.writeBytes("Content-Type: "+ contentType + CRLF);
		dos.writeBytes("Content-Transfer-Encoding: binary" + CRLF);
		dos.writeBytes(CRLF);

		FileInputStream fileInputStream = new FileInputStream(file);
		int bytesAvailable = fileInputStream.available();
		int maxBufferSize = 1024;
		int bufferSize = Math.min(bytesAvailable, maxBufferSize);
		byte[] buffer = new byte[bufferSize];

		int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

		while (bytesRead > 0) {
			dos.write(buffer, 0, bufferSize);
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = null;
			buffer = new byte[bufferSize];
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		}
		dos.writeBytes(CRLF);
		dos.flush();
		fileInputStream.close();
		System.gc();
	}


	private String closeBoundary() {
		return boundary() + "--" + CRLF;
	}

	private String boundary() {
		return "--" + BOUNDARY;
	}

}