package com.nowsci.odm;

import java.io.IOException;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.view.Gravity;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.FrameLayout;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CamPreview extends TextureView implements SurfaceTextureListener {

	private Camera mCamera;

	public CamPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
		setLayoutParams(new FrameLayout.LayoutParams(previewSize.width, previewSize.height, Gravity.CENTER));

		try {
			mCamera.setPreviewTexture(surface);
		} catch (IOException t) {
		}

		mCamera.startPreview();
		this.setVisibility(INVISIBLE); // Make the surface invisible as soon as
										// it is created
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		// Put code here to handle texture size change if you want to
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		return true;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// Update your view here!
	}
}
