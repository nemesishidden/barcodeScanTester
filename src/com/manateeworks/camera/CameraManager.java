package com.manateeworks.camera;

import java.io.IOException;
import java.util.Currency;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager {

	private static final String TAG = CameraManager.class.getSimpleName();

	private static final int MIN_FRAME_WIDTH = 240;
	private static final int MIN_FRAME_HEIGHT = 160;
	private static final int MAX_FRAME_WIDTH = 1920;
	private static final int MAX_FRAME_HEIGHT = 1080;

	private static CameraManager cameraManager;

	static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT
	static {
		int sdkInt;
		try {
			sdkInt = Integer.parseInt(Build.VERSION.SDK);
		} catch (NumberFormatException nfe) {
			// Just to be safe
			sdkInt = 10000;
		}
		SDK_INT = sdkInt;
	}

	private final Context context;
	public final CameraConfigurationManager configManager;
	public Camera camera;
	private Rect framingRect;
	private Rect framingRectInPreview;
	private boolean initialized;
	public boolean previewing;
	private final boolean useOneShotPreviewCallback;
	public static boolean useBufferedCallback = true;
	private Camera.PreviewCallback cb;

	public static int mDesiredWidth = 0;
	public static int mDesiredHeight = 0;
	
	public SurfaceHolder lastHolder;
	
	private static boolean shouldTakePicture = false;

	public static void setDesiredPreviewSize(int width, int height) {

		mDesiredWidth = width;
		mDesiredHeight = height;

	}
	
	public Point getMaxResolution() {

		if (camera != null)
			return CameraConfigurationManager.getMaxResolution(camera.getParameters());
		else
			return null;

	}
	

	/**
	 * Preview frames are delivered here, which we pass on to the registered
	 * handler. Make sure to clear the handler so it will only receive one
	 * message.
	 */
	public final PreviewCallback previewCallback;
	/**
	 * Autofocus callbacks arrive here, and are dispatched to the Handler which
	 * requested them.
	 */
	private final AutoFocusCallback autoFocusCallback;

	/**
	 * Initializes this static object with the Context of the calling Activity.
	 * 
	 * @param context
	 *            The Activity which wants to use the camera.
	 */
	public static void init(Context context) {
		if (cameraManager == null) {
			cameraManager = new CameraManager(context);
		}
	}

	/**
	 * Gets the CameraManager singleton instance.
	 * 
	 * @return A reference to the CameraManager singleton.
	 */
	public static CameraManager get() {
		return cameraManager;
	}

	private CameraManager(Context context) {

		this.context = context;
		this.configManager = new CameraConfigurationManager(context);

		// Camera.setOneShotPreviewCallback() has a race condition in Cupcake,
		// so we use the older
		// Camera.setPreviewCallback() on 1.5 and earlier. For Donut and later,
		// we need to use
		// the more efficient one shot callback, as the older one can swamp the
		// system and cause it
		// to run out of memory. We can't use SDK_INT because it was introduced
		// in the Donut SDK.
		// useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) >
		// Build.VERSION_CODES.CUPCAKE;
		useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > 3; // 3
																				// =
																				// Cupcake
		useBufferedCallback = Integer.parseInt(Build.VERSION.SDK) >= 8;

		previewCallback = new PreviewCallback(configManager, useOneShotPreviewCallback);
		autoFocusCallback = new AutoFocusCallback();
	}

	/**
	 * Opens the camera driver and initializes the hardware parameters.
	 * 
	 * @param holder
	 *            The surface object which the camera will draw preview frames
	 *            into.
	 * @throws IOException
	 *             Indicates the camera driver failed to open.
	 */
	public void openDriver(SurfaceHolder holder, boolean isPortrait) throws IOException {
		if (camera == null) {
			camera = Camera.open();
			if (camera == null) {
				throw new IOException();
			}
			
			if (isPortrait)
            	camera.setDisplayOrientation(90);
			
			if (holder != null){
				lastHolder = holder;
				camera.setPreviewDisplay(holder);
			} else {
				camera.setPreviewDisplay(lastHolder);
			}
			
			

			if (!initialized) {
				initialized = true;
				configManager.initFromCameraParameters(camera);
			}
			configManager.setDesiredCameraParameters(camera);
			setPictureSize(1280, 1280);

		}
	}
	
	public boolean setPictureSize (int width, int height){
		try{
			Camera.Parameters cp =  camera.getParameters();
			List<Size> sizes = cp.getSupportedPictureSizes();
			
			int minDiff = 9999999;
			int minIndex = -1;
			if (sizes != null && sizes.size() > 0)
				for (int i = 0; i < sizes.size(); i++){
					int size =sizes.get(i).width * sizes.get(i).height;
					if (size >= width * height){
						if (size -width * height < minDiff){
							minDiff = size -width * height;
							minIndex = i;
						}
					}
				}
			if (minIndex >=0){
				cp.setPictureSize(sizes.get(minIndex).width, sizes.get(minIndex).height);
				camera.setParameters(cp);
				return true;
			} else
				return false;
		} catch (Exception e){
			return false;
		}
		
	}
	
	public void requestImageCapture (){
		
		shouldTakePicture = true;
		
	}
	
	private void CaptureImage(int orientation){
    	
    	  if (useBufferedCallback){
    		 
    		  camera.setOneShotPreviewCallback(cb);
    		  
    	  }
    	  
    	  camera.autoFocus(autoFocusCallback);
    	  
    	  AutoFocusCallback.takePicture = true;
    	  previewing = false;
    	
    }

	public boolean isTorchAvailable() {

		if (camera == null)
			return false;

		Parameters cp = camera.getParameters();
		List<String> flashModes = cp.getSupportedFlashModes();
		if (flashModes != null && flashModes.contains(Parameters.FLASH_MODE_TORCH))
			return true;
		else
			return false;

	}

	public void setTorch(boolean enabled) {

		if (camera == null)
			return;

		try {
			Parameters cp = camera.getParameters();

			List<String> flashModes = cp.getSupportedFlashModes();

			if (flashModes != null && flashModes.contains(Parameters.FLASH_MODE_TORCH)) {

				if (enabled)
					cp.setFlashMode(Parameters.FLASH_MODE_TORCH);
				else
					cp.setFlashMode(Parameters.FLASH_MODE_OFF);

				camera.setParameters(cp);

			}
		} catch (Exception e) {

		}

	}

	/**
	 * Closes the camera driver if still in use.
	 */
	public void closeDriver() {

		if (camera != null) {

			if (useBufferedCallback) {

			}

			camera.release();
			camera = null;
		}
	}

	/**
	 * Asks the camera hardware to begin drawing preview frames to the screen.
	 */
	public void startPreview() {
		if (camera != null && !previewing) {
			camera.startPreview();
			previewing = true;
		}
	}

	/**
	 * Tells the camera to stop drawing preview frames.
	 */
	public void stopPreview() {
		if (camera != null && previewing) {

			if (useBufferedCallback) {
				previewCallback.setPreviewCallback(camera, null, 0, 0);
			}
			if (!useOneShotPreviewCallback) {
				camera.setPreviewCallback(null);
			}

			camera.stopPreview();
			previewCallback.setHandler(null, 0);
			autoFocusCallback.setHandler(null, 0);
			previewing = false;
		}
	}

	/**
	 * A single preview frame will be returned to the handler supplied. The data
	 * will arrive as byte[] in the message.obj field, with width and height
	 * encoded as message.arg1 and message.arg2, respectively.
	 * 
	 * @param handler
	 *            The handler to send the message to.
	 * @param message
	 *            The what field of the message to be sent.
	 */
	public void requestPreviewFrame(Handler handler, int message) {
		
		if (shouldTakePicture){
			if (handler != null){
				previewCallback.setHandler(handler, message);
				shouldTakePicture = false;
				CaptureImage(0);
				return;
			}
		}
		
		if (camera != null && previewing) {
			previewCallback.setHandler(handler, message);
			if (useBufferedCallback) {
				//if (cb == null) {

					cb = previewCallback.getCallback();
				//}
				previewCallback.setPreviewCallback(camera, cb, configManager.cameraResolution.x, configManager.cameraResolution.y);
			} else if (useOneShotPreviewCallback) {
				camera.setOneShotPreviewCallback(previewCallback);
			} else {
				camera.setPreviewCallback(previewCallback);
			}
		}
	}

	/**
	 * Asks the camera hardware to perform an autofocus.
	 * 
	 * @param handler
	 *            The Handler to notify when the autofocus completes.
	 * @param message
	 *            The message to deliver.
	 */
	public void requestAutoFocus(Handler handler, int message) {
		if (camera != null && previewing) {
			autoFocusCallback.setHandler(handler, message);
			// Log.d(TAG, "Requesting auto-focus callback");
			camera.autoFocus(autoFocusCallback);
		}
	}

	/**
	 * Calculates the framing rect which the UI should draw to show the user
	 * where to place the barcode. This target helps with alignment as well as
	 * forces the user to hold the device far enough away to ensure the image
	 * will be in focus.
	 * 
	 * @return The rectangle to draw on screen in window coordinates.
	 */
	public Rect getFramingRect() {
		Point screenResolution = configManager.getScreenResolution();
		if (framingRect == null) {
			if (camera == null) {
				return null;
			}

			int width, height;

			width = screenResolution.y;
			height = screenResolution.y;

			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height) / 2;
			framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
			Log.d(TAG, "Calculated framing rect: " + framingRect);
		}
		return framingRect;
	}

	/**
	 * Like {@link #getFramingRect} but coordinates are in terms of the preview
	 * frame, not UI / screen.
	 */
	public Rect getFramingRectInPreview() {
		if (framingRectInPreview == null) {
			Rect rect = new Rect(getFramingRect());
			Point cameraResolution = configManager.getCameraResolution();
			Point screenResolution = configManager.getScreenResolution();
			rect.left = rect.left * cameraResolution.x / screenResolution.x;
			rect.right = rect.right * cameraResolution.x / screenResolution.x;
			rect.top = rect.top * cameraResolution.y / screenResolution.y;
			rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
			framingRectInPreview = rect;
		}
		return framingRectInPreview;
	}

	/**
	 * Converts the result points from still resolution coordinates to screen
	 * coordinates.
	 * 
	 * @param data
	 *            A preview frame.
	 * @param width
	 *            The width of the image.
	 * @param height
	 *            The height of the image.
	 * @return A PlanarYUVLuminanceSource instance.
	 */
	public byte[] buildLuminanceSource(byte[] data, int width, int height) {
		Rect rect = getFramingRect();
		int w = rect.right - rect.left + 1;
		int h = rect.bottom - rect.top + 1;
		int i = width * rect.top + rect.left;
		int j = 0;
		byte[] image = new byte[w * h];
		for (int y = rect.top; y <= rect.bottom; y++) {
			for (int x = rect.left; x <= rect.right; x++) {
				image[j++] = data[i + x];
			}
			i += width;
		}
		width = w;
		height = h;
		return image;
	}

	public Bitmap renderCroppedGreyscaleBitmap(byte[] data, int width, int height) {
		int[] pixels = new int[width * height];
		byte[] yuv = data;
		int row = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int grey = yuv[row + x] & 0xff;
				pixels[row + x] = 0xFF000000 | (grey * 0x00010101);
			}
			row += width;
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	public int getFrameWidth() {
		Rect rect = getFramingRect();
		return rect.right - rect.left + 1;
	}

	public int getFrameHeight() {
		Rect rect = getFramingRect();
		return rect.bottom - rect.top + 1;
	}

}
