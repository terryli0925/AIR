/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.coldboottest;

import java.io.*;

import android.view.View;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.os.SystemProperties;
import android.widget.Button;
import android.view.View.OnClickListener;


import com.compal.genericfunction.GenericFunction;

public class ColdBootTestActivity extends Activity {
    private final static String TAG = " luke ColdBootTest";
    private final static String mPKGName = "com.compal.coldboottest";
    private final static String mDomain = "coldboot";
    private final static String mType = "cold boot";
    private final static String mDescription = "cold boot";
    private final static int TIMEOUT = 1;
    private int countDownSec=30  ; //seconds

    private Boolean isGameThreadOver = true; //.......
    private Handler handler = new Handler(); //Handler

    private TextView totalTest, delayCount, remainCount;
    private Button stopButton, settingButton, backButton;

    int getTestCount = 0;
    int remaincounter = 0;

    String mId = "";
    String mLog = "";
    String mRemark1 = "";
    String mRemark2 = "";
    String mRemark3 = "";

    Process proc = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        String bootCycle = SystemProperties.get("persist.sys.bootcycle");
        String totalTestStr = SystemProperties.get("persist.sys.totalbootcycle");

        findView();
        buttonRegister();

        if(!bootCycle.equals(""))
            getTestCount = Integer.parseInt(bootCycle);
        else
            getTestCount = 0;

        if(!totalTestStr.equals("")) {
            totalTest.setText(totalTestStr);
            //remainCounter = Integer.parseInt(totalTestStr) - Integer.parseInt(bootCycle);
            remaincounter = Integer.parseInt(totalTestStr) - getTestCount;
            remainCount.setText(Integer.toString(remaincounter));
        }

	mId = GenericFunction.getTag(mPKGName, mDomain, mType, mDescription, "ID");

        if(isEnableColdBootTest() || getTestCount>0) {
            SystemProperties.set("persist.sys.test", "ColdBootIsTesting");
            
            mHandler.sendEmptyMessageDelayed(TIMEOUT, countDownSec*1000*2 ); // for tolerance
            timerStart();
        }else{
        	SystemProperties.set("persist.sys.test","ColdBootTestIsdone");
        	
        	if(totalTestStr.equals("")){
        		finish();
        	}
        	
        }
    }

    private void buttonRegister() {
        stopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                timerStop();
            }
        });

        settingButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                timerStop();

                Intent intent = new Intent(ColdBootTestActivity.this,BootSettingActivity .class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
		finish();
            }
        });

        backButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                timerStop();
		finish();
            }
        });

    }

    private void findView() {
        totalTest = (TextView) findViewById(R.id.boot_total_counter);
        delayCount = (TextView) findViewById(R.id.delay_time_counter );
        remainCount = (TextView) findViewById(R.id.runing_test_counter);
        stopButton = (Button) findViewById(R.id.stop_button);
        settingButton = (Button) findViewById(R.id.setting_button);
	backButton = (Button) findViewById(R.id.back_button);
    }

    private void startCold() {
	
	//set Hanlder Timeout
        //mHandler.sendEmptyMessageDelayed(TIMEOUT, countDownSec*1000*2 ); // for tolerance

        setRTC();
        getTestCount --;
        SystemProperties.set("persist.sys.bootcycle", Integer.toString(getTestCount));

        Log.i(TAG, "Shutdown");
        Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    private boolean isEnableColdBootTest() {

        String testSatus = SystemProperties.get("persist.sys.test");
        if(testSatus.equals("ColdBootTestIsdone")) {
            putResult();
            return false;
        }
        return GenericFunction.getConfig(mPKGName, mDomain, mType);
    }

    private void setRTC() {
        Log.i(TAG, "setRTC");
        try {
            /* Don't need to add "adb shell" in front of command*/
            String cmd = "/system/bin/ColdBootTest";
            proc = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String line = null;
            while ((line = reader.readLine()) != null) {
                Log.i(TAG, line);
            }
        } catch (IOException e) {
            Log.i(TAG, "Error:"+e);
            e.printStackTrace();
        }
    }

    private void putResult() {
    	int testcount;
    	
    	if(remaincounter == 0) {
    		testcount = 1;
    	}else{
    		testcount = remaincounter;
    	}     
    	
    	mLog = "Count of test: "+testcount+"-> Cold boot success\n";

        GenericFunction.putResult(mPKGName, mDomain, mType, mDescription, true, mId, mLog, mRemark1, mRemark2, mRemark3);

        //finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Keyguard unlock
        KeyguardManager km = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        km.newKeyguardLock(TAG).disableKeyguard();
    }


    private void timerStart() {
        try {
            String bootdelay = SystemProperties.get("persist.sys.bootdelay");

            // default countDownSec is 30 seconds
            if(!bootdelay.equals(""))
                countDownSec = Integer.parseInt(bootdelay);

            handler.post(timerCounterThread);
        } catch(Exception ex) {
            Log.e(TAG, "timerStart Error:"+ex.getMessage());
        }
    }

    private void timerStop() {
        try {
            //..runnable
            handler.removeCallbacks(timerCounterThread);

        } catch(Exception ex) {
            Log.e(TAG, "...........:"+ex.getMessage());
        }
    }

    Runnable timerCounterThread = new Runnable() {
        public void run() {
            try {
                //...........thread
                if(countDownSec<=0) {
                    startCold();
                    timerStop();
                } else {
                    //........
                    handler.postDelayed(this, 1000);
                    countDownSec--;
                    delayCount.setText(Integer.toString(countDownSec));
                }
            } catch(Exception ex) {
                Log.e(TAG, "Runnable.... :"+ex.getMessage());
            }
        }
    };

    // for error handling
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.i(TAG,"Timeout --> Close Activity");
            
            int testcount;
        	
        	if(remaincounter == 0) {
        		testcount = 1;
        	}else{
        		testcount = remaincounter;
        	}   

            mLog = "Count of test: "+getTestCount+"-> Cold boot doesn't success\n";
            GenericFunction.putResult(mPKGName, mDomain, mType, mDescription, false, mId, mLog, mRemark1, mRemark2, mRemark3);
		
            SystemProperties.set("persist.sys.test","ColdBootTestIsdone");
	    
            timerStop();

            finish();
        }
    };
}
