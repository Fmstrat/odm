package com.nowsci.odm;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.checkStorageDir;

public class AudioService extends Service {
	private static final String TAG= "ODMAudioService";

	MediaRecorder mr;
	Context context;
	String audFile;
	String audFilePath;
	int seconds = 15;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@SuppressLint("InlinedApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logd(TAG, "Starting audio service.");
		context = getApplicationContext();
		setFilePath();
		String message = intent.getStringExtra("message");
		String s = message.replace("Command:Audio:", "");
		seconds = Integer.parseInt(s);
		Logd(TAG, "Recording for " + s + " seconds.");
		mr = new MediaRecorder();
		mr.setMaxDuration(seconds*1000);
		mr.setAudioSource(MediaRecorder.AudioSource.MIC);
		mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mr.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
		Logd(TAG, "Recording " + audFilePath);
		mr.setOutputFile(audFilePath);
        File outFile = new File(audFilePath);
        if (outFile.exists()) {
            outFile.delete();
        }
        try {
			mr.prepare();
	        mr.start();
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						Logd(TAG, "Stopping capture...");
						stopCapture();
					} catch (RuntimeException e) {
						Logd(TAG, e.getMessage());
					}
				}
			}, seconds*1000);
		} catch (IllegalStateException e) {
			Logd(TAG, "Error: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Logd(TAG, "Error: " + e.getLocalizedMessage());
		}
		return startId;
	}

	@Override
	public void onDestroy() {
		Logd(TAG, "audio service.");
		super.onDestroy();
	}
	
	public void stopCapture() {
		Logd(TAG, "Stopping capture.");
		if (mr != null) {
			mr.stop();
			mr.reset();
			mr.release();
			mr = null;
		}
		Logd(TAG, "Sending " + audFilePath);
		Intent intent = new Intent("com.nowsci.odm.FileService");
		intent.putExtra("message", "Command:SendFile:" + audFilePath);
		intent.putExtra("filenameonly", "true");
		intent.putExtra("delete", "true");
		context.startService(intent);
		this.stopSelf();
	}

	
	@SuppressLint("SimpleDateFormat")
	public void setFilePath() {
        checkStorageDir();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
		String date = dateFormat.format(new Date());
		audFile = "aud_" + date + ".3gpp";
		audFilePath = Environment.getExternalStorageDirectory() + "/Android/data/com.nowsci.odm/.storage/" + audFile;
	}
}
