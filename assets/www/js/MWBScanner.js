/**
   * @name Basic return values for API functions
   * @{
   */
  var  MWB_RT_OK =                  	  0;
  var  MWB_RT_FAIL =                	 -1;
  var  MWB_RT_NOT_SUPPORTED =       	 -2;
  var  MWB_RT_BAD_PARAM =            	 -3;
  
  

  /** @brief  Code39 decoder flags value: require checksum check
   */
  var  MWB_CFG_CODE39_REQUIRE_CHECKSUM =  0x2;
  /**/
  
  /** @brief  Code39 decoder flags value: don't require stop symbol - can lead to false results
   */
  var  MWB_CFG_CODE39_DONT_REQUIRE_STOP = 0x4;
  /**/
      
  /** @brief  Code39 decoder flags value: decode full ASCII
   */
  var MWB_CFG_CODE39_EXTENDED_MODE =      0x8;
  /**/
  
  /** @brief  Code93 decoder flags value: decode full ASCII
   */
  var MWB_CFG_CODE93_EXTENDED_MODE =      0x8;
  /**/


  /** @brief  Code25 decoder flags value: require checksum check
  */
  var  MWB_CFG_CODE25_REQ_CHKSUM =        0x1;
 /**/
  
  /** @brief  Codabar decoder flags value: include start/stop symbols in result
  */
  var  MWB_CFG_CODABAR_INCLUDE_STARTSTOP =        0x1;
 /**/

  /** @brief  Global decoder flags value: apply sharpening on input image
   */
  var  MWB_CFG_GLOBAL_HORIZONTAL_SHARPENING =          0x01;
  var  MWB_CFG_GLOBAL_VERTICAL_SHARPENING =            0x02;
  var  MWB_CFG_GLOBAL_SHARPENING =                     0x03;
  
  /** @brief  Global decoder flags value: apply rotation on input image
   */
  var  MWB_CFG_GLOBAL_ROTATE90 =                       0x04;
  
  /**
   * @name Bit mask identifiers for supported decoder types
   * @{ */
  var MWB_CODE_MASK_NONE =             0x00000000;
  var MWB_CODE_MASK_QR =               0x00000001;
  var MWB_CODE_MASK_DM =               0x00000002;
  var MWB_CODE_MASK_RSS =              0x00000004;
  var MWB_CODE_MASK_39 =               0x00000008;
  var MWB_CODE_MASK_EANUPC =           0x00000010;
  var MWB_CODE_MASK_128 = 	           0x00000020;
  var MWB_CODE_MASK_PDF = 	           0x00000040;
  var MWB_CODE_MASK_AZTEC =	           0x00000080;
  var MWB_CODE_MASK_25 =	           0x00000100;
  var MWB_CODE_MASK_93 =               0x00000200;
  var MWB_CODE_MASK_CODABAR =          0x00000400;
  var MWB_CODE_MASK_ALL =              0xffffffff;
  /** @} */
  
  
  /**
   * @name Bit mask identifiers for RSS decoder types
   * @{ */
  var MWB_SUBC_MASK_RSS_14 =           0x00000001;
  var MWB_SUBC_MASK_RSS_LIM =          0x00000004;
  var MWB_SUBC_MASK_RSS_EXP =          0x00000008;
  /** @} */
  
  /**
   * @name Bit mask identifiers for Code 2 of 5 decoder types
   * @{ */
  var MWB_SUBC_MASK_C25_INTERLEAVED =  0x00000001;
  var MWB_SUBC_MASK_C25_STANDARD =     0x00000002;
  /** @} */
  
  /**
   * @name Bit mask identifiers for UPC/EAN decoder types
   * @{ */
  var MWB_SUBC_MASK_EANUPC_EAN_13 =    0x00000001;
  var MWB_SUBC_MASK_EANUPC_EAN_8 =     0x00000002;
  var MWB_SUBC_MASK_EANUPC_UPC_A =     0x00000004;
  var MWB_SUBC_MASK_EANUPC_UPC_E =     0x00000008;
  /** @} */
  
  /**
   * @name Bit mask identifiers for 1D scanning direction 
   * @{ */
  var MWB_SCANDIRECTION_HORIZONTAL =   0x00000001;
  var MWB_SCANDIRECTION_VERTICAL =     0x00000002;
  var MWB_SCANDIRECTION_OMNI =         0x00000004;
  var MWB_SCANDIRECTION_AUTODETECT =   0x00000008;
  /** @} */
  
  var FOUND_NONE = 			0;
  var FOUND_DM = 			1;
  var FOUND_39 = 			2;
  var FOUND_RSS_14 = 		3;
  var FOUND_RSS_14_STACK = 	4;
  var FOUND_RSS_LIM = 		5;
  var FOUND_RSS_EXP = 		6;
  var FOUND_EAN_13 = 		7;
  var FOUND_EAN_8 = 		8;
  var FOUND_UPC_A = 		9;
  var FOUND_UPC_E = 		10;
  var FOUND_128 = 			11;
  var FOUND_PDF = 			12;
  var FOUND_QR = 			13;
  var FOUND_AZTEC= 			14;
  var FOUND_25_INTERLEAVED =15;
  var FOUND_25_STANDARD =   16; 
  var FOUND_93 = 			17;
  var FOUND_CODABAR =		18;
  
  
  
  var BarcodeScanner = {
	
	 MWBinitDecoder: function(callback) 
      {
		 cordova.exec(callback, function(){}, "MWBarcodeScanner", "initDecoder", []);
	  },
	  
	MWBstartScanning: function(callback) 
    {
	 	cordova.exec(callback, function(err) 
	 	{
		 	callback('Error: ' + err);
		 }, "MWBarcodeScanner", "startScanner", []);
  	 },
  		  
	MWBsetActiveCodes: function(activeCodes) 
    {
   	     cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setActiveCodes", [activeCodes]);
   	},
   	
   	MWBsetActiveSubcodes: function(codeMask, activeSubcodes) 
    {
   	     cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setActiveSubcodes", [codeMask, activeSubcodes]);
   	},
   	
   	MWBsetFlags: function(codeMask, flags) 
    {
   	     cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setFlags", [codeMask, flags]);
   	},
   	
   	MWBsetDirection: function(direction) 
    {
   	     cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setDirection", [direction]);
   	},
   	
   	MWBsetScanningRect: function(codeMask, left, top, width, height) 
    {
   	     cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setScanningRect", [codeMask, left, top, width, height]);
   	},
      
    MWBsetLevel: function(level)
    {
         cordova.exec(function(){}, function(){}, "MWBarcodeScanner", "setLevel", [level]);
    }
	
  };
  
  
  var startScanning = function()
  {
   		//Initialize decoder with default params	
   		BarcodeScanner.MWBinitDecoder(function(){

			//You can set specific params after InitDecoder to optimize the scanner according to your needs

			//BarcodeScanner.MWBsetActiveCodes(MWB_CODE_MASK_39);
			//BarcodeScanner.MWBsetLevel(2);
   			//BarcodeScanner.MWBsetFlags(MWB_CODE_MASK_39, MWB_CFG_CODE39_EXTENDED_MODE);
   			//BarcodeScanner.MWBsetDirection(MWB_SCANDIRECTION_VERTICAL);
   			//BarcodeScanner.MWBsetScanningRect(MWB_CODE_MASK_39, 20,20,60,60);	

			// Call the barcode scanner screen
			 BarcodeScanner.MWBstartScanning(function(result) 
   			 {
                                    
				/*
				 result.code - string representation of barcode result
				 result.type - type of barcode detected
				 result.bytes - bytes array of raw barcode result
				 */

				if (result && result.code){
            		navigator.notification.alert(result.code, function(){}, result.type, 'Close');
				}
                        
			 });



		});

        
  }  
  
 