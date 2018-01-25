package com.sinosoft.mobileshop.service.receiver;

import java.io.File;

import com.way.pattern.App;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReceiver extends BroadcastReceiver {

	private String localFile;

	@Override
	public void onReceive(Context context, Intent intent) {
		// 安装广播
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")
				|| intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
			Intent intenta = new Intent();
			intenta.setAction("com.sinosoft.msg.install");
			context.sendBroadcast(intenta);
			deleteApk();
		}
		// 卸载广播
		if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
			Intent intenta = new Intent();
			intenta.setAction("com.sinosoft.msg.install");
			context.sendBroadcast(intenta);
		}
	}

	/**
	 * 删除apk
	 * 
	 * @param localFile
	 */
	private void deleteApk() {
		localFile = App.get("localFile", "");
		if(localFile != null && !"".equals(localFile)) {
			try {
				File file = new File(localFile);
				if (file != null && file.exists()) {
					file.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
