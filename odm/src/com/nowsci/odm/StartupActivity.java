package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.setVAR;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class StartupActivity extends Activity {

	private static final String TAG = "StartupActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences mPrefs = getSharedPreferences("usersettings", 0);
		setVAR("SERVER_URL", mPrefs.getString("SERVER_URL", ""));
		setVAR("NAME", mPrefs.getString("NAME", ""));
		setVAR("USERNAME", mPrefs.getString("USERNAME", ""));
		setVAR("ENC_KEY", mPrefs.getString("ENC_KEY", ""));
		setVAR("REG_ID", mPrefs.getString("REG_ID", ""));
		setVAR("VALID_SSL", mPrefs.getString("VALID_SSL", ""));
		setVAR("DEBUG", mPrefs.getString("DEBUG", ""));

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
			if (mPrefs.getString("SERVER_URL", "") == "") {
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
