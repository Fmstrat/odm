package com.nowsci.odm;

import static com.nowsci.odm.misc.CommonUtilities.Logd;
import static com.nowsci.odm.misc.CommonUtilities.getVAR;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.nowsci.odm.GetLocation.LocationResult;
import com.nowsci.odm.misc.ApiProtocolHandler;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

public class LocationService extends Service {
	private static final String TAG = "LocationService";
	Context context;
	Boolean gpsonly = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();
		String message = intent.getStringExtra("message");
		String requestId = intent.getStringExtra("requestid");
		context = getApplicationContext();
		if (message.equals("Command:GetLocationGPS")) {
			gpsonly = true;
		}
		
		LocationResult locationResult = new CustomLocationResult(requestId);
		GetLocation myLocation = new GetLocation();
		myLocation.getLocation(context, locationResult, gpsonly);
		return START_STICKY;
	}

	public void stopLocation() {
		this.stopSelf();
	}
	
	class CustomLocationResult extends LocationResult {
		String requestID;
		
		public CustomLocationResult(String requestID) {
			super();
			this.requestID = requestID;
		}
		
		
		@Override
		public void gotLocation(Location location) {
			//Got the location! Send to server
			if (location != null) {
				String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN).format(Calendar.getInstance().getTime());
				Logd(TAG, "Got location for reqID: " + this.requestID);
				ApiProtocolHandler.apiMessageLocation(getVAR("REG_ID"), this.requestID, String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude()), location.getProvider(), timestamp, String.valueOf(location.getAltitude()), String.valueOf(location.getAccuracy()));
			} else {
				Logd(TAG, "No location provided by device.");
			}
			stopLocation();
		}
	}
}
