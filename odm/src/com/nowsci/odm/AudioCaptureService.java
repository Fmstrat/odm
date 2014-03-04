package com.nowsci.odm;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.nowsci.odm.misc.ApiProtocolHandler;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import static com.nowsci.odm.misc.CommonUtilities.Logd;
import static com.nowsci.odm.misc.CommonUtilities.getVAR;

public class AudioCaptureService extends Service {
	private static final String TAG = "AudioCaptureService";
	private String requestID = "";
	private int captureLenght = 20 * 1000; // 20 seconds
	
	// run on another Thread to avoid crash
	private Handler mHandler = new Handler();
	// timer handling
	private Timer mTimer = null;

	private MediaRecorder myRecorder;
	Context context;
	File outputDir = OdmApplication.getAppContext().getCacheDir(); // context
																	// being the
																	// Activity
																	// pointer
	File outputFile;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logd(TAG, "Starting audio recoder service...");
		requestID = intent.getStringExtra("requestid");
		captureLenght = intent.getIntExtra("capturelength", captureLenght) * 1000;
		if(captureLenght < 10000)
			captureLenght = 10000; // min 10 secs
		
		// cancel if already existed
        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }

		try {
			outputFile = File.createTempFile("prefix_acs", ".3gpp", outputDir);
			myRecorder = new MediaRecorder();
			myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
			myRecorder.setMaxDuration(0); // we can capture forever
			myRecorder.setOutputFile(outputFile.getAbsolutePath());
			Logd(TAG, "Audio file: " + outputFile.getAbsolutePath());
		} catch (Exception e) {
			Logd(TAG, "Soundfile write failed: " + e.getMessage());
		}

		if (myRecorder != null) {
			try {
				myRecorder.prepare();
				myRecorder.start();
			} catch (IllegalStateException e) {
				// start:it is called before prepare()
				// prepare: it is called after start() or before
				// setOutputFormat()
				Logd(TAG, e.getMessage());
			} catch (IOException e) {
				// prepare() fails
				Logd(TAG, e.getMessage());
			}

		}

		// schedule task
		Logd(TAG,"Capturing for " + captureLenght + " secs");
		mTimer.schedule(new AudioCaptureTimerTask(), captureLenght);
		
		return START_STICKY;
	}

	class AudioCaptureTimerTask extends TimerTask {

		@Override
		public void run() {
			// run on another thread
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						myRecorder.stop();
						myRecorder.release();
						myRecorder = null;
						Logd(TAG, "Stopped Recorder!");
						ApiProtocolHandler.apiMessageAudio(getVAR("REG_ID"), requestID, String.valueOf(captureLenght), outputFile.getAbsolutePath());
						mTimer.cancel();
						mTimer.purge();
						mTimer = null;
					} catch (IllegalStateException e) {
						// it is called before start()
						Logd(TAG, "AudioCapture ISEX: " + e.getMessage());
					} catch (RuntimeException e) {
						// no valid audio/video data has been received
						Logd(TAG, "AudioCapture RTEX: " + e.getMessage());
					}
				}
			});
		}
	}
}
