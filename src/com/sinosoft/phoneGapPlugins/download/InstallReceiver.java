package com.sinosoft.phoneGapPlugins.download;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReceiver extends BroadcastReceiver{
    
    private String localFile;

	@Override  
    public void onReceive(Context context, Intent intent){
        //接收安装广播 
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {   
            String packageName = intent.getDataString();  
          
              System.out.println("安装了:"  + "222222222222");  
              Intent intent1  = new Intent();  
              intent1.setAction("NEW_LIFEFORMM");  
              intent1.putExtra("qudao", "11111111111111");  
              context.sendBroadcast(intent1);
              Intent intent2  = new Intent();  
              intent2.setAction("GET_PACKINSTALLNAME");  
              intent2.putExtra("packageName", packageName);
              context.sendBroadcast(intent2);

        }
        if(intent.getAction().equals("NEW_LIFEFORM")){
        	  localFile = intent.getStringExtra("localFile");
              System.out.println("取到值了:"  + localFile);  
        }
        //接收卸载广播  
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {   
            String packageName = intent.getDataString();   
            System.out.println("卸载了:"  + packageName + "包名的程序");
            String unInstallFlag = "1";
            Intent intent2  = new Intent();  
            intent2.setAction("UNINSTALLAPPLOCATION");  
            intent2.putExtra("unInstallFlag", unInstallFlag);
            context.sendBroadcast(intent2);
        }
    }
	
	public void DeleteApk(String localFile){
		  File file = new File(localFile);
		  file.delete();  

	}	

}  
