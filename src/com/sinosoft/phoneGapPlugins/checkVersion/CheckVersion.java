package com.sinosoft.phoneGapPlugins.checkVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class CheckVersion extends CordovaPlugin {

	CallbackContext callbackContext;
	Context context;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		context = cordova.getActivity().getApplicationContext();
		this.callbackContext = callbackContext;
		PackageManager manager = context.getPackageManager();
		PackageInfo info = null;
		
		if("checkVersionCode".equals(action)){
			System.out.println(args.getString(0));
			try {
				info = manager.getPackageInfo(args.getString(0), 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

			String versionName = info.versionName;//0.0.1
			callbackContext.success(versionName);
		
			return true;
		}
		
		if("checkVersionCodeByArray".equals(action)){
			System.out.println(args.getString(0));
			String[] array=args.getString(0).split("#");
			JSONObject jsonObject=new JSONObject();
			String versionName = "";
			for (String string : array) {
				try {
					info = manager.getPackageInfo(string, 0);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				versionName = info.versionName;
				jsonObject.put(string, versionName);
			}
			
			callbackContext.success(jsonObject);
			
			return true;
		}
		
		return true;
	}
	
	
}
