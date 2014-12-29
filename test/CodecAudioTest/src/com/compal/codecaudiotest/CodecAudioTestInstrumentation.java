/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.codecaudiotest;

import com.compal.genericfunction.GenericFunction;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
//import android.os.SystemProperties;

public class CodecAudioTestInstrumentation extends ActivityInstrumentationTestCase2<CodecAudioTestActivity>{
	private static final String TAG = "CodecAudioTest";
	private static final String mPKGName = "com.compal.codecaudiotest";
	private static final String mDomain = "codec-audio";
	private final static int SLEEP_TIME = 30*1000;
	
	private Activity mActivity;
	
	private boolean AudioAacLcLtpBeTest = false;
	private boolean AudioAacplusBeTest = false;
	private boolean AudioAmrNbBeTest = false;
	private boolean AudioAmrWbBeTest = false;
	private boolean AudioMp3BeTest = false;
	private boolean AudioMidiBeTest = false;
	private boolean AudioOggVorbisBeTest = false;
	private boolean AudioPcmWaveBeTest = false;
	
	public CodecAudioTestInstrumentation() {
		super(CodecAudioTestActivity.class);
	}
	
	@Override
    protected void setUp() throws Exception {
		AudioAacLcLtpBeTest = GenericFunction.getConfig(mPKGName, mDomain, "aac_lc_ltp");
		AudioAacplusBeTest = GenericFunction.getConfig(mPKGName, mDomain, "aac+");
		AudioAmrNbBeTest = GenericFunction.getConfig(mPKGName, mDomain, "amr-nb");
		AudioAmrWbBeTest = GenericFunction.getConfig(mPKGName, mDomain, "amr-wb");
		AudioMp3BeTest = GenericFunction.getConfig(mPKGName, mDomain, "mp3");
		AudioMidiBeTest = GenericFunction.getConfig(mPKGName, mDomain, "midi");
		AudioOggVorbisBeTest = GenericFunction.getConfig(mPKGName, mDomain, "ogg");
		AudioPcmWaveBeTest = GenericFunction.getConfig(mPKGName, mDomain, "pcm_wave");
		
        super.setUp();
        
    }

    @Override
    protected void tearDown() throws Exception {
        if(mActivity != null) mActivity.finish();
        
    	super.tearDown();
    }
    
    public void test_a_aac_lc_ltp_3gp() throws Exception{
		Log.i(TAG, "test_a_aac_lc_ltp_3gp");
		
		if(AudioAacLcLtpBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_3gp_aac_lc_ltp");
			intent.putExtra("android.intent.extras.TYPE", "aac_lc_ltp");
			intent.putExtra("android.intent.extras.DESCRIPTION", "aac_lc_ltp_3gp");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish(); 
		}   
    }
    
    public void test_b_aac_lc_ltp_mp4() throws Exception{
		Log.i(TAG, "test_b_aac_lc_ltp_mp4");
		
		if(AudioAacLcLtpBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_mp4_aac_lc_ltp");
			intent.putExtra("android.intent.extras.TYPE", "aac_lc_ltp");
			intent.putExtra("android.intent.extras.DESCRIPTION", "aac_lc_ltp_mp4");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish(); 
		}   
    }
    
    public void test_c_aac_lc_ltp_m4a() throws Exception{
		Log.i(TAG, "test_c_aac_lc_ltp_m4a");
		
		if(AudioAacLcLtpBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_m4a_aac_lc_ltp");
			intent.putExtra("android.intent.extras.TYPE", "aac_lc_ltp");
			intent.putExtra("android.intent.extras.DESCRIPTION", "aac_lc_ltp_m4a");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish(); 
		}	   
    }
    
    public void test_d_aacplus_3gp() throws Exception{
		Log.i(TAG, "test_d_aacplus_3gp");
		
		if(AudioAacplusBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_3gp_aacplus");
			intent.putExtra("android.intent.extras.TYPE", "aac+");
			intent.putExtra("android.intent.extras.DESCRIPTION", "aac+_3gp");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
    }
    
    public void test_e_aacplus_mp4() throws Exception{
		Log.i(TAG, "test_e_aacplus_mp4");
		
		if(AudioAacplusBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_mp4_aacplus");
			intent.putExtra("android.intent.extras.TYPE", "aac+");
			intent.putExtra("android.intent.extras.DESCRIPTION", "aac+_mp4");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
    }
    
    public void test_f_aacplus_m4a() throws Exception{
		Log.i(TAG, "test_f_aacplus_m4a");
		
		if(AudioAacplusBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_m4a_aacplus");
			intent.putExtra("android.intent.extras.TYPE", "aac+");
			intent.putExtra("android.intent.extras.DESCRIPTION", "aac+_m4a");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
    }
	
	public void test_g_amr_nb() throws Exception{
		Log.i(TAG, "test_g_amr_nb");
		
		if(AudioAmrNbBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_3gp_amr_nb");
			intent.putExtra("android.intent.extras.TYPE", "amr-nb");
			intent.putExtra("android.intent.extras.DESCRIPTION", "amr-nb_3gp");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();	
		}		
    }
	
	public void test_h_amr_wb() throws Exception{
		Log.i(TAG, "test_h_amr_wb");
		
		if(AudioAmrWbBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_3gp_amr_wb");
			intent.putExtra("android.intent.extras.TYPE", "amr-wb");
			intent.putExtra("android.intent.extras.DESCRIPTION", "amr-wb_3gp");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
	}

	public void test_i_mp3() throws Exception{
		Log.i(TAG, "test_i_mp3");
		
		if(AudioMp3BeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_mp3");
			intent.putExtra("android.intent.extras.TYPE", "mp3");
			intent.putExtra("android.intent.extras.DESCRIPTION", "mp3");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
	}
	
	public void test_j_midi_mid() throws Exception{
		Log.i(TAG, "test_j_midi_mid");
		
		if(AudioMidiBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_mid_midi");
			intent.putExtra("android.intent.extras.TYPE", "midi");
			intent.putExtra("android.intent.extras.DESCRIPTION", "midi_mid");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
	}
	
	public void test_k_midi_rtx() throws Exception{
		Log.i(TAG, "test_k_midi_rtx");
		
		if(AudioMidiBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_rtx_midi");
			intent.putExtra("android.intent.extras.TYPE", "midi");
			intent.putExtra("android.intent.extras.DESCRIPTION", "midi_rtx");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
	}
	
	public void test_l_midi_imy() throws Exception{
		Log.i(TAG, "test_l_midi_imy");
		
		if(AudioMidiBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_imy_midi");
			intent.putExtra("android.intent.extras.TYPE", "midi");
			intent.putExtra("android.intent.extras.DESCRIPTION", "midi_imy");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
	}
	
	public void test_m_ogg_vorbis() throws Exception{
		Log.i(TAG, "test_m_ogg_vorbis");
		
		if(AudioOggVorbisBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_ogg_vorbis");
			intent.putExtra("android.intent.extras.TYPE", "ogg");
			intent.putExtra("android.intent.extras.DESCRIPTION", "ogg");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
	}
	
	public void test_n_pcm_wave() throws Exception{
		Log.i(TAG, "test_n_pcm_wave");
		
		if(AudioPcmWaveBeTest){
			Intent intent = new Intent();
			intent.setClass(getInstrumentation().getTargetContext(), CodecAudioTestActivity.class );
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("android.intent.extras.FILE", "audio_wave_pcm");
			intent.putExtra("android.intent.extras.TYPE", "pcm_wave");
			intent.putExtra("android.intent.extras.DESCRIPTION", "pcm_wave");
			
			mActivity = getInstrumentation().startActivitySync(intent);
			Thread.sleep(SLEEP_TIME);
			mActivity.finish();
		}
		
		//SystemProperties.set("persist.sys.test","CodecAudioTestIsdone");
	}
}
