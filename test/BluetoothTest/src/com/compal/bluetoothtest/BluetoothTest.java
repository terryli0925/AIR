/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.bluetoothtest;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.util.Log;
import junit.framework.TestCase;
import android.view.Window;
import android.os.SystemProperties;

import com.compal.genericfunction.*;

@SuppressWarnings("rawtypes")
public class BluetoothTest extends ActivityInstrumentationTestCase2{
//public class BluetoothTest extends AndroidTestCase{
	
	public static final String TARGET_PACKAGE_ID="com.android.settings";
	public static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME="com.android.settings.Settings";
	public static final String pkgName = "com.compal.bluetoothtest";
	public static final String mDomain = "bluetooth";
	public static final String tBluetoothBasicFunc = "bluetooth basic function";
	public static final String dBluetoothOn = "bluetooth on";
	public static final String dBluetoothOff = "bluetooth off";
	public static final String dSetDeviceName = "set device name";
	public static final String dBluetoothAddr = "bluetooth address";
	public static final String sId = "ID";
    public String predefineDeviceName = "predefineDeviceName";
	static final String tag = "BluetoothTest"; // for Log
	
    private static final int ENABLE_DISABLE_TIMEOUT = 20000;
    private static final int CHANGE_NAME_TIMEOUT = 5000;
    private static final int PAIR_UNPAIR_TIMEOUT = 20000;
    private static final int DISCOVERABLE_UNDISCOVERABLE_TIMEOUT = 5000;
    private static final int POLL_TIME = 100;
    private static final int test_BTOff = 1;
    private static final int test_BTOn = 2;
    
	BluetoothAdapter adapter;
	BluetoothDevice  device;
	Context context;
	
	String nameToCompare;
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	String mRemark3 = "";
	String testCaseId;
	
	public int passkey = 123456;  //!!!
	public byte[] pin = {'1','2','3','4'};  //!!!
	public boolean scanCallbackCalled = false;

	// default are false
	boolean BTBasicFunctionBeTest = false;
	
	private static Class<?> launcherActivityClass;
	static{
		try
		{
			launcherActivityClass=Class.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
		} catch (ClassNotFoundException e){
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public BluetoothTest()throws ClassNotFoundException{
		super(TARGET_PACKAGE_ID,launcherActivityClass);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		loadConfig();
		getActivity();
		context = getInstrumentation().getTargetContext();
		adapter = BluetoothAdapter.getDefaultAdapter();
		if(adapter == null){
			mLog = "BluetoothAdapter is null";
			Log.i(tag, "adapter == null");
		}
	}
	
	public void test_1st_a_BluetoothOn(){
		if(BTBasicFunctionBeTest){
			testCaseId = getTestCaseId(tBluetoothBasicFunc, dBluetoothOn, sId);
			if(adapter != null){
				mLog = "check bt on";
				if(checkBluetoothOnOffState(test_BTOff)){
					enableBluetooth(true);
				}
				enableBluetooth(false);
				enableBluetooth(true);
				
				if(checkBluetoothOnOffState(test_BTOn)){
					Log.i(tag, "test_1st_a_BlutoothOn true");
					outputResult(true, tBluetoothBasicFunc, dBluetoothOn, testCaseId);
				}else{
					Log.i(tag, "test_1st_a_BlutoothOn false");
					mLog = "check bt on fail";
					outputResult(false, tBluetoothBasicFunc, dBluetoothOn, testCaseId);
				}
			}else{
				mLog = "adapter = null";
				outputResult(false, tBluetoothBasicFunc, dBluetoothOn, testCaseId);
			}
		}
	}
	
	public boolean checkBluetoothOnOffState(int toTest){
		long startTime = System.currentTimeMillis();
        long tempTime = startTime; //debug
        while((System.currentTimeMillis() - startTime) < ENABLE_DISABLE_TIMEOUT ){
        	if(toTest == test_BTOn)
        		if(adapter.getState() == BluetoothAdapter.STATE_ON && adapter.isEnabled()){
        			Log.i(tag, "adapter.getState() == BluetoothAdapter.STATE_ON");
        			return true;
        		}
        	if(toTest == test_BTOff){
        		if(adapter.getState() == BluetoothAdapter.STATE_OFF && !adapter.isEnabled()){
        			Log.i(tag, "adapter.getState() == BluetoothAdapter.STATE_OFF");
        			return true;
        		}
        	}
        }
        return false;
	}
	
	public boolean enableBluetooth(boolean willBTOn){
		if(willBTOn){
			adapter.enable();
		}else{
			adapter.disable();
		}
		int state;
		long startTime = System.currentTimeMillis();
        long tempTime = startTime; //debug
        while((System.currentTimeMillis() - startTime) < ENABLE_DISABLE_TIMEOUT){
        	state = adapter.getState();
        	if(willBTOn){
        		if(state == BluetoothAdapter.STATE_ON && adapter.isEnabled()){
	        		Log.i(tag, "state == adapter.STATE_ON");
	        		sleep(3000);
	        		return true;
	        	}
        	}else{
        		if(state == BluetoothAdapter.STATE_OFF && !adapter.isEnabled()){
	        		Log.i(tag, "state == adapter.STATE_OFF");
	        		sleep(3000);
	        		return true;
	        	}
        	}
        	sleep(POLL_TIME);
        }
        if(willBTOn)
        	Log.i(tag, "enableBT on fail");
        else
        	Log.i(tag, "disableBT off fail");
        sleep(3000);
        return false;
	}
	
	public void test_1st_b_BluetoothOff(){
		if(BTBasicFunctionBeTest){
			testCaseId = getTestCaseId(tBluetoothBasicFunc, dBluetoothOff, sId);
			if(adapter != null){
				mLog = "check bt off";
				if(checkBluetoothOnOffState(test_BTOn)){
					enableBluetooth(false);
				}
				enableBluetooth(true);
				enableBluetooth(false);
				
				if(checkBluetoothOnOffState(test_BTOff)){
					Log.i(tag, "test_1st_b_BlutoothOff true");
					outputResult(true, tBluetoothBasicFunc, dBluetoothOff, testCaseId);
				}else{
					Log.i(tag, "test_1st_b_BlutoothOff false");
					mLog = "check bt off fail";
					outputResult(false, tBluetoothBasicFunc, dBluetoothOff, testCaseId);
				}
			}else{
				mLog = "adapter = nulll";
				outputResult(false, tBluetoothBasicFunc, dBluetoothOff, testCaseId);
			}
		}
	}
	
	public void test_2rd_BTaddr(){
		if(BTBasicFunctionBeTest){
			testCaseId = getTestCaseId(tBluetoothBasicFunc, dBluetoothAddr, sId);
			if(adapter != null){
				mLog = "check BT device addr valid";
				
				if(checkBluetoothOnOffState(test_BTOn)){
					checkAddr();
					Log.i(tag, "BT addr : " + adapter.getAddress());
				}else{
					if(enableBluetooth(true)){
						Log.i(tag, "enableBluetooth(true) BTaddr");
						Log.i(tag, "BT addr : " + adapter.getAddress());
						checkAddr();
					}else{
						mLog = "enable BT fail at addr";
						Log.i(tag, "enable BT fail at addr");
						outputResult(false, tBluetoothBasicFunc, dBluetoothAddr, testCaseId);
					}
				}
			}else{
				mLog = "adapter = null";
				Log.i(tag, "adapter = null");
				outputResult(false, tBluetoothBasicFunc, dBluetoothAddr, testCaseId);
			}
		}
	}
	
	public void checkAddr(){
		if(BluetoothAdapter.checkBluetoothAddress(adapter.getAddress())){
			Log.i(tag, "addr valid");
			Log.i(tag, "test_2rd_BTaddr : true");
			outputResult(true, tBluetoothBasicFunc, dBluetoothAddr, testCaseId);
		}else{
			Log.i(tag, "addr not valid");
			Log.i(tag, "test_2rd_BTaddr : false");
			outputResult(false, tBluetoothBasicFunc, dBluetoothAddr, testCaseId);
		}
	}
	
	public void test_3rd_SetDeviceName(){
		if(BTBasicFunctionBeTest){
			testCaseId = getTestCaseId(tBluetoothBasicFunc, dSetDeviceName, sId);
			if(adapter != null){
				mLog = "set BT device name";
				if(checkBluetoothOnOffState(test_BTOn)){
					Log.i(tag, "predefineDeviceName:"+predefineDeviceName);
					nameToCompare = predefineDeviceName + "_" + Long.toString(System.currentTimeMillis());
					BTDeviceNameTest(nameToCompare);
				}else{
					if(enableBluetooth(true)){
						Log.i(tag, "predefineDeviceName:"+predefineDeviceName);
						Log.i(tag, "enableBluetooth(true) SetDeviceName");
						nameToCompare = predefineDeviceName + "_" + Long.toString(System.currentTimeMillis());
						BTDeviceNameTest(nameToCompare);
						
					}else{
						mLog = "enable BT fail at SetDeviceName";
						Log.i(tag, "enable BT fail at SetDeviceName");
						outputResult(false, tBluetoothBasicFunc, dSetDeviceName, testCaseId);
					}
				}
			}else{
				mLog = "adapter = null";
				Log.i(tag, "adapter = null");
				outputResult(false, tBluetoothBasicFunc, dSetDeviceName, testCaseId);
			}
		}
		SystemProperties.set("persist.sys.test","BluetoothTestIsdone"); // set SystemProperties
		enableBluetooth(false);  								//turn off BT finally
	}
	
	public void BTDeviceNameTest(String nameToTest){
		if(setBTDeviceName(nameToTest)){
			Log.i(tag, "setBTDeviceName(nameToTest) : true");
			//Log.i(tag, "predefineDeviceName:"+predefineDeviceName);
			//Log.i(tag, "nameToTest:"+nameToTest);
			//Log.i(tag, "nameToCompare:"+nameToCompare);
			if(checkBTDeviceName(nameToCompare)){
				Log.i(tag, "test_3rd_SetDeviceName : true");
				outputResult(true, tBluetoothBasicFunc, dSetDeviceName, testCaseId);
			}else{
				Log.i(tag, "test_3rd_SetDeviceName : false");
				outputResult(false, tBluetoothBasicFunc, dSetDeviceName, testCaseId);
			}
		}else{
			Log.i(tag, "setBTDeviceName(predefineDeviceName) : false");
			outputResult(false, tBluetoothBasicFunc, dSetDeviceName, testCaseId);
		}
	}
	
	public boolean checkBTDeviceName(String beCheckName){
		Log.i(tag, "now name:" + adapter.getName());
		
		long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) < CHANGE_NAME_TIMEOUT * 2){
        	//Log.i(tag, adapter.getName());
    		//Log.i(tag, beCheckName);
        	if(adapter.getName().equalsIgnoreCase(beCheckName)){
        		Log.i(tag, "adapter.getName().equalsIgnoreCase(beCheckName)");
    			return true;
        	}
        	sleep(POLL_TIME);
        }
        Log.i(tag, "checkBTDeviceName fail");
        return false;
        
	}
	
	public boolean setBTDeviceName(String beSetName){
		if(adapter.setName(beSetName))
			return true;
		else
			return false;
	}
	
	
	public void loadConfig(){
		Log.i(tag, "loadConfig");
		
		BTBasicFunctionBeTest = GenericFunction.getConfig(pkgName, mDomain, tBluetoothBasicFunc);
	}
	
	public void outputResult(boolean result,String sType ,String sDescription, String sCaseId){
		GenericFunction.putResult(pkgName, mDomain, sType, sDescription,
				result, sCaseId, mLog, mRemark1, mRemark2, mRemark3);
	}
	
	public String getTestCaseId(String mType, String mDescription, String mTag){
		
		return GenericFunction.getTag(pkgName, mDomain, mType, mDescription, mTag);
	}
	
	private void sleep(long time){
		try{
			Thread.sleep(time);
		}catch(InterruptedException e){
			
		}
	}
	 
	protected void tearDown() throws Exception {
		super.tearDown();
		
	}

}
