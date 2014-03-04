package com.nowsci.odm;

import com.nowsci.odm.R;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import static com.nowsci.odm.misc.CommonUtilities.Logd;

public class AudioService extends Service {
	private static final String TAG = "AudioService";

	MediaPlayer mPlayer;
	int currentVolume;
	Context context;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Logd(TAG, "Starting ringer.");
		context = getApplicationContext();
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		audio.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		mPlayer = MediaPlayer.create(context, R.raw.ring);
		mPlayer.setLooping(true);
		mPlayer.start();
	}

	@Override
	public void onDestroy() {
		Logd(TAG, "Stopping ringer.");
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		mPlayer.stop();
		super.onDestroy();
	}
}
