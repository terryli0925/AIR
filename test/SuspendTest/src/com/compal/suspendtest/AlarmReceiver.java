/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.suspendtest;

import com.compal.genericfunction.GenericFunction;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.os.SystemProperties;

public class AlarmReceiver extends BroadcastReceiver {
	private final static String TAG = "SuspendTest";
	private final static String mPKGName = "com.compal.suspendtest";
	private final static String mDomain = "suspend";
	private final static String mType = "suspend";
	private final static String mDescription = "suspend";
	
	private KeyguardManager km;
	private PowerManager pm;
	private WakeLock wakelock;
	
	boolean GetService = false;
	boolean isScreenOn = false;
	boolean getWakeLock = false;
	int TestCount = 0;
	int TotalTest = 0;
	String mId = "";
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	String mRemark3 = "";
	
	@Override
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		if (action.equals("SuspendTestIntent")) {
			Log.i(TAG, "Receive suspend test intent");
		
			getService(context);
				
			if(GetService){
				getProperty();
				
				getScreenStatus();		

				if(isScreenOn == false){
					unlockKeyguard();
	        
					getWakeLock = setWakeLock();
					
					if(getWakeLock){
						putResult(true);
		
						Log.i(TAG, "Wakelock release");
						wakelock.release();
					}else{
						putResult(false);
					}
				}else{
					Log.i(TAG, "Resume doesn't by alarm");
			    	mLog += "SuspendTest false: Resume doesn't by alarm\n";
					
			    	putResult(false);
				}						
			
			
				TestCount++;
				SystemProperties.set("persist.sys.suspendtestcount", String.valueOf(TestCount));	
				Log.i(TAG, "Test count: "+TestCount);
			
				Log.i(TAG, "Intent: Start SuspendTestAcivity");	
				
				Intent StartSuspendTestIntent = new Intent(context, SuspendTestActivity.class);
				StartSuspendTestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				context.startActivity(StartSuspendTestIntent);
			}
		}
		
	}
	
	private void getService(Context context){
		Log.i(TAG, "getService");
		
		km = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
		if(km == null){
    		Log.i(TAG,"Can't get KEYGUARD_SERVICE");
			mLog += "Can't get KEYGUARD_SERVICE\n"; 		
    	}
		
		pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		if(pm == null){
    		Log.i(TAG,"Can't get POWER_SERVICE");
			mLog += "Can't get POWER_SERVICE\n"; 		
    	}
		
		if(km == null || pm == null){
			mLog += "SuspendTest doesn't success\n";
			
			putResult(false);
			SystemProperties.set("persist.sys.test","SuspendTestIsdone");
		}else{
			GetService = true;
		}
	}
	
	private void getProperty(){
    	Log.i(TAG, "getProperty");
    	
        TestCount = Integer.parseInt(SystemProperties.get("persist.sys.suspendtestcount"));
        TotalTest = Integer.parseInt(SystemProperties.get("persist.sys.suspendtotaltest"));
        
        Log.i(TAG, "Test count: "+TestCount);
  	    Log.i(TAG, "Total test: "+TotalTest);	
    }
	
	private void getScreenStatus(){
		Log.i(TAG, "getScreenStatus");
		
		isScreenOn = pm.isScreenOn();
		Log.i(TAG, "isScreenOn: "+isScreenOn);
	}
	
	private void unlockKeyguard(){
		Log.i(TAG, "unlockKeyguard");
		
		//Keyguard unlock		
        km.newKeyguardLock(TAG).disableKeyguard();
	}
	
	private boolean setWakeLock(){
		Log.i(TAG, "setWakeLock");

		wakelock = pm.newWakeLock(PowerManager.ON_AFTER_RELEASE|PowerManager.FULL_WAKE_LOCK, TAG);
		if(wakelock == null){
			Log.i(TAG,"Can't get wakelock");
			mLog += "Can't get wakelock\n"; 
			
			return false;
		}else{
			wakelock.acquire();
			
			return true;
		}
	}
	
	private void putResult(boolean result){
		if(result){
			Log.i(TAG, "Count of test: "+TestCount+" -> SuspendTest success");
	    	mRemark1 = "Count of test: "+TestCount;
	    	mLog += "Count of test: "+TestCount+" -> SuspendTest success\n\n";	
		}else{
			Log.i(TAG, "Count of test: "+TestCount+" -> SuspendTest doesn't success");
			mRemark1 = "Count of test: "+TestCount;
	    	mLog += "Count of test: "+TestCount+" -> SuspendTest doesn't success\n\n";
		}
		mId = GenericFunction.getTag(mPKGName, mDomain, mType, mDescription, "ID");
		
		GenericFunction.putResult(mPKGName, mDomain, mType, mDescription, result, mId, mLog, mRemark1, mRemark2, mRemark3);
	}
};