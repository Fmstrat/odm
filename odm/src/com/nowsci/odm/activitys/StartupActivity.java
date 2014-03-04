package com.nowsci.odm.activitys;


import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.nowsci.odm.GetAdminReceiver;
import com.nowsci.odm.misc.CommonUtilities;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class StartupActivity extends Activity {

	private static final String TAG = "StartupActivity";
	Boolean version_check = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String su = CommonUtilities.getVAR("SERVER_URL");
		if (CommonUtilities.getVAR("TOKEN").equals("")) {
			Log.e(TAG, "TOKEN is blank. You likely need to update the Web application and/or restart the ODM app to re-register.");
		}
		// Default to true to start version checks from here out
		if (CommonUtilities.getVAR("VERSION").equals("true") || CommonUtilities.getVAR("VERSION").equals(""))
			version_check = true;
		else
			version_check = false;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    version_check = extras.getBoolean("VERSION_CHECK", true);
		}
		
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
				CommonUtilities.setVAR("SERVER_URL", "");
				su = "";
			}
		}

		// Clears preference data for testing
		/*
		Editor editor = getApplicationContext().getSharedPreferences("usersettings", Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		*/

		CommonUtilities.Logd(TAG, "Getting admin permissions");
		DevicePolicyManager mDPM;
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName mDeviceAdmin;
		mDeviceAdmin = new ComponentName(this, GetAdminReceiver.class);
		if (mDPM.isAdminActive(mDeviceAdmin)) {
			CommonUtilities.Logd(TAG, "We have admin");
			if (su.equals("")) {
				Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
				startActivity(i);
			} else {
				Intent i = new Intent(getApplicationContext(), MainActivity.class);
				if (version_check)
					i.putExtra("VERSION_CHECK", version_check);
				startActivity(i);
			}
		} else {
			CommonUtilities.Logd(TAG, "We need admin");
			Intent intent = new Intent(getApplicationContext(), GetAdminActivity.class);
			this.startActivity(intent);
		}
		finish();
	}
}
