package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.setVAR;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class StartupActivity extends Activity {

	private static final String TAG = "StartupActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences mPrefs = getSharedPreferences("usersettings", 0);
		String su = mPrefs.getString("SERVER_URL", "");
		setVAR("SERVER_URL", su);
		setVAR("NAME", mPrefs.getString("NAME", ""));
		setVAR("USERNAME", mPrefs.getString("USERNAME", ""));
		setVAR("ENC_KEY", mPrefs.getString("ENC_KEY", ""));
		setVAR("REG_ID", mPrefs.getString("REG_ID", ""));
		setVAR("VALID_SSL", mPrefs.getString("VALID_SSL", ""));
		setVAR("DEBUG", mPrefs.getString("DEBUG", ""));
		
		// Eliminate FC's from bad URL in settings for previous users
		if (!su.equals("")) {
			Boolean cont = false;
			try {
				URL u = new URL(su);
				u.toURI();
				cont = true;
			} catch (MalformedURLException e) {
				Log.d(TAG, e.getMessage());
			} catch (URISyntaxException e) {
				Log.d(TAG, e.getMessage());
			}
			if (!cont) {
				setVAR("SERVER_URL", "");
				su = "";
			}
		}

		// Clears preference data for testing
		/*
		Editor editor = getApplicationContext().getSharedPreferences("usersettings", Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		*/

		Logd(TAG, "Getting admin permissions");
		DevicePolicyManager mDPM;
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName mDeviceAdmin;
		mDeviceAdmin = new ComponentName(this, GetAdminReceiver.class);
		if (mDPM.isAdminActive(mDeviceAdmin)) {
			Logd(TAG, "We have admin");
			if (su.equals("")) {
				Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
				startActivity(i);
			} else {
				Intent i = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(i);
			}
		} else {
			Logd(TAG, "We need admin");
			Intent intent = new Intent(getApplicationContext(), GetAdminActivity.class);
			this.startActivity(intent);
		}
		finish();
	}
}
