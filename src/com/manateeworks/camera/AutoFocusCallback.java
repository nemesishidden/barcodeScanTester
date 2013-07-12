
package com.manateeworks.camera;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Message;

final class AutoFocusCallback implements Camera.AutoFocusCallback
{

    private static final long AUTOFOCUS_INTERVAL_MS = 2500L;

    private Handler autoFocusHandler;
    private int autoFocusMessage;
    public static  boolean takePicture = false;

    void setHandler(Handler autoFocusHandler, int autoFocusMessage)
    {
        this.autoFocusHandler = autoFocusHandler;
        this.autoFocusMessage = autoFocusMessage;
    }
    
    public void onAutoFocus(boolean success, Camera camera)
    {
    	
    	
    	if (takePicture){

    		CameraManager.get().previewCallback.callbackActive = false;
    		
	    		synchronized (this) {
	    		
	    		if (CameraManager.useBufferedCallback)
						try {
							
				    		int delay = (int) (2000.f / PreviewCallback.currentFPS);
				    		if (delay > 1000)
				    			delay= 1000;
				    		if (delay < 200)
				    			delay = 200;
				    		
							wait(delay );
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    		
	    		camera.takePicture(null,null,
				
				 new Camera.PictureCallback() {
					
					@Override
					public void onPictureTaken(byte[] data, Camera camera) {
						if (CameraManager.useBufferedCallback){
				    		  camera.setOneShotPreviewCallback(null);
				    		  
				    	  }
						 
						Parameters p =  camera.getParameters();
						int sizeM = 	p.getPictureSize().height*0x10000+
								    p.getPictureSize().width;
						
						if (data!=null){
							takePicture = false;
							
							int w = p.getPictureSize().width;
							int h = p.getPictureSize().height;
							
							
							//int rgb[]=new int[w*h];
							//Fx.YUV2RGB(yuvs,rgb,w,h, Fx.getCurrentColorMode());
							
							Bitmap tmp;
							Options opts = new Options();
							opts.inJustDecodeBounds=true;
							
							tmp = BitmapFactory.decodeByteArray(data, 0, data.length,opts);
							
							int size = Math.max(opts.outWidth,opts.outHeight);
							opts.inSampleSize=1;
							while (size>=2000){
								size/=2;
								opts.inSampleSize *= 2;
							}
							if (opts.inSampleSize>1)
								opts.inSampleSize /= 2;
							
							opts.inJustDecodeBounds=false;
							
							opts.inPreferredConfig = Bitmap.Config.RGB_565;
							
							//Buffer buf = ByteBuffer.wrap(jpg);
							tmp = BitmapFactory.decodeByteArray(data, 0, data.length,opts);
							
							int newsize = Math.max(tmp.getWidth(),tmp.getHeight());
							float sizeScale = 1f;
							
							
							Bitmap tmpMutable;
							
							{
								Matrix matrixm = new Matrix();
								matrixm.setScale(sizeScale, sizeScale);
								tmpMutable = Bitmap.createBitmap((int)(sizeScale * tmp.getWidth()), (int) (sizeScale * tmp.getHeight()), 
										Bitmap.Config.RGB_565);
								Canvas c = new Canvas(tmpMutable);
								Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
								c.drawBitmap(tmp, matrixm, paint);
								c.setBitmap(Bitmap.createBitmap(1,1,Config.RGB_565));
								
							}
							
							 
							tmp.recycle();
							
							
							short rgb[] = new short[tmpMutable.getWidth()*tmpMutable.getHeight()];
							byte grayscale[] = new byte[tmpMutable.getWidth()*tmpMutable.getHeight()];
							
							
							
							Buffer buf = ShortBuffer.wrap(rgb);
							try{
							tmpMutable.copyPixelsToBuffer(buf);
							buf.position(0);
							
							for (int i = 0; i < tmpMutable.getWidth()*tmpMutable.getHeight(); i++){
								int color = rgb[i];
								int red = ((color & 0xf800) >> 11) << 3;
			                	int green = ((color & 0x07e0) >> 5) << 2;
			                	int blue = (color & 0x001f) << 3;
			                	int gray = (red + green + blue) / 3;
			                	
								grayscale[i] = (byte) gray;
							}
							
							
							} catch (Exception e){
								
								String s = e.getMessage();
								s+="";
							}
							tmp.recycle();
							tmpMutable.recycle();
							rgb = null;
							
							CameraManager cm = CameraManager.get();
							Message message = cm.previewCallback.previewHandler.obtainMessage(cm.previewCallback.previewMessage+1, p.getPictureSize().width, p.getPictureSize().height, grayscale);
							message.sendToTarget();
								
							CameraManager.get().previewing = true;
							CameraManager.get().camera.startPreview();
								/*CameraManager.get().closeDriver();
								try {
									CameraManager.get().openDriver(null);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}*/
								
							//CameraManager.get().configManager.setDesiredCameraParameters(CameraManager.get().camera);
							
							/*Message message = autoFocusHandler.obtainMessage(autoFocusMessage, 1, size, data);
				            // Simulate continuous autofocus by sending a focus request every
				            // AUTOFOCUS_INTERVAL_MS milliseconds.
				            autoFocusHandler.sendMessage(message);
				            autoFocusHandler = null;*/
							
						}
						
						if (autoFocusHandler != null)
				        {
				            Message message = autoFocusHandler.obtainMessage(autoFocusMessage, false);
				            // Simulate continuous autofocus by sending a focus request every
				            // AUTOFOCUS_INTERVAL_MS milliseconds.
				            autoFocusHandler.sendMessageDelayed(message, AUTOFOCUS_INTERVAL_MS);
				            autoFocusHandler = null;
				        }
						
							
						
					}
				});
    		}
    	
    	} else
        if (autoFocusHandler != null)
        {
            Message message = autoFocusHandler.obtainMessage(autoFocusMessage, success);
            // Simulate continuous autofocus by sending a focus request every
            // AUTOFOCUS_INTERVAL_MS milliseconds.
            autoFocusHandler.sendMessageDelayed(message, AUTOFOCUS_INTERVAL_MS);
            autoFocusHandler = null;
        }
    }

}
