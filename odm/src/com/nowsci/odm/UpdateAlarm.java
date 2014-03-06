package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.getVAR;
import static com.nowsci.odm.CommonUtilities.loadVARs;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

public class UpdateAlarm extends BroadcastReceiver {
	private static final String TAG= "ODMUpdateAlarm";
	Boolean version_check = false;
	Context alarmContext;
	Context globalContext;

	class BackgroundUpdate extends AsyncTask<String, String, String> {

		@SuppressLint("Wakelock")
		@Override
		protected String doInBackground(String... arg0) {
			loadVARs(globalContext);
			PowerManager pm = (PowerManager) globalContext.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
			wl.acquire();
			Logd(TAG, "Woken and checking.");
			String html = "";
			// Check APK version
			int versionCode = 0;
			try {
				versionCode = globalContext.getPackageManager().getPackageInfo(globalContext.getPackageName(), 0).versionCode;
			} catch (NameNotFoundException e) {
				Log.e(TAG, "Error: " + e.getMessage());
			}
			Logd(TAG, "versionCode: " + versionCode);
			String vc = "android:versionCode=\"" + versionCode + "\"";
			try {
				html = CommonUtilities.get("https://raw.github.com/Fmstrat/odm/master/odm/AndroidManifest.xml");
			} catch (IOException e) {
				Log.e(TAG, "Error: " + e.getMessage());
			}
			if (!html.equals("")) {
				Pattern pattern = Pattern.compile("android:versionCode=\"[^\"]\"");
				Matcher matcher = pattern.matcher(html);
				while (matcher.find()) {
					Logd(TAG, "Match: " + matcher.group());
					if (!vc.equals(matcher.group())) {
						// There is a new version
						Logd(TAG, "New version found.");
						generateNotification(globalContext, "ODM update available. Tap to download.", "APK", 0);
					} else {
						Logd(TAG, "No new version found.");
					}
				}
			}
			// Check web version
			String curWebVersion = "0";
			String newWebVersion = "0";
			String su = getVAR("SERVER_URL");
			Map<String, String> postparams = new HashMap<String, String>();
			postparams.put("username", getVAR("USERNAME"));
			postparams.put("password", getVAR("ENC_KEY"));
			if (!su.equals("")) {
				html = "";
				html = CommonUtilities.post(su + "version.php", postparams);
				if (!html.equals("")) {
					try {
						curWebVersion = html;
					} finally {
					}
				}
			}
			Logd(TAG, "Cur Web Verison: " + curWebVersion);
			html = "";
			try {
				html = CommonUtilities.get("https://raw.github.com/Fmstrat/odm-web/master/odm/include/version.php");
			} catch (IOException e) {
				Log.e(TAG, "Error: " + e.getMessage());
			}
			if (!html.equals("")) {
				String result = html.substring(html.indexOf("= ") + 2, html.indexOf("; ?>"));
				newWebVersion = result;
			}
			Logd(TAG, "New Web Verison: " + newWebVersion);
			if (!newWebVersion.equals(curWebVersion)) {
				generateNotification(globalContext, "Your ODM-Web is out of date. Tap to view.", "WEB", 1);
			}
			wl.release();
			return null;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		globalContext = context;
		loadVARs(globalContext);
		Logd(TAG, "Running update alarm.");
		ConnectionDetector cd = new ConnectionDetector(context);
		// Check if Internet present
		if (cd.isConnectingToInternet())
			new BackgroundUpdate().execute();
	}

	public void SetAlarmContext(Context context) {
		alarmContext = context;
	}

	public void SetAlarm(Context context) {
		alarmContext = context;
		loadVARs(alarmContext);
		Logd(TAG, "Setting update alarm.");
		CancelAlarm(alarmContext);
		Long time = new GregorianCalendar().getTimeInMillis() + 1000 * 60 * 60 * 24 * 7;
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(alarmContext, UpdateAlarm.class);
		PendingIntent pi = PendingIntent.getBroadcast(alarmContext, 98, i, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC_WAKEUP, time, 1000 * 60 * 60 * 24 * 7, pi); // Millisec * Second * Minute * Hour * Days
		//am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000*60, 1000 * 60, pi); // Millisec * Second * Minute * Hour * Days
	}

	public void CancelAlarm(Context context) {
		Logd(TAG, "Unsetting update alarm.");
		Intent i = new Intent(alarmContext, UpdateAlarm.class);
		PendingIntent pi = PendingIntent.getBroadcast(alarmContext, 98, i, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) alarmContext.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pi);
	}

	@SuppressWarnings("deprecation")
	private static void generateNotification(Context context, String message, String type, int activity_num) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);
		String title = context.getString(R.string.app_name);
		Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
		if (type.equals("ODM"))
			notificationIntent.setData(Uri.parse("https://github.com/Fmstrat/odm/raw/master/latest/odm.apk"));
		else
			notificationIntent.setData(Uri.parse("https://github.com/Fmstrat/odm-web"));
		PendingIntent intent = PendingIntent.getActivity(context, activity_num, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(activity_num, notification);
	}
}