package com.sinosoft.phoneGapPlugins.download;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class Othercast extends BroadcastReceiver{

	private String localFile;
	private SharedPreferences sp;

	@Override
	public void onReceive(Context context, Intent intent) {
	String qudao =	intent.getStringExtra("qudao");
	System.out.println("quxiao"+"111111111");
	sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
		if(qudao!=null){
			localFile = sp.getString("localFile", "");
			DeleteApk(localFile);
		}
	}
	
	public void DeleteApk(String localFile){
		  File file = new File(localFile);
		  file.delete();  

	}	

}
