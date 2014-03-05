package com.nowsci.odm.misc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.nowsci.odm.OdmApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

public final class CommonUtilities extends Activity {

	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "CommonUtilities";
	static final String GOOGLE_PROJECT_ID = "590633583092";

	/**
	 * Set this to true to enable all input fields and settings
	 */
	public static boolean APP_ALL_OPTIONS = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	static public String getVAR(String s) {		
		SharedPreferences mPrefs = OdmApplication.getAppContext().getSharedPreferences("usersettings", 0);
		
		if(s.equals("SENDER_ID") && mPrefs.getString(s, "").equals("")) {
			SharedPreferences.Editor mEditor = mPrefs.edit();			
			mEditor.putString(s, GOOGLE_PROJECT_ID).commit();
			return GOOGLE_PROJECT_ID;
		}
		
		return mPrefs.getString(s, "");
	}

	static public void setVAR(String s, String v) {
		SharedPreferences mPrefs = OdmApplication.getAppContext().getSharedPreferences("usersettings", 0);
		SharedPreferences.Editor mEditor = mPrefs.edit();
		
		mEditor.putString(s, v).commit();
	}

	static public void Logd(String inTAG, String message) {
		if (getVAR("DEBUG").equals("true"))
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
	static public void displayMessage(Context context, String message) {
		Intent intent = new Intent("com.nowsci.odm.DISPLAY_MESSAGE");
		intent.putExtra("message", message);
		context.sendBroadcast(intent);
	}
	
	static public void changeStatusIcon(Context context, int iconResID) {
		Intent intent = new Intent("com.nowsci.odm.STATUS_CHANGED");
		intent.putExtra("statusImageID", iconResID);
		context.sendBroadcast(intent);
	}
	
	public static String SHA256 (String text) throws NoSuchAlgorithmException {

	    MessageDigest md = MessageDigest.getInstance("SHA-256");

	    md.update(text.getBytes());
	    byte[] digest = md.digest();

	    return Base64.encodeToString(digest, Base64.DEFAULT);
	}
}
