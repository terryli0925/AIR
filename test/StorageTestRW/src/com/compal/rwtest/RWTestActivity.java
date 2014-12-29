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


package com.compal.rwtest;

import java.io.FileOutputStream;
import android.os.StatFs; // for Storage

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.compal.genericfunction.*;

public class RWTestActivity extends Activity {
	private static final String TAG = "StorageTest";
	private static final String PKGName = "com.compal.rwtest";
	public static final String SdFileString = Long.toString(System.currentTimeMillis());
	boolean bWritePass=false;
	boolean bReadPass=false;
	
	private StorageManager mStorageManager = null;
	public static final String mDomain = "storage";
	public static String mType = "";
	public static String mDescription = "";
	private static final String ConfigID = "ID";
	private static final long KB_IN_BYTES = 1024;
	private static final long MB_IN_BYTES = KB_IN_BYTES * 1024;
	private static final long GB_IN_BYTES = MB_IN_BYTES * 1024;

	String SdCardPath = "";
	
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	String mRemark3 = "";

	private boolean StorageReadWriteSdCardBeTest = false;
	private boolean StorageReadWriteInternalStroageBeTest = false;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
	loadConfig();
	String ID;
	mStorageManager = (StorageManager) this.getSystemService(this.STORAGE_SERVICE);
	if (mStorageManager == null) {
		mLog = "mStorageManager == null";
		if(StorageReadWriteInternalStroageBeTest){
			mDescription = "read and write sd card";
			mType="storage read and write";
			ID = GenericFunction.getTag(PKGName, mDomain, mType, mDescription,ConfigID);
			GenericFunction.putResult(PKGName, mDomain, mType, mDescription,false, ID, mLog, mRemark1, mRemark2, mRemark3);
		}
		if(StorageReadWriteSdCardBeTest){
			mDescription = "read and write Internal Storage";
			mType="storage read and write";
			ID = GenericFunction.getTag(PKGName, mDomain, mType, mDescription,ConfigID);
			GenericFunction.putResult(PKGName, mDomain, mType, mDescription,false, ID, mLog, mRemark1, mRemark2, mRemark3);
		}
	}else{
   		StorageReadWriteInternalStroage();
		StorageReadWriteSdCard();
	}
    }
    
    public void StorageReadWriteSdCard(){
		if(StorageReadWriteSdCardBeTest){
			boolean bWritePass=false;
			boolean bReadPass=false;
			mDescription = "read and write sd card";
			mType="storage read and write";
			mLog="";
			String ID = GenericFunction.getTag(PKGName, mDomain, mType, mDescription,ConfigID);
			bWritePass = WriteFileToSdCard();
			if ( bWritePass ){
				mLog+="\nW Sd Card OK";
			}else{
				mLog+="\nW Sd Card Fail";
			}

			bReadPass = ReadFileFromSdCard();
			if ( bReadPass ){
				mLog+="\nR Sd Card OK";
			}else{
				mLog+="\nR Sd Card Fail";
			}

			if (bWritePass && bReadPass){
				GenericFunction.putResult(PKGName, mDomain, mType, mDescription,true, ID, mLog, mRemark1, mRemark2, mRemark3);
			}else{
				GenericFunction.putResult(PKGName, mDomain, mType, mDescription,false, ID, mLog, mRemark1, mRemark2, mRemark3);
			}


		}
    }	

    private boolean WriteFileToSdCard() {
		boolean bResult = false;
		String SdCardPath = FindSdCardPath();
		if ( 0 == getTotalSize(SdCardPath)){
			return bResult;
		}
		FileWriter myWFile = null;
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

    public void StorageReadWriteInternalStroage(){
		if(StorageReadWriteInternalStroageBeTest){
			boolean bWritePass=false;
			boolean bReadPass=false;
			mDescription = "read and write Internal Storage";
			mType="storage read and write";
			mLog="";
			String ID = GenericFunction.getTag(PKGName, mDomain, mType, mDescription,ConfigID);
		   	bWritePass = WriteFileToInternalStorage();			
			if ( bWritePass ){
				mLog+="\nW internal OK";
			}else{
				mLog+="\nW internal Fail";
			}

		   	bReadPass = ReadFileFromInternalStorage();
			if ( bReadPass ){
				mLog+="\nR internal OK";
			}else{
				mLog+="\nR internal Fail";
			}
			if (bWritePass && bReadPass){
				GenericFunction.putResult(PKGName, mDomain, mType, mDescription,true, ID, mLog, mRemark1, mRemark2, mRemark3);
			}else{
				GenericFunction.putResult(PKGName, mDomain, mType, mDescription,false, ID, mLog, mRemark1, mRemark2, mRemark3);
			}

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
	
	private long getTotalSize(String mPath){
		StatFs stat = new StatFs( mPath );
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		long mTotalSize = totalBlocks * blockSize;
		return mTotalSize/MB_IN_BYTES;
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

    	public void loadConfig(){
		Log.i(TAG, "loadConfig");
		StorageReadWriteSdCardBeTest = GenericFunction.getConfig(PKGName, mDomain, "storage read and write");
		StorageReadWriteInternalStroageBeTest = GenericFunction.getConfig(PKGName, mDomain,"storage read and write");
    	}
}
