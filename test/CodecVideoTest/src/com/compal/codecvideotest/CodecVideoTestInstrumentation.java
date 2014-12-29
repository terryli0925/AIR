/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.codecvideotest;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
//import android.os.SystemProperties;

import com.compal.genericfunction.GenericFunction;

public class CodecVideoTestInstrumentation extends ActivityInstrumentationTestCase2<CodecVideoTestActivity> {
	private static final String TAG = "CodecVideoTest";
	private static final String mPKGName = "com.compal.codecvideotest";
	private static final String mDomain = "codec-video";
	private final static int SLEEP_TIME = 30*1000;
	
	private Activity mActivity;
	
	private boolean VideoH2633gpBeTest = false;
	private boolean VideoH264Avc3gpBeTest = false;
	private boolean VideoMpeg4Sp3gpBeTest = false;
	private boolean VideoH263Mp4BeTest = false;
	private boolean VideoH264AvcMp4BeTest = false;
	private boolean VideoMpeg4SpMp4BeTest = false;
	
	public CodecVideoTestInstrumentation() {
		super(CodecVideoTestActivity.class);
	}
	
	@Override
    protected void setUp() throws Exception {
		VideoH2633gpBeTest = GenericFunction.getConfig(mPKGName, mDomain, "h263_3gp");
		VideoH264Avc3gpBeTest = GenericFunction.getConfig(mPKGName, mDomain, "h264_avc_3gp");
		VideoMpeg4Sp3gpBeTest = GenericFunction.getConfig(mPKGName, mDomain, "mpeg-4_sp_3gp");
		VideoH263Mp4BeTest = GenericFunction.getConfig(mPKGName, mDomain, "h263_mp4");
		VideoH264AvcMp4BeTest = GenericFunction.getConfig(mPKGName, mDomain, "h264_avc_mp4");
		VideoMpeg4SpMp4BeTest = GenericFunction.getConfig(mPKGName, mDomain, "mpeg-4_sp_mp4");
		
        super.setUp();
        
    }

    @Override
    protected void tearDown() throws Exception {
        if(mActivity != null) mActivity.finish();
        
    	super.tearDown();
    }

    public void test_1st_h263_3gp() throws Exception{
		Log.i(TAG, "test_1st_h263_3gp");
		
		if(VideoH2633gpBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecVideoTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "video_3gp_h263");
			intent.putExtra("android.intent.extras.TYPE", "h263_3gp");
			intent.putExtra("android.intent.extras.DESCRIPTION", "h263_3gp");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
    }
    
    public void test_2nd_h264_avc_3gp() throws Exception{
		Log.i(TAG, "test_2nd_h264_3gp");
		
		if(VideoH264Avc3gpBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecVideoTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "video_3gp_h264_avc");
			intent.putExtra("android.intent.extras.TYPE", "h264_avc_3gp");
			intent.putExtra("android.intent.extras.DESCRIPTION", "h264_avc_3gp");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
    }

    public void test_3th_mpeg4_sp_3gp() throws Exception{
		Log.i(TAG, "test_3th_mpeg4_sp_3gp");
		
		if(VideoMpeg4Sp3gpBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecVideoTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "video_3gp_mpeg4_sp");
			intent.putExtra("android.intent.extras.TYPE", "mpeg-4_sp_3gp");
			intent.putExtra("android.intent.extras.DESCRIPTION", "mpeg-4_sp_3gp");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();	
		}
	}
     
	public void test_4rd_h263_mp4() throws Exception{
		Log.i(TAG, "test_4rd_h263_mp4");
		
		if(VideoH263Mp4BeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecVideoTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "video_mp4_h263");
			intent.putExtra("android.intent.extras.TYPE", "h263_mp4");
			intent.putExtra("android.intent.extras.DESCRIPTION", "h263_mp4");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
    }
	
	public void test_5th_h264_avc_mp4() throws Exception{
		Log.i(TAG, "test_5th_h264_avc_mp4");
		
		if(VideoH264AvcMp4BeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecVideoTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "video_mp4_h264_avc");
			intent.putExtra("android.intent.extras.TYPE", "h264_avc_mp4");
			intent.putExtra("android.intent.extras.DESCRIPTION", "h264_avc_mp4");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();	
		}
    }
	
	public void test_6th_mpeg4_sp_mp4() throws Exception{
		Log.i(TAG, "test_6th_mpeg4_sp_mp4");
		
		if(VideoMpeg4SpMp4BeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecVideoTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "video_mp4_mpeg4_sp");
			intent.putExtra("android.intent.extras.TYPE", "mpeg-4_sp_mp4");
			intent.putExtra("android.intent.extras.DESCRIPTION", "mpeg-4_sp_mp4");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();	
		}
		
		//SystemProperties.set("persist.sys.test","CodecVideoTestIsdone");
	}
}
