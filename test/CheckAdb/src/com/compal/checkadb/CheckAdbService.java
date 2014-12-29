/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.checkadb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.os.SystemProperties;

public class CheckAdbService extends Service {
	private final static String TAG = "CheckAdb";
	
	int AdbCheckTime = 0;
	String mAdbState = ""; 
	
	@Override
    public void onCreate() {	
		
    }
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	
    	AdbCheckTime = intent.getIntExtra("android.intent.extras.TEST_TIME", 0) * 1000;
    	Log.i(TAG, "CheckTime: "+AdbCheckTime);    	

    	Thread thread=new Thread(new Runnable()  
        {  
            @Override  
            public void run()  
            {  
            	Log.i(TAG, "Run thread");  
                
            	long startTime = System.currentTimeMillis();

            	while((System.currentTimeMillis() - startTime) < AdbCheckTime ){ 
            		sleep(1000);
            	}
            	
            	getProperty();
            	
            	if(mAdbState.equals("0")){
         		   restartDebugMode();
         	    }
            	
            	stopSelf();
            }  
        });  
        thread.start();  
    	
        return START_STICKY;
    }
    
    private void getProperty(){   	
    	mAdbState = SystemProperties.get("persist.sys.adbstate");
    	Log.i(TAG, "persist.sys.adbstate: "+mAdbState);
    }
	
	private void sleep(int time){
		Log.i(TAG, "sleep"); 
		
		try { 
			//Thread.sleep(AdbCheckTime);
			Thread.sleep(time);
		} 
		catch(InterruptedException e) { 
			Log.i(TAG, "Error:"+e);
		}
	}
	
	private void restartDebugMode(){
		Log.i(TAG, "restartDebugMode");
		
		Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
    	Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 1);
	}
	
	@Override
    public void onDestroy() {
    	Log.i(TAG, "onDestroy");
    }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
