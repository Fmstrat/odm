package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.getVAR;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraService extends Service {
	private static final String TAG = "CameraService";

	private SurfaceHolder sHolder;
	private Camera mCamera;
	private Parameters parameters;
	Context context;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO keep camera from crashing
		super.onCreate();
		String message = intent.getStringExtra("message");
		context = getApplicationContext();
		// Waking up mobile if it is sleeping
		WakeLocker.acquire(context);
		boolean cont = true;
		if (message.equals("Command:RearPhoto")) {
			try {
				mCamera = Camera.open();
			} catch (RuntimeException e) {
				Logd(TAG, e.getMessage());
				cont = false;
			} finally {
				if (mCamera != null) {
					mCamera.stopPreview();
					mCamera.release();
					mCamera = null;
				}
			}
			try {
				mCamera = Camera.open();
			} catch (RuntimeException e) {
				Logd(TAG, e.getMessage());
				cont = false;
			}
		} else {
			int cameraCount = 0;
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras();
			for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
				Camera.getCameraInfo(camIdx, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					try {
						mCamera = Camera.open(camIdx);
					} catch (RuntimeException e) {
						Logd(TAG, e.getMessage());
						cont = false;
					} finally {
						if (mCamera != null) {
							mCamera.stopPreview();
							mCamera.release();
							mCamera = null;
						}
					}
					try {
						mCamera = Camera.open(camIdx);
					} catch (RuntimeException e) {
						Logd(TAG, e.getMessage());
						cont = false;
					}
				}
			}
		}
		if (cont) {
			if (android.os.Build.VERSION.SDK_INT >= 14) { //14
				CamPreview camPreview = new CamPreview(context, mCamera);
				camPreview.setSurfaceTextureListener(camPreview);
				CamCallback camCallback = new CamCallback();
				mCamera.setPreviewCallback(camCallback);
				mCamera.startPreview();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mCamera.takePicture(null, null, mCall);
				/*
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
							mCamera.takePicture(null, null, mCall);
						} catch (RuntimeException e) {
							Logd(TAG, e.getMessage());
						}
					}
				}, 2000);
				*/
			} else {
				// Pre-android 4.0
				SurfaceView sv = new SurfaceView(context);
				sHolder = sv.getHolder();
				sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				try {
					mCamera.setPreviewDisplay(sHolder);
					parameters = mCamera.getParameters();
					mCamera.setParameters(parameters);
					mCamera.startPreview();
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							try {
								mCamera.takePicture(null, null, mCall);
							} catch (RuntimeException e) {
								Logd(TAG, e.getMessage());
							}
						}
					}, 2000);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// Releasing wake lock
		WakeLocker.release();
		// TODO Ensure all to self.stopService(); or this.stopSelf();
		return START_STICKY;
	}

	Camera.PictureCallback mCall = new Camera.PictureCallback() {

		@SuppressWarnings("deprecation")
		@SuppressLint("SimpleDateFormat")
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// Save for later if we need to save to storage.
			/*
			FileOutputStream outStream = null;
			try {
				outStream = new FileOutputStream("/sdcard/Image.jpg");
				outStream.write(data);
				outStream.close();
			} catch (FileNotFoundException e) {
				Logd(TAG, e.getMessage());
			} catch (IOException e) {
				Logd(TAG, e.getMessage());
			}
			*/
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
			AsyncTask<byte[], Void, Void> postTask;
			postTask = new AsyncTask<byte[], Void, Void>() {
				@Override
				protected Void doInBackground(byte[]... params) {
					byte[] data = params[0];
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
					String date = dateFormat.format(new Date());
					String photoFile = "img_" + date + ".jpg";
					Map<String, String> postparams = new HashMap<String, String>();
					postparams.put("regId", getVAR("REG_ID"));
					postparams.put("username", getVAR("USERNAME"));
					postparams.put("password", getVAR("ENC_KEY"));
					postparams.put("message", "img:" + photoFile);
					postparams.put("data", URLEncoder.encode(Base64.encodeToString(data, Base64.DEFAULT)));
					try {
						CommonUtilities.post(getVAR("SERVER_URL") + "message.php", postparams);
					} catch (IOException e) {
						Logd(TAG, "Failed to post to server.");
					}
					return null;
				}
			};
			postTask.execute(data, null, null);
		}
	};

	// For API 14 and newer
	public class CamCallback implements Camera.PreviewCallback {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			/*
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mCamera.takePicture(null, null, mCall);
					//camera.takePicture(null, null, mCall);
				}
			}, 2000);
			*/
		}
	}
}
