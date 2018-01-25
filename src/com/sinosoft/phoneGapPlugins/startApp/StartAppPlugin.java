package com.sinosoft.phoneGapPlugins.startApp;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import com.sinosoft.phoneGapPlugins.util.Constant;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class StartAppPlugin extends CordovaPlugin {

	CallbackContext callbackContext;
	private Context context;
	
	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
	context = cordova.getActivity().getApplicationContext();
		//启动
		if("startApp".equals(action)){
			String activityClass = args.getString(0);
			this.startApp(activityClass);
			return true;
		}
		if("openApp".equals(action)){
			String packagename = args.getString(0);
			String packageclassname = args.getString(1);
			openApp(packagename,packageclassname);
			return true;
		}
		if(action.equals("callbackflag")){
			SharedPreferences sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
			String callback =sp.getString("callbackflag","1");
			callbackContext.success(callback);
			return true;
		}
		if(action.equals("callbackputflag")){
			String callbackflag =	args.getString(0);
			SharedPreferences sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putString("callbackflag",callbackflag);
			editor.commit();
			return true;
		}
		
		return false;
	}
	
	/**
	 * 启动App
	 * @param activityClass
	 */
	private void startApp(String activityClass){
//		方式1：
		Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(activityClass);  
		
		cordova.getActivity().startActivity(LaunchIntent);
//		方式2：
//		ComponentName component = new ComponentName("com.example.download","download");
//		Intent myIntent = new Intent();
//		myIntent.setComponent(component);
//		cordova.getActivity().startActivity(myIntent);
	}
	
	private void openApp(String packagename,String packkageclassname) {
		Intent mIntent = new Intent( ); 
		ComponentName comp = new ComponentName(packagename, packkageclassname);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mIntent.setComponent(comp); 
		mIntent.setAction("android.intent.action.VIEW"); 
		context.startActivity(mIntent);
		
		}


	
}
