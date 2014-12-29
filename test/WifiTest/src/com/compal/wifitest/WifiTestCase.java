/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.wifitest;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.text.format.Formatter;
import android.util.Log;
import android.os.SystemProperties;

import com.compal.genericfunction.*;

@SuppressWarnings("rawtypes")
public class WifiTestCase extends ActivityInstrumentationTestCase2{

	public static final String TARGET_PACKAGE_ID="com.android.settings";
	public static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME="com.android.settings.Settings";
	public static final String pkgName = "com.compal.wifitest";
	public static final String mDomain = "wifi";
	public static final String tWifiBasicFunc = "wifi basic function";
	public static final String tWep = "wep";
	public static final String tWpa = "wpa";
	public static final String tWpa2 = "wpa2";
	public static final String tShared = "shared";
	public static final String dWifiOn = "wifi on";
	public static final String dWifiOff = "wifi off";
	public static final String dApScanning = "ap scanning";
	public static final String dAirplainOn = "airplane mode on";
	public static final String dAirplainOff = "airplane mode off";
	public static final String dWep = "wep";
	public static final String dWpa = "wpa";
	public static final String dWpa2 = "wpa2";
	public static final String dShared = "shared";
	public static final String sId = "ID";
	public static final String sRemark1 = "Remark1";
	public static final String sRemark2 = "Remark2";
	
	//public static final String mDesciption ;
	public static final long SHORT_TIMEOUT = 5 * 1000;
	public static final long LONG_TIMEOUT = 10 * 1000;
	public static final long TEST_AP_TIMEOUT = 60 * 1000;
	static final String tag = "WifiTest"; // for Log
	
	private volatile int httpStatusCode = -1;  //represent httpStatusCode not return yet.
	private volatile Thread runner;
	private volatile Thread threadRunner;
	private static final int test_WifiOff = 1;  
	private static final int test_WifiOn = 2;
	WifiManager mWifiManager = null; 
	WifiInfo mWifiInfo = null;
	SupplicantState mSupplicantState = null;
	String mHttpClientTestResult;
	String currentAPType;
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	String mRemark3 = "";
	String testCaseId;
	
	String loadedWepAPSsid;
	String loadedWepAPKey;
	String loadedWpaAPSsid;
	String loadedWpaAPKey;
	String loadedSharedAPSsid;
	String loadedWpa2APSsid;
	String loadedWpa2APKey;	
	
	String WepAPSsid;
	String WepAPKey;
	String WpaAPSsid;
	String WpaAPKey;
	String SharedAPSsid;
	String Wpa2APSsid;
	String Wpa2APKey;
	
	// default are false
	boolean WifiBasicFunctionBeTest = false;
	boolean WepBeTest = false;
	boolean WpaBeTest = false;
	boolean Wpa2BeTest = false;
	boolean SharedBeTest = false;
	boolean VpnBeTest = false;

	
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
	public WifiTestCase()throws ClassNotFoundException{
		super(TARGET_PACKAGE_ID,launcherActivityClass);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		loadConfig(); 
		mWifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
		/*if null put result*/
		if(mWifiManager == null){
			mLog = "WifiManager is null";
		}
	}
	
	public void outputResult(boolean result,String sType ,String sDescription, String sCaseId){
		GenericFunction.putResult(pkgName, mDomain, sType, sDescription,
				result, sCaseId, mLog, mRemark1, mRemark2, mRemark3);
	}
	
	public String getTestCaseId(String mType, String mDescription, String mTag){
		
		return GenericFunction.getTag(pkgName, mDomain, mType, mDescription, mTag);
	}
	
	public void loadConfig(){
		
		Log.i(tag, "loadConfig");
		
		WifiBasicFunctionBeTest = GenericFunction.getConfig(pkgName, mDomain, tWifiBasicFunc);
		WepBeTest = GenericFunction.getConfig(pkgName, mDomain, tWep);
		WpaBeTest = GenericFunction.getConfig(pkgName, mDomain, tWpa);
		Wpa2BeTest = GenericFunction.getConfig(pkgName, mDomain, tWpa2);
		SharedBeTest = GenericFunction.getConfig(pkgName, mDomain, tShared);
	}
	
	public String convertToQuotedString(String string) {
		return "\"" + string + "\"";
	}
	
	public void test_1st_a_WifiOff(){
		if(WifiBasicFunctionBeTest){ 
			testCaseId = getTestCaseId(tWifiBasicFunc, dWifiOff, sId);
			if(mWifiManager != null){
				mLog = "check Wifi off";
				if(checkWifiOnOffState(test_WifiOn)){//now WiFi is on
					enableWifi(false);
				}
				enableWifi(true);
				enableWifi(false);
				if(checkWifiOnOffState(test_WifiOff)){
					Log.i(tag, "test_1st_a_WifiOff true");
					outputResult(true, tWifiBasicFunc, dWifiOff, testCaseId);
				}else{
					Log.i(tag, "test_1st_a_WifiOff false");
					outputResult(false, tWifiBasicFunc, dWifiOff, testCaseId);
				}
			}else{
				outputResult(false, tWifiBasicFunc, dWifiOff, testCaseId);
			}
		}
	}
	
	public void test_1st_b_WifiOn(){
		if(WifiBasicFunctionBeTest){
			testCaseId = getTestCaseId(tWifiBasicFunc, dWifiOn, sId);
			if(mWifiManager != null){
				mLog = "check Wifi on";
				if(checkWifiOnOffState(test_WifiOff)){ //now wifi is off
					enableWifi(true);
				}
				enableWifi(false);
				enableWifi(true);
				if(checkWifiOnOffState(test_WifiOn)){
					Log.i(tag, "test_1st_b_WifiOn true");
					outputResult(true, tWifiBasicFunc, dWifiOn, testCaseId);
				}else{
					Log.i(tag, "test_1st_b_WifiOn false");
					outputResult(false, tWifiBasicFunc, dWifiOn, testCaseId);
				}
			}else{
				outputResult(false, tWifiBasicFunc, dWifiOn, testCaseId);
			}
		}
	}
	
	public boolean checkWifiOnOffState(int toTest){
		long startTime = System.currentTimeMillis();
        long tempTime = startTime; //debug
        while((System.currentTimeMillis() - startTime) < LONG_TIMEOUT ){
        	if(toTest == test_WifiOn)
        		if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED &&
        				mWifiManager.isWifiEnabled()){
        			Log.i(tag, "mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED");
        			return true;
        		}
        	if(toTest == test_WifiOff){
        		if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED &&
        				!mWifiManager.isWifiEnabled()){
        			Log.i(tag, "mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED");
        			return true;
        		}
        	}
        }
        return false;
	}
	
	public void test_2nd_a_AirplaneMode_On() throws InterruptedException{
		if(WifiBasicFunctionBeTest){
			testCaseId = getTestCaseId(tWifiBasicFunc, dAirplainOn, sId);
			if(mWifiManager != null){
				mLog = "check AirplaneMode On";
				boolean airplaneModeIsOn = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
				//airplane mode on
				if(!airplaneModeIsOn){
					enableAirplaneMode(true);
				}
				if(checkWifiOnOffState(test_WifiOff)){
					Log.i(tag, "test_2nd_AirplaneMode_On true");
					outputResult(true, tWifiBasicFunc, dAirplainOn, testCaseId);
				}else{
					Log.i(tag, "test_2nd_AirplaneMode_On false");
					outputResult(false, tWifiBasicFunc, dAirplainOn, testCaseId);
				}
			}else{
				outputResult(false, tWifiBasicFunc, dAirplainOn, testCaseId);
			}
		}
	}
	
	public void test_2nd_b_AirplaneMode_Off() throws InterruptedException{
		if(WifiBasicFunctionBeTest){
			testCaseId = getTestCaseId(tWifiBasicFunc, dAirplainOff, sId);
			if(mWifiManager != null){
				mLog = "check AirplaneMode Off";
				boolean airplaneModeIsOff = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 0;
				if(!airplaneModeIsOff){
					enableAirplaneMode(false);
				}	
				if(checkWifiOnOffState(test_WifiOn)){
					Log.i(tag, "test_2nd_AirplaneMode_Off true");
					outputResult(true, tWifiBasicFunc, dAirplainOff, testCaseId);
				}else{
					Log.i(tag, "test_2nd_AirplaneMode_Off false");
					outputResult(false, tWifiBasicFunc, dAirplainOff, testCaseId);
				}
			}else{
				outputResult(false, tWifiBasicFunc, dAirplainOff, testCaseId);
			}
		}
	}
	
	public void enableAirplaneMode(boolean isAirplaneModeBeOn){
		if(isAirplaneModeBeOn)
			Settings.System.putInt(getActivity().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
		else
			Settings.System.putInt(getActivity().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
		
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", true);
		getActivity().sendBroadcast(intent);
		
		long startTime = System.currentTimeMillis();
		long tempTime = startTime; ///debug
		while((System.currentTimeMillis() - startTime) < LONG_TIMEOUT ){
			if(isAirplaneModeBeOn){
				if(Settings.System.getInt(getActivity().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0){
					Log.i(tag, "Settings.System.AIRPLANE_MODE_ON, 0) != 0 : on");
					return;
				}
			}else{
				if(Settings.System.getInt(getActivity().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 0){
					Log.i(tag, "Settings.System.AIRPLANE_MODE_ON, 0) == 0 : off");
					return;
				}
			}
			sleep(100);
		}
		Log.i(tag, "airplane on/off fail");
	}	
	
	public boolean enableWifi(boolean willWifiOn){
		if(willWifiOn){
			mWifiManager.setWifiEnabled(true);
		}else{
			mWifiManager.setWifiEnabled(false);
		}
		long startTime = System.currentTimeMillis();
        long tempTime = startTime; //debug
        while((System.currentTimeMillis() - startTime) < LONG_TIMEOUT){
        	if(willWifiOn){
        		if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
	        		Log.i(tag, "enableWifi WifiManager.WIFI_STATE_ENABLED");
	        		return true;
	        	}
        	}else{
        		if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED){
	        		Log.i(tag, "enableWifi WifiManager.WIFI_STATE_DISABLED");
	        		return true;
	        	}
        	}
        	sleep(100);
        }
        Log.i(tag, "enableWifi fail");
        return false;
	}
	
	public void test_3rd_APscanning(){
		if(WifiBasicFunctionBeTest){
			testCaseId = getTestCaseId(tWifiBasicFunc, dApScanning, sId);
			if(mWifiManager != null){
				mLog = "check APscanning";
				if(checkWifiOnOffState(test_WifiOff)){
					if(enableWifi(true)){ //open wifi function
						Log.i(tag, "enableWifi(true)");
						scanningTest();
					}else{
						Log.i(tag, "enableWifi(true) fail");
						outputResult(false, tWifiBasicFunc, dApScanning, testCaseId);
						Log.i(tag, "test_3rd_APscanning false due to enable false");
					}
				}else{
					Log.i(tag, "wifi already on");
					scanningTest();
				}
			}else{
				outputResult(false, tWifiBasicFunc, dApScanning, testCaseId);
			}
		}
		enableWifi(false); 
	}
	
	public void scanningTest(){
		if(mWifiManager.startScan()){
			Log.i(tag, "AP scanning");
			long startTime = System.currentTimeMillis();
	        long tempTime = startTime; //debug
	        while((System.currentTimeMillis() - startTime) < LONG_TIMEOUT * 6){
	        	List<ScanResult> wifiList = mWifiManager.getScanResults();
	        	//debug
	        	if((System.currentTimeMillis() - tempTime) > 2000){
	        		Log.i(tag, wifiList.toString());
	        		Log.i(tag, Integer.toString(wifiList.size()));
	        	}
	        	//debug
	        	if(wifiList != null && wifiList.size() != 0){
	        		Log.i(tag, wifiList.toString());
	        		Log.i(tag, Integer.toString(wifiList.size()));
	        		outputResult(true, tWifiBasicFunc, dApScanning, testCaseId);
	        		Log.i(tag, "test_3rd_APscanning true");
	        		return;
	        	}
	        	sleep(100);
	        }	
	        outputResult(false, tWifiBasicFunc, dApScanning, testCaseId);
			Log.i(tag, "test_3rd_APscanning false");
	        return ;
		}
	}
	
	public void test_4th_b_Wpa2Connection() throws InterruptedException{
		if(Wpa2BeTest){
			testCaseId = getTestCaseId(tWpa2, dWpa2, sId);
			if(mWifiManager != null){
				currentAPType =  dWpa2;
				mLog = "Wpa2Connection";
				Log.i(tag, "test_4th_b_Wpa2Connection start");
				if(checkWifiOnOffState(test_WifiOff)){
					enableWifi(true);
				}
				loadedWpa2APSsid = getTestCaseId(tWpa2, dWpa2, sRemark1);
				loadedWpa2APKey = getTestCaseId(tWpa2, dWpa2, sRemark2);
				Wpa2APSsid = convertToQuotedString(loadedWpa2APSsid);
				Wpa2APKey = convertToQuotedString(loadedWpa2APKey);
				if(testAP(dWpa2)){
					checkHttpClientTest(dWpa2);
				}else{
					//output fail result
		        	mLog = "supplicant state not complete or connecion timeout or can't get ip";
		        	outputResult(false, tWpa2, dWpa2, testCaseId);
				}
				Log.i(tag, "test_4th_b_Wpa2Connection end");
			}else{
				outputResult(false, tWpa2, dWpa2, testCaseId);
			}
		}
	}
    
	
	public void test_4th_a_SharedConnection() throws InterruptedException{
		if(SharedBeTest){
			testCaseId = getTestCaseId(tShared, dShared, sId);
			if(mWifiManager != null){
				currentAPType =  dShared;
				mLog = "SharedConnection";
				Log.i(tag, "test_4th_a_SharedConnection start");
				if(checkWifiOnOffState(test_WifiOff)){
					enableWifi(true);
				}
				loadedSharedAPSsid = getTestCaseId(tShared, dShared, sRemark1);
				SharedAPSsid = convertToQuotedString(loadedSharedAPSsid);
				if(testAP(dShared)){
					checkHttpClientTest(dShared);
				}else{
					//output fail result
		        	mLog = "supplicant state not complete or connecion timeout or can't get ip";
		        	outputResult(false, tShared, dShared, testCaseId);
				}
				Log.i(tag, "test_4th_a_SharedConnection end");
			}else{
				outputResult(false, tShared, dShared, testCaseId);
			}
		}
	}
	
	
		
	public void test_4th_c_WpaConnection() throws InterruptedException{
		if(WpaBeTest){
			testCaseId = getTestCaseId(tWpa, dWpa, sId);
			if(mWifiManager != null){
				currentAPType = dWpa;
				mLog = "WpaConnection";
				Log.i(tag, "test_4th_c_WpaConnection start");
				if(checkWifiOnOffState(test_WifiOff)){
					enableWifi(true);
				}
				loadedWpaAPSsid = getTestCaseId(tWpa, dWpa, sRemark1);
				loadedWpaAPKey = getTestCaseId(tWpa, dWpa, sRemark2);
				WpaAPSsid = convertToQuotedString(loadedWpaAPSsid);
				WpaAPKey = convertToQuotedString(loadedWpaAPKey);
				if(testAP(dWpa)){
					checkHttpClientTest(dWpa);
				}else{
					//output fail result
		        	mLog = "supplicant state not complete or connecion timeout or can't get ip";
		        	outputResult(false, tWpa, dWpa, testCaseId);
				}
				Log.i(tag, "test_4th_c_WpaConnection end");
			}else{
				outputResult(false, tWpa, dWpa, testCaseId);
			}
		}
		
	}
    
	public void test_4th_d_WepConnection() throws InterruptedException{
		if(WepBeTest){
			testCaseId = getTestCaseId(tWep, dWep, sId);
			if(mWifiManager != null){
				currentAPType =  dWep;
				mLog = "WepConnection";
				Log.i(tag, "test_4th_d_WepConnection start");
				if(checkWifiOnOffState(test_WifiOff)){
					enableWifi(true);
				}
				loadedWepAPSsid = getTestCaseId(tWep, dWep, sRemark1);
				loadedWepAPKey = getTestCaseId(tWep, dWep, sRemark2);
				WepAPSsid = convertToQuotedString(loadedWepAPSsid);
				WepAPKey = convertToQuotedString(loadedWepAPKey);
				if(testAP(dWep)){
					checkHttpClientTest(dWep);
				}else{
					//output fail result
		        	mLog = "supplicant state not complete or connecion timeout or can't get ip";
		        	outputResult(false, tWep, dWep, testCaseId);
				}
				Log.i(tag, "test_4th_d_WepConnection end");
			}else{
				outputResult(false, tWep, dWep, testCaseId);
			}
		}
		SystemProperties.set("persist.sys.test","WifiTestIsdone");
		enableWifi(false); //turn off wifi in the end
	}
	
	public void checkHttpClientTest(String APType){
		long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) < LONG_TIMEOUT * 3){
        	httpClientTest();
        	if(httpStatusCode != -1){
        		Log.i(tag, "httpStatusCode != -1");
				//output pass result
				outputResult(true, APType, APType, testCaseId);
				return;
        	}
        	sleep(100);
        }
        //output fail result 
      	mLog = "can't browse web";
      	outputResult(false, APType, APType, testCaseId);
        Log.i(tag, "httpClientTest fail");
	}
	
	public boolean testAP(String APSsid){
		Log.i(tag, "test AP SSID:"+ APSsid);
		long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) < TEST_AP_TIMEOUT * 2){
        	if(saveAPConfig(APSsid)){
        		Log.i(tag, "IP && supplicant state complete = true : " + APSsid);
        		return true;
        	}
        	sleep(100);
        }
        Log.i(tag, "IP || supplicant state complete = false : " + APSsid);
		return false;
	}
	
	public boolean saveAPConfig(String mAPType)
    {
        WifiConfiguration wc = new WifiConfiguration(); 
        
        if(mAPType.equalsIgnoreCase(dWpa)){
        	wc.SSID = WpaAPSsid;
        	wc.preSharedKey = WpaAPKey;
        	wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        	wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.priority = 40;  //!!
        }
        if(mAPType.equalsIgnoreCase(dWep)){
        	wc.SSID = WepAPSsid; 
        	wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        	wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wc.wepKeys[0] = WepAPKey; //This is the WEP Password
            wc.wepTxKeyIndex = 0;
        }
        if(mAPType.equalsIgnoreCase(dWpa2)){
        	wc.SSID = Wpa2APSsid;
        	wc.preSharedKey = Wpa2APKey;
        	wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        	wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }
        if(mAPType.equalsIgnoreCase(dShared)){
        	wc.SSID = SharedAPSsid; 
        	wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        	wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }
        wc.hiddenSSID = true;
        wc.status = WifiConfiguration.Status.DISABLED;     
        wc.priority = 41;  //!!
        
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); 
        wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        
        //boolean res1 = mWifiManager.setWifiEnabled(true);
        int res = mWifiManager.addNetwork(wc);
        Log.i(tag, "add" + mAPType + "Network returned " + res );
        boolean es = mWifiManager.saveConfiguration();
        Log.i(tag, mAPType +" saveConfiguration returned " + es );
        boolean b = mWifiManager.enableNetwork(res, true);   
        Log.i(tag, mAPType + " enableNetwork returned " + b );  
        
        long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) < LONG_TIMEOUT * 5){
        	if(mWifiManager.getConnectionInfo().getSupplicantState().equals((SupplicantState) SupplicantState.COMPLETED)
        			&& !Formatter.formatIpAddress(mWifiManager.getConnectionInfo().getIpAddress()).equalsIgnoreCase("0.0.0.0")){
        		Log.i(tag, Formatter.formatIpAddress(mWifiManager.getConnectionInfo().getIpAddress()));
        		Log.i(tag, "getConnectionInfo().getSupplicantState().equals((SupplicantState) SupplicantState.COMPLETED)");
        		Log.i(tag, "IP && supplicant state complete = true : " + mAPType);
        		return true;
        	}
        	sleep(100);
        }
        Log.i(tag, "IP || supplicant state complete = false : " + mAPType);
        return false;
        
    }
	
	public void httpClientTest() {
		Log.i(tag, "httpClientTest run");
		HttpClient client = new DefaultHttpClient();
		try {
		HttpGet request = new HttpGet("http://www.google.com.tw");
		HttpResponse response = client.execute(request);
		Log.i(tag, "httpClientTest try ");
			if (response.getStatusLine().getStatusCode() == 200) { //200 OK (HTTP/1.0 - RFC 1945)
				httpStatusCode = 200; //200 OK (HTTP/1.0 - RFC 1945)
				mHttpClientTestResult = "Pass";
				Log.i(tag, mHttpClientTestResult);
			} else {
				mHttpClientTestResult = "Fail: Code: " + String.valueOf(response);
				Log.i(tag, mHttpClientTestResult);
			}
			request.abort();
		} catch (IOException e) {
			mHttpClientTestResult = "Fail: IOException";
			Log.i(tag, "mHttpClientTestResult:" + mHttpClientTestResult);
		}
		Log.i(tag, mHttpClientTestResult);

    }
	
	private void sleep(long time){
		try{
			Thread.sleep(time);
		}catch(InterruptedException e){
			
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		Log.i(tag, "I'm tear down");
	}

}
