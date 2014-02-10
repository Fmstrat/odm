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
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;
import android.view.Display;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class CameraCapture implements SurfaceHolder.Callback {
	public CameraService cs;
	public View v;
	SurfaceHolder sh;
	SurfaceHolder sh_created;
	SurfaceView sv;
	int cameraInt = 0;
	Camera c;
	Camera.PictureCallback mCall;
	Camera.Parameters params;
	Camera.PictureCallback callback;
	Display display;

	private static final String TAG = "CameraCapture";

	public CameraCapture() {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (c != null) {
			params = c.getParameters();
			params.set("orientation", "portrait");
			c.setParameters(params);
			c.startPreview();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		sh_created = holder;
		startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (this.c != null) {
			this.c.stopPreview();
			this.c.release();
			this.c = null;
		}
	}

	public void setCamera(int inCameraInt) {
		cameraInt = inCameraInt;
	}

	public void setDisplay(Display inDisplay) {
		display = inDisplay;
	}

	public void setServiceContainer(CameraService inCameraService) {
		cs = inCameraService;
	}

	public void startWindowManager() {
		Logd(TAG, "Starting up the window manager...");
		if (v != null) {
			((WindowManager) cs.getSystemService("window")).removeView(v);
			v = null;
		}
		WindowManager wm = (WindowManager) cs.getSystemService("window");
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT); //width, height, _type (2007), _flags (32), _format (-3)
		lp.y = 0;
		lp.x = 0;
		lp.gravity = Gravity.LEFT;
		try {
			v = ((LayoutInflater) cs.getSystemService("layout_inflater")).inflate(R.layout.camera, null);
		} catch (InflateException e) {
			Logd(TAG, "Error: " + e.getLocalizedMessage());
		}
		v.setVisibility(0);
		wm.addView(v, lp);
		sv = ((SurfaceView) v.findViewById(R.id.surfaceView));
		sh = sv.getHolder();
		sh.addCallback(this);
	}

	public void stopCapture() {
		Logd(TAG, "Stopping capture.");
		if (c != null) {
			c.stopPreview();
			c.release();
			c = null;
		}
		if (v != null) {
			((WindowManager) this.cs.getSystemService("window")).removeView(v);
			v = null;
		}
	}

	void startPreview() {
		Logd(TAG, "Starting CC preview...");
		if (c != null) {
			c.stopPreview();
			c.release();
			c = null;
		}
		try {
			c = Camera.open(cameraInt);
			if (c == null) {
				Logd(TAG, "Error opening camera.");
				return;
			}
		} catch (RuntimeException e) {
			Logd(TAG, "Error: " + e.getLocalizedMessage());
		}
		while (true) {
			try {
				// TODO Handle orientation and resolution.
				// Two versions of code below exist, but neither work properly.
				/*
				params = c.getParameters();
				Logd(TAG, "Orientation: " + cs.getResources().getConfiguration().orientation);
				if (cs.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
					Logd(TAG, "Setting orientation to portrait.");
					params.set("orientation", "portrait");
					if (cameraInt == 1)
						params.set("rotation", 270);
					else {
						params.set("rotation", 90);
					}
				} else if (cs.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					Logd(TAG, "Setting orientation to landscape.");
					params.set("orientation", "landscape");
					//params.set("rotation", 90);
				}
				c.setParameters(params);
				*/
				/*
				Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
				Camera.getCameraInfo(cameraInt, info);
				int rotation = display.getRotation();
				Logd(TAG, "Camera rotation: " + rotation);
				int degrees = 0;
				switch (rotation) {
				case Surface.ROTATION_0:
					degrees = 0;
					break;
				case Surface.ROTATION_90:
					degrees = 90;
					break;
				case Surface.ROTATION_180:
					degrees = 180;
					break;
				case Surface.ROTATION_270:
					degrees = 270;
					break;
				}
				Logd(TAG, "Camera compensation degrees: " + degrees);
				int result;
				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					result = (info.orientation + degrees) % 360;
					result = (360 - result) % 360; // compensate the mirror
				} else { // back-facing
					result = (info.orientation - degrees + 360) % 360;
				}
				Logd(TAG, "Camera compensation result: " + result);
				c.setDisplayOrientation(result);
				*/
				c.setPreviewDisplay(sh_created);
				c.startPreview();
				return;
			} catch (Exception localException) {
				c.release();
				c = null;
				return;
			}
		}
	}

	public void captureImage() {
		try {
			Logd(TAG, "Starting captureImage...");
			params = c.getParameters();
			c.setParameters(params);
			Logd(TAG, "Starting preview...");
			c.startPreview();
			callback = new Camera.PictureCallback() {
				@Override
				public void onPictureTaken(byte[] data, Camera camera) {
					Logd(TAG, "Image captured.");
					AsyncTask<byte[], Void, Void> postTask;
					postTask = new AsyncTask<byte[], Void, Void>() {
						@SuppressLint("SimpleDateFormat")
						@SuppressWarnings("deprecation")
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
							cs.stopCamera();
							return null;
						}
					};
					postTask.execute(data, null, null);
					stopCapture();
				}
			};
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						Logd(TAG, "Taking the picture...");
						c.takePicture(null, null, callback);
					} catch (RuntimeException e) {
						Logd(TAG, e.getMessage());
					}
				}
			}, 2000);
		} catch (Exception e) {
			Logd(TAG, "Error: " + e.getMessage());
			if (c != null) {
				c.stopPreview();
				c.release();
				c = null;
			}
		}
	}
}
