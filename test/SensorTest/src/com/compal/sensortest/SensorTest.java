/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.sensortest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import com.compal.genericfunction.GenericFunction;

public class SensorTest extends Activity {
    /** Called when the activity is first created. */
	
	private float accel_x, accel_y, accel_z, gyro_x, gyro_y, gyro_z, compass_x, compass_y, compass_z,
	light, thermal, mProximity, mPressure;
    private SensorManager sensorManager;
    private Sensor g_sensor, gyro_sensor, compass_sensor, thermal_sensor, light_sensor, proximity_sensor, pressure_sensor;
    private accelEventListener maccelEventListener;
    private gyroEventListener mgyroEventListener;
    private compassEventListener mcompassEventListener; 
    private lightEventListener mlightEventListener;
    private thermalEventListener mthermalEventListener;
    private proximityEventListener mproximityEventListener;
    private pressureEventListener mpressureEventListener;
    private final static String TAG = "SensorTest";   //Using_Comment: reference from frameworks/base/
    private final static String MSG = "TestTool";
    private final static String PKGName = "com.compal.sensortest";
	private final static String TagID = "ID";
    private final static String mDomain = "sensors";
    private final static String mGDes = "accelerometer sensor";
    private final static String mMDes = "compass sensor";
    private final static String mGyDes = "gyro sensor";
    private final static String mLDes = "light sensor";
    private final static String mPDes = "proximity sensor";
    private final static String mTDes = "thermal sensor";
    private final static String mADes = "pressure sensor";

    private final static int CHECK_TIMEOUT = 1500;
    private final static int CHECK_TIMEOUT_ALL = 15000;
    boolean showAccelValue = true;
	private String mAccelLog="";
	private String mAccelRemark1="";
	private String mAccelRemark2="";

    boolean showGyroValue = true;
	private String mGyroLog="";
	private String mGyroRemark1="";
	private String mGyroRemark2="";

    boolean showCompassValue = true;
	private String mCompassLog="";
	private String mCompassRemark1="";
	private String mCompassRemark2="";

    boolean showLightValue = true;
	private String mLightLog="";
	private String mLightRemark1="";
	private String mLightRemark2="";

    boolean showThermalValue = true;
	private String mThermalLog="";
	private String mThermalRemark1="";
	private String mThermalRemark2="";

    boolean showProximityValue = true;
	private String mProximityLog="";
	private String mProximityRemark1="";
	private String mProximityRemark2="";

    boolean showPressureValue = true;
	private String mPressureLog="";
	private String mPressureRemark1="";
	private String mPressureRemark2="";

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		SystemProperties.set("persist.sys.test", "SensorIsTesing");
    }
    
    public void onResume(){
    	super.onResume();       
		SystemProperties.set("persist.sys.test", "SensorIsTesing");
    	try {
			registerSensor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void onPause(){
    	super.onPause();    
    	unregisterSensor();
    }

	private String getID(String xDesc){
		return GenericFunction.getTag(PKGName, mDomain, xDesc, xDesc, TagID);
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			checkStatus(msg.what);
		}
	};

	private void checkStatus(int what){
			if(what==Sensor.TYPE_ACCELEROMETER){
				if(showAccelValue){
					showAccelValue=false;
					mAccelLog+="G-sensor timeout => Fail\n";
					GenericFunction.putResult(PKGName, mDomain, mGDes, mGDes, false, getID(mGDes), mAccelLog, mAccelRemark1, mAccelRemark2);
				}
			}
			if(what==Sensor.TYPE_GYROSCOPE){
				if(showGyroValue){
					showGyroValue=false;
					mGyroLog+="Gyro-sensor timeout => Fail\n";
					GenericFunction.putResult(PKGName, mDomain, mGyDes, mGyDes, false,getID(mGyDes), mGyroLog, mGyroRemark1, mGyroRemark2);
				}
			}
			if(what==Sensor.TYPE_MAGNETIC_FIELD){
				if(showCompassValue){
					showCompassValue=false;
					mCompassLog+="Compass-sensor timeout => Fail\n";
					GenericFunction.putResult(PKGName, mDomain, mMDes, mMDes, false, getID(mMDes), mCompassLog, mCompassRemark1, mCompassRemark2);
				}
			}
			if(what==Sensor.TYPE_LIGHT){
				if(showLightValue){
					showLightValue=false;
					mLightLog+="Light-sensor timeout => Fail\n";
					GenericFunction.putResult(PKGName, mDomain, mLDes, mLDes, false, getID(mLDes), mLightLog, mLightRemark1, mLightRemark2);
				}
			}
			if(what==Sensor.TYPE_TEMPERATURE){
				if(showThermalValue){
					showThermalValue=false;
					mThermalLog+="Thermal-sensor timeout => Fail\n";
					GenericFunction.putResult(PKGName, mDomain, mTDes, mTDes, false, getID(mTDes), mThermalLog, mThermalRemark1, mThermalRemark2);
				}
			}
			if(what==Sensor.TYPE_PROXIMITY){
				if(showProximityValue){
					showProximityValue=false;
					mProximityLog+="Proximity-sensor timeout => Fail\n";
					GenericFunction.putResult(PKGName, mDomain, mPDes, mPDes, false, getID(mPDes), mProximityLog, mProximityRemark1, mProximityRemark2);
				}
			}
			if(what==Sensor.TYPE_PRESSURE){
				if(showPressureValue){
					showPressureValue=false;
					mPressureLog+="Pressure-sensor timeout => Fail\n";
					GenericFunction.putResult(PKGName, mDomain, mADes, mADes, false, getID(mADes), mPressureLog, mPressureRemark1, mPressureRemark2);
				}
			}
			if(what==Sensor.TYPE_ALL){
					/*
				if(showAccelValue || showGyroValue || showCompassValue ||
				   showLightValue || showThermalValue || showProximityValue ||
				   showPressureValue)*/
				Log.i(TAG,"Timeout --> Close Activity");
				SystemProperties.set("persist.sys.test", "SensorIsdone");
				finish();				
			}
	
	}
    private void getAccelValue() throws IOException{
		mAccelLog+="Accel X = "+accel_x+"\n";
		mAccelLog+="Accel Y = "+accel_y+"\n";
		mAccelLog+="Accel Z = "+accel_z+"\n";
    	if(showAccelValue == true){
    		Float f1 = new Float(accel_x);
    		Float f2 = new Float(accel_y);
    		Float f3 = new Float(accel_z);
    		if(f1.isNaN()==true || f2.isNaN()==true || f3.isNaN()==true){
				mAccelLog+="G-sensor isNaN => Fail\n";
				GenericFunction.putResult(PKGName, mDomain, mGDes, mGDes, false, getID(mGDes), mAccelLog, mAccelRemark1, mAccelRemark2);
    		}
    		else{
				mAccelLog+="G-sensor test => Pass\n";
				GenericFunction.putResult(PKGName, mDomain, mGDes, mGDes, true, getID(mGDes), mAccelLog, mAccelRemark1, mAccelRemark2);
    		}
    		showAccelValue = false;
    	}
    }
    
    private void getGyroValue() throws IOException{
		mGyroLog+="Gyro X = "+gyro_x+"\n";
		mGyroLog+="Gyro y = "+gyro_y+"\n";
		mGyroLog+="Gyro z = "+gyro_z+"\n";
    	if(showGyroValue == true){
    		Float f1 = new Float(gyro_x);
    		Float f2 = new Float(gyro_y);
    		Float f3 = new Float(gyro_z);
    		if(f1.isNaN()==true || f2.isNaN()==true || f3.isNaN()==true){
				mGyroLog+="Gyro isNaN => Fail\n";
				GenericFunction.putResult(PKGName, mDomain, mGyDes, mGyDes, false, getID(mGyDes), mGyroLog, mGyroRemark1, mGyroRemark2);
    		}
    		else{
				mGyroLog+="Gyro test => Pass\n";
				GenericFunction.putResult(PKGName, mDomain, mGyDes, mGyDes, true, getID(mGyDes), mGyroLog, mGyroRemark1, mGyroRemark2);
    		}
    		showGyroValue = false;
    	}
    }
    
    private void getCompassValue() throws IOException{
		mCompassLog+="Compass X = "+compass_x+"\n";
		mCompassLog+="Compass y = "+compass_y+"\n";
		mCompassLog+="Compass z = "+compass_z+"\n";
    	if(showCompassValue == true){
    		Float f1 = new Float(compass_x);
    		Float f2 = new Float(compass_y);
    		Float f3 = new Float(compass_z);
    		if(f1.isNaN()==true || f2.isNaN()==true || f3.isNaN()==true){
				mCompassLog+="Compass isNaN => Fail\n";
				GenericFunction.putResult(PKGName, mDomain, mMDes, mMDes, false, getID(mMDes), mCompassLog, mCompassRemark1, mCompassRemark2);
    		}
    		else{
				mCompassLog+="Compass test => Pass\n";
				GenericFunction.putResult(PKGName, mDomain, mMDes, mMDes, true, getID(mMDes), mCompassLog, mCompassRemark1, mCompassRemark2);
    		}
    		showCompassValue = false;
    	}
    }
    
    private void getLightValue() throws IOException{
		mLightLog+="Light = "+light+"\n";
    	if(showLightValue == true){
    		Float f1 = new Float(light);
    		if(f1.isNaN()==true){
				mLightLog+="Light isNaN => Fail\n";
				GenericFunction.putResult(PKGName, mDomain, mLDes, mLDes, false, getID(mLDes), mLightLog, mLightRemark1, mLightRemark2);
    		}
    		else{
				mLightLog+="Light-sensor test => Pass\n";
				GenericFunction.putResult(PKGName, mDomain, mLDes, mLDes, true, getID(mLDes), mLightLog, mLightRemark1, mLightRemark2);
    		}
    		showLightValue = false;
    	}
    }
    
    private void getThermalValue() throws IOException{
		mThermalLog+="Thermal = "+thermal+"\n";
    	if(showThermalValue == true){
    		Float f1 = new Float(thermal);
    		if(f1.isNaN()==true){
				mThermalLog+="Thermal isNaN => Fail\n";
				GenericFunction.putResult(PKGName, mDomain, mTDes, mTDes, false, getID(mTDes), mThermalLog, mThermalRemark1, mThermalRemark2);
    		}
    		else{
				mThermalLog+="Thermal-sensor test => Pass\n";
				GenericFunction.putResult(PKGName, mDomain, mTDes, mTDes, true, getID(mTDes), mThermalLog, mThermalRemark1, mThermalRemark2);
    		}
    		showThermalValue = false;
    	}
    }

    private void getProximityValue() throws IOException{ 
		mProximityLog+="Proximity = "+mProximity+"\n";
    	if(showProximityValue == true){
    		Float f1 = new Float(mProximity);
    		if(f1.isNaN()==true){
				mProximityLog+="Proximity isNaN => Fail\n";
				GenericFunction.putResult(PKGName, mDomain, mPDes, mPDes, false, getID(mPDes), mProximityLog, mProximityRemark1, mProximityRemark2);
    		}
    		else{
				mProximityLog+="Proximity-sensor test => Pass\n";
				GenericFunction.putResult(PKGName, mDomain, mPDes, mPDes, true, getID(mPDes), mProximityLog, mProximityRemark1, mProximityRemark2);
    		}
    		showProximityValue = false;
    	}
    }

    private void getPressureValue() throws IOException{ 
		mPressureLog+="Pressure = "+mPressure+"\n";
    	if(showPressureValue == true){
    		Float f1 = new Float(mPressure);
    		if(f1.isNaN()==true){
				mPressureLog+="Pressure isNaN => Faile\n";
				GenericFunction.putResult(PKGName, mDomain, mADes, mADes, false, getID(mADes), mPressureLog, mPressureRemark1, mPressureRemark2);
    		}
    		else{
				mPressureLog+="Pressure-sensor test => Pass\n";
				GenericFunction.putResult(PKGName, mDomain, mADes, mADes, true, getID(mADes), mPressureLog, mPressureRemark1, mPressureRemark2);
    		}
    		showPressureValue = false;
    	}
    }

    private void registerSensor() throws IOException{
        maccelEventListener = new accelEventListener();
        mgyroEventListener = new gyroEventListener();
        mcompassEventListener = new compassEventListener();
        mlightEventListener = new lightEventListener();
        mthermalEventListener = new thermalEventListener();
        mproximityEventListener = new proximityEventListener();
        mpressureEventListener = new pressureEventListener();

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        
		if(sensorManager == null){
    		Log.i(TAG,"sensorManager is null");
    	}
    	else{
    		Log.i(TAG,"sensorManger is not null");
    	}

		mHandler.sendEmptyMessageDelayed(Sensor.TYPE_ALL,CHECK_TIMEOUT_ALL);
		boolean TestAccelSensor=false;
        TestAccelSensor=GenericFunction.getConfig(PKGName, mDomain, mGDes);
    	if(TestAccelSensor){
				g_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				if(g_sensor == null){
						mAccelLog+= "g_sensor is null\n";
						mAccelLog+= "G-sensor test => Fail\n";
						GenericFunction.putResult(PKGName, mDomain, mGDes, mGDes, false, getID(mGDes), mAccelLog, mAccelRemark1, mAccelRemark2);
				}
				else{
						mAccelLog+= "g_sensor is not null\n";
						mHandler.sendEmptyMessageDelayed(Sensor.TYPE_ACCELEROMETER,CHECK_TIMEOUT);
						sensorManager.registerListener(maccelEventListener, g_sensor, SensorManager.SENSOR_DELAY_NORMAL);
				}
		}

		boolean TestGyroSensor=false;
        TestGyroSensor=GenericFunction.getConfig(PKGName, mDomain, mGyDes);
		if(TestGyroSensor){
				gyro_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
				if(gyro_sensor == null){
						mGyroLog+= "gyro_sensor is null\n";
						mGyroLog+= "Gyro test => Fail\n";
						GenericFunction.putResult(PKGName, mDomain, mGyDes, mGyDes, false, getID(mGyDes), mGyroLog, mGyroRemark1, mGyroRemark2);
				}
				else{
						mGyroLog+= "gyro_sensor is not null\n";
						mHandler.sendEmptyMessageDelayed(Sensor.TYPE_GYROSCOPE,CHECK_TIMEOUT);
						sensorManager.registerListener(mgyroEventListener, gyro_sensor, SensorManager.SENSOR_DELAY_NORMAL);
				}
		}


		boolean TestCompassSensor=false;
        TestCompassSensor=GenericFunction.getConfig(PKGName, mDomain, mMDes);
		if(TestCompassSensor){
				compass_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
				if(compass_sensor == null){
						mCompassLog+= "compass_sensor is null\n";
						mCompassLog+= "Compass test => Fail\n";
						GenericFunction.putResult(PKGName, mDomain, mMDes, mMDes, false, getID(mMDes), mCompassLog, mCompassRemark1, mCompassRemark2);
				}
				else{
						mCompassLog+= "compass_sensor is not null\n";
						mHandler.sendEmptyMessageDelayed(Sensor.TYPE_MAGNETIC_FIELD,CHECK_TIMEOUT);
						sensorManager.registerListener(mcompassEventListener, compass_sensor, SensorManager.SENSOR_DELAY_NORMAL);
				}
		}

		boolean TestLightSensor=false;
        TestLightSensor=GenericFunction.getConfig(PKGName, mDomain, mLDes);
		if(TestLightSensor){
				light_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
				if(light_sensor == null){
						mLightLog+= "light_sensor is null\n";
						mLightLog+= "Light-sensor test => Fail\n";
						GenericFunction.putResult(PKGName, mDomain, mLDes, mLDes, false, getID(mLDes), mLightLog, mLightRemark1, mLightRemark2);
				}
				else{
						mLightLog+= "light_sensor is not null\n";
						mHandler.sendEmptyMessageDelayed(Sensor.TYPE_LIGHT,CHECK_TIMEOUT);
						sensorManager.registerListener(mlightEventListener, light_sensor, SensorManager.SENSOR_DELAY_NORMAL);
				}
		}

		boolean TestThermalSensor=false;
        TestThermalSensor=GenericFunction.getConfig(PKGName, mDomain, mTDes);
		if(TestThermalSensor){
				thermal_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
				if(thermal_sensor == null){
						mThermalLog+= "thermal_sensor is null\n";
						mThermalLog+= "Thermal-sensor test => Fail\n";
						GenericFunction.putResult(PKGName, mDomain, mTDes, mTDes, false, getID(mTDes), mThermalLog, mThermalRemark1, mThermalRemark2);
				}
				else{
						mThermalLog+= "thermal_sensor is not null\n";
						mHandler.sendEmptyMessageDelayed(Sensor.TYPE_TEMPERATURE,CHECK_TIMEOUT);
						sensorManager.registerListener(mthermalEventListener, thermal_sensor, SensorManager.SENSOR_DELAY_NORMAL);
				}
		}

		boolean TestProximitySensor=false;
        TestProximitySensor=GenericFunction.getConfig(PKGName, mDomain, mPDes);
		if(TestProximitySensor){
				proximity_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
				if(proximity_sensor == null){ 
						mProximityLog+= "proximity_sensor is null\n";
						mProximityLog+= "Proximity-sensor test => Fail\n";
						GenericFunction.putResult(PKGName, mDomain, mPDes, mPDes, false, getID(mPDes), mProximityLog, mProximityRemark1, mProximityRemark2);
				}
				else{
						mProximityLog+= "proximity_sensor is not null\n";
						mHandler.sendEmptyMessageDelayed(Sensor.TYPE_PROXIMITY,CHECK_TIMEOUT);
						sensorManager.registerListener(mproximityEventListener, proximity_sensor, SensorManager.SENSOR_DELAY_NORMAL);
				}
		}

		boolean TestPressureSensor=false;
        TestPressureSensor=GenericFunction.getConfig(PKGName, mDomain, mADes);
		if(TestPressureSensor){
				pressure_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
				if(pressure_sensor == null){
						mPressureLog+= "pressure_sensor is null\n";
						mPressureLog+= "Pressure-sensor test => Fail\n";
						GenericFunction.putResult(PKGName, mDomain, mADes, mADes, false, getID(mADes), mPressureLog, mPressureRemark1, mPressureRemark2);
				}
				else{
						mPressureLog+= "pressure_sensor is not null\n";
						mHandler.sendEmptyMessageDelayed(Sensor.TYPE_PRESSURE,CHECK_TIMEOUT);
						sensorManager.registerListener(mpressureEventListener, pressure_sensor, SensorManager.SENSOR_DELAY_NORMAL);
				}
		}
    }

    private void unregisterSensor(){
    	if(sensorManager!=null){
    		sensorManager.unregisterListener(maccelEventListener);
    		sensorManager.unregisterListener(mgyroEventListener);
    		sensorManager.unregisterListener(mcompassEventListener);
    		sensorManager.unregisterListener(mlightEventListener);
    		sensorManager.unregisterListener(mthermalEventListener);
    		sensorManager.unregisterListener(mproximityEventListener); 
    		sensorManager.unregisterListener(mpressureEventListener);
    	}
    }		
    
    class accelEventListener implements SensorEventListener{
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
        }

        public void onSensorChanged(SensorEvent event) {
              accel_x = event.values[0];
              accel_y = event.values[1];
              accel_z = event.values[2];
              try {
				getAccelValue();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}      
        }
    }
    
    class gyroEventListener implements SensorEventListener{
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
        }

        public void onSensorChanged(SensorEvent event) {
              gyro_x = event.values[0];
              gyro_y = event.values[1];
              gyro_z = event.values[2];
              try {
				getGyroValue();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}      
        }
    }
    
    class compassEventListener implements SensorEventListener{
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
        }

        public void onSensorChanged(SensorEvent event) {
              compass_x = event.values[0];
              compass_y = event.values[1];
              compass_z = event.values[2];
              try {
				getCompassValue();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        }
    }
    
    class lightEventListener implements SensorEventListener{
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
        }

        public void onSensorChanged(SensorEvent event) {
              light = event.values[0];
              try {
				getLightValue();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        }
    }
    
    class thermalEventListener implements SensorEventListener{
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
        }

        public void onSensorChanged(SensorEvent event) {
              thermal = event.values[0];
              try {
				getThermalValue();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        }
    }

    class proximityEventListener implements SensorEventListener{
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
        }

        public void onSensorChanged(SensorEvent event) {
              mProximity = event.values[0];
              try {
				getProximityValue();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        }
    }

    class pressureEventListener implements SensorEventListener{
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
        }

        public void onSensorChanged(SensorEvent event) {
              mPressure = event.values[0];
              try {
				getPressureValue();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        }
    }
}
