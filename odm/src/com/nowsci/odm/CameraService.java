package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;

import com.nowsci.odm.CameraCapture;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Display;
import android.view.WindowManager;

public class CameraService extends Service {
	private static final String TAG= "ODMCameraService";

	int cameraInt = 0;
	Context context;
	Boolean max = false;
	int volume = 0;
	AudioManager am;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logd(TAG, "Starting camera service...");
		String message = intent.getStringExtra("message");
		context = getApplicationContext();
		if (message.equals("Command:FrontPhoto") || message.equals("Command:FrontPhotoMAX"))
			cameraInt = 1;
		if (message.equals("Command:RearPhotoMAX") || message.equals("Command:FrontPhotoMAX"))
			max = true;
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				captureImage();
			}
		}, 2000);
		am = (AudioManager) getApplicationContext().getSystemService("audio");
		volume = am.getStreamVolume(1);
		Logd(TAG, "Current volume: " + volume);
		//if (Build.VERSION.SDK_INT < 14)
			am.setStreamVolume(1, 0, 0);
		return START_STICKY;
	}

	private void captureImage() {
		Logd(TAG, "About to capture image...");
		final CameraCapture cc = new CameraCapture();
		cc.setMax(max);
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
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Logd(TAG, "Capturing image...");
				cc.captureImage();
			}
		}, 2000);
	}

	public void stopCamera() {
		Logd(TAG, "Stopping camera service.");
		//if (Build.VERSION.SDK_INT < 14)
			am.setStreamVolume(1, volume, 0);
		this.stopSelf();
	}
}
