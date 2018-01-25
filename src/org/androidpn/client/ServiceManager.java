/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidpn.client;

import java.util.Properties;

import com.sinosoft.bean.VPNAddressBean;
import com.sinosoft.vpn.VPNAddress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/** 
 * 
 * 管理通知服务，加载配置
 * This class is to manage the notification service and to load the configuration.
 * 
 */
public final class ServiceManager {

    private static final String LOGTAG = LogUtil
            .makeLogTag(ServiceManager.class);

    private Context context;
    private SharedPreferences sharedPrefs;

    private Properties props;

    private String version = "0.5.0";

    private String apiKey;

    private String xmppHost;

    private String xmppPort;
    
    private String xmppHost1;

    private String xmppPort1;

    private String callbackActivityPackageName;

    private String callbackActivityClassName;
	public SharedPreferences sp;
	private int VPNflag;
    public ServiceManager(Context context,String usercode) {
        this.context = context;
        VPNAddress();
        //获取调用者Activity的包名、类名
        if (context instanceof Activity) {
            Log.i(LOGTAG, "Callback Activity...");
            Activity callbackActivity = (Activity) context;
            callbackActivityPackageName = callbackActivity.getPackageName();
            callbackActivityClassName = callbackActivity.getClass().getName();
        }

        props = loadProperties();
//        apiKey = props.getProperty("apiKey", "");
        apiKey = com.sinosoft.phoneGapPlugins.util.Constant.APIKEY;
//        xmppHost = props.getProperty("xmppHost", "127.0.0.1");
        xmppHost = com.sinosoft.phoneGapPlugins.util.Constant.XMPPHOST;
//        xmppPort = props.getProperty("xmppPort", "5222");
        xmppPort = com.sinosoft.phoneGapPlugins.util.Constant.XMPPPPORT;
        Log.i(LOGTAG, "apiKey=" + apiKey);
        Log.i(LOGTAG, "xmppHost=" + xmppHost);
        Log.i(LOGTAG, "xmppPort=" + xmppPort);

        //将上面获取的Properties信息存入SharedPreferences方便以后直接调用
        sharedPrefs = context.getSharedPreferences(
                Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPrefs.edit();
        editor.putString(Constants.API_KEY, apiKey);
        editor.putString(Constants.VERSION, version);
        editor.putString(Constants.XMPP_HOST, xmppHost1);
        editor.putInt(Constants.XMPP_PORT, Integer.parseInt(xmppPort1));
        editor.putString(Constants.CALLBACK_ACTIVITY_PACKAGE_NAME,
                callbackActivityPackageName);
        editor.putString("USER_CODE", usercode);
        editor.putString(Constants.CALLBACK_ACTIVITY_CLASS_NAME,
                callbackActivityClassName);
        editor.commit();
    }

    /**
     * 启动服务
     */
    public void startService() {
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = NotificationService.getIntent();
                context.startService(intent);
            }
        });
        serviceThread.start();
    }

    /**
     * ֹͣ停止服务
     */
    public void stopService() {
        Intent intent = NotificationService.getIntent();
        context.stopService(intent);
    }

    /**
     *  读取raw中的androidpn.properties文件的内容
     */
    private Properties loadProperties() {
        Properties props = new Properties();
        try {
            int id = context.getResources().getIdentifier("androidpn", "raw",
                    context.getPackageName());
            props.load(context.getResources().openRawResource(id));
        } catch (Exception e) {
            Log.e(LOGTAG, "Could not find the properties file.", e);
        }
        return props;
    }
    
    public void setNotificationIcon(int iconId) {
        Editor editor = sharedPrefs.edit();
        editor.putInt(Constants.NOTIFICATION_ICON, iconId);
        editor.commit();
    }

    public static void viewNotificationSettings(Context context) {
        Intent intent = new Intent().setClass(context,
                NotificationSettingsActivity.class);
        context.startActivity(intent);
    }
    
	private void VPNAddress() {
		sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
		VPNflag = sp.getInt("VPNFlag", 1);
		VPNAddress vpnaddress = new VPNAddress(context);
		if (VPNflag == 1) {
			VPNAddressBean bean = vpnaddress.queryVPN(1);
			String xmppaddress = bean.getXmpp();
			String[] tras1 = xmppaddress.split(":");
			if (tras1 != null && tras1.length > 0) {
				xmppHost1 = tras1[0];
				xmppPort1 = tras1[1];
				System.out.println("xmppHost:" + xmppHost + "xmppPort"
						+ xmppPort);
			}
		} else {
			VPNAddressBean bean = vpnaddress.queryVPN(2);
			String xmppaddress = bean.getXmpp();
			String[] tras1 = xmppaddress.split(":");
			if (tras1 != null && tras1.length > 0) {
				xmppHost1 = tras1[0];
				xmppPort1 = tras1[1];
			}
		}
	}

}
