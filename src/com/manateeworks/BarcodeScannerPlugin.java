package com.manateeworks;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;

public class BarcodeScannerPlugin extends CordovaPlugin {
	
	  // !!! Rects are in format: x, y, width, height !!!
    public static final Rect RECT_LANDSCAPE_1D = new Rect(0, 20, 100, 60);
    public static final Rect RECT_LANDSCAPE_2D = new Rect(20, 5, 60, 90);
    public static final Rect RECT_PORTRAIT_1D = new Rect(20, 0, 60, 100);
    public static final Rect RECT_PORTRAIT_2D = new Rect(20, 5, 60, 90);
    public static final Rect RECT_FULL_1D = new Rect(0, 0, 100, 100);
    public static final Rect RECT_FULL_2D = new Rect(20, 5, 60, 90);
	private static CallbackContext cbc;
	private static String lastType;

    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
       
    	if ("initDecoder".equals(action)) {
         	
            initDecoder();
            callbackContext.success();
         	return true;
             
         } 
    	else if ("startScanner".equals(action)) {
        	
        	cbc = callbackContext;
        	Context context=this.cordova.getActivity().getApplicationContext();
        	Intent intent = new Intent(context, com.manateeworks.ScannerActivity.class);
			this.cordova.startActivityForResult(this, intent, 1);
            return true;
            
        } 
        else if ("getLastType".equals(action)) {
        	
            callbackContext.success(lastType);
        	return true;
            
        } 
        else if ("setLevel".equals(action)) {
        	
            BarcodeScanner.MWBsetLevel(args.getInt(0));
        	return true;
            
        } 
       
        else if ("setActiveCodes".equals(action)) {
        	
            BarcodeScanner.MWBsetActiveCodes(args.getInt(0));
        	return true;
            
        } 
    	
        else if ("setActiveSubcodes".equals(action)) {
        	
            BarcodeScanner.MWBsetActiveSubcodes(args.getInt(0), args.getInt(1));
        	return true;
            
        } 
    	
        else if ("setFlags".equals(action)) {
        	
            callbackContext.success(BarcodeScanner.MWBsetFlags(args.getInt(0), args.getInt(1)));
        	return true;
            
        } 
    	
        else if ("setDirection".equals(action)) {
        	
            BarcodeScanner.MWBsetDirection(args.getInt(0));
        	return true;
            
        } 
    	
        else if ("setScanningRect".equals(action)) {
        	
            BarcodeScanner.MWBsetScanningRect(args.getInt(0), args.getInt(1), args.getInt(2), args.getInt(3), args.getInt(4));
        	return true;
            
        } 
        
        return false;
    }
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	
    	
    	if (requestCode == 1){
    	
	    	if (resultCode == 1){
	    		JSONObject jsonResult = new JSONObject();
	    		try {
					jsonResult.put("code", intent.getStringExtra("code"));
					jsonResult.put("type", intent.getStringExtra("type"));
					
					JSONArray rawArray= new JSONArray();
					byte[] bytes = intent.getByteArrayExtra("bytes");
					for(int i=0;i<bytes.length;i++) {
					    rawArray.put((int)(0xff & bytes[i]));
					}
					
					jsonResult.put("bytes", rawArray);
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	    		cbc.success(jsonResult);
	    		
	    		
	    	} else 
	    		if (resultCode == 0){
		    		JSONObject jsonResult = new JSONObject();
		    		try {
						jsonResult.put("code", "");
						jsonResult.put("type", "");
						jsonResult.put("bytes", "");
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		
		    		cbc.success(jsonResult);
		    		
		    		
		    	}
	    		
    	}
    }
    
    
    public static void initDecoder() {
    	
		// register your copy of the mobiScan SDK with the given user name / key
        BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_25,     "username", "key");
        BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_39,     "username", "key");
        BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_93,     "username", "key");
        BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_128,    "username", "key");
        BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_AZTEC,  "username", "key");
        BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_DM,     "username", "key");
        BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_EANUPC, "username", "key");
        BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_PDF,    "username", "key");
        BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_QR,     "username", "key");
        BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_RSS,    "username", "key");
        BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_CODABAR,"username", "key");
        
        // choose code type or types you want to search for
        
        // Our sample app is configured by default to search all supported barcodes...
        BarcodeScanner.MWBsetActiveCodes(BarcodeScanner.MWB_CODE_MASK_25     | 
                                         BarcodeScanner.MWB_CODE_MASK_39     |
                                         BarcodeScanner.MWB_CODE_MASK_93     | 
                                         BarcodeScanner.MWB_CODE_MASK_128    | 
                                         BarcodeScanner.MWB_CODE_MASK_AZTEC  | 
                                         BarcodeScanner.MWB_CODE_MASK_DM     |
                                         BarcodeScanner.MWB_CODE_MASK_EANUPC | 
                                         BarcodeScanner.MWB_CODE_MASK_PDF    | 
                                         BarcodeScanner.MWB_CODE_MASK_QR     | 
                                         BarcodeScanner.MWB_CODE_MASK_CODABAR|
                                         BarcodeScanner.MWB_CODE_MASK_RSS);

        // But for better performance, only activate the symbologies your application requires...
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_25 ); 
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_39 );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_93 ); 
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_128 ); 
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_AZTEC ); 
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_DM ); 
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_EANUPC ); 
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_PDF ); 
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_QR ); 
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_RSS );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_CODABAR ); 
        

        // Our sample app is configured by default to search both directions...
        BarcodeScanner.MWBsetDirection(BarcodeScanner.MWB_SCANDIRECTION_HORIZONTAL | BarcodeScanner.MWB_SCANDIRECTION_VERTICAL);
        // set the scanning rectangle based on scan direction(format in pct: x, y, width, height)
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_25,     RECT_FULL_1D);     
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_39,     RECT_FULL_1D);     
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_93,     RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_128,    RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_AZTEC,  RECT_FULL_2D);    
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DM,     RECT_FULL_2D);    
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_EANUPC, RECT_FULL_1D);     
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF,    RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR,     RECT_FULL_2D);     
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_RSS,    RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_CODABAR,RECT_FULL_1D); 

        // But for better performance, set like this for PORTRAIT scanning...
        // BarcodeScanner.MWBsetDirection(BarcodeScanner.MWB_SCANDIRECTION_VERTICAL);
        // set the scanning rectangle based on scan direction(format in pct: x, y, width, height)
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_25,     RECT_PORTRAIT_1D);     
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_39,     RECT_PORTRAIT_1D); 
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_93,     RECT_PORTRAIT_1D); 
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_128,    RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_AZTEC,  RECT_PORTRAIT_2D);    
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DM,     RECT_PORTRAIT_2D);    
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_EANUPC, RECT_PORTRAIT_1D);     
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF,    RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR,     RECT_PORTRAIT_2D);     
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_RSS,    RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_CODABAR,RECT_PORTRAIT_1D); 
        
        // or like this for LANDSCAPE scanning - Preferred for dense or wide codes...
        // BarcodeScanner.MWBsetDirection(BarcodeScanner.MWB_SCANDIRECTION_HORIZONTAL);
        // set the scanning rectangle based on scan direction(format in pct: x, y, width, height)
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_25,     RECT_LANDSCAPE_1D);     
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_39,     RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_93,     RECT_LANDSCAPE_1D); 
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_128,    RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_AZTEC,  RECT_LANDSCAPE_2D);    
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DM,     RECT_LANDSCAPE_2D);    
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_EANUPC, RECT_LANDSCAPE_1D);     
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF,    RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR,     RECT_LANDSCAPE_2D);     
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_RSS,    RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_CODABAR,RECT_LANDSCAPE_1D); 
        
        
        // set decoder effort level (1 - 5)
        // for live scanning scenarios, a setting between 1 to 3 will suffice
		// levels 4 and 5 are typically reserved for batch scanning 
        BarcodeScanner.MWBsetLevel(2);
		
	}
    
    
}