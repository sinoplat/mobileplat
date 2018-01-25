package com.sinosoft.phoneNumberPlugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;

import com.sinosoft.getPhoneNumberUtils.SIMCardInfo;

public class phoneNumber extends CordovaPlugin {

	private String phonename;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		
		if (action.equals("phonenumber")) {

			Context context = cordova.getActivity().getApplicationContext();
			SIMCardInfo siminfo = new SIMCardInfo(context);
			String phonenumber = siminfo.getNativePhoneNumber();
			callbackContext.success(phonenumber);
            return true;
		}else if(action.equals("phonename")){
			
			Context context = cordova.getActivity().getApplicationContext();
			SIMCardInfo siminfo = new SIMCardInfo(context);
			try {
				phonename = siminfo.getProvidersName();
			} catch (Exception e) {
				callbackContext.success("");
				return false;
			}
			
			callbackContext.success(phonename);
			 return true;
		}

		return false;
	}

}
