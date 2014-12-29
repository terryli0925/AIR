/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.suspendtest;

import java.lang.System;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.SystemProperties;

import com.compal.genericfunction.GenericFunction;

public class SuspendTestActivity extends Activity {
	private final static String TAG = "SuspendTest";
	private final static String mPKGName = "com.compal.suspendtest";
	private final static String mDomain = "suspend";
	private final static String mType = "suspend";
	private final static String mDescription = "suspend";
	private final static int TIMEOUT = 1;
	
	TextView TestCount;
	Button ButtonStop;
	
	private AlarmManager alarms;
	private PowerManager pm;
	private Calendar calendar;
	private PendingIntent pendingIntent;
	
	boolean TestSuspend = false;
	boolean EnableByShell = false;
	int TestCountInt = 0;
	int TotalTest = 0;
	int SuspendDelayTime = 0;
	int DelayTriggerTime = 0;	//sec
	int CheckTimeout = 0;	//Millis
	long GoToSuspend = 0;
	String mId = "";
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	String mRemark3 = "";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);
                
        getProperty();
        
        findView();
        
        /* Check if test suspend */
        if(TestCountInt <= TotalTest){
        	/* Only shell enable need, and read once */
        	if(EnableByShell && TestCountInt == 1){
        		getConfig();
        	}
        
        	getService();
        
        	if(!isFinishing()){
        		setTimeout();
        	
        		/* Set Handler Timeout */
        		mHandler.sendEmptyMessageDelayed(TIMEOUT, CheckTimeout);

        		runThread();
        	}
        	
        }else{
        	TestCount.setText("Total test: "+TotalTest+"\n\nSuspend/Resume Test Finish");
        }
        
        ButtonStop.setOnClickListener( new View.OnClickListener(){
            public void onClick(View v) {   	
            	SystemProperties.set("persist.sys.test","SuspendTestIsdone");
            	
            	if(!EnableByShell){
            		Log.i(TAG, "Intent: Start SuspendTestMain");
            		
            		Intent StartSuspendTestMainIntent = new Intent("StartSuspendTestMain");
            		StartSuspendTestMainIntent.setClass(SuspendTestActivity.this, SuspendTestMain.class);	        	  
            		startActivity(StartSuspendTestMainIntent);	
            	}

            	Log.i(TAG, "Finish SuspendTestActivity by stop button");
                finish();
            }
    	});  
    }
    
    private void getProperty(){
    	Log.i(TAG, "getProperty");
    	
    	EnableByShell = Boolean.parseBoolean(SystemProperties.get("persist.sys.enablebyshell"));
    	TestCountInt = Integer.parseInt(SystemProperties.get("persist.sys.suspendtestcount"));
        TotalTest = Integer.parseInt(SystemProperties.get("persist.sys.suspendtotaltest"));
        SuspendDelayTime = Integer.parseInt(SystemProperties.get("persist.sys.suspenddelaytime"));
        DelayTriggerTime = Integer.parseInt(SystemProperties.get("persist.sys.suspendtriggertime"));
        
        Log.i(TAG, "Is enable by shell: "+EnableByShell);
        Log.i(TAG, "Test count: "+TestCountInt);
  	    Log.i(TAG, "Total test: "+TotalTest);
  	    Log.i(TAG, "Suspend delay time(s): "+SuspendDelayTime);
  	    Log.i(TAG, "Delay trigger time(s): "+DelayTriggerTime);
    }
    
    private void findView(){
    	Log.i(TAG, "findView");
    	
    	TestCount = (TextView)findViewById(R.id.test_count);
    	TestCount.setText("Count of suspend test: "+TestCountInt+"\n\nSuspend delay time: "+SuspendDelayTime+"s\n\nDelay trigger time: "+DelayTriggerTime+"s");
    	
    	ButtonStop = (Button)findViewById(R.id.stop);
    }
    
    private void getConfig(){    	
    	Log.i(TAG, "getConfig");
    		
    	TestSuspend = GenericFunction.getConfig(mPKGName, mDomain, mType);
    	mId = GenericFunction.getTag(mPKGName, mDomain, mType, mDescription, "ID");
    	
    	if(!TestSuspend){
    		Log.i(TAG, "Not need to test suspend");
    		
    		SystemProperties.set("persist.sys.test","SuspendTestIsdone");
    		finish();
    	}
    	
    	
    }
    
    private void getService(){
    	Log.i(TAG, "getService");
    	
    	if(!isFinishing()){
    		alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    		if(alarms == null){
    			Log.i(TAG,"Can't get ALARM_SERVICE");
    			mLog += "Can't get ALARM_SERVICE\n"; 		
    		}
    	
    		pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
    		if(pm == null){
    			Log.i(TAG,"Can't get POWER_SERVICE");
    			mLog += "Can't get POWER_SERVICE\n"; 		
    		}
    		
    		if(alarms == null || pm == null){
    			Log.i(TAG, "Count of test: "+TestCountInt+" -> SuspendTest doesn't success");
    			mRemark1 = "Count of test: "+TestCountInt;
    	    	mLog += "Count of test: "+TestCountInt+" -> SuspendTest doesn't success\n\n";
    	    	
    			GenericFunction.putResult(mPKGName, mDomain, mType, mDescription, false, mId, mLog, mRemark1, mRemark2, mRemark3);
    		
    			SystemProperties.set("persist.sys.test","SuspendTestIsdone");
    			finish();
    		}
    	}
    }
    
    private void setTimeout(){
    	Log.i(TAG, "setTimeout");
    	
    	//CheckTimeout = (DelayTriggerTime + SuspendDelayTime + 5) * 1000;
    	CheckTimeout = (SuspendDelayTime + 10) * 1000;
        Log.i(TAG, "CheckTimeout(ms): "+CheckTimeout);
    }
    
    private void runThread(){
    	Thread thread = new Thread(new Runnable()  
		{  
			@Override  
			public void run()  
			{  
				Log.i(TAG, "Run thread"); 
            
				sleep();
				
				if(!isFinishing()){
					getCurrentTime();  

					setAlarm();
            
					Log.i(TAG, "Go to sleep");        	
					pm.goToSleep(SystemClock.uptimeMillis());
				
					Log.i(TAG, "Finish SuspendTestActivity by thread");
					finish();	
				}
				
			}  
		});
	
		thread.start();
    }
    
    private void sleep(){ 
		Log.i(TAG, "Sleep(s): "+SuspendDelayTime);
		
		try { 
			Thread.sleep(SuspendDelayTime*1000);
		} 
		catch(InterruptedException e) { 
			Log.i(TAG, "Error:"+e);
			mLog += "Error:"+e;
		}
	}  
    
    private void getCurrentTime(){
    	Log.i(TAG, "getCurrentTime");
    	
    	calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        
        Log.i(TAG, "Get current time: "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND));
    }
    
    private void setAlarm(){    	
    	Log.i(TAG, "setAlarm");
    	
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("SuspendTestIntent"), PendingIntent.FLAG_CANCEL_CURRENT);
        
        calendar.add(Calendar.SECOND, DelayTriggerTime);
        
        Log.i(TAG, "Set alarm time: "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND));
        
        alarms.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
    
    private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(!isFinishing()){
				Log.i(TAG, "Count of test: "+TestCountInt+" -> Activity Timeout, SuspendTest doesn't success");
				mRemark1 = "Count of test: "+TestCountInt;
		    	mLog += "Count of test: "+TestCountInt+" -> Activity Timeout, SuspendTest doesn't success\n\n";
			
				GenericFunction.putResult(mPKGName, mDomain, mType, mDescription, false, mId, mLog, mRemark1, mRemark2, mRemark3);
				
				SystemProperties.set("persist.sys.test","SuspendTestIsdone");
				finish();
			}
		}
    };
}