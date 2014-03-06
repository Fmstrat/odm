package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.loadVARs;
import static com.nowsci.odm.CommonUtilities.getVAR;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver {
	private static final String TAG= "ODMAutoStart";
	UpdateAlarm updateAlarm = new UpdateAlarm();
	LocationAlarm locationAlarm = new LocationAlarm();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			loadVARs(context);
			String interval = getVAR("INTERVAL");
			String version = getVAR("VERSION");
			if (!interval.equals("0")) {
				Logd(TAG, "Good to start location alarm.");
				locationAlarm.SetInterval(interval);
				locationAlarm.SetAlarm(context);
			}
			if (version.equals("true")) {
				Logd(TAG, "Good to start update alarm.");
				updateAlarm.SetAlarm(context);
			}
		}
	}
}
