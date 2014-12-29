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

package com.compal.storagetest;

import android.app.Activity;
//import android.app.ActivityManager;
//import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
//import android.test.InstrumentationTestCase;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;

import android.util.Log;

import java.lang.String;
import java.io.IOException;
import com.compal.genericfunction.*;
//====================
import android.os.StatFs; // for Storage
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import java.text.DecimalFormat;
import java.lang.Long;
import android.os.Environment; 
import android.os.storage.IMountService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.IBinder;
import android.os.SystemProperties;
import java.io.OutputStreamWriter;
import java.io.OutputStream; //sd card
import java.io.FileOutputStream; //sd card
import java.io.InputStreamReader;
import java.io.InputStream; //Sd card
import java.io.FileInputStream; //Sd card
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.lang.System;
import java.lang.Float;
import java.lang.NumberFormatException;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

//=====================

@SuppressWarnings("rawtypes")
public class StorageTest extends ActivityInstrumentationTestCase2{

	private static final String TAG = "StorageTest";
	private static final String PKGName = "com.compal.storagetest";
	public static final String TARGET_PACKAGE_ID="com.android.gallery3d";
	public static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME="com.android.gallery3d.app.Gallery";
	public static final String mDomain = "storage";
	public static String mType = "";
	public static String mDescription = "";
	private static final String ConfigID = "ID";
	private static final String ConfigRemark1 = "Remark1";
	private static final String ConfigRemark2 = "Remark2";
	public static final String SdFileString = "This is a test by AIR.";
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	String mRemark3 = "";
    	private static final int SECOND = 1000;
	
	private static final long WAIT_FOR_MOUNT_UMOUNT = 10 * SECOND; 
    	private Activity mActivity;

	//===================
	private StorageManager mStorageManager = null;
    	private static final long KB_IN_BYTES = 1024;
	private static final long MB_IN_BYTES = KB_IN_BYTES * 1024;
	private static final long GB_IN_BYTES = MB_IN_BYTES * 1024;
	private static final float ERR_ALPHA = 0.1f;
	private static final String RWString = "This is a test1.";
    	//===================
	private boolean StorageReadWriteSdCardBeTest = false;
	private boolean StorageReadWriteInternalStroageBeTest = false;
	private boolean StorageStorageInformationBeTest = false;

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
	public StorageTest()throws ClassNotFoundException{
		super(TARGET_PACKAGE_ID,launcherActivityClass);
	}

    public void setUp()throws Exception{
		loadConfig();
		mActivity = getActivity();
		if (mStorageManager == null) {
			mStorageManager = (StorageManager) getActivity().getSystemService(Context.STORAGE_SERVICE);
		}
   		super.setUp();
    }

    public void tearDown()throws Exception{
	 super.tearDown();
    }

	public void loadConfig(){
		Log.i(TAG, "loadConfig");
		StorageReadWriteSdCardBeTest = GenericFunction.getConfig(PKGName, mDomain, "storage read and write");
		StorageReadWriteInternalStroageBeTest = GenericFunction.getConfig(PKGName, mDomain,"storage read and write" );
		StorageStorageInformationBeTest = GenericFunction.getConfig(PKGName, mDomain, "storage information");
	}

	String[] filesize(long size){
		String str="";
		double Dsize = (new Long(size)).doubleValue();
		if(Dsize>=1024){
			str="KB";
			Dsize/=1024;
			if(Dsize>=1024){
				str="MB";
				Dsize/=1024;
				if(Dsize>=1024){
					str="GB";
					Dsize/=1024;
				}
			}
		}
		DecimalFormat formatter=new DecimalFormat();
		formatter.setGroupingSize(3);
		String result[] =new String[2];
		result[0]=formatter.format(Dsize);
		result[1]=str;
		return result;
	}
	
    public void test_1st_StorageStorageInformation()throws Exception{
		if(StorageStorageInformationBeTest){
			String ID = "";
			mType = "storage information";
			mDescription = "sd card information";
			mLog="";
			ID = GenericFunction.getTag(PKGName, mDomain, mType, mDescription,ConfigID);
			String Remark1 = GenericFunction.getTag(PKGName, mDomain, mType, mDescription,ConfigRemark1);
			float SdSize = 0;
			try{
				SdSize = Float.parseFloat(Remark1);
			}catch(NumberFormatException e){
				e.printStackTrace();
				SdSize = 0;
			}       
			String SdPath = FindSdCardPath();
			mLog+="Config:"+SdSize+", Device:"+getTotalSize(SdPath)+"\n";
			if( 0 == getTotalSize(SdPath) ){
				GenericFunction.putResult(PKGName, mDomain, mType, mDescription,false , ID, mLog, mRemark1, mRemark2, mRemark3);
			}else if( inRange((long)SdSize, getTotalSize(SdPath)) ){
				GenericFunction.putResult(PKGName, mDomain, mType, mDescription,true , ID, mLog, mRemark1, mRemark2, mRemark3);
			}else{
				GenericFunction.putResult(PKGName, mDomain, mType, mDescription,false, ID, mLog, mRemark1, mRemark2, mRemark3);
			}

			mDescription = "internal storage information";
			mLog="";
			ID = GenericFunction.getTag(PKGName, mDomain, mType, mDescription,ConfigID);
			Remark1 = GenericFunction.getTag(PKGName, mDomain, mType, mDescription,ConfigRemark1);
			float InternalSize = 0;
			try{
				InternalSize = Float.parseFloat(Remark1);
			}catch(NumberFormatException e){
				e.printStackTrace();
				InternalSize = 0;
			}       
			String InternalPath = FindInternalPath();
			mLog+="Config:"+InternalSize+", Device:"+getTotalSize(InternalPath)+"\n";
			if( inRange((long)InternalSize, getTotalSize(InternalPath)) ){
				GenericFunction.putResult(PKGName, mDomain, mType, mDescription,true , ID, mLog, mRemark1, mRemark2, mRemark3);
			}else{
				GenericFunction.putResult(PKGName, mDomain, mType, mDescription,false , ID, mLog, mRemark1, mRemark2, mRemark3);
			}

			
		}
    }
	private boolean inRange(long configValue, long deviceValue){
		if((configValue*(1-ERR_ALPHA) < deviceValue) && (deviceValue < configValue*(1+ERR_ALPHA))){
			return true;
		}else{
			return false;
		}
	}

	private boolean WriteFileToInternalStorage() {
		boolean bResult = false;
		String InternalPath = FindInternalPath();
		if ( 0 == getTotalSize(InternalPath)){
			return bResult;
		}
		
		FileWriter myWInFile = null;
		try {
			myWInFile = new FileWriter(InternalPath+"/InternalTest.txt");
			if (myWInFile != null)
				myWInFile.write(SdFileString);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myWInFile != null) {
				try {
					bResult = true;
					myWInFile.close();
					return bResult;
				} catch (IOException e){
					e.printStackTrace();
					return bResult;
				}
			}
			return bResult;
		}
	}
	private boolean ReadFileFromInternalStorage() {
		
		boolean bResult = false;
		String InternalPath = FindInternalPath();
		if ( 0 == getTotalSize(InternalPath)){
			return bResult;
		}
		
		FileReader myRInFile = null;
		BufferedReader inputIn = null;
		String line="";
		try {
			myRInFile = new FileReader(InternalPath+"/InternalTest.txt");
			if (myRInFile != null)
				inputIn = new BufferedReader(myRInFile);
			if (inputIn != null) {
				while((line = inputIn.readLine())!=null){
				Log.i(TAG, "=====Line ======="+line);
					if (line.equals(SdFileString))
						bResult = true;
				}
				try {
					inputIn.close();
				} catch (IOException e){
					e.printStackTrace();
					return bResult;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myRInFile != null) {
				try {
					myRInFile.close();
					return bResult;
				} catch (IOException e){
					e.printStackTrace();
					return bResult;
				}
			}
			return bResult;
		}
	}
	private String FindInternalPath () {
		StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
		int length = storageVolumes.length;
		int Index_Internal=-1;
		String sDescription = "";
		String InternalPath = "";
		for (int i = 0; i < length; i++) {
			sDescription = storageVolumes[i].getDescription();
			Log.i(TAG, "Storage description:"+sDescription);
			if (sDescription.equals("Internal Storage")){
				Index_Internal = i;
			}
		}
		if (Index_Internal != -1){
			InternalPath = storageVolumes[Index_Internal].getPath();
		}
		Log.i(TAG, "~~~~~~~~~Sd Card Path:"+InternalPath);
		return InternalPath;
	}
	private String FindSdCardPath () {
		StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
		int length = storageVolumes.length;
		int Index_SD=-1;
		String sDescription = "";
		String SdCardPath = "";
		for (int i = 0; i < length; i++) {
			sDescription = storageVolumes[i].getDescription();
			Log.i(TAG, "Storage description:"+sDescription);
			if (sDescription.equals("SD Card")){
				Index_SD = i;
			}
		}
		if (Index_SD != -1){
			SdCardPath = storageVolumes[Index_SD].getPath();
		}
		Log.i(TAG, "~~~~~~~~~Sd Card Path:"+SdCardPath);
		return SdCardPath;
	}
	private boolean WriteFileToSdCard() {
		boolean bResult = false;
		String SdCardPath = FindSdCardPath();
		if ( 0 == getTotalSize(SdCardPath)){
			return bResult;
		}		FileWriter myWFile = null;
		try {
			myWFile = new FileWriter(SdCardPath+"/SdTest.txt");
			if (myWFile != null)
				myWFile.write(SdFileString);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myWFile != null) {
				try {
					bResult = true;
					myWFile.close();
					return bResult;
				} catch (IOException e){
					e.printStackTrace();
					return bResult;
				}
			}
			return bResult;
		}
	}
	private long getTotalSize(String mPath){
		StatFs stat = new StatFs( mPath );
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		long mTotalSize = totalBlocks * blockSize;
		return mTotalSize/MB_IN_BYTES;
	}
	private boolean ReadFileFromSdCard() {
		boolean bResult = false;
		String SdCardPath = FindSdCardPath();
		if ( 0 == getTotalSize(SdCardPath)){
			return bResult;
		}
		
		FileReader myRFile = null;
		BufferedReader input = null;
		String line="";
		try {
			myRFile = new FileReader(SdCardPath+"/SdTest.txt");
			if (myRFile != null)
				input = new BufferedReader(myRFile);
			if (input != null) {
				while((line = input.readLine())!=null){
				Log.i(TAG, "=====Line ======="+line);
					if (line.equals(SdFileString))
						bResult = true;
				}
				try {
					input.close();
				} catch (IOException e){
					e.printStackTrace();
					return bResult;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myRFile != null) {
				try {
					myRFile.close();
					return bResult;
				} catch (IOException e){
					e.printStackTrace();
					return bResult;
				}
			}
			return bResult;
		}
	}
}
