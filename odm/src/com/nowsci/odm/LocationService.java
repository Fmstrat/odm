package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.getVAR;
import static com.nowsci.odm.CommonUtilities.loadVARs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.nowsci.odm.GetLocation.LocationResult;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;

public class LocationService extends Service {
	private static final String TAG= "ODMLocationService";
	Context context;
	String locationType = "";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();
		Logd(TAG, "Location service started.");
		loadVARs(context);
		String message = "";
		if (intent.getStringExtra("message") != null) {
			message = intent.getStringExtra("message");
			Logd(TAG, "Message: " + message);
			if (message.equals("Command:GetLocationGPS"))
				locationType = "gpsonly";
			if (message.equals("Command:GetLocationNetwork"))
				locationType = "networkonly";
		}
		if (intent.getStringExtra("alarm") != null) {
			// To overcome a context reference bug, we need to recheck if it's an alarm.
			if (getVAR("NETWORK_ONLY").equals("true")) {
				locationType = "networkonly";
			} else {
				locationType = "";
			}
		}
		LocationResult locationResult = new LocationResult() {
			@Override
			public void gotLocation(Location location) {
				//Got the location! Send to server
				if (location != null) {
					String notification = "Location: " + location.getLatitude() + " " + location.getLongitude();
					AsyncTask<String, Void, Void> postTask;
					postTask = new AsyncTask<String, Void, Void>() {
						@Override
						protected Void doInBackground(String... params) {
							String notification = params[0];
							Map<String, String> postparams = new HashMap<String, String>();
							loadVARs(getApplicationContext());
							postparams.put("regId", getVAR("REG_ID"));
							postparams.put("username", getVAR("USERNAME"));
							postparams.put("password", getVAR("ENC_KEY"));

							postparams.put("message", notification);
							CommonUtilities.post(getVAR("SERVER_URL") + "message.php", postparams);
							return null;
						}
					};
					postTask.execute(notification, null, null);
				} else {
					Logd(TAG, "No location provided by device.");
				}
				stopLocation();
			}
		};
		GetLocation myLocation = new GetLocation();
		Logd(TAG, "Location type: " + locationType);
		myLocation.getLocation(context, locationResult, locationType);
		return START_STICKY;
	}

	public void stopLocation() {
		this.stopSelf();
	}
}
