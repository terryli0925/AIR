/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.bluetoothtestpair;

import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.os.SystemProperties;

import com.compal.genericfunction.GenericFunction;

@SuppressWarnings("rawtypes")
public class BluetoothTestPair extends ActivityInstrumentationTestCase2{
	
	public static final String TARGET_PACKAGE_ID="com.android.settings";
	public static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME="com.android.settings.Settings";
	public static final String pkgName = "com.compal.bluetoothtestpair";
	public static final String mDomain = "bluetooth";
	public static final String tPairAndDiscovery = "pair and discovery";
	public static final String dBluetoothScan = "scan bluetooth device";
	public static final String dBluetoothDiscoverable = "bluetooth device discoverable";
	public static final String dBluetoothPairing = "bluetooth device pairing";
	public static final String sId = "ID"; 
	public static final String tag = "BluetoothTestPair"; // for Log
	
    private static final int ENABLE_DISABLE_TIMEOUT = 20000;
    private static final int CHANGE_NAME_TIMEOUT = 5000;
    private static final int SCAN_TIMEOUT = 20000;
    private static final int PAIR_UNPAIR_TIMEOUT = 50000;
    private static final int DISCOVERABLE_UNDISCOVERABLE_TIMEOUT = 50000;
    private static final int POLL_TIME = 100;
    private static final int test_BTOff = 1;
    private static final int test_BTOn = 2;
    private static final int scan_times = 3;
    
	BluetoothAdapter adapter;
	BluetoothDevice  device;
	private Context mContext;
	Context context;
	public static boolean basicFuncOK = false;
	
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	String mRemark3 = "";
	String testCaseId;
	
	public String NbBtAddr = "";
	public int passkey = 123456;  //!!!
	public byte[] pin = {'1','2','3','4'};  //!!!
	public boolean checkNbBtAddr = false;
	public boolean pairCallbackCalled = false;
	// default are false
	boolean PairAndConnectBeTest = false;
	
	// for test are true
	//boolean PairAndConnectBeTest = true;
	
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
	public BluetoothTestPair()throws ClassNotFoundException{
		super(TARGET_PACKAGE_ID,launcherActivityClass);
	}
	
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		Log.i(tag, "setUp()");
		loadConfig(); 
		getActivity();
		context = getInstrumentation().getTargetContext();
		adapter = BluetoothAdapter.getDefaultAdapter();
		if(adapter == null){
			mLog = "BluetoothAdapter is null";
			Log.i(tag, "adapter == null");
		}else{
			
			//communication bt addr
			NbBtAddr = SystemProperties.get("persist.sys.test_nbaddr");
			if(NbBtAddr.isEmpty()){
				mLog = "can't get nb bt addr";
			}
			Log.i(tag, "nb bt addr :"+NbBtAddr);
			
			if(checkBluetoothOnOffState(test_BTOn)){
				basicFuncOK = true;
				Log.i(tag, "test_BTOn adapter.getAddress():"+adapter.getAddress());
				SystemProperties.set("persist.sys.test_dutaddr",adapter.getAddress());
			}else{
				if(enableBluetooth(true)){
					basicFuncOK = true;
					Log.i(tag, "test_BTOff adapter.getAddress():"+adapter.getAddress());
					SystemProperties.set("persist.sys.test_dutaddr",adapter.getAddress());
				}else{
					mLog = "enableBluetooth fail";
					testCaseId = getTestCaseId(tPairAndDiscovery, dBluetoothPairing, sId);
					outputResult(false, tPairAndDiscovery, dBluetoothPairing, testCaseId);
				}
			}
		}
	}
	
	public void test_1st_a_BluetoothDeviceDiscoverable(){		
		if(PairAndConnectBeTest && basicFuncOK){
			testCaseId = getTestCaseId(tPairAndDiscovery, dBluetoothDiscoverable, sId);
			if(adapter != null){
				mLog = "Bluetooth Device Discoverable";
				if(checkBluetoothOnOffState(test_BTOn)){
					Log.i(tag, "Bt is already on, device will set discoverable");
					BTDeviceDiscoverable();
				}else{
					Log.i(tag, "enableBluetooth(true) and device will set discoverable");
					if(enableBluetooth(true)){
						Log.i(tag, "enableBluetooth(true) : true DeviceDiscoverable");
						BTDeviceDiscoverable();
					}else{
						Log.i(tag, "enableBluetooth(true) : fail");
						mLog = "enable BT fail at addr";
						outputResult(false, tPairAndDiscovery, dBluetoothDiscoverable, testCaseId);
					}
				}
			}else{
				outputResult(false, tPairAndDiscovery, dBluetoothDiscoverable, testCaseId);
			}
		}
	}
	

	public void test_1st_b_BluetoothDeviceScan(){
		if(PairAndConnectBeTest && basicFuncOK){
			testCaseId = getTestCaseId(tPairAndDiscovery, dBluetoothScan, sId);
			context.registerReceiver(ActionFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		
			if(adapter != null){
				mLog = "Bluetooth Device Scan";
				if(checkBluetoothOnOffState(test_BTOn)){
					Log.i(tag, "Bt is already on, DeviceScan");
					DeviceScan();
				}else{
					if(enableBluetooth(true)){
						Log.i(tag, "enableBluetooth(true)");	
						DeviceScan();
					}else{
						Log.i(tag, "enableBluetooth(true) : fail");
						mLog = "enable BT fail at addr";
						outputResult(false, tPairAndDiscovery, dBluetoothScan, testCaseId);
					}
				}
			}else{
				outputResult(false, tPairAndDiscovery, dBluetoothScan, testCaseId);
			}
			
		context.unregisterReceiver(ActionFoundReceiver); //unregister broadcast receiver
		
		}			
	}

	public void DeviceScan(){
		for(int i = 0 ; i < scan_times ; i++){
			adapter.startDiscovery();   
			if(checkScanCallback()){
				Log.i(tag, "test_1st_b_BluetoothDeviceScan true");
				outputResult(true, tPairAndDiscovery, dBluetoothScan, testCaseId);
				return;
			}
		}
		Log.i(tag, "test_1st_b_BluetoothDeviceScan false");
		outputResult(false, tPairAndDiscovery, dBluetoothScan, testCaseId);	
	}
	
	public void BTDeviceDiscoverable(){
		Log.i(tag, "BTDeviceDiscoverable");
		
		if(setBTDeviceDiscoverable()){
			Log.i(tag, "test_1st_a_BluetoothDeviceDiscoverable true");
			outputResult(true, tPairAndDiscovery, dBluetoothDiscoverable, testCaseId);
		}else{
			Log.i(tag, "test_1st_a_BluetoothDeviceDiscoverable : fail");
			outputResult(false, tPairAndDiscovery, dBluetoothDiscoverable, testCaseId);
		}
	}
	
	public boolean setBTDeviceDiscoverable(){
		
		String DutBeScanned;
		int scanMode = adapter.getScanMode();
		if (scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			adapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
		}
				
		long startTime = System.currentTimeMillis();
		while((System.currentTimeMillis() - startTime) < DISCOVERABLE_UNDISCOVERABLE_TIMEOUT ){ //!!!
			scanMode = adapter.getScanMode();
        	if(scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
	        	//Log.i(tag, "scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE");
	        	
	        	SystemProperties.set("persist.sys.test_scanflag", "true"); //set scan flag
	        	DutBeScanned = SystemProperties.get("persist.sys.test_scannedresult");
	        	//Log.i(tag, "DutBeScanned : "+DutBeScanned);
	        	if(DutBeScanned.equals("DutBeScanned")){
	        		Log.i(DutBeScanned, "DutBeScanned.equals DutIsBeScanned");
	        		return true;
	        	}
	        }
        	sleep(POLL_TIME);
        }
        Log.i(tag, "setBTDeviceDiscoverable fail");
        return false;
	}
		
	public boolean checkScanCallback(){
		Log.i(tag, "checkScanCallback()");
		
		long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) < SCAN_TIMEOUT){
        	if(checkNbBtAddr){
        		Log.i(tag, "checkNbBtAddr true");
    			return true;
        	}
        	sleep(POLL_TIME);
        }
        Log.i(tag, "checkNbBtAddr fail");
        return false;
	}
	
	
	public void test_2nd_pairDevice(){
		if(PairAndConnectBeTest && basicFuncOK){
			testCaseId = getTestCaseId(tPairAndDiscovery, dBluetoothPairing, sId);
			Log.i(tag, "test_2nd_pairDevice start");
			if(adapter != null){
				mLog = "pair BT device";
				
				IntentFilter mIntent = new IntentFilter();
				mIntent.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
				context.registerReceiver(PairReceiver, mIntent);
				
				SystemProperties.set("persist.sys.test_pairflag", "true"); //set pair flag
				
				if(checkBluetoothOnOffState(test_BTOn)){
					Log.i(tag, "checkBluetoothOnOffState(test_BTOn)  pairDevice");
					pairDevice();
				}else{
					if(enableBluetooth(true)){
						pairDevice(); 
					}else{
						Log.i(tag, "enableBluetooth(true) : fail pairDevice");
						mLog = "enable BT fail at pairDevice";
						outputResult(false, tPairAndDiscovery, dBluetoothPairing, testCaseId);
					}
				}
			}else{
				outputResult(false, tPairAndDiscovery, dBluetoothPairing, testCaseId);
			}
		}
				
		SystemProperties.set("persist.sys.test","BluetoothTestIsdone"); // set SystemProperties
		enableBluetooth(false);  								//turn off BT finally
	}
	
	public boolean pairDevice(){
		
		if(BluetoothAdapter.checkBluetoothAddress(NbBtAddr)){
			
			device = adapter.getRemoteDevice(NbBtAddr);
			
			/**************/
			//device.createBond(); 
			/***************/
					
			long startTime = System.currentTimeMillis();
	        while((System.currentTimeMillis() - startTime) < PAIR_UNPAIR_TIMEOUT){
	        	if(adapter.getBondedDevices().contains(device)){  //check bounded device
	        		Log.i(tag, "adapter.getBondedDevices().contains(device)");
	        		SystemProperties.set("persist.sys.test_pairflag", "false"); //set pair flag
	        		outputResult(true, tPairAndDiscovery, dBluetoothPairing, testCaseId);
	        		Log.i(tag, "test_2nd_pairDevice true");
	    			return true;
	        	}
	        	sleep(POLL_TIME);
	        }
	        Log.i(tag, "adapter.getBondedDevices().contains(device) fail");
	        outputResult(false, tPairAndDiscovery, dBluetoothPairing, testCaseId);
	        Log.i(tag, "test_2nd_pairDevice false");
	        return false;
			
			//mDevice.setPin(mPin);
			//final BluetoothSocket mmSocket;
	        //String mSocketType;
	        
	        //device.createInsecureRfcommSocketToServiceRecord(null);
		}else{
			outputResult(false, tPairAndDiscovery, dBluetoothPairing, testCaseId);
			return false;
		}
		
		
        
	}
	
	
	private final BroadcastReceiver PairReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			Log.i(tag, "PairReceiver onReceive");
			String action = intent.getAction();
			Log.i(tag, intent.getAction().toString());  
			//if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
			//if(intent.getAction().equals("ACTION_PAIRING_REQUEST")){
			if(intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")){
				Log.i(tag, "BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)");
				//pairCallbackCalled = true;
			}
			
			if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction())) {
				int varient = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, -1);
				Log.i(tag, Integer.toString(varient));
				switch (varient) {
				case BluetoothDevice.PAIRING_VARIANT_PIN:
					device.setPin(pin);
					Log.i(tag, "case BluetoothDevice.PAIRING_VARIANT_PIN:");
					break;
				case BluetoothDevice.PAIRING_VARIANT_PASSKEY:
					device.setPasskey(passkey);
					Log.i(tag, "case BluetoothDevice.PAIRING_VARIANT_PASSKEY:");
				    break;
				case BluetoothDevice.PAIRING_VARIANT_CONSENT:
					Log.i(tag, "case BluetoothDevice.PAIRING_VARIANT_CONSENT:");
				case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
					device.setPairingConfirmation(true);
					Log.i(tag, "case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:");
					break;
				case BluetoothDevice.PAIRING_VARIANT_OOB_CONSENT:
					Log.i(tag, "case BluetoothDevice.PAIRING_VARIANT_OOB_CONSENT:");
					device.setRemoteOutOfBandData();
					break;
				}
			}
		}
	};
	

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
        	Log.i(tag, "enableBT fail");
        else
        	Log.i(tag, "disableBT fail");
        sleep(3000);
        return false;
	}
	 
	private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				Log.i(tag, "BluetoothDevice.ACTION_FOUND.equals(action)");
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.i(tag, "find addr:" + device.getAddress());
				if(device.getAddress().equals(NbBtAddr))
					checkNbBtAddr = true;
			}
		}
	};
	
	private void sleep(long time){
		try{
			Thread.sleep(time);
		}catch(InterruptedException e){
			
		}
	}
	
	public String getTestCaseId(String mType, String mDescription, String mTag){
		
		Log.i(tag, "getTestCaseId");
		
		return GenericFunction.getTag(pkgName, mDomain, mType, mDescription, mTag);
	}
	
	public void outputResult(boolean result,String sType ,String sDescription, String sCaseId){
		GenericFunction.putResult(pkgName, mDomain, sType, sDescription,
				result, sCaseId, mLog, mRemark1, mRemark2, mRemark3);
	}
	
	public void loadConfig(){
		Log.i(tag, "loadConfig");
		
		PairAndConnectBeTest = GenericFunction.getConfig(pkgName, mDomain, tPairAndDiscovery);
	}
	
}

