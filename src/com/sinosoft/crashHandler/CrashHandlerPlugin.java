package com.sinosoft.crashHandler;

import java.io.File;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class CrashHandlerPlugin  extends CordovaPlugin {
	
	private String filename;
	private Thread thread;
	private Context context;
	@Override
	public boolean execute(String action, CordovaArgs args,
			CallbackContext callbackContext) throws JSONException {
	context = cordova.getActivity().getApplicationContext();
		if(action.equals("crash")){
			isFile();
			return true;
		}
		
		return false;
	}
	
	private void isFile() {
		SharedPreferences sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
		filename = sp.getString("filename", "");
		String fileurl = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + "crash" + '/' + filename;
		if (filename.equals("")) {

		} else {
			File file = new File(fileurl);
			if (file.exists()) {
				
				thread = new Thread(new Runnable() {
					public void run() {
						HttpAssist httpassist = new HttpAssist(context,filename);
						httpassist.Init();
					}
				});
				thread.start();
			}
			
		}
	}

}
