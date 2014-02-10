package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.setVAR;
import static com.nowsci.odm.CommonUtilities.getVAR;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsManager;
import android.util.Log;

public class HelperIntentService extends IntentService {
	private static final String TAG = "HelperIntentService";
	Context context;

	public HelperIntentService() {
		super("HelperIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		context = getApplicationContext();
		Bundle extras = intent.getExtras();
		String msg = intent.getStringExtra("message");
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				Logd(TAG, "Error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				Logd(TAG, "Deleted messages on server: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				// If it's a regular GCM message, do some work.
				try {
					SharedPreferences mPrefs = getSharedPreferences("usersettings", 0);
					setVAR("SERVER_URL", mPrefs.getString("SERVER_URL", ""));
					setVAR("NAME", mPrefs.getString("NAME", ""));
					setVAR("USERNAME", mPrefs.getString("USERNAME", ""));
					setVAR("ENC_KEY", mPrefs.getString("ENC_KEY", ""));
					setVAR("REG_ID", mPrefs.getString("REG_ID", ""));
					setVAR("VALID_SSL", mPrefs.getString("VALID_SSL", ""));
					setVAR("DEBUG", mPrefs.getString("DEBUG", ""));
					setVAR("TOKEN", mPrefs.getString("TOKEN", ""));
					if (getVAR("TOKEN").equals("")) {
						Log.e(TAG, "TOKEN is blank. You likely need to update the Web application and/or restart the ODM app to re-register.");
					}
					setVAR("VERSION", mPrefs.getString("VERSION", ""));
					MCrypt mcrypt = new MCrypt();
					String decrypted = new String(mcrypt.decrypt(msg));
					Logd(TAG, "Received message: " + decrypted);
					handleMessage(decrypted);
				} catch (Exception e) {
					Logd(TAG, "Error: " + e.getMessage());
				}
			}
		}
		WakefulBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void handleMessage(String message) {
		// Waking up mobile if it is sleeping
		//WakeLocker.acquire(getApplicationContext());
		if (message.startsWith("Command:Notify:")) {
			String notification = message.replaceFirst("Command:Notify:", "");
			generateNotification(context, notification);
		} else if (message.startsWith("Command:SMS:")) {
			String destinationAddress = message.replaceFirst("Command:SMS:", "");
			SmsManager sms = SmsManager.getDefault();
			try {
				sms.sendTextMessage(destinationAddress, null, "This number currently has this device.", null, null);
				Logd(TAG, "Sent SMS");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		} else if (message.equals("Command:Wipe")) {
			DevicePolicyManager mDPM;
			mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
			mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
		} else if (message.equals("Command:Lock")) {
			DevicePolicyManager mDPM;
			mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
			ComponentName mDeviceAdmin;
			mDeviceAdmin = new ComponentName(context, GetAdminReceiver.class);
			if (mDPM.isAdminActive(mDeviceAdmin)) {
				Logd(TAG, "Locking device");
				mDPM.lockNow();
			}
		} else if (message.startsWith("Command:LockPass:")) {
			String password = message.replaceFirst("Command:LockPass:", "");
			DevicePolicyManager mDPM;
			mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
			ComponentName mDeviceAdmin;
			mDeviceAdmin = new ComponentName(context, GetAdminReceiver.class);
			if (mDPM.isAdminActive(mDeviceAdmin)) {
				Logd(TAG, "Locking device with password");
				mDPM.setPasswordQuality(mDeviceAdmin,DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
				mDPM.setPasswordMinimumLength(mDeviceAdmin, 4);
				mDPM.resetPassword(password, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
				mDPM.lockNow();
			}
		} else if (message.equals("Command:StartRing")) {
			Logd(TAG, "About to start ringer service.");
			Intent intent = new Intent("com.nowsci.odm.AudioService");
			context.startService(intent);
		} else if (message.equals("Command:StopRing")) {
			Logd(TAG, "About to stop ringer service.");
			Intent intent = new Intent("com.nowsci.odm.AudioService");
			context.stopService(intent);
		} else if (message.equals("Command:FrontPhoto") || message.equals("Command:RearPhoto")) {
			Logd(TAG, "About to start camera service.");
			Intent intent = new Intent("com.nowsci.odm.CameraService");
			intent.putExtra("message", message);
			context.startService(intent);
		} else if (message.equals("Command:GetLocation") || message.equals("Command:GetLocationGPS")) {
			Logd(TAG, "About to start location service.");
			Intent intent = new Intent("com.nowsci.odm.LocationService");
			intent.putExtra("message", message);
			context.startService(intent);
		}
		// Releasing wake lock
		//WakeLocker.release();
	}

	// Issues a notification to inform the user that server has sent a message.
	@SuppressWarnings("deprecation")
	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);
		String title = context.getString(R.string.app_name);
		Intent notificationIntent = new Intent(context, MainActivity.class);
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// Play default notification sound
		// notification.defaults |= Notification.DEFAULT_SOUND;
		// notification.sound = Uri.parse("android.resource://" +
		// context.getPackageName() + "your_sound_file_name.mp3");
		// Vibrate if vibrate is enabled
		// notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);
	}
}
