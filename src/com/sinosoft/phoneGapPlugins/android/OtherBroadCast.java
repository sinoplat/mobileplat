package com.sinosoft.phoneGapPlugins.android;


import org.androidpn.client.NotificationService;
import org.androidpn.client.ServiceManager;

import com.sinosoft.gyicPlat.R;
import com.sinosoft.phoneGapPlugins.util.GetUserCode;
import com.sinosoft.vpn.VPN;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

public class OtherBroadCast extends BroadcastReceiver {
	State wifiState = null;
	State mobileState = null;
	private String userCode;
	private Context context;
	private ServiceManager serviceManager;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		// 获取手机的连接服务管理器，这里是连接管理器类
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
			mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
					.getState();
		}
		String flag = "";
		Intent intent2 = new Intent(context, NotificationService.class);
		// 有网时启动服务
		if ((wifiState != null && State.CONNECTED == wifiState)
				|| (mobileState != null && State.CONNECTED == mobileState)) {
			GetUserCode users = new GetUserCode(context);
			String user = users.getUsercode();
			if (user != null) {
				serviceManager = new ServiceManager(context, user);
				serviceManager.setNotificationIcon(R.drawable.desktop_icon);
				serviceManager.startService();
				context.startService(intent2);
			}
			flag = "1";
		}
		if (wifiState != null && State.CONNECTED != wifiState
				&& State.CONNECTED != mobileState) {
			flag = "0";
			Toast.makeText(context, "手机网络异常，请检测或重新开启", Toast.LENGTH_LONG).show();
		}
		
//		Message msg=new Message();
//        msg.what=1;
//        Bundle bundle=new Bundle();
//        bundle.putString("messagecode", flag);
//        msg.setData(bundle);
//        .sendMessage(msg);
        
	}
	

}