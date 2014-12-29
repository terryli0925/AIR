/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.coldboottest;

import java.io.*;

import android.app.Activity;
import android.view.View;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.os.SystemProperties;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.content.Intent;


import com.compal.genericfunction.GenericFunction;

public class BootSettingActivity extends Activity {
    private final static String TAG = " luke ColdBootTest";
    private final static String mPKGName = "com.compal.coldboottest";
    private final static String mDomain = "coldboot";
    private final static String mType = "cold boot";
    private final static String mDescription = "cold boot";
    //private final static int TOTAL_TEST = 2;		//default

    private Button startButton;
    private Button backButton;
    private EditText cycleCountEdit;
    private EditText delayCountEdit;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        Log.i(TAG, "start setting configuration");
        startButton = (Button) findViewById(R.id.start_button);
        backButton = (Button) findViewById(R.id.back_button);
        cycleCountEdit = (EditText) findViewById(R.id.boot_cyclee_count_edit);
        delayCountEdit = (EditText) findViewById(R.id.boot_time_count_edit);

        startButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "start luke");

                if (setConfig()) {
                Intent intent = new Intent(BootSettingActivity.this,ColdBootTestActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
	  }
        });

        backButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "back luke");
		finish();
            }
        });
    }

    public boolean setConfig() {
        // set cold boot cycle counter and delay time

        Log.i(TAG, "set Config  luke" + cycleCountEdit.getText().toString() + "   " + delayCountEdit.getText().toString());
        String cycleCount = cycleCountEdit.getText().toString();
        String delayCount = delayCountEdit.getText().toString();

        if( cycleCount.equals("") || delayCount.equals("")) {
            Toast.makeText(this, "Please Input Test Cycle and Delay Time", Toast.LENGTH_LONG).show();
	    return false;
        } else {
            SystemProperties.set("persist.sys.bootcycle",cycleCount);
            SystemProperties.set("persist.sys.totalbootcycle",cycleCount);
            SystemProperties.set("persist.sys.bootdelay",delayCount);
            SystemProperties.set("persist.sys.test","");
        }
	return true;
    }

}
