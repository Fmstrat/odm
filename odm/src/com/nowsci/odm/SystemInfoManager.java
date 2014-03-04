package com.nowsci.odm;

import static com.nowsci.odm.misc.CommonUtilities.Logd;
import static com.nowsci.odm.misc.CommonUtilities.getVAR;

import com.nowsci.odm.misc.ApiProtocolHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;

public class SystemInfoManager {
	private static final String TAG = "SystemInfoManager";
	
	private Context context;

	private String osVersion = "";
	private int osApiLevel = 0;
	private String device = "";
	private String model = "";
	private String product = "";
	private int batteryLevel = 0;
	private String deviceID = "";
	private String phoneNr = "";
	
	public SystemInfoManager(Context ctx) {
		this.context = ctx;
	}

	// update battery stats if changed
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctxt, Intent intent) {
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			Logd(TAG, "Received battery level: " + level);
			batteryLevel = level;
		}
	};

	private int getBatteryLevel() {
		Intent batteryIntent = context.registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		return batteryIntent.getIntExtra("level", -1);
	}

	public void initSystemInfo() {
		osVersion = System.getProperty("os.version");
		osApiLevel = android.os.Build.VERSION.SDK_INT;
		device = android.os.Build.DEVICE;
		model = android.os.Build.MODEL;
		product = android.os.Build.PRODUCT;
		batteryLevel = getBatteryLevel();
		
		TelephonyManager phoneManager = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		phoneNr = phoneManager.getLine1Number();
		
		// do we need this? if not - comment the
		// android.permission.READ_PHONE_STATE in the manifest
		try {
			TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			deviceID = telephonyManager.getDeviceId();
		} catch(Exception e) {
			Logd(TAG, "Device ID exception: " + e.getMessage());
			deviceID = "unknown";
		}
	}

	public void sendSystemInfo(String requestID) {
		Logd(TAG, "Sending system info to server.");
		
		ApiProtocolHandler.apiMessageSysinfo(getVAR("REG_ID"),requestID, osVersion, Integer.toString(osApiLevel), device, model, product, Integer.toString(batteryLevel), deviceID, phoneNr);
	}
}
