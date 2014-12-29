/*
 * (C) Copyright 2011-2012 Compal Electronics, Inc. 
 *
 * This software is the property of Compal Electronics, Inc.
 * You have to accept the terms in the license file before use.
 *
 */
package com.compal.genericfunction;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;


public class GenericFunction{
    /** Called when the activity is first created. */
	public static final String TESTITEM_CONFIG_FILENAME="TestItemConfig.xml";	
	public static final String TESTITEM_RESULT_FILENAME="TestItemResult.xml";	
	public static final String TESTITEM_CONFIG_PATH="/data/data/";	
	public static final String TESTITEM_RESULT_PATH="/data/data/";	
	public static boolean getConfig(String mPKGName, String mDomain, String mType){
		Log.i(mPKGName, "getConfig is start");
		String line="";
		try{
				InputStream fs;
				fs = new FileInputStream(TESTITEM_CONFIG_PATH + mPKGName + "/" +TESTITEM_CONFIG_FILENAME);
				BufferedReader rd = new BufferedReader(new InputStreamReader(fs));
				while((line=rd.readLine())!=null){
						Log.i(mPKGName, "Line:"+line);
						if((line.indexOf(mDomain)!=-1)&&(line.indexOf(mType)!=-1)){
							line=rd.readLine();
							if(line.indexOf("<Check>True</Check>")!=-1){
								//Log.i(mPKGName, mDomain+"-"+mType+"-"+"need to test!!!");
								return true;		
							}else{
								break; //not need test, escape to prevent parse following line
							}
						}		
				}
				rd.close();
				fs.close();
		} catch (IOException e){
			Log.i(mPKGName, "Error:"+e);
			e.printStackTrace();
		}
		return false;
	
	}
	public static String getTag(String mPKGName, String mDomain, String mType, String mDescr, String mTag){
		Log.i(mPKGName, "getTag is start");
		String line="";
		String result="";
		try{
				InputStream fs;
				fs = new FileInputStream(TESTITEM_CONFIG_PATH + mPKGName + "/" +TESTITEM_CONFIG_FILENAME);
				BufferedReader rd = new BufferedReader(new InputStreamReader(fs));
				while((line=rd.readLine())!=null){
						if((line.indexOf(mDomain)!=-1)&&(line.indexOf(mType)!=-1)&&(line.indexOf(mDescr)!=-1)){
							Log.i(mPKGName, "Line:"+line);
							while( !( (line=rd.readLine()).equals("</TestItem>")) ){
								Log.i(mPKGName, "Line:"+line);
								if(line.indexOf(mTag)!=-1){
									String input = line;
									int iStart = input.indexOf("<"+mTag+">");
									int iEnd = input.indexOf("</"+mTag+">");
									result = input.substring(iStart+("<"+mTag+">").length(),iEnd);
									Log.i(mPKGName, "result:"+result);
									return result;
								}
							}
						}
				}
				rd.close();
				fs.close();
		} catch (IOException e){
			Log.i(mPKGName, "Error:"+e);
			e.printStackTrace();
			result="";
		} finally {
			return result;
		}
	}
	public static void putResult(String mPKGName, String mDomain, String mType, String mDescription, boolean mResultIsPass, String mLog, String mRemark1, String mRemark2){
		putResult(mPKGName, mDomain, mType, mDescription, mResultIsPass, "-1", mLog, mRemark1, mRemark2, "");
	}
	public static void putResult(String mPKGName, String mDomain, String mType, String mDescription, boolean mResultIsPass, String Id, String mLog, String mRemark1, String mRemark2){
		putResult(mPKGName, mDomain, mType, mDescription, mResultIsPass, Id, mLog, mRemark1, mRemark2, "");
	}
	public static void putResult(String mPKGName, String mDomain, String mType, String mDescription, boolean mResultIsPass, String Id,String mLog, String mRemark1, String mRemark2, String mRemark3){
		String TestItemStartTag="<TestItem domain=\""+mDomain+"\" type=\""+mType+"\" description=\""+mDescription+"\">\n";
		String TestItemEndTag="</TestItem>\n\n";
		String ResultWithTag="";
		if(mResultIsPass){
			ResultWithTag="<Pass>True</Pass>\n";
		}else{
			ResultWithTag="<Pass>False</Pass>\n";
		}
		String LogFileName=mDescription.replaceAll(" ","_")+".log";
		//String LogWithTag="<Log>"+mLog+"</Log>\n";
		String IdWithTag="<ID>"+Id+"</ID>\n";
		String LogWithTag="<Log>"+LogFileName+"</Log>\n";
		String Remark1WithTag="<Remark1>"+mRemark1+"</Remark1>\n";
		String Remark2WithTag="<Remark2>"+mRemark2+"</Remark2>\n";
		String Remark3WithTag="<Remark3>"+mRemark3+"</Remark3>\n";
		File mResultDir = new File(TESTITEM_RESULT_PATH + mPKGName+ "/" + mDomain+ "/");
		if(!mResultDir.exists()){
			Log.i(mPKGName, "============ dir not exist");
			mResultDir.mkdirs();
		}
		try{
			BufferedWriter bwLogFile = new BufferedWriter(new FileWriter(TESTITEM_RESULT_PATH + mPKGName + "/" + mDomain + "/" +LogFileName, true));
			bwLogFile.write(mLog);
			bwLogFile.close();
			BufferedWriter bw = new BufferedWriter(new FileWriter(TESTITEM_RESULT_PATH + mPKGName + "/" + mDomain + "/" +TESTITEM_RESULT_FILENAME, true));
			bw.write(TestItemStartTag);
			bw.write(ResultWithTag);
			bw.write(IdWithTag);
			bw.write(LogWithTag);
			bw.write(Remark1WithTag);
			bw.write(Remark2WithTag);
			bw.write(Remark3WithTag);
			bw.write(TestItemEndTag);
			bw.close();
		}catch (IOException e){
			Log.i(mPKGName, "Error:"+e);
			e.printStackTrace();
		}

	}
}
