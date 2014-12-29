/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.gpstestcase;

import java.util.Iterator;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.SystemProperties;

import com.compal.genericfunction.*;

public class GpsTestCaseActivity extends Activity implements LocationListener{
	
	/**0223_1400**/
	
	public static final long TEST_TIME_OUT = 60000;
	public static final int TIME_OUT = 1;
	public static final String pkgName = "com.compal.gpstestcase";
	public static final String mDomain = "gps";
	public static final String tGpsBasicFunc = "gps basic function";
	public static final String dGpsOn = "gps on";
	public static final String dGpsOff = "gps off";
	public static final String dSatelliteInformation = "satellite information";
	public static final String sId = "ID";
	static final String tag = "GpsTest"; // for Log
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	String mRemark3 = "";
	String iGpsOn;
	String iGpsOff;
	String iSatelliteInformation;
	
	TextView satellites_num;
	TextView txtInfo;
	TextView satellites_status;
	TextView after60secResult;
	TextView showSec;
	LocationManager lm;
	GpsStatus gpsStatus;
	
	int numSatellites = 0;
	
	boolean GpsBeTest = false;
	boolean isCallbackCalled = false;
	long timeStart;
	long elapsed;
	boolean resultIsWriten = false;
		
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        findView(); 
        loadConfig();
        loadId();
        Log.i(tag, Boolean.toString(GpsBeTest));
        if(!GpsBeTest){
        	SystemProperties.set("persist.sys.test","GpsTestIsdone");
        	finish();
        }
        
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(lm != null)
        	lm.addGpsStatusListener(listener);
        else        	 
        	outputTotalFalseResult();  //should output total false result
        
        timeStart = System.currentTimeMillis();
        
        if(GpsBeTest){
        	offStateTest();
        	onStateTest();
        } 
        if(GpsBeTest)
        	mHandler.sendEmptyMessageDelayed(TIME_OUT,TEST_TIME_OUT);
        else{
        	SystemProperties.set("persist.sys.test","GpsTestIsdone");
        	finish();
        }
    }
       
    public void loadId(){
    	iGpsOn = getTestCaseId(tGpsBasicFunc, dGpsOn, sId);
    	iGpsOff = getTestCaseId(tGpsBasicFunc, dGpsOff, sId);
    	iSatelliteInformation = getTestCaseId(tGpsBasicFunc, dSatelliteInformation, sId);
    }
    
    public String getTestCaseId(String mType, String mDescription, String mTag){
		
		Log.i(tag, "getTestCaseId");
		
		return GenericFunction.getTag(pkgName, mDomain, mType, mDescription, mTag);
	}
    
    private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(!isCallbackCalled && !resultIsWriten && GpsBeTest){
				mLog = "no received satellite, callback not be called";
				outputResult(false, tGpsBasicFunc, dSatelliteInformation, iSatelliteInformation);
				resultIsWriten = true;
			}
			
			if(resultIsWriten){
				SystemProperties.set("persist.sys.test","GpsTestIsdone");
	        	finish();
			}
			
		}
	};
    
	private void outputTotalFalseResult(){
		mLog = "LocationManager is null";
		outputResult(false, tGpsBasicFunc, dGpsOn, iGpsOn);
		mLog = "LocationManager is null";
		outputResult(false, tGpsBasicFunc, dGpsOff, iGpsOff);
		mLog = "LocationManager is null";
		outputResult(false, tGpsBasicFunc, dSatelliteInformation, iSatelliteInformation);
	}
	
    private void onStateTest(){
    	mLog = "test on state";
    	if(Settings.Secure.isLocationProviderEnabled(getContentResolver(), 
        		LocationManager.GPS_PROVIDER)){
        
    		checkTurnOnGps();
    	}
    	else{ 
    		toggleGpsOn();
    		
    		checkTurnOnGps();
    	}
    }
    
    private void checkTurnOnGps(){
    	if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
    		outputResult(true, tGpsBasicFunc, dGpsOn, iGpsOn);
		else
			outputResult(false, tGpsBasicFunc, dGpsOn, iGpsOn);
    }
    
    private void offStateTest(){
    	mLog = "test off state";
    	if(Settings.Secure.isLocationProviderEnabled(getContentResolver(), 
        		LocationManager.GPS_PROVIDER)){
    		//satellites_status.setText("LocationProviderEnabled : true");
    		toggleGpsOff();
    		checkTurnOffGps();
    	}else{
    		checkTurnOffGps();
    	}
    }
    
    private void checkTurnOffGps(){
    	if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))	
    		outputResult(true, tGpsBasicFunc, dGpsOff, iGpsOff);
		else
			outputResult(false, tGpsBasicFunc, dGpsOff, iGpsOff);
    }
    
    private void loadConfig(){
    	GpsBeTest = GenericFunction.getConfig(pkgName, mDomain, tGpsBasicFunc); 
    }
    
    private void findView(){
    	
 		satellites_status = (TextView) findViewById(R.id.satellites_status);
        satellites_num = (TextView) findViewById(R.id.satellites_num);
        after60secResult = (TextView) findViewById(R.id.after60secResult);
        showSec = (TextView) findViewById(R.id.showSec);
    }
    
    public void toggleGpsOn(){
    	Settings.Secure.setLocationProviderEnabled(getContentResolver(),
    			LocationManager.GPS_PROVIDER, true);
    }
    public void toggleGpsOff(){
    	Settings.Secure.setLocationProviderEnabled(getContentResolver(),
    			LocationManager.GPS_PROVIDER, false);
    }
    
    private void GpsInOpenSpaceCanGetAtLeast3SvResult(int count){ 
    	mLog = "time passed "+ Long.toString(elapsed) + 
    			" and receieve " + Integer.toString(count) + " SV" +
    				"listener can be callback so test result is OK";
    	if(!resultIsWriten){
    		outputResult(true, tGpsBasicFunc, dSatelliteInformation, iSatelliteInformation);
    		after60secResult.setText("callback called " + Integer.toString(count) +" SV" );
    		resultIsWriten = true;
    	}
    }
    
    GpsStatus.Listener listener = new GpsStatus.Listener() {
    	public void onGpsStatusChanged(int event) {
    		
    		isCallbackCalled = true;
    		
    		Log.i(tag,"start Time :" + Long.toString(timeStart));
    		
            switch (event) {
            
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Log.i(tag, "GPS_EVENT_FIRST_FIX");
                break;
            
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
            	
                Log.i(tag, "GPS_EVENT_SATELLITE_STATUS");
                
                GpsStatus gpsStatus = lm.getGpsStatus(null);
                
                int maxSatellites = gpsStatus.getMaxSatellites();
                 
                Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                int count = 0;     
                
                while (iters.hasNext() && count <= maxSatellites) {
                    GpsSatellite s = iters.next();     
                    count++;     
                }   
                
                elapsed = System.currentTimeMillis() - timeStart;
                Log.i(tag,"start Time :" + Long.toString(elapsed));
                
                numSatellites = count;
                
                showSec.setText("elapsed : " + Long.toString(elapsed));
                
                GpsInOpenSpaceCanGetAtLeast3SvResult(numSatellites);
                
                satellites_num.setText(Integer.toString(count));
 
                break;
            
            case GpsStatus.GPS_EVENT_STARTED:    
                break;
            
            case GpsStatus.GPS_EVENT_STOPPED:
                break;
            }
    	}
    };

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
		Log.v(tag, "Location Changed");
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onResume() {
		
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		/* GPS, as it turns out, consumes battery like crazy */
		lm.removeUpdates(this);
		super.onResume();
	}
    
	public void outputResult(boolean result,String sType ,String sDescription, String sCaseId){
		GenericFunction.putResult(pkgName, mDomain, sType, sDescription,
				result, sCaseId, mLog, mRemark1, mRemark2, mRemark3);
	}
	
	@Override
	protected void onStop() {
		/* may as well just finish since saving the state is not important for this toy app */
		finish();
		super.onStop();
	}
	
}
