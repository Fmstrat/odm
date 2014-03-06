package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;

import java.io.File;
import java.io.IOException;

import com.nowsci.odm.CameraCapture;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.view.Display;
import android.view.WindowManager;

public class VideoService extends Service {
	private static final String TAG= "ODMVideoService";

	int cameraInt = 0;
	Context context;
	Boolean max = false;
	int volume = 0;
	AudioManager am;
	String filepath = "";
	int seconds = 15;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logd(TAG, "Starting video service...");
		String message = intent.getStringExtra("message");
		context = getApplicationContext();
		if (message.startsWith("Command:FrontVideo:") || message.startsWith("Command:FrontVideoMAX:"))
			cameraInt = 1;
		if (message.startsWith("Command:RearVideoMAX:") || message.startsWith("Command:FrontVideoMAX:"))
			max = true;
		String s = message.replace("Command:FrontVideo:", "").replace("Command:FrontVideoMAX:", "").replace("Command:RearVideo:", "").replace("Command:RearVideoMAX:", "");
		seconds = Integer.parseInt(s);
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
		Logd(TAG, "About to capture video...");
		final VideoCapture vc = new VideoCapture();
		vc.setMax(max);
		Logd(TAG, "Setting camera...");
		vc.setCamera(cameraInt);
		Logd(TAG, "Setting length...");
		vc.setSeconds(seconds);
		WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = window.getDefaultDisplay();
		Logd(TAG, "Setting display...");
		vc.setDisplay(display);
		Logd(TAG, "Getting filename...");
		filepath = vc.setFilePath();
		Logd(TAG, "Creating container...");
		vc.setServiceContainer(this);
		Logd(TAG, "Starting window manager...");
		vc.startWindowManager();
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Logd(TAG, "Capturing video...");
				vc.captureVideo();
			}
		}, 2000);
	}
	
	class MyFileObserver extends FileObserver {

	    public MyFileObserver (String path, int mask) {
	        super(path, mask);
	    }

	    public void onEvent(int event, String path) {

	    }
	}
	
	public void stopAll() {
		Logd(TAG, "Stopping video service.");
		am.setStreamVolume(1, volume, 0);
		Logd(TAG, "Sending " + filepath);
		Intent intent = new Intent("com.nowsci.odm.FileService");
		intent.putExtra("message", "Command:SendFile:" + filepath);
		intent.putExtra("filenameonly", "true");
		intent.putExtra("delete", "true");
		context.startService(intent);
		this.stopSelf();		
	}
	

	public void stopCamera() {
        // Delay to ensure shutter sound doesn't go off.
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				stopAll();
			}
		}, 2000);
	}
}
