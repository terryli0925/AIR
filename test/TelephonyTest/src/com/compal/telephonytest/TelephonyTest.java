/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc.
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */

/*
Copyright 2011-2012 Compal Electronics, Inc.. All rights reserved.

By installing or using this software or any portion thereof, you ("You") agrees to be bound by the following terms of use ("Terms of Use").
This software, and any portion thereof, is referred to herein as the "Software."

USE OF SOFTWARE.  This software is the property of Compal Electronics, Inc. (Compal) and is made available by Compal to You, and may be used only by You for personal or proje    ct evaluation.

RESTRICTIONS.  You shall not claim the ownership of the Software and shall not sell the Software. The software shall not be distributed separately via internet or any other m    edium unless You have a separate contract with Compal.

INDEMNITY.  You agree to hold harmless and indemnify Compal and Compal's subsidiaries, affiliates, officers, agents, and employees from and against any claim, suit, or action     arising from or in any way related to Your use of the Software or Your violation of th    ese Terms of Use, including any liability or expense arising from all claims, losses, d    amages, suits, judgments, litigation costs and attorneys' fees, of every kind and natur    e.
In such a case, Compal will provide You with written notices of such claim, suit, or action.

DISCLAIMER.  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS    " AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WAR    RANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO E    VENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCI    DENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PR    OCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS I    NTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT     LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE P    OSSIBILITY OF SUCH DAMAGE.
*/
package com.compal.telephonytest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.telephony.ServiceState;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import junit.framework.TestCase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.RemoteCallbackList;
import android.os.ServiceManager;
import com.android.internal.telephony.OperatorInfo;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.SystemProperties;
import android.net.LinkProperties;
import com.android.internal.telephony.ApnSetting;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.Settings;

import com.compal.genericfunction.*;

@SuppressWarnings("rawtypes")
public class TelephonyTest extends ActivityInstrumentationTestCase2{

	public static final String TARGET_PACKAGE_ID="com.android.settings";
	public static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME="com.android.settings.Settings";
	public static final String pkgName = "com.compal.telephonytest";
	public static final String mDomain = "telephony";
	public static final String tTelephonyBasicFunc = "telephony basic function";
	public static final String tSms = "sms";
	public static final String dSimDetect = "sim card detection with boot";
	public static final String dBrowseWeb = "web page browsing";
	public static final String dDataEnaler = "data connection enabler";
	public static final String dApnSetting = "apn setting";
	public static final String dBasicInformation= "basic information";
	public static final String dSimRead = "sim phonebook read";
	public static final String dSendAndReceiveSms = "send and receive sms";
	public static final String sId = "ID";
	public static final long WiFi_TIMEOUT = 10 * 1000;
	public static final int TIME_OUT = 1000 * 60 * 5;
	public static final int SHORT_TIME_OUT = 1000 * 60;
	public static final String tag = "TelephonyTest"; // for Log
	
	private Intent mSendIntent;
	private Intent mDeliveryIntent;
	private static final String SMS_SEND_ACTION = "SMS_SEND_ACTION";
	private static final String SMS_DELIVERY_ACTION = "SMS_DELIVERY_ACTION";
	private static final String DATA_SMS_RECEIVED_ACTION = "android.intent.action.DATA_SMS_RECEIVED";
	private String mText = "This is a test message";
	private String mPhoneNumber = "";
	String testCaseId;
	
	TelephonyManager mTelephonyManager;
	WifiManager mWifiManager; 
	SmsManager mSmsManager;
	ConnectivityManager mConnectivityManager;
	ServiceState mServiceState;
	LinkProperties mLinkProperties;
	//INetworkQueryService mNetworkQueryService = null;
	
	boolean testV1String, testV2String, testV2StringWithSpace, parseV2IncorrectFormat;
	
	Context context;
	String mReceivedText="";
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	String mRemark3 = "";
	final Short port = 19989;
	static boolean isTelephonyManagerNull = false;
	static boolean isContextNull = false;
	static boolean isConnectivityManagerNull = false;
	static boolean isIccCardNull = false;
	
	// default are false
	boolean TelephonyBeTest = false;
	boolean SmsBeTest = false;
	
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
	public TelephonyTest()throws ClassNotFoundException{
		super(TARGET_PACKAGE_ID,launcherActivityClass);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		loadConfig();
		mTelephonyManager = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		context = getInstrumentation().getTargetContext();
		mConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if(mConnectivityManager == null){
			isConnectivityManagerNull = true;
			Log.i(tag, "isConnectivityManagerNull = true");
		}
		
		if(context == null){
			isContextNull = true;
			Log.i(tag, "isContextNull = true");
		}
		
		if(mTelephonyManager == null){
			isTelephonyManagerNull = true;
			mLog = "TelephonyManager is null";
			Log.i(tag, "isTelephonyManagerNull = true");
		}
		
	}
	
	public void test_0th_detectSIM() {
		Log.i(tag, "test_0th_detectSIM start");
		if(SmsBeTest){
			testCaseId = getTestCaseId(tTelephonyBasicFunc, dSimDetect, sId);
			if(!isTelephonyManagerNull){
				mLog = "detect SIM";
				if(mTelephonyManager.hasIccCard()){
					Log.i(tag, "test_0th_detectSIM() true");
					outputResult(true, tTelephonyBasicFunc, dSimDetect, testCaseId);
				}else{
					Log.i(tag, "test_0th_detectSIM() false");
					isIccCardNull = true;
					mLog = "detect no SIM card";
					outputResult(false, tTelephonyBasicFunc, dSimDetect, testCaseId);
				}
			}else{
				//mLog = "mTelephonyManager == null";
				outputResult(false, tTelephonyBasicFunc, dSimDetect, testCaseId);
			}
		}
	}
	
	
	public void test_1st_DataEnabler() {
		if(TelephonyBeTest){
			Log.i(tag, "test_1st_DataEnabler start");
			testCaseId = getTestCaseId(tTelephonyBasicFunc, dDataEnaler, sId);
			if(!isTelephonyManagerNull && !isConnectivityManagerNull && !isIccCardNull){
				mLog = "Data Enabler";
				boolean nowStatus, checkDataEnabled, checkDataEnabledBar;
				mWifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
				if(mWifiManager == null){
					mLog = "mWifiManager == null";
				}
				if(!turnOffWifi()){
					mLog = "turn Off Wifi fail";
				}
				nowStatus = mConnectivityManager.getMobileDataEnabled();
				
				enableDataEnabled(!nowStatus);
				checkDataEnabled = enableDataEnabled(nowStatus);
				checkDataEnabledBar = enableDataEnabled(!nowStatus);
				if(nowStatus){
					enableDataEnabled(nowStatus);
				}
				
				if(checkDataEnabled && checkDataEnabledBar && browseWeb()){
					Log.i(tag, "test_1st_DataEnabler true");
					outputResult(true, tTelephonyBasicFunc, dDataEnaler, testCaseId);
				}else{
					Log.i(tag, "test_1st_DataEnabler false");
					outputResult(false, tTelephonyBasicFunc, dDataEnaler, testCaseId);
				}
			}else{
				mLog = "mTelephonyManager == null || isConnectivityManagerNull == null || isIccCardNull";
				outputResult(false, tTelephonyBasicFunc, dDataEnaler, testCaseId);
			}
		}
	}
	
	public boolean enableDataEnabled(boolean willDataEnabled){
		
		if(isConnectivityManagerNull){
			mLog = "ConnectivityManager == null";
			return false;
		}
		
		if(willDataEnabled){
			mConnectivityManager.setMobileDataEnabled(true);
		}else{
			mConnectivityManager.setMobileDataEnabled(false);
		}
		
		long startTime = System.currentTimeMillis();
		while((System.currentTimeMillis() - startTime) < SHORT_TIME_OUT){
			if(willDataEnabled){
				if(mConnectivityManager.getMobileDataEnabled() && 
						mTelephonyManager.getDataState() == mTelephonyManager.DATA_CONNECTED ){
					Log.i(tag, "getMobileDataEnabled() && mTelephonyManager.getDataState() == mTelephonyManager.DATA_CONNECTED");
					sleep(3000);
					return true;
				}
			}else{
				if(!mConnectivityManager.getMobileDataEnabled() && 
						mTelephonyManager.getDataState() == mTelephonyManager.DATA_DISCONNECTED){
					Log.i(tag, "!getMobileDataEnabled() && mTelephonyManager.getDataState() == mTelephonyManager.DATA_DISCONNECTED");
					sleep(3000);
					return true;
				}
			}
			sleep(100);
		}
		sleep(3000);
		mLog = "enableDataEnabled fail";
		return false;
	}
	
	public void test_2nd_BrowseWeb(){
		if(TelephonyBeTest){
			Log.i(tag, "test_2nd_BrowseWeb start");
			testCaseId = getTestCaseId(tTelephonyBasicFunc, dBrowseWeb, sId);
			if(!isTelephonyManagerNull && !isContextNull && !isConnectivityManagerNull && !isIccCardNull){
				mLog = "Browsing web";
				mWifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
				if(mWifiManager == null){
					mLog = "mWifiManager == null";
				}
				if(!turnOffWifi()){
					mLog = "turn Off Wifi fail";
				}
				if(mConnectivityManager.getMobileDataEnabled()){
					Log.i(tag, "Data already enabled");
					checkBrowseWeb();
				}else{
					if(enableDataEnabled(true)){
						Log.i(tag, "enableDataEnabled() true browse web");
						checkBrowseWeb();
					}else{
						Log.i(tag, "enableDataEnabled() fail browse web");
						mLog = "enableDataEnabled() fail browse web";
						outputResult(false, tTelephonyBasicFunc, dBrowseWeb, testCaseId);
					}
				}
			}else{
				mLog = "mTelephonyManager == null || context == null || isIccCardNull";
				outputResult(false, tTelephonyBasicFunc, dBrowseWeb, testCaseId);
			}
		}
	}
	
	public void checkBrowseWeb(){
		if(browseWeb()){
			Log.i(tag, "test_2nd_BrowseWeb true");
			outputResult(true, tTelephonyBasicFunc, dBrowseWeb, testCaseId);
		}else{
			Log.i(tag, "test_2nd_BrowseWeb fail");
			mLog = "browse web time out";
			outputResult(false, tTelephonyBasicFunc, dBrowseWeb, testCaseId);
		}
	}
	
	public boolean browseWeb(){
		long startTime = System.currentTimeMillis();
		while((System.currentTimeMillis() - startTime) < WiFi_TIMEOUT * 3){
			if(httpClientTest()){
				return true;
			}
			sleep(100);
		}
		return false;
	}
	
	public boolean httpClientTest() {
		Log.i(tag, "httpClientTest run");
		HttpClient client = new DefaultHttpClient();
		try {
		HttpGet request = new HttpGet("http://www.google.com.tw");
		HttpResponse response = client.execute(request);
		Log.i(tag, "httpClientTest try ");
			if (response.getStatusLine().getStatusCode() == 200) { //200 OK (HTTP/1.0 - RFC 1945)
				Log.i(tag, "Pass");
				return true;
			} else {
				Log.i(tag, "Fail: Code: " + String.valueOf(response));
				//return false;
			}
			request.abort();
		} catch (IOException e) {
			Log.i(tag, "Fail: IOException");
			return false;
		}
		return false;
    }
	
	public void test_3th_ApnSetting() {
		if(TelephonyBeTest){
			Log.i(tag, "test_3th_ApnSetting() start");
			testCaseId = getTestCaseId(tTelephonyBasicFunc, dApnSetting, sId);
			mLog = "APN setting";
			if(!isIccCardNull){
				ApnCheck();
			
				if(testV1String && testV2String && testV2StringWithSpace && !parseV2IncorrectFormat){
					Log.i(tag, "test_3th_ApnSetting() true");
					outputResult(true, tTelephonyBasicFunc, dApnSetting, testCaseId);
				}else{
					Log.i(tag, "test_3th_ApnSetting() false");
					mLog = "test_3th_ApnSetting() false";
					outputResult(false, tTelephonyBasicFunc, dApnSetting, testCaseId);
				}
			}else{
				mLog = "isIccCardNull";
				outputResult(false, tTelephonyBasicFunc, dApnSetting, testCaseId);
			}
		}
	}
	
	public void ApnCheck(){
		
		String[] dunTypes = {"DUN"};
		String[] mmsTypes = {"mms", "*"};
	
		ApnSetting expected_apn;
		String testString;
	
		// A real-world v1 example string.
		testString = "Vodafone IT,web.omnitel.it,,,,,,,,,222,10,,DUN";
		expected_apn =  new ApnSetting(
			-1, "22210", "Vodafone IT", "web.omnitel.it", "", "",
			"", "", "", "", "", 0, dunTypes, "IP", "IP",true,0);
		Log.i(tag, "testV1String:" + Boolean.toString(ApnSettingEqual(expected_apn, ApnSetting.fromString(testString))));
		testV1String = ApnSettingEqual(expected_apn, ApnSetting.fromString(testString));
	
	 
		// A v2 string.
		testString = "[ApnSettingV2] Name,apn,,,,,,,,,123,45,,mms|*,IPV6,IP,true,14";
		expected_apn =  new ApnSetting(
			-1, "12345", "Name", "apn", "", "",
	        	"", "", "", "", "", 0, mmsTypes, "IPV6", "IP",true,14);
		Log.i(tag, "testV1String:" + Boolean.toString(ApnSettingEqual(expected_apn, ApnSetting.fromString(testString))));
		testV2String = ApnSettingEqual(expected_apn, ApnSetting.fromString(testString));
	
		// A v2 string with spaces.
		testString = "[ApnSettingV2] Name,apn, ,,,,,,,,123,45,,mms|*,IPV4V6, IP,true,14";
		expected_apn =  new ApnSetting(
			-1, "12345", "Name", "apn", "", "",
				"", "", "", "", "", 0, mmsTypes, "IPV4V6", "IP",true,14);
		Log.i(tag, "testV2StringWithSpace:" + Boolean.toString(ApnSettingEqual(expected_apn, ApnSetting.fromString(testString))));
		testV2StringWithSpace =  ApnSettingEqual(expected_apn, ApnSetting.fromString(testString));
	
		// Parse (incorrect) V2 format without the tag as V1.
		testString = "Name,apn,,,,,,,,,123, 45,,mms|*,IPV6,true,14";
		String[] incorrectTypes = {"mms|*", "IPV6"};
		expected_apn =  new ApnSetting(-1, "12345", "Name", "apn", "", "",
				"", "", "", "", "", 0, incorrectTypes, "IP", "IP",true,14);
		Log.i(tag, "parseV2IncorrectFormat" + Boolean.toString(ApnSettingEqual(expected_apn, ApnSetting.fromString(testString))));
		parseV2IncorrectFormat = ApnSettingEqual(expected_apn, ApnSetting.fromString(testString));
		
	}
		
	public boolean ApnSettingEqual(ApnSetting a1, ApnSetting a2) {
		
		if(a1.carrier.equals(a2.carrier) && 
			a1.apn.equals(a2.apn) &&
			a1.proxy.equals(a2.proxy) &&
			a1.port.equals(a2.port) &&
			a1.user.equals(a2.user) &&
			a1.password.equals(a2.password) &&
			a1.numeric.equals(a2.numeric) &&
			a1.protocol.equals(a2.protocol) &&
			a1.roamingProtocol.equals(a2.roamingProtocol) &&
			a1.authType == a2.authType &&
			a1.id == a2.id &&
			a1.bearer == a2.bearer &&
			!(a1.carrierEnabled ^ a2.carrierEnabled)){
			
			return true;
		}else{
			return false;
		}
		
	}
	
	public void test_4th_SendAndReceiveSMSMessages() throws InterruptedException {
		if(TelephonyBeTest){
			Log.i(tag, "test_4th_SendAndReceiveSMSMessages() start");
			testCaseId = getTestCaseId(tSms, dSendAndReceiveSms, sId);
			mPhoneNumber = GenericFunction.getTag(pkgName, mDomain, tSms, dSendAndReceiveSms, "Remark1");

			mLog = "Send and receive SMS message";
			mSmsManager = SmsManager.getDefault();
			if(mSmsManager != null && !isIccCardNull){
				
				prepareSMS();
				
				if(checkSMSMessage()){
					Log.i(tag, "test_4th_SendAndReceiveSMSMessages() true");
					outputResult(true, tSms, dSendAndReceiveSms, testCaseId);
				}else{
					Log.i(tag, "test_4th_SendAndReceiveSMSMessages() false");
					mLog = "check SMS time out";
					outputResult(false, tSms, dSendAndReceiveSms, testCaseId);
				}
				
				context.unregisterReceiver(SmsBroadcastReceiver);
			}else{
				mLog = "mTelephonyManager == null || !isIccCardNull";
				outputResult(false, tSms, dSendAndReceiveSms, testCaseId);
			}
		}
	}
	
	public void prepareSMS(){
		
		IntentFilter sendIntentFilter = new IntentFilter(SMS_SEND_ACTION);
		IntentFilter deliveryIntentFilter = new IntentFilter(SMS_DELIVERY_ACTION);
		IntentFilter dataSmsReceivedIntentFilter = new IntentFilter(DATA_SMS_RECEIVED_ACTION);

		mSendIntent = new Intent(SMS_SEND_ACTION);
		mDeliveryIntent = new Intent(SMS_DELIVERY_ACTION);

		dataSmsReceivedIntentFilter.addDataScheme("sms");
		dataSmsReceivedIntentFilter.addDataAuthority("localhost", port.toString());

		context.registerReceiver(SmsBroadcastReceiver, sendIntentFilter);
		context.registerReceiver(SmsBroadcastReceiver, deliveryIntentFilter);
		context.registerReceiver(SmsBroadcastReceiver, dataSmsReceivedIntentFilter);

		mText = mText + "_" + Long.toString(System.currentTimeMillis());

		PendingIntent mSentIntent = PendingIntent.getBroadcast(getInstrumentation().getTargetContext(), 0, mSendIntent,
				PendingIntent.FLAG_ONE_SHOT);
		PendingIntent mDeliveredIntent = PendingIntent.getBroadcast(getInstrumentation().getTargetContext(), 0, mDeliveryIntent,
				PendingIntent.FLAG_ONE_SHOT);

		byte[] data = mText.getBytes();
		mSmsManager.sendDataMessage(mPhoneNumber, null, port, data, mSentIntent, mDeliveredIntent);
		
	} 
	
	public boolean checkSMSMessage(){
		long startTime = System.currentTimeMillis();
		while((System.currentTimeMillis() - startTime) < TIME_OUT){
			if(mReceivedText.equals(mText)){
				Log.i(tag, "mReceivedText.equals(mText)");
				return true;
			}
			sleep(100);
		}
		return false;
	}
	
	private final BroadcastReceiver SmsBroadcastReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			Log.i(tag, "SmsBroadcastReceiver onReceive");
			
			if(intent.getAction().equals(DATA_SMS_RECEIVED_ACTION)){
				Log.i(tag, "intent.getAction().equals(DATA_SMS_RECEIVED_ACTION)");
				StringBuilder sb = new StringBuilder();
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					Object[] obj = (Object[]) bundle.get("pdus");
					SmsMessage[] message = new SmsMessage[obj.length];
					for (int i = 0; i < obj.length; i++) {
						message[i] = SmsMessage.createFromPdu((byte[]) obj[i]);
					}
					
					for (SmsMessage currentMessage : message) {
						byte[] binaryContent = currentMessage.getUserData();
						String readableContent = new String(binaryContent);
						sb.append(readableContent);
					}
				}
				mReceivedText = sb.toString();
				Log.i(tag, "mReceivedText:"+ mReceivedText);
			}
			
		}
	};
	
	public void test_5th_InformatonAndStatus(){
		boolean hasNullManager = false;
		if(TelephonyBeTest){
			Log.i(tag, "test_5th_InformatonAndStatus start");
			testCaseId = getTestCaseId(tTelephonyBasicFunc, dBasicInformation, sId);
			mLog = "Information and status";
			
			if(!isConnectivityManagerNull && !isIccCardNull){	
				mLinkProperties = mConnectivityManager.getActiveLinkProperties();
				
				if(!mConnectivityManager.getMobileDataEnabled()){
					Log.i(tag, "!mConnectivityManager.getMobileDataEnabled()");
					if(enableDataEnabled(true)){
						Log.i(tag, "test_5th_InformatonAndStatus enableDataEnabled() true");
					}else{
						Log.i(tag, "test_5th_InformatonAndStatus enableDataEnabled() fail");
						mLog = "enableDataEnabled() fail";
					}
				}
				
				if(mLinkProperties == null){
					Log.i(tag, "mLinkProperties == null");
					hasNullManager = true;
					mLog = "LinkProperties is Null";
				}
			}else{
				hasNullManager = true;
				mLog = "ConnectivityManager is Null || isIccCardNull";
			}
			
			if(!hasNullManager && !isTelephonyManagerNull && !isContextNull){
				
				InformatonAndStatusCheck();				
			}else{
				Log.i(tag, "mConnectivityManager = null || hasNullManager || isContextNull");
				mLog = "mConnectivityManager = null || hasNullManager || isContextNull InformatonAndStatus";
				outputResult(false, tTelephonyBasicFunc, dBasicInformation, testCaseId);
			}
			
		}
		enableDataEnabled(false); //turn off 3g 
	}
	
	public void InformatonAndStatusCheck(){
		
		StringBuilder sb = new StringBuilder("");
		boolean hasFailInfo = false;
		
		if(mTelephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE){
			sb.append("getPhoneType:" + Integer.toString(mTelephonyManager.getPhoneType()) + "\n");
		}else{
			sb.append("getPhoneType:" + "fail");
			hasFailInfo = true;
		}
		
		if(mTelephonyManager.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN){ 
			sb.append("getNetworkType:" + Integer.toString(mTelephonyManager.getNetworkType()) + "\n");
		}else{
			sb.append("getNetworkType:" + "fail");
			hasFailInfo = true;
		}
		
		String operatorName = mTelephonyManager.getNetworkOperatorName();
		if(operatorName != null && !operatorName.equals("")){ // OperatorName
			sb.append("getNetworkOperatorName:" + mTelephonyManager.getNetworkOperatorName() + "\n");
		}else{
			sb.append("getNetworkOperatorName:" + "fail");
			hasFailInfo = true;
		}
		
		String serial = Build.SERIAL;
		if(serial != null && !serial.equals("")){ // SerialNumber
			sb.append("get Serial Number:" + serial + "\n");
		}else{
			sb.append("get Serial Number:" + "fail");
			hasFailInfo = true;
		}
		
		if(mTelephonyManager.getSimState() != TelephonyManager.SIM_STATE_UNKNOWN){
			sb.append("getSimState:" + Integer.toString(mTelephonyManager.getSimState()) + "\n");
		}else{
			sb.append("getSimState:" + "fail");
			hasFailInfo = true;
		}
		
		//It's all ok
		sb.append("isNetworkRoaming:" + Boolean.toString(mTelephonyManager.isNetworkRoaming()) + "\n");
		
		if(mTelephonyManager.getDataState() != TelephonyManager.DATA_DISCONNECTED){ 
			sb.append("getDataState:" + Integer.toString(mTelephonyManager.getDataState()) + "\n");	
		}else{
			sb.append("getDataState:" + "fail");
			hasFailInfo = true;
		}
		
		String formatIP = formatIpAddresses(mLinkProperties);
		if(formatIP != null && !formatIP.equals("")){ //IP Address
			sb.append("IP Address:" + formatIpAddresses(mLinkProperties) + "\n");
		}else{
			sb.append("IP Address:" + "fail");
			hasFailInfo = true;
		}

		if(mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM){
			if(checkGsmDeviceId(mTelephonyManager.getDeviceId())){
				sb.append("IMEI:" + mTelephonyManager.getDeviceId() + "\n");
			}else{
				sb.append("IMEI:" + "fail");
				hasFailInfo = true;
			}
		}else{
			sb.append("IMEI:" + "fail");
			hasFailInfo = true;
		}
	
		Log.i(tag, sb.toString());
		
		mLog = sb.toString();
		if(!hasFailInfo){
			Log.i(tag, "test_5th_InformatonAndStatus true");
			outputResult(true, tTelephonyBasicFunc, dBasicInformation, testCaseId);
		}else{
			Log.i(tag, "test_5th_InformatonAndStatus false");
			outputResult(false, tTelephonyBasicFunc, dBasicInformation, testCaseId);
		}
		
		//Log.i(tag, "getPhoneType:" + Integer.toString(mTelephonyManager.getPhoneType()));
		//Log.i(tag, "getNetworkType:" + Integer.toString(mTelephonyManager.getNetworkType()));
		//Log.i(tag, "getNetworkOperatorName:" + mTelephonyManager.getNetworkOperatorName());
		//Log.i(tag, "getSimSerialNumber:" + mTelephonyManager.getSimSerialNumber());
		//Log.i(tag, "getSimState:" + Integer.toString(mTelephonyManager.getSimState()));
		//Log.i(tag, "isNetworkRoaming:" + Boolean.toString(mTelephonyManager.isNetworkRoaming()));
		//Log.i(tag, "getDataState:" + Integer.toString(mTelephonyManager.getDataState()));	
		//Log.i(tag,"formatIpAddresses(mLinkProperties): " + formatIpAddresses(mLinkProperties));
	
	}
	
	private static String formatIpAddresses(LinkProperties prop) {
		if (prop == null) return null;
			Iterator<InetAddress> iter = prop.getAddresses().iterator();
			// If there are no entries, return null
		    if (!iter.hasNext()) return null;
		    	// Concatenate all available addresses, comma separated
		    	String addresses = "";
		        while (iter.hasNext()) {
		            addresses += iter.next().getHostAddress();
		            if (iter.hasNext()) addresses += ", ";
		        }
		        return addresses;
	}

	public void test_6th_ReadPhoneBook() {
		String sName = "name";
		String sNumber = "number";
		String sEmails = "emails";
		String s_id = "_id";
		if(TelephonyBeTest){
			Log.i(tag, "test_6th_ReadPhoneBook start");
			testCaseId = getTestCaseId(tTelephonyBasicFunc, dSimRead, sId);
			mLog = "Read PhoneBook";
			if(!isIccCardNull){
				Uri simUri = Uri.parse("content://icc/adn");
				Cursor cursorSim = context.getContentResolver().query(simUri, null, null,null, null);
				Log.i(tag,"getColumnIndex(sName):" + Integer.toString(cursorSim.getColumnIndex(sName)));
				Log.i(tag,"getColumnIndex(sNumber):" + Integer.toString(cursorSim.getColumnIndex(sNumber)));
				Log.i(tag, "sim row count:" + Integer.toString(cursorSim.getCount()));
				String[] simColNm = cursorSim.getColumnNames();
				for(int i = 0; i < simColNm.length ;i++)
					Log.i(tag,"simColNm:" + simColNm[i]);
			
				if(simColNm.length >= 0 && simColNm[0].equals(sName)
						&& simColNm[1].equals(sNumber)
						&& simColNm[2].equals(sEmails)
						&& simColNm[3].equals(s_id)){
					Log.i(tag, "test_6th_ReadPhoneBook() true");
					outputResult(true, tTelephonyBasicFunc, dSimRead, testCaseId);
				}else{
					Log.i(tag, "test_6th_ReadPhoneBook() false");
					mLog = "simColNm.length < 0 || read column name error";
					outputResult(false, tTelephonyBasicFunc, dSimRead, testCaseId);
				}
			}else{
				Log.i(tag, "test_6th_ReadPhoneBook() isIccCardNull");
				mLog = "isIccCardNull";
				outputResult(false, tTelephonyBasicFunc, dSimRead, testCaseId);
			}
		}
		
		SystemProperties.set("persist.sys.test","TelephonyTestIsdone");
		enableDataEnabled(false); //turn off 3g in the end
	}
	
	public boolean turnOffWifi(){
		long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) < WiFi_TIMEOUT){
        	if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED && 
        			!mWifiManager.isWifiEnabled()){
	        	Log.i(tag, "WifiManager.WIFI_STATE_DISABLED");
	        	return true;
	        }
        }
        Log.i(tag, "turnOffWifi fail");
        return false;
	}
	
	public boolean checkGsmDeviceId(String deviceId){
		// IMEI may include the check digit
		String imeiPattern = "[0-9]{14,15}";
		int expectedCheckDigit = getLuhnCheckDigit(deviceId.substring(0, 14));
		int actualCheckDigit = Character.digit(deviceId.charAt(14), 10);
		if(Pattern.matches(imeiPattern, deviceId)){
			if(deviceId.length() == 15)
				if(expectedCheckDigit == actualCheckDigit)
						return true;
			return true;
		}
		return false;
	}
	
	/**
	* Use decimal value (0-9) to index into array to get sum of its digits
	* needed by Lunh check.
	*
	* Example: DOUBLE_DIGIT_SUM[6] = 3 because 6 * 2 = 12 => 1 + 2 = 3
	*/
	private static final int[] DOUBLE_DIGIT_SUM = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9};
	
	/**
	* Calculate the check digit by starting from the right, doubling every
	* each digit, summing all the digits including the doubled ones, and
	* finding a number to make the sum divisible by 10.
	*
	* @param deviceId not including the check digit
	* @return the check digit
	*/
	private static int getLuhnCheckDigit(String deviceId) {
		int sum = 0;
	    int dontDoubleModulus = deviceId.length() % 2;
	    for (int i = deviceId.length() - 1; i >= 0; --i) {
	    	int digit = Character.digit(deviceId.charAt(i), 10);
	    	if (i % 2 == dontDoubleModulus) {
	            sum += digit;
	        } else {
	            sum += DOUBLE_DIGIT_SUM[digit];
	        }
	    }
	    sum %= 10;
	    return sum == 0 ? 0 : 10 - sum;
	}
	
	public String getTestCaseId(String mType, String mDescription, String mTag){
		
		Log.i(tag, "getTestCaseId");
		
		return GenericFunction.getTag(pkgName, mDomain, mType, mDescription, mTag);
	}
	
	public void loadConfig(){
		
		Log.i(tag, "loadConfig");
		
		TelephonyBeTest = GenericFunction.getConfig(pkgName, mDomain, tTelephonyBasicFunc);
		SmsBeTest = GenericFunction.getConfig(pkgName, mDomain, tSms);
	}
	
	public void outputResult(boolean result,String sType ,String sDescription, String sCaseId){
		GenericFunction.putResult(pkgName, mDomain, sType, sDescription,
				result, sCaseId, mLog, mRemark1, mRemark2, mRemark3);
	}
	
	private void sleep(long time){
		try{
			Thread.sleep(time);
		}catch(InterruptedException e){
			
		}
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		testCaseId = "-1"; //initial ID variable
	}
}
