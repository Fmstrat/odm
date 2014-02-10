package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import java.io.IOException;
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
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

public class UpdateAlarm extends BroadcastReceiver {
	private static final String TAG = "UpdateAlarm";
	Boolean version_check = false;
	Context globalContext;

	class BackgroundUpdate extends AsyncTask<String, String, String> {

		@SuppressLint("Wakelock")
		@Override
		protected String doInBackground(String... arg0) {
			PowerManager pm = (PowerManager) globalContext.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
			wl.acquire();
			Log.d(TAG, "Woken and checking.");
			String html = "";
			// Check APK version
			int versionCode = 0;
			try {
				versionCode = globalContext.getPackageManager().getPackageInfo(globalContext.getPackageName(), 0).versionCode;
			} catch (NameNotFoundException e) {
				Log.e(TAG, "Error: " + e.getMessage());
			}
			Log.d(TAG, "versionCode: " + versionCode);
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
					Log.d(TAG, "Match: " + matcher.group());
					if (!vc.equals(matcher.group())) {
						// There is a new version
						Log.d(TAG, "New version found.");
						generateNotification(globalContext, "ODM update available. Tap to download.", "APK", 0);
					} else {
						Log.d(TAG, "No new version found.");
					}
				}
			}
			// Check web version
			String curWebVersion = "0";
			String newWebVersion = "0";
			SharedPreferences mPrefs = globalContext.getSharedPreferences("usersettings", 0);
			String su = mPrefs.getString("SERVER_URL", "");
			Map<String, String> postparams = new HashMap<String, String>();
			postparams.put("username", mPrefs.getString("USERNAME", ""));
			postparams.put("password", mPrefs.getString("ENC_KEY", ""));
			if (!su.equals("")) {
				html = "";
				try {
					html = CommonUtilities.post(su + "version.php", postparams);
				} catch (IOException e) {
					Log.e(TAG, "Error: " + e.getMessage());
				}
				if (!html.equals("")) {
					try {
						curWebVersion = html;
					} finally { }
				}
			}
			Log.d(TAG,"Cur Web Verison: " + curWebVersion);
			html = "";
			try {
				html = CommonUtilities.get("https://raw.github.com/Fmstrat/odm-web/master/odm/include/version.php");
			} catch (IOException e) {
				Log.e(TAG, "Error: " + e.getMessage());
			}
			if (!html.equals("")) {
				String result = html.substring(html.indexOf("= ")+2, html.indexOf("; ?>"));
				newWebVersion = result;
			}
			Log.d(TAG,"New Web Verison: " + newWebVersion);
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
		Log.d(TAG, "Running update alarm.");
		SharedPreferences mPrefs = context.getSharedPreferences("usersettings", 0);
		if (mPrefs.getString("VERSION", "true").equals("true"))
			version_check = true;
		else
			version_check = false;
		Log.d(TAG, "Check for new version: " + version_check);
		if (version_check) {
			new BackgroundUpdate().execute();
		}
	}

	public void SetAlarm(Context context) {
		Logd(TAG, "Setting update alarm.");
		CancelAlarm(context);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, UpdateAlarm.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, Intent.FILL_IN_DATA);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60 * 24 * 7, pi); // Millisec * Second * Minute * Hour * Days
		//am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 30, pi); // Millisec * Second * Minute * Hour * Days
	}

	public void CancelAlarm(Context context) {
		Logd(TAG, "Unsetting update alarm.");
		Intent intent = new Intent(context, UpdateAlarm.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
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