/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.mounttest;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import android.view.View;
import android.content.Context;

import android.os.Environment; //write data
import android.os.SystemProperties;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

//====================
import android.os.StatFs; // for Storage
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import java.text.DecimalFormat;
import java.lang.Long;
//import android.os.Environment; 
import android.os.storage.IMountService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.IBinder;
import com.compal.genericfunction.*;
import android.os.Handler;
import android.os.Message;
//import android.os.Process;
import java.lang.Process;
import java.lang.Runtime;

//=====================
import android.provider.MediaStore;
import android.database.Cursor;
import android.content.ContentResolver;

//====================
public class MountTest extends Activity {
    /** Called when the activity is first created. */
	TextView current;
	
	//===================
	private static final String TAG = "MountTest";
	private static final String PKGName = "com.compal.mounttest";
	public static final String mDomain = "storage";
	public static String mType = "sd card mount and umount";
	public static String mDescription = "";
	public static final String TagCheck = "check";
	public static final String TagID = "ID";
	public static final String TagRemark1 = "Remark1";
	public static final String TagRemark2 = "Remark2";
	public static final String TagRemark3 = "Remark3";
	String ID = "";
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	String mRemark3 = "";
	private static final String test_1st_Default = "check media files with insert sd card";
	private static final String test_2nd_MountSd = "mount sd card for checking media file";
	private static final String test_3rd_UnMountSd = "umount sd card for checking media file";
	private static final int SECOND = 1000;
	private static final long WAIT_FOR_MOUNT_UMOUNT = 5 * SECOND;
	private StorageManager mStorageManager = null;
    private static final long KB_IN_BYTES = 1024;
	private static final long MB_IN_BYTES = KB_IN_BYTES * 1024;
	private static final long GB_IN_BYTES = MB_IN_BYTES * 1024;
    // Access using getMountService()
	private IMountService mMountService = null;
	private boolean StorageSDCardMountUnmountBeTest = false;
	private boolean bMountPass=false;
	private boolean bUnMountPass=false;
	private String sPath ="";
    //===================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		loadConfig();

		if(StorageSDCardMountUnmountBeTest){
			mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
			sPath = FindSdCardPath() ;
			SystemProperties.set("persist.sys.sdpath", sPath);
			SystemProperties.set("persist.sys.mounttime", ""+((4*WAIT_FOR_MOUNT_UMOUNT) / SECOND) );

			if (mStorageManager == null) {
				mLog += "Can not get StorageManager instance\n";
				GenericFunction.putResult(PKGName, mDomain, mType, test_1st_Default,false, getID(test_1st_Default), mLog, mRemark1, mRemark2, mRemark3);
				GenericFunction.putResult(PKGName, mDomain, mType, test_2nd_MountSd,false, getID(test_2nd_MountSd), mLog, mRemark1, mRemark2, mRemark3);
				GenericFunction.putResult(PKGName, mDomain, mType, test_3rd_UnMountSd,false, getID(test_3rd_UnMountSd), mLog, mRemark1, mRemark2, mRemark3);
			}else {	
				if(getMountStat(sPath)==1){ //Orig: A(Mount)
					GenericFunction.putResult(PKGName, mDomain, mType, test_1st_Default,true, getID(test_1st_Default), mLog, mRemark1, mRemark2, mRemark3);
					doUnmount(sPath);                  //A->A'
					goSleep(WAIT_FOR_MOUNT_UMOUNT);
					if(getMountStat(sPath)==0){
						doMount(sPath);                    //Test: A'->A
						goSleep(WAIT_FOR_MOUNT_UMOUNT);
						if(getMountStat(sPath)==1){
							bMountPass=true;
							doUnmount(sPath);              //Test A->A'
							goSleep(WAIT_FOR_MOUNT_UMOUNT);
							if(getMountStat(sPath)==0){
								bUnMountPass=true;
							}else{
								bUnMountPass=false;
							}
						}else{
							bMountPass=false;
							bUnMountPass=false;
						}
					}else{
						bMountPass=false;
						bUnMountPass=false;
					}
				}else if(getMountStat(sPath)==0){//Orig: A(unMount)
					GenericFunction.putResult(PKGName, mDomain, mType, test_1st_Default,false, getID(test_1st_Default), mLog, mRemark1, mRemark2, mRemark3);
					doMount(sPath);                  //A->A'
					goSleep(WAIT_FOR_MOUNT_UMOUNT);
					if(getMountStat(sPath)==1){
						doUnmount(sPath);                    //Test: A'->A
						goSleep(WAIT_FOR_MOUNT_UMOUNT);
						if(getMountStat(sPath)==0){
							bUnMountPass=true;
							doMount(sPath);              //Test A->A'
							goSleep(WAIT_FOR_MOUNT_UMOUNT);
							if(getMountStat(sPath)==1){
								bMountPass=true;
							}else{
								bMountPass=false;
							}
						}else{
							bUnMountPass=false;
							bMountPass=false;
						}
					}else{
						bUnMountPass=false;
						bMountPass=false;
					}
				}else if(getMountStat(sPath)==-1){
					mLog += "Mount State error\n";
					GenericFunction.putResult(PKGName, mDomain, mType, test_1st_Default,false, getID(test_1st_Default), mLog, mRemark1, mRemark2, mRemark3);
					GenericFunction.putResult(PKGName, mDomain, mType, test_2nd_MountSd,false, getID(test_2nd_MountSd), mLog, mRemark1, mRemark2, mRemark3);
					GenericFunction.putResult(PKGName, mDomain, mType, test_3rd_UnMountSd,false, getID(test_3rd_UnMountSd), mLog, mRemark1, mRemark2, mRemark3);
				}
				if(bMountPass){
				   	mLog+="Mount Pass\n";
					GenericFunction.putResult(PKGName, mDomain, mType, test_2nd_MountSd,true, getID(test_2nd_MountSd), mLog, mRemark1, mRemark2, mRemark3);
				}else{
				   	mLog+="Mount Fail\n";
					GenericFunction.putResult(PKGName, mDomain, mType, test_2nd_MountSd,false, getID(test_2nd_MountSd), mLog, mRemark1, mRemark2, mRemark3);
				}
				if(bUnMountPass){
				   	mLog+="UnMount Pass\n";
					GenericFunction.putResult(PKGName, mDomain, mType, test_3rd_UnMountSd,true, getID(test_3rd_UnMountSd), mLog, mRemark1, mRemark2, mRemark3);
				}else{
				   	mLog+="UnMount Fail\n";
					GenericFunction.putResult(PKGName, mDomain, mType, test_3rd_UnMountSd,false, getID(test_3rd_UnMountSd), mLog, mRemark1, mRemark2, mRemark3);
				}/*
				if(bMountPass && bUnMountPass){
					GenericFunction.putResult(PKGName, mDomain, mType, mDescription,true, mLog, mRemark1, mRemark2);
				}else{
					GenericFunction.putResult(PKGName, mDomain, mType, mDescription,false, mLog, mRemark1, mRemark2);
				}*/
			}
			//String AA = getShellResult("/system/bin/ls /mnt/ext_sdcard");
			//Log.i(TAG, "Got Result:"+ AA);
		}
		SystemProperties.set("persist.sys.test","MountTestIsdone");
		finish();
	}
	private String getID(String xDesc){
		return GenericFunction.getTag(PKGName, mDomain, mType, xDesc, TagID); 
	}
	private void goSleep(long sec){
		goSleep(sec,1);
	}
	private void goSleep(long sec, int times){
		for(int index = 1; index <= times ; index++){
			try{
				Thread.sleep( sec ); 
			}catch(Exception e){
				Log.i(TAG, "Got exception: " + e.toString());
			}
		}
	}
	public void loadConfig(){
		Log.i(TAG, "loadConfig");
		StorageSDCardMountUnmountBeTest = GenericFunction.getConfig(PKGName, mDomain, mType);
		String mDesc = "check media files with insert sd card";
		Log.i(TAG, TagCheck+":"+GenericFunction.getTag(PKGName, mDomain, mType, mDesc, TagCheck));
		Log.i(TAG, TagID+":"+GenericFunction.getTag(PKGName, mDomain, mType, mDesc, TagID));
		Log.i(TAG, TagRemark1+":"+GenericFunction.getTag(PKGName, mDomain, mType, mDesc, TagRemark1));
		Log.i(TAG, "xaxbxcxd"+":"+GenericFunction.getTag(PKGName, mDomain, mType, mDesc, "xaxbxcxd"));
		Log.i(TAG, TagRemark2+":"+GenericFunction.getTag(PKGName, mDomain, mType, mDesc, TagRemark2));
		Log.i(TAG, TagRemark3+":"+GenericFunction.getTag(PKGName, mDomain, mType, mDesc, TagRemark3));
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
			//Refer MountService.java -> readStorageList -> R.xml.storage_list -> find storage_list.xml -> @string/storage_sd_card
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
	private long getTotalSize(String mPath){
		StatFs stat = new StatFs( mPath );
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		long mTotalSize = totalBlocks * blockSize;
		return mTotalSize;
	}
	private int getMountStat(String mPath){
		int mMounted = -1; // -1:error, 0:unMount, 1:Mount
		int queryResult = querySdDatabaseFileCount();
		if ( 0 != getTotalSize(mPath) ){ // Has mounted
			if(queryResult>0){
				mMounted = 1;//Has mounted well.
			Log.i(TAG, "Mounted well");
			}
			if(queryResult<=0){
				mMounted = -1;//Has mounted, but not item in database
			Log.i(TAG, "Mounted, no items");
			}
		}else{
			if(queryResult>0){
				mMounted = -1;//Has unMounted, but has item in database
			Log.i(TAG, "UnMounted, but has item inDB");
			}
			if(queryResult==0){
			   	mMounted = 0;//has unMounted well
			Log.i(TAG, "UnMounted well");
			}
		}
		return mMounted;
	}
	private int querySdDatabaseFileCount(){
		int mFilesCount=-1;
		Cursor c;
		String SdPath= "%"+FindSdCardPath()+"%";
		ContentResolver r = getContentResolver();
		//c = r.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,			
		c = r.query(MediaStore.Files.getContentUri("external"),						//new String[]{MediaStore.MediaColumns.DATA},"_data='/mnt/ext_sdcard/AIRtest123abcxyz.png'",null,null);
		new String[]{MediaStore.MediaColumns.DATA},"_data LIKE ?",new String[]{SdPath},null);
		if(c !=null){
			mFilesCount = c.getCount();
			Log.i(TAG, "Got Count:"+ mFilesCount);
			if(c.moveToFirst()){
				do{
					//int idColumn = c.getColumnIndex("_id");
					Log.i(TAG, "Got Result:"+ c.getString(0));
				} while (c.moveToNext());
			}
		}else{
			Log.i(TAG, "Can not get Cursor");
		}
		return mFilesCount;
	}
	private String getShellResult(String mCmd){
		try{
			Process p = Runtime.getRuntime().exec(mCmd);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			int read;
			char[] buffer = new char[4096];
			StringBuffer output = new StringBuffer();
			while((read = reader.read(buffer))>0){
				output.append(buffer, 0, read);
			}
			reader.close();
			p.waitFor();
			return output.toString();
		} catch (IOException e){
			throw new RuntimeException(e);
		} catch (InterruptedException e){
			throw new RuntimeException(e);
		}
	}

	private synchronized IMountService getMountService() {
		if (mMountService == null) {
			IBinder service = ServiceManager.getService("mount");
			if (service != null) {
				mMountService = IMountService.Stub.asInterface(service);
			} else {
				Log.i("===========","Can't get mount service");
			}
		}
		return mMountService;
	}
	private void doUnmount(){
		doUnmount(FindSdCardPath());
	}
	private void doUnmount(String sPath){
		 IMountService mountService = getMountService();
		 try{
			//mountService.unmountVolume(mClickedMountPoint, true, false);
			if("" != sPath)
				mountService.unmountVolume(sPath, true, false);
		 } catch (RemoteException e){
			Log.i("=============", "Error to do unmount");	 
		 }
	}
	private void doMount(){
			doMount(FindSdCardPath());
	}
	private void doMount(String sPath){
		 IMountService mountService = getMountService();
		 try{
			//mountService.unmountVolume(mClickedMountPoint, true, false);
			if("" != sPath)
				mountService.mountVolume(sPath);
		 } catch (RemoteException e){
			Log.i("=============", "Error to do mount");	 
		 }
	}
}
