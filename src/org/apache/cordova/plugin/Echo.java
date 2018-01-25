package org.apache.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Echo extends CordovaPlugin{
	
	  @Override  
	    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {  
	        if (action.equals("echo")) {  
	            String message = args.getString(0);  
	            this.echo(message, callbackContext);  
	            return true;  
	        }  
	        return false;  
	    }  
	  
	    private void echo(String message, CallbackContext callbackContext) {  
	        if (message != null && message.length() > 0) {  
	            callbackContext.success("回调成功了");  
	        } else {  
	            callbackContext.error("Expected one non-empty string argument.");  
	        }  
	    }  
	  
}  
