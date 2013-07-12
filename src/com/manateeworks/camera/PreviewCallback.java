package com.manateeworks.camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public final class PreviewCallback implements Camera.PreviewCallback {

	int fpscount;
	public static float currentFPS = 0f;
	long lasttime = 0;

	private final CameraConfigurationManager configManager;
	private final boolean useOneShotPreviewCallback;
	public Handler previewHandler;
	public int previewMessage;

	public byte[][] frameBuffers;
	public int fbCounter = 0;
	public boolean callbackActive = false;

	PreviewCallback(CameraConfigurationManager configManager, boolean useOneShotPreviewCallback) {
		this.configManager = configManager;
		this.useOneShotPreviewCallback = useOneShotPreviewCallback;
	}

	void setHandler(Handler previewHandler, int previewMessage) {
		this.previewHandler = previewHandler;
		this.previewMessage = previewMessage;
	}

	public void onPreviewFrame(byte[] data, Camera camera) {
		if (AutoFocusCallback.takePicture)
			return;
		updateFps();

		Point cameraResolution = configManager.getCameraResolution();
		if (!useOneShotPreviewCallback) {
			camera.setPreviewCallback(null);
		}
		if (previewHandler != null) {
			Message message = previewHandler.obtainMessage(previewMessage, cameraResolution.x, cameraResolution.y, data);
			message.sendToTarget();
			previewHandler = null;
		}
	}

	public void closeCallback() {

	}

	public int setPreviewCallback(Camera camera, Camera.PreviewCallback callback, int width, int height) {

		if (callback != null) {
			if (frameBuffers == null) {
				// add 10% additional space for any case
				frameBuffers = new byte[2][width * height * 2 * 110 / 100];
				fbCounter = 0;
				Log.i("preview resolution", String.valueOf(width) + "x" + String.valueOf(height));

			}
			if (!callbackActive) {
				camera.setPreviewCallbackWithBuffer(callback);
				callbackActive = true;
			}
			// CameraDriver.bufferProccessed = -1;
			camera.addCallbackBuffer(frameBuffers[fbCounter]);
			fbCounter = 1 - fbCounter;
		} else {
			camera.setPreviewCallbackWithBuffer(callback);
			callbackActive = false;
		}

		if (callback == null) {
			frameBuffers = null;
			System.gc();
		}

		return 0;
	}

	public Camera.PreviewCallback getCallback() {

		return new Camera.PreviewCallback() {
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				if (AutoFocusCallback.takePicture)
					return;
				updateFps();
				// camera.addCallbackBuffer(frameBuffers[fbCounter]);
				// fbCounter = 1 - fbCounter;

				Point cameraResolution = configManager.getCameraResolution();

				if (previewHandler != null) {
					Message message = previewHandler.obtainMessage(previewMessage, cameraResolution.x, cameraResolution.y, data);
					message.sendToTarget();
					previewHandler = null;
				}

				// if (1==1)return;
				/*
				 * if (Preview.stillCapturing) { // return; if (fbCounter > 1)
				 * fbCounter = 1; if (fbCounter < 0) fbCounter = 0;
				 * camera.addCallbackBuffer(frameBuffers[fbCounter]);
				 * 
				 * // Preview.self.notify(); } else if
				 * (CameraDriver.bufferProccessed >= 0) { fbCounter = 1 -
				 * bufferProccessed; if (fbCounter > 1) fbCounter = 1; if
				 * (fbCounter < 0) fbCounter = 0;
				 * 
				 * camera.addCallbackBuffer(frameBuffers[fbCounter]); } else {
				 * fbCounter = 1 - fbCounter;
				 * 
				 * camera.addCallbackBuffer(frameBuffers[fbCounter]); if
				 * (!preview.closingCamera) { CameraDriver.bufferProccessed = 1
				 * - fbCounter; preview.SendMessage(Preview.MSG_ID,
				 * Preview.MSG_PREVIEWREADY, CameraDriver.bufferProccessed,
				 * data); } Preview.fCount++; }
				 */

			}
		};

	}

	private void updateFps() {
		if (lasttime == 0) {
			lasttime = System.currentTimeMillis();
			fpscount = 0;
			currentFPS = 0;
		} else {
			long delay = System.currentTimeMillis() - lasttime;
			if (delay > 1000) {
				lasttime = System.currentTimeMillis();
				currentFPS = fpscount * 10000 / delay;
				currentFPS /= 10;
				fpscount = 0;
			}
		}
		fpscount++;
	}

}
