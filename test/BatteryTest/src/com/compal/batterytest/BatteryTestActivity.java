/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.batterytest;

import android.app.Activity;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Handler;
import android.widget.TextView;
import android.util.Log;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import com.compal.genericfunction.GenericFunction;


public class BatteryTestActivity extends Activity {
	private final static String TAG = "BatteryTest";
	private final static String PKGName = "com.compal.batterytest";
	private final static String mDomain = "Battery";
	private final static int CHECK_TIMEOUT = 30000;	//Millis
	private final static int TIMEOUT = 1;
	String technology;
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	
	TextView mPlugType, mStatus, mHealth, mLevel, mScale, mVoltage, mTemperature, mTechnology, mPresent;	
	int plugType, status, health, level, scale, voltage, temperature;
	boolean present = false;
	boolean TestBatteryStatus = false;
	boolean resultIsWriten = false;	

	private IntentFilter filter;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        SystemProperties.set("persist.sys.test", "BatteryIsTesting");
        
        TestBatteryStatus = GenericFunction.getConfig(PKGName, mDomain, "BatteryStatus");
        if(!TestBatteryStatus){
        	Log.i(TAG,"Not need to test battery");
        	
        	SystemProperties.set("persist.sys.test","BatteryTestIsdone");
        	finish();
        }
        
        if(!isFinishing()){
        	Log.i(TAG, "Not finishing");
        	
        	mHandler.sendEmptyMessageDelayed(TIMEOUT, CHECK_TIMEOUT);
        	
        	// create the IntentFilter that will be used to listen to battery status broadcasts
        	filter = new IntentFilter();
        	filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
		registerReceiver(mBroadcastReceiver, filter);
	}
    
    @Override
    public void onPause() {
        super.onPause();
		unregisterReceiver(mBroadcastReceiver);
	}
    
    private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.i(TAG,"Timeout --> Close Activity");
			
			if(!resultIsWriten){
				Log.i(TAG,"Put false result");
				
				mLog = "BatteryTest doesn't success\n";
				GenericFunction.putResult(PKGName, mDomain, "BatteryStatus", "BatteryStatus", false, mLog, mRemark1, mRemark2);
					
				SystemProperties.set("persist.sys.test","BatteryTestIsdone");
				finish();
			}
		}
	};
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent){
			Log.i(TAG, "onReceive");
			
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				if(!resultIsWriten){
					status = intent.getIntExtra("status", 0);
					plugType = intent.getIntExtra("plugged", 0);
					level = intent.getIntExtra("level", 0);
					health = intent.getIntExtra("health", 0);
					present = intent.getBooleanExtra("present",false);				
					scale = intent.getIntExtra("scale", 0);
					voltage = intent.getIntExtra("voltage", 0);
					temperature = intent.getIntExtra("temperature",0);
					technology = intent.getStringExtra("technology");
				
					showBatteryStatus();							
					checkBatteryStatus();
					writeResult();
					
					Log.i(TAG, "BatteryStatus test end");
					SystemProperties.set("persist.sys.test","BatteryTestIsdone");
					finish();
				}	
			}
		}
	};
	
    private void showBatteryStatus(){
    	Log.i(TAG, "showBatteryStatus");
		Log.i(TAG,"plugType: "+plugType+", status: "+status+", " +"health: "+health);
		Log.i(TAG,"present: "+present+", scale: "+scale+", " +"temperature: "+temperature);
		Log.i(TAG,"Level: "+level+", voltage: "+voltage+", " +"technology: "+technology);
		
		mStatus = (TextView)findViewById(R.id.status);
		mPlugType = (TextView)findViewById(R.id.plugType);
		mLevel = (TextView)findViewById(R.id.level);
		mHealth = (TextView)findViewById(R.id.health);
		mPresent = (TextView)findViewById(R.id.present);	
		mScale = (TextView)findViewById(R.id.scale);
		mTemperature = (TextView)findViewById(R.id.temperature);
		mVoltage = (TextView)findViewById(R.id.voltage);
		mTechnology = (TextView)findViewById(R.id.technology);
	    
		mStatus.setText("Status : "+status);
		mPlugType.setText("PlugType : "+plugType);	
		mLevel.setText("Level : "+level);
		mHealth.setText("Health : "+health);
		mPresent.setText("Present : "+present);
		mScale.setText("Scale : "+scale);
		mTemperature.setText("Temperature : "+temperature);
		mVoltage.setText("Voltage : "+voltage);
		mTechnology.setText("Technology : "+technology);
	}
	
    private void checkBatteryStatus(){
    	Log.i(TAG, "checkBatteryStatus");
    	
    	//If battery status is unknown, we think it's fail.
    	if(status <= 0 || status == BatteryManager.BATTERY_STATUS_UNKNOWN || status > 5){
			mLog += "Status is " + status + " => Fail\n";
		}
		if(plugType != BatteryManager.BATTERY_PLUGGED_AC && plugType != BatteryManager.BATTERY_PLUGGED_USB){
			mLog += "Plugged is " + plugType + " => Fail\n";
		}				
		//If battery level is small than 4, we think it's fail.
		if(level <= 4 || level > 100 || level > scale){
			mLog += "Level is " + level + " => Fail\n";
		}
		
		//If battery health is unknown, we think it's fail.
		if(health <= 0 || health == BatteryManager.BATTERY_HEALTH_UNKNOWN || health > 7){
			mLog += "Health is " + health + " => Fail\n";
		}
		if(present == false){
			mLog += "Present is " + present + " => Fail\n";
		}
		if(scale  <= 0 || scale > 100){
			mLog += "Scale is " + level + " => Fail\n";
		}
		if(voltage <= 0){
			mLog += "Voltage is " + voltage + " => Fail\n";
		}
		if(temperature <= 0){
			mLog += "Temperature is " + temperature + " => Fail\n";
		}
		if(technology == ""){
			mLog += "Technology is null => Fail\n";
		}
    }
    
    private void writeResult(){
    	Log.i(TAG, "writeResult");
    	
    	if(mLog != ""){
    		GenericFunction.putResult(PKGName, mDomain, "BatteryStatus", "BatteryStatus", false, mLog, mRemark1, mRemark2);
		}else{
    		mLog = "BatteryStatus success\n";
			GenericFunction.putResult(PKGName, mDomain, "BatteryStatus", "BatteryStatus", true, mLog, mRemark1, mRemark2);
		}
    	resultIsWriten = true;
    }
}