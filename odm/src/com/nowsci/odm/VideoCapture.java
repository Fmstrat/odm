package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;
import static com.nowsci.odm.CommonUtilities.checkStorageDir;
import static com.nowsci.odm.CommonUtilities.getVAR;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nowsci.odm.FileService.SendFileBackground;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Base64;
import android.view.Display;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class VideoCapture implements SurfaceHolder.Callback {
	public VideoService vs;
	public View v;
	SurfaceHolder sh;
	SurfaceHolder sh_created;
	SurfaceView sv;
	int cameraInt = 0;
	Camera c;
	Camera.PictureCallback mCall;
	Camera.Parameters params;
	Camera.PictureCallback callback;
	MediaRecorder mr;
	Display display;
	Boolean max = false;
	Boolean focused = false;
	int focusTimeout = 10; // in seconds
	long focusStart = 0;
	String vidFilePath = "";
	String vidFile = "";
	int seconds = 15;

	private static final String TAG= "ODMVideoCapture";

	public VideoCapture() {
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
		Logd(TAG, "Surface removed.");
		if (mr != null) {
			mr.stop();
			mr.reset();
			mr.release();
			mr = null;
		}
		if (this.c != null) {
			this.c.stopPreview();
			this.c.release();
			this.c = null;
		}
	}

	public void setMax(Boolean m) {
		max = m;
	}

	public void setSeconds(Integer s) {
		seconds = s;
	}

	public void setCamera(int inCameraInt) {
		cameraInt = inCameraInt;
	}

	public void setDisplay(Display inDisplay) {
		display = inDisplay;
	}

	public void setServiceContainer(VideoService inVideoService) {
		vs = inVideoService;
	}

	public void startWindowManager() {
		Logd(TAG, "Starting up the window manager...");
		if (v != null) {
			((WindowManager) vs.getSystemService("window")).removeView(v);
			v = null;
		}
		WindowManager wm = (WindowManager) vs.getSystemService("window");
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT); //width, height, _type (2007), _flags (32), _format (-3)
		lp.y = 0;
		lp.x = 0;
		lp.gravity = Gravity.LEFT;
		try {
			v = ((LayoutInflater) vs.getSystemService("layout_inflater")).inflate(R.layout.camera, null);
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
		if (mr != null) {
			mr.stop();
			mr.reset();
			mr.release();
			mr = null;
		}
		if (c != null) {
			c.stopPreview();
			c.release();
			c = null;
		}
		if (v != null) {
			((WindowManager) this.vs.getSystemService("window")).removeView(v);
			v = null;
		}
	}
	
	void startPreview() {
		Logd(TAG, "Starting CC preview...");
		if (mr != null) {
			mr.stop();
			mr.reset();
			mr.release();
			mr = null;
		}
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
				// TODO Include resolution selection for video
				/*
				if (max) {
					params = c.getParameters();
					List<Size> sl = params.getSupportedPictureSizes();
					int w = 0;
					int h = 0;
					for (Size s : sl) {
						if (s.width > w) {
							w = s.width;
							h = s.height;
						}
					}
					params.setPictureSize(w, h);
					c.setParameters(params);
				}
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
	
	public String setFilePath() {
        checkStorageDir();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
		String date = dateFormat.format(new Date());
		vidFile = "vid_" + date + ".mp4";
		vidFilePath = Environment.getExternalStorageDirectory() + "/Android/data/com.nowsci.odm/.storage/" + vidFile;
		return vidFilePath;
	}
	
	public void waitForFocus() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				long curTime = System.currentTimeMillis();
				if (focused == true || (curTime - focusStart >= (focusTimeout*1000))) { 
					try {
						Logd(TAG, "Taking video...");
						//c.takePicture(null, null, callback);
						mr = new MediaRecorder();
						c.unlock();
						mr.setCamera(c);
						mr.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
						mr.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
						mr.setPreviewDisplay(null);
						CamcorderProfile cpHigh = null;
						if (max)
							cpHigh = CamcorderProfile.get(cameraInt, CamcorderProfile.QUALITY_HIGH);
						else
							cpHigh = CamcorderProfile.get(cameraInt, CamcorderProfile.QUALITY_LOW);
				        mr.setProfile(cpHigh);
				        mr.setOutputFile(vidFilePath);
				        File outFile = new File(vidFilePath);
				        if (outFile.exists()) {
				            outFile.delete();
				        }
				        mr.setMaxDuration(seconds * 1000);
				        mr.prepare();
				        //mr.addCallback();
						Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								try {
									Logd(TAG, "Starting video...");
									mr.start();
								} catch (RuntimeException e) {
									Logd(TAG, e.getMessage());
								}
							}
						}, 2000);
						handler = new Handler();
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								try {
									Logd(TAG, "Stopping capture...");
									stopCapture();
									vs.stopCamera();
								} catch (RuntimeException e) {
									Logd(TAG, e.getMessage());
								}
							}
						}, 2000+(seconds*1000));
					} catch (RuntimeException e) {
						Logd(TAG, e.getMessage());
					} catch (IOException e) {
						Logd(TAG, e.getMessage());
					}
				} else {
					waitForFocus();
				}
			}
		}, 2000);
	}

	
	public void captureVideo() {
		try {
			Logd(TAG, "Starting captureVideo...");
			params = c.getParameters();
			c.setParameters(params);
			Logd(TAG, "Starting preview...");
			c.startPreview();
			final AutoFocusCallback afc = new AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					// TODO Auto-generated method stub
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							try {
								Logd(TAG, "Got focus.");
								focused = true;
								//c.takePicture(null, null, callback);
							} catch (RuntimeException e) {
								Logd(TAG, e.getMessage());
							}
						}
					}, 2000);
				}
			};
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						Logd(TAG, "Focusing...");
						//c.takePicture(null, null, callback);
						focusStart = System.currentTimeMillis();
						c.autoFocus(afc);
						waitForFocus();
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
