package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.getVAR;
import static com.nowsci.odm.CommonUtilities.loadVARs;
import static com.nowsci.odm.CommonUtilities.getFileName;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;

public class FileService extends Service {
	private static final String TAG= "ODMFileService";
	Context context;
	Boolean stopservice = false;
	String file = "";
	String message = "";
	boolean filenameonly = false;
	boolean delete = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();
		loadVARs(context);
		if (intent.getStringExtra("filenameonly") != null)
			if (intent.getStringExtra("filenameonly").equals("true"))
				filenameonly = true;
		if (intent.getStringExtra("delete") != null)
			if (intent.getStringExtra("delete").equals("true"))
				delete = true;
		if (intent.getStringExtra("message") != null) {
			message = intent.getStringExtra("message");
			if (message.startsWith("Command:SendFile:")) {
				file = intent.getStringExtra("message").replaceFirst("Command:SendFile:", "");
				Logd(TAG,"Sending file...");
				new SendFileBackground().execute();
			} else {
				file = intent.getStringExtra("message").replaceFirst("Command:GetFile:", "");				
				Logd(TAG,"Getting file...");
				new GetFileBackground().execute();
			}
		}
		stopShell();
		return START_STICKY;
	}

	public void stopShell() {
		if (stopservice) {
			this.stopSelf();
		} else {
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					stopShell();
				}
			}, 2000);
		}
	}

	class SendFileBackground extends AsyncTask<String, String, String> {

		@SuppressWarnings("deprecation")
		@Override
		protected String doInBackground(String... arg0) {
			try {
				File dfile = new File(file);
				byte[] data = new byte[(int) dfile.length()];
				DataInputStream dis = new DataInputStream(new FileInputStream(dfile));
				dis.readFully(data);
				dis.close();
				Map<String, String> postparams = new HashMap<String, String>();
				postparams.put("regId", getVAR("REG_ID"));
				postparams.put("username", getVAR("USERNAME"));
				postparams.put("password", getVAR("ENC_KEY"));
				if (file.endsWith(".jpg") || file.endsWith(".JPG") || file.endsWith(".gif") || file.endsWith(".GIF")) {
					postparams.put("message", "img:" + file);
				} else if (file.endsWith(".mp4")) {
					if (filenameonly)
						postparams.put("message", "vid:" + getFileName(file));
					else
						postparams.put("message", "vid:" + file);
				} else if (file.endsWith(".3gpp") || file.endsWith(".wav") || file.endsWith(".mp3")) {
					if (filenameonly)
						postparams.put("message", "aud:" + getFileName(file));
					else
						postparams.put("message", "aud:" + file);
				} else {
					postparams.put("message", "file:" + file);
				}
				CommonUtilities.post(getVAR("SERVER_URL") + "file.php", postparams, file);
			} catch (IOException ex) {
				Logd(TAG, "Error: " + ex.getMessage());
			}
			/*
			if (delete) {
		        File outFile = new File(file);
		        if (outFile.exists()) {
		            outFile.delete();
		        }
			}
			*/
			stopservice = true;
			return null;
		}
	}
	
	class GetFileBackground extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			CommonUtilities.DownloadFile(file);
			stopservice = true;
			return null;
		}
	}

}
