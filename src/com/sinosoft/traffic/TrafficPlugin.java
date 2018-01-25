package com.sinosoft.traffic;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;

public class TrafficPlugin extends CordovaPlugin {

	private Context context;
	public Intent intent;

	@Override
	public boolean execute(String action, CordovaArgs args,
			CallbackContext callbackContext) throws JSONException {
		context = cordova.getActivity().getApplicationContext();
		intent = new Intent(context, TrafficService.class);
		if (action.equals("startservice")) {
			context.startService(intent);
			return true;
		} else if (action.equals("stopservice")) {
           context.stopService(intent);
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onDestroy() {
		if(intent!=null){
			  context.stopService(intent);	
		}
		super.onDestroy();
	}

}
