package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.getVAR;
import static com.nowsci.odm.CommonUtilities.loadVARs;

import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;

public class LocationAlarm extends BroadcastReceiver {
	private static final String TAG= "ODMLocationAlarm";
	int interval = 0;
	Context alarmContext;
	Context globalContext;

	class BackgroundLocation extends AsyncTask<String, String, String> {

		@SuppressLint("Wakelock")
		@Override
		protected String doInBackground(String... arg0) {
			loadVARs(globalContext);
			PowerManager pm = (PowerManager) globalContext.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
			wl.acquire();
			Logd(TAG, "Location: Woken and checking.");
			Intent intent = new Intent("com.nowsci.odm.LocationService");
			if (getVAR("NETWORK_ONLY").equals("true"))
				intent.putExtra("message", "Command:GetLocationNetwork");
			else
				intent.putExtra("message", "Command:GetLocation");
			intent.putExtra("alarm", "true");
			globalContext.startService(intent);
			wl.release();
			return null;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		globalContext = context;
		loadVARs(globalContext);
		Logd(TAG, "Running location alarm.");
		interval = Integer.parseInt(getVAR("INTERVAL"));
		Logd(TAG, "Location interval: " + interval);
		ConnectionDetector cd = new ConnectionDetector(context);
		// Check if Internet present
		if (cd.isConnectingToInternet())
			new BackgroundLocation().execute();
	}

	public void SetAlarmContext(Context context) {
		alarmContext = context;
	}

	public void SetInterval(String i) {
		interval = Integer.parseInt(i);
	}

	public void SetAlarm(Context context) {
		alarmContext = context;
		loadVARs(alarmContext);
		Logd(TAG, "Setting location alarm.");
		CancelAlarm(alarmContext);
		Long time = new GregorianCalendar().getTimeInMillis() + 1000 * 60 * interval;
		AlarmManager am = (AlarmManager) alarmContext.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(alarmContext, LocationAlarm.class);
		PendingIntent pi = PendingIntent.getBroadcast(alarmContext, 99, i, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC_WAKEUP, time, 1000 * 60 * interval, pi); // Millisec * Second * Minute(interval)
	}

	public void CancelAlarm(Context context) {
		Logd(TAG, "Unsetting location alarm.");
		Intent i = new Intent(alarmContext, LocationAlarm.class);
		PendingIntent pi = PendingIntent.getBroadcast(alarmContext, 99, i, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) alarmContext.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pi);
	}
}