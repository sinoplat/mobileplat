package com.sinosoft.phoneGapPlugins.notifyplugin;

import org.androidpn.client.NotificationService;
import org.androidpn.client.ServiceManager;
import org.androidpn.client.XmppManager;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.sinosoft.gyicPlat.R;
import com.sinosoft.phoneGapPlugins.android.SmsActivity;

public class NotifyMessage extends CordovaPlugin {

	private ServiceManager serviceManager;
	private Context context;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		context = this.cordova.getActivity().getApplicationContext();
		if ("aeep".equals(action)) {
			String usercode = args.getString(0);
				if(usercode!=null){		
			serviceManager = new ServiceManager(cordova.getActivity()
					.getApplicationContext(), usercode);
			serviceManager.setNotificationIcon(R.drawable.desktop_icon);			
			Activity act = cordova.getActivity();
			Intent i = new Intent(act, NotificationService.class);	
			context.startService(i);// 启动后台服务  
				}

			return true;
		}else if("stop".equals(action)){
			Activity act = cordova.getActivity();
			Intent i = new Intent(act, NotificationService.class);	
			context.stopService(i);// 停止后台服务 
			return true;
		}else {
			return false;
		}
	}

	private void notifySms(JSONArray args) {
		try {
			String tickerText1 = args.getString(0);

			String titlebar = args.getString(1);
			String titletext = args.getString(2);
			Context context = this.cordova.getActivity()
					.getApplicationContext();
			// 定义NotificationManager
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(ns);
			// 定义通知栏展现的内容信息
			int icon = R.drawable.desktop_icon;
			CharSequence tickerText = tickerText1;
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);

			// 定义下拉通知栏时要展现的内容信息

			CharSequence contentTitle = titlebar;
			CharSequence contentText = titletext;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					new Intent(context, SmsActivity.class), 0);
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);

			// 用mNotificationManager的notify方法通知用户生成标题栏消息通知
			mNotificationManager.notify(1, notification);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
