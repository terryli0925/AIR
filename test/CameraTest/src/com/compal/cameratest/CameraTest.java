/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.compal.cameratest;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.test.InstrumentationTestCase;
import android.view.KeyEvent;

import android.util.Log;
import com.android.camera.Camera;
import com.android.camera.VideoCamera;

import java.lang.String;
import java.io.IOException;
import com.compal.genericfunction.GenericFunction;

public class CameraTest extends InstrumentationTestCase {
	private final static String TAG = "CameraTest";
	private final static String PKGName = "com.compal.cameratest";
    private static final String PACKAGE_UNDER_TEST = "com.android.camera";
    private static final String ACTIVITY_UNDER_TEST = "Camera";
    private static final int ACTIVITY_STARTUP_WAIT_TIME = 1000;
	private static final long WAIT_FOR_SWITCH_CAMERA = 3000; //3 seconds
	private static final long WAIT_FOR_PREVIEW = 1500; //1.5 seconds
	private static final long VIDEO_DURATION = 5000;//5.0 seconds
	private static final long WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN = 1500;   //1.5 sedconds
	private final static String EXTRAS_CAMERA_FACING = "android.intent.extras.CAMERA_FACING";
    private Context ctx;
    private Activity a, b;

	private final static String mDomain = "camera";
	private final static String mType_1st = "front camera";
	private final static String mType_2nd = "rear camera";
	private boolean CameraFrontCameraBeTest = false;
	private boolean CameraRearCameraBeTest = false;
	//private boolean CameraFlashlightBeTest = false;


    public void setUp()throws Exception{
		loadConfig();
   		super.setUp();
    }

    public void tearDown()throws Exception{
		if(a!=null){
	   		a.finish();
			a=null;
		}
   		if(b!=null){
		   	b.finish();
			b=null;
		}
		Runtime.getRuntime().gc();
		Runtime.getRuntime().runFinalization();
		Runtime.getRuntime().gc();
	 super.tearDown();
    }

	public void loadConfig(){
		Log.i(TAG, "loadConfig");
		CameraFrontCameraBeTest = GenericFunction.getConfig(PKGName, mDomain, mType_1st);
		CameraRearCameraBeTest = GenericFunction.getConfig(PKGName, mDomain, mType_2nd);
		//CameraFlashlightBeTest = GenericFunction.getConfig(PKGName, mDomain, "Flashlight");
	}

    public void test_1st_SwitchCameraBack()throws Exception{
		if(CameraRearCameraBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), Camera.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(EXTRAS_CAMERA_FACING,android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
			a = getInstrumentation().startActivitySync(intent);
			Instrumentation inst = getInstrumentation();
			Thread.sleep(WAIT_FOR_SWITCH_CAMERA);
			captureImages(inst);
			Thread.sleep(3000);
			a.finish();
		}
    }
    public void test_2nd_SwitchVideoCameraBack()throws Exception{
		if(CameraRearCameraBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), VideoCamera.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(EXTRAS_CAMERA_FACING,android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
			b = getInstrumentation().startActivitySync(intent);
			Instrumentation inst = getInstrumentation();
			Thread.sleep(WAIT_FOR_SWITCH_CAMERA);
			captureVideos(inst);
			Thread.sleep(3000);
			b.finish();
		}
    }
    public void test_3rd_SwitchCameraFront()throws Exception{
		if(CameraFrontCameraBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), Camera.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(EXTRAS_CAMERA_FACING,android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
			a = getInstrumentation().startActivitySync(intent);
			Instrumentation inst = getInstrumentation();
			Thread.sleep(WAIT_FOR_SWITCH_CAMERA);
			captureImages(inst);
			Thread.sleep(3000);
			a.finish();
		}
    }
    public void test_4th_SwitchVideoCameraFront()throws Exception{
		if(CameraFrontCameraBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), VideoCamera.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(EXTRAS_CAMERA_FACING,android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
			b = getInstrumentation().startActivitySync(intent);
			Instrumentation inst = getInstrumentation();
			Thread.sleep(WAIT_FOR_SWITCH_CAMERA);
			captureVideos(inst);
			Thread.sleep(3000);
			b.finish();
		}
    }
	public void captureImages(Instrumentation inst) {
		KeyEvent focusEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FOCUS);
		try{
			Thread.sleep(WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN);
			inst.sendKeySync(focusEvent);
			inst.sendCharacterSync(KeyEvent.KEYCODE_CAMERA);
			Thread.sleep(WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN);
		} catch (Exception e) {
			Log.i("======testImageCapture======", "Got exception: " + e.toString());
			assertTrue("testImageCapture", false);
		}
	}

	public void captureVideos(Instrumentation inst){
		try{
			Thread.sleep(WAIT_FOR_PREVIEW);
			inst.sendCharacterSync(KeyEvent.KEYCODE_CAMERA);
			Thread.sleep(VIDEO_DURATION);
			inst.sendCharacterSync(KeyEvent.KEYCODE_CAMERA);
		} catch (Exception e){
			Log.i("======testVideoCapture======", "Got exception: " + e.toString());
			assertTrue("testVideoCapture", false);
		}
	}
}
