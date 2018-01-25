package com.sinosoft.phoneGapPlugins.android;

import org.androidpn.client.NotificationService;
import org.androidpn.client.ServiceManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sinosoft.gyicPlat.R;
import com.sinosoft.phoneGapPlugins.pgsqliteplugin.DatabaseHelper;
import com.sinosoft.phoneGapPlugins.util.GetUserCode;

public class BootBroadcastReceiver extends BroadcastReceiver {

	private Context context;
	private ServiceManager serviceManager;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			this.context = context;
			Editor edit = context.getSharedPreferences("Config", Context.MODE_PRIVATE).edit();
			edit.putBoolean("isQuit", true);
			edit.commit();
			GetUserCode users = new GetUserCode(context);
			String user = users.getUsercode();
			if (user != null) {
				serviceManager = new ServiceManager(context, user);
				serviceManager.setNotificationIcon(R.drawable.desktop_icon);
				serviceManager.startService();
				Intent intent2 = new Intent(context, NotificationService.class);
				context.startService(intent2);
			}
		}
	}

}
