package com.sinosoft.phoneGapPlugins.getInstalledPackageName;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class GetInstalledPackageName extends CordovaPlugin {

	private GetInstallNamecast othercast;
	private Context context;

	private String packageName;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		context = cordova.getActivity().getApplicationContext();
		if (action.equals("startcast")) {
			othercast = new GetInstallNamecast();
			IntentFilter filter = new IntentFilter("GET_PACKINSTALLNAME");
			context.registerReceiver(othercast, filter);
			return true;
		}else if(action.equals("packname")){
			Intent intent3 = new Intent();
			
			callbackContext.success(packageName);
			return true;
		}
		
		
		return false;
	}

	private class GetInstallNamecast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals("GET_PACKINSTALLNAME")) {
				packageName = intent.getStringExtra("packageName");

			}
		}

	}

}
