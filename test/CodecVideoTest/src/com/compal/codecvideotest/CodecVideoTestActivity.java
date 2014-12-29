/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.codecvideotest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.widget.MediaController;
import android.widget.VideoView;

import com.compal.genericfunction.GenericFunction;

public class CodecVideoTestActivity extends Activity {
	private static final String TAG = "CodecVideoTest";
	private static final String mPKGName = "com.compal.codecvideotest";
	private static final String mDomain = "codec-video";
	private final static int VIDEO_DURATION = 20*1000;
	private final static int CHECK_TIMEOUT = 25*1000;
	private final static int TIMEOUT = 1;
	
	private VideoView mVideoView;
	private AudioManager mAudioManage;
	
	boolean resultIsWriten = false;	
	String mFile = "";
	String mType = "";
	String mDescription = "";
	String mId = "";
	String mLog = "";
	String mRemark1 = "";
	String mRemark2 = "";
	String mRemark3 = "";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
     
        mVideoView = (VideoView) findViewById(R.id.videoview);
        
        Intent intent = getIntent(); 
        mFile = intent.getStringExtra("android.intent.extras.FILE");
        mType = intent.getStringExtra("android.intent.extras.TYPE");
        mDescription = intent.getStringExtra("android.intent.extras.DESCRIPTION");
        if(intent == null || mFile == null || mType == null || mDescription == null){
        	Log.i(TAG, "Get extra fail from intent");
	    	mLog += "Get extra fail from intent\n";
	    	
	    	putResult(false);
	    	
	    	//finish();
        }
        
        mId = GenericFunction.getTag(mPKGName, mDomain, mType, mDescription, "ID"); 
         
        if(!isFinishing()){
        	setVolume();	
        	playVideo();
        }   
    }
    
    private void setVolume(){
    	mAudioManage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    	if(mAudioManage == null){
			Log.i(TAG,"Can't get AUDIO_SERVICE");
			mLog += "Can't get AUDIO_SERVICE\n"; 		
		}else{
			mAudioManage.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManage.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2, 0);  
		}
    }
    
    private void playVideo(){
    	int resID = getResources().getIdentifier(mFile, "raw", mPKGName);
    	Log.i(TAG, "File: "+mFile+"  ResID: "+resID);
    	
    	//mVideoView.setVideoPath("android.resource://com.compal.codecvideotest/"+resID);
    	mVideoView.setVideoURI(Uri.parse("android.resource://"+mPKGName+"/"+resID));
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.setKeepScreenOn(true);
        mHandler.sendEmptyMessageDelayed(TIMEOUT, CHECK_TIMEOUT);
        
        mVideoView.start();
        
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.i(TAG, "onPrepared");
				
				int videoDuration = mVideoView.getDuration();
				Log.i(TAG, "Video duration: "+videoDuration);
				
				if(videoDuration > VIDEO_DURATION){
					mVideoView.seekTo(videoDuration - VIDEO_DURATION);
				}
				
				Log.i(TAG, "Current position: "+mVideoView.getCurrentPosition());
			}
		});
    
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
            	Log.i(TAG, "Media player had error " + what + " playing video");
            	mLog += "Media player had error " + what + " playing video\n";
            	
            	if(resultIsWriten == false){
            		putResult(false);            	
            		resultIsWriten = true;
            	}
            	
            	//finish();
            	
                return true;
            }
        });
        
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.i(TAG, "onCompletion");
            	
            	if(resultIsWriten == false){
            		putResult(true);            	
            		resultIsWriten = true;
            	}
				
            	//finish();
			}
		});
    }
    
    private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(!isFinishing()){
				Log.i(TAG, "Handler: Activity Timeout");
		    	mLog += "Handler: Activity Timeout\n";
		    	
		    	if(resultIsWriten == false){
		    		putResult(false);            	
            		resultIsWriten = true;
            	}

				finish();
			}
		}
    };
    
    private void putResult(boolean result){
		if(result){
			Log.i(TAG, mDescription+" success");
	    	mLog += mDescription+" success\n";	
		}else{
			Log.i(TAG, mDescription+" doesn't success");
	    	mLog += mDescription+" doesn't success\n";
		}
		
		GenericFunction.putResult(mPKGName, mDomain, mType, mDescription, result, mId, mLog, mRemark1, mRemark2, mRemark3);
	}
    
    
}