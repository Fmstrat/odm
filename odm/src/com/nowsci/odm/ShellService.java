package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.getVAR;
import static com.nowsci.odm.CommonUtilities.loadVARs;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;

public class ShellService extends Service {
	private static final String TAG= "ODMShellService";
	Context context;
	Boolean stopservice = false;
	String incmd = "ls";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();
		loadVARs(context);
		if (intent.getStringExtra("message") != null) {
			incmd = intent.getStringExtra("message").replaceFirst("Command:ShellCmd:", "");
		}
		new ShellBackground().execute();
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

	class ShellBackground extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			try {
				Logd(TAG, "Running command.");
				String[] cmd = { "/system/bin/sh", "-c", incmd };
				String output = "";
				String error = "";
				Process proc = Runtime.getRuntime().exec(cmd);
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				// read the output from the command
				String s = null;
				while ((s = stdInput.readLine()) != null) {
					output = output + "\n" + s;
				}
				Logd(TAG, "Output: " + output);
				// read any errors from the attempted command
				while ((s = stdError.readLine()) != null) {
					error = error + "\n" + s;
				}
				Logd(TAG, "Error: " + error);
				JSONObject json = new JSONObject();
				try {
					json.put("output", output);
					json.put("error", error);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				String notification = "shell:" + incmd;
				Map<String, String> postparams = new HashMap<String, String>();
				loadVARs(getApplicationContext());
				postparams.put("regId", getVAR("REG_ID"));
				postparams.put("username", getVAR("USERNAME"));
				postparams.put("password", getVAR("ENC_KEY"));
				postparams.put("message", notification);
				postparams.put("data", json.toString());
				CommonUtilities.post(getVAR("SERVER_URL") + "message.php", postparams);
			} catch (IOException ex) {
				Logd(TAG, "Error :" + ex.getMessage());
			}
			stopservice = true;
			return null;
		}
	}

}
