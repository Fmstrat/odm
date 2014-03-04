package com.nowsci.odm;

import static com.nowsci.odm.misc.CommonUtilities.Logd;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.view.Display;
import android.view.WindowManager;

public class CameraService extends Service {
	private static final String TAG = "CameraService";

	int cameraInt = 0;
	Context context;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logd(TAG, "Starting camera service...");
		String message = intent.getStringExtra("message");
		final String requestid = intent.getStringExtra("requestid");
		context = getApplicationContext();
		if (message.equals("Command:FrontPhoto"))
			cameraInt = 1;
		Handler handler = new Handler();
		
		class CustomStarterRunnable implements Runnable {
			String messageid;
			
			public CustomStarterRunnable(String messageid) {
				this.messageid = messageid;
			}
			
			@Override
			public void run() {
				captureImage(this.messageid);
				
			}
		}
		
		handler.postDelayed(new CustomStarterRunnable(requestid), 2000);
		return START_STICKY;
	}

	private void captureImage(String requestid) {
		Logd(TAG, "About to capture image...");
		final CameraCapture cc = new CameraCapture();
		Logd(TAG, "Setting camera...");
		cc.setCamera(cameraInt);
	    WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE); 
	    Display display = window.getDefaultDisplay();
		Logd(TAG, "Setting display...");
		cc.setDisplay(display);
		Logd(TAG, "Creating container...");
		cc.setServiceContainer(this);
		Logd(TAG, "Starting window manager...");
		cc.startWindowManager();
		Handler handler = new Handler();
		
		class CustomCaptureRunnable implements Runnable {
			String messageid;
			
			public CustomCaptureRunnable(String messageid) {
				this.messageid = messageid;
			}
			
			@Override
			public void run() {
				Logd(TAG, "Capturing image...");
				cc.captureImage(this.messageid);
			}
		}
		
		handler.postDelayed(new CustomCaptureRunnable(requestid), 2000);
	}
	
	public void stopCamera() {
		Logd(TAG, "Stopping camera service.");
		this.stopSelf();
	}
}
