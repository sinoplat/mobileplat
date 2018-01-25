package com.sinosoft.phoneGapPlugins.checkAppExists;

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

import com.sinosoft.phoneGapPlugins.download.DownloadLib;


public class CheckApp extends CordovaPlugin {

	@Override
	public boolean execute(String action, String rawArgs,
			CallbackContext callbackContext) throws JSONException {
		// TODO Auto-generated method stub
		return super.execute(action, rawArgs, callbackContext);
	}

	CallbackContext callbackContext;
	Context context;
	DownloadLib fd2;
	private boolean isAvilible(Context context, String packageName){ 
        final PackageManager packageManager = context.getPackageManager();
        List< PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
       
        if(pinfo != null){ 
            for(int i = 0; i < pinfo.size(); i++){ 
                String pn = pinfo.get(i).packageName; 
                pName.add(pn); 
              //  System.out.println(pn);
            } 
        } 
        return pName.contains(packageName);
  } 

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		context = cordova.getActivity().getApplicationContext();
		this.callbackContext = callbackContext;
		
		if("checkAppExist".equals(action)){
			// System.out.println(args.getString(0));
			if (isAvilible(context,args.getString(0))) {
				callbackContext.success("1");
			}else {
				callbackContext.success("0");
			}
			
			
			
			
			return true;
		}
		JSONArray apkInfos=new JSONArray();
		if ("checkExistAndCheckVersion".equals(action)) {
			for (int i = 0; i < args.length(); i++) {
				JSONObject jsonObject=args.getJSONObject(i);
				JSONObject apkInfo=new JSONObject();
				String packageName= jsonObject.get("packageName").toString();
				String version= jsonObject.get("version").toString();
				apkInfo.put("packageName", packageName);
				if (isAvilible(context,packageName)) {
					apkInfo.put("exist", "1");
					PackageManager manager = context.getPackageManager();
					String currentVersion="";
					try {
						 currentVersion=manager.getPackageInfo(packageName, 0).versionName;
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (version.equals(currentVersion)) {
						apkInfo.put("state", "2");
					}else {
						apkInfo.put("state", "1");
					}
					
				}else {
					apkInfo.put("exist", "0");
					//0：未安装，1：已经安装，版本不是最新的，2：版本是最新的
					apkInfo.put("state", "0");
				}
				apkInfos.put(apkInfo);
			}
			callbackContext.success(apkInfos);
			
			return true;
		}
		
		if("checkAppExistByArray".equals(action)){
			// System.out.println(args.getString(0));
			String[] array=args.getString(0).split("#");
			JSONObject jsonObject=new JSONObject();
			for (String string : array) {
				if (isAvilible(context,string)) {
					jsonObject.put(string, "1");
				}else {
					jsonObject.put(string, "0");
				}
			}
			
			callbackContext.success(jsonObject);
			
			
			return true;
		}
		return true;
	}
	

	
}
