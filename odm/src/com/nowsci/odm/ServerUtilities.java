package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.displayMessage;
import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.getVAR;
import static com.nowsci.odm.CommonUtilities.setVAR;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public final class ServerUtilities {
	private static final String TAG = "ServerUtilities";

	/**
	 * Register this account/device pair within the server.
	 * 
	 */
	static void register(final Context context, final String regId) {
		AsyncTask<String, Void, Void> postTask;
		postTask = new AsyncTask<String, Void, Void>() {
			@Override
			protected Void doInBackground(String... params) {
				Logd(TAG, "About to register device");
				String regId = params[0];
				Logd(TAG, "Registering device (regId = " + regId + ")");
				String serverUrl = getVAR("SERVER_URL") + "register.php";
				Map<String, String> postparams = new HashMap<String, String>();
				postparams.put("regId", regId);
				postparams.put("name", getVAR("NAME"));
				postparams.put("username", getVAR("USERNAME"));
				postparams.put("password", getVAR("ENC_KEY"));
				try {
					displayMessage(context, "Attempting to register.");
					String html = CommonUtilities.post(serverUrl, postparams);
					if (html.startsWith("success:")) {
						String token = html.replaceFirst("success:", "");
						setVAR("TOKEN", token);
						SharedPreferences mPrefs = context.getSharedPreferences("usersettings", 0);
						SharedPreferences.Editor mEditor = mPrefs.edit();
						mEditor.putString("TOKEN", token).commit();
						displayMessage(context, "Server successfully registered device.");
					} else {
						displayMessage(context, "Server registration failed, check your settings.");
						Logd(TAG, "Server registration failed with: " + html);
					}
					return null;
				} catch (IOException e) {
					Log.e(TAG, "Failed to register: " + e);
				}
				displayMessage(context, "Failed to register device.");
				return null;
			}
		};
		postTask.execute(regId, null, null);
	}
}