/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.coldboottest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;


public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent ColdBootTestIntent = new Intent(context, ColdBootTestActivity.class);
            ColdBootTestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            SystemProperties.set("persist.sys.test", "ColdBootTestIsdone");
	   
            context.startActivity(ColdBootTestIntent);
        }
    }

}
