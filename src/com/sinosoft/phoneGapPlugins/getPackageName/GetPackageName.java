package com.sinosoft.phoneGapPlugins.getPackageName;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.CallLog;

public class GetPackageName extends CordovaPlugin{

	 public static PackageManager packageManager = null;
	    public static PackageInfo packageInfo = null;
	    public static ApplicationInfo applicationInfo = null;
		private String packageNames;
		private Context context;
	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		context = cordova.getActivity().getApplicationContext();
	
		if(action.equals("getpackname")){
			String packinfo =	getPackageInfo(context);
			callbackContext.success(packinfo);
		}
		
		return false;
	}
	
	private  String getPackageInfo(Context context) {
        if(packageInfo == null) {
            try {
            	 packageManager = context.getApplicationContext().getPackageManager();
                packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                packageNames = packageInfo.packageName;
            } catch (PackageManager.NameNotFoundException e) {
                packageInfo = null;
            }
        }
        return packageNames;
    }
	
}
