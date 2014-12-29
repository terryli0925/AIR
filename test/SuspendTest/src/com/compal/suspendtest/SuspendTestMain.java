/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.suspendtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.os.SystemProperties;

public class SuspendTestMain extends Activity {
	private final static String TAG = "SuspendTest";
	private final static String mPKGName = "com.compal.suspendtest";
	private final static String mDomain = "suspend";
	
	EditText SuspendDelayTime, DelayTriggerTime, TotalTest;
	Button ButtonStart;
	
	int TotalTestInt = 0;
	int SuspendDelayTimeInt = 0;
	int DelayTriggerTimeInt = 0;
	Boolean CheckResult = false;
	String TotalTestString = "";
	String SuspendDelayTimeString = "";
	String DelayTriggerTimeString = "";
	
/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		findView();
		
		showRemoveUsbDialog();
		
		ButtonStart.setOnClickListener( new View.OnClickListener(){
	          public void onClick(View v) {
	        	  
	        	  getParameter();
	        	  
	        	  CheckResult = checkParameter();
	        	  
	        	  if(CheckResult){
	        		  setProperty();
		        	  
	        		  Log.i(TAG, "Intent: Start SuspendTestAcivity");
		        	  Intent StartSuspendTestActivityIntent = new Intent("StartSuspendTestActivity");
		        	  StartSuspendTestActivityIntent.setClass(SuspendTestMain.this, SuspendTestActivity.class);	        	  
		        	  startActivity(StartSuspendTestActivityIntent);
		        	  
		        	  Log.i(TAG, "Finish SuspendTestMain");
		        	  finish();
	        	  }else{
	        		  showIncorrectValueDialog();
	        	  }
	          }
		});
	}  
	
	private void findView(){
    	Log.i(TAG, "findView");
    	
    	SuspendDelayTime = (EditText)findViewById(R.id.suspend_delay_time);
    	DelayTriggerTime = (EditText)findViewById(R.id.delay_trigger_time);
    	TotalTest = (EditText)findViewById(R.id.total_test);
    	ButtonStart = (Button)findViewById(R.id.start);
    }
	
	private void showRemoveUsbDialog() {
		Log.i(TAG, "showRemoveUsbDialog");
		
		new AlertDialog.Builder(this)
			.setTitle("SuspendTest")
			.setMessage("Must remove usb cable!!")
			.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}
			)
			.show();
	}
	
	private void getParameter(){
		Log.i(TAG, "getParameter");
		
		TotalTestString = TotalTest.getText().toString();
		SuspendDelayTimeString = SuspendDelayTime.getText().toString();
  	    DelayTriggerTimeString = DelayTriggerTime.getText().toString();
  	  
  	    Log.i(TAG, "Total test: "+TotalTestString);
  	    Log.i(TAG, "Suspend delay time(s): "+SuspendDelayTimeString);
  	    Log.i(TAG, "Delay trigger time(s): "+DelayTriggerTimeString);
  	    
  	    
	}
	
	private boolean checkParameter(){
		boolean IsParameterNull = false;
		
		Log.i(TAG, "checkParameter");
		
		try { 
  		    TotalTestInt = Integer.parseInt(TotalTestString);
  		    SuspendDelayTimeInt = Integer.parseInt(SuspendDelayTimeString);
    	    DelayTriggerTimeInt = Integer.parseInt(DelayTriggerTimeString);
	    } 
	    catch(NumberFormatException e) {
		    Log.i(TAG, "NumberFormatException: Parameter is null");
		    IsParameterNull = true;
	    }
		finally{
	    	if(!IsParameterNull && TotalTestInt > 0 && DelayTriggerTimeInt > 0){
	    		return true;
	    	}else{
	    		return false;
	    	}
	    }
	}
	
	private void setProperty(){
		Log.i(TAG, "setProperty");
		
		SystemProperties.set("persist.sys.suspendtestcount", "1");
  	    SystemProperties.set("persist.sys.suspendtotaltest", TotalTestString);
  	    SystemProperties.set("persist.sys.suspenddelaytime", SuspendDelayTimeString);
  	    SystemProperties.set("persist.sys.suspendtriggertime", DelayTriggerTimeString);
  	    SystemProperties.set("persist.sys.enablebyshell", "False");
	}
	
	private void showIncorrectValueDialog(){
		Log.i(TAG, "showIncorrectValueDialog");
		
		new AlertDialog.Builder(this)
			.setTitle("SuspendTest")
			.setMessage("Input incorrect value!!")
			.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}
			)
			.show();
	}
}
