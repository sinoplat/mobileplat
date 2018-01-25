package com.sinosoft.phoneGapPlugins.unInstallApp;

import java.util.ArrayList;
import java.util.List;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import com.sinosoft.phoneGapPlugins.pgsqliteplugin.DatabaseHelper;

import android.R.dimen;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class UnInstallAppPlugin extends CordovaPlugin {

	CallbackContext callbackContext;
	public static PackageInfo packageInfo = null;
	private Context context;
	private List<String> list;
	private GetUnInstallcast othercast;
	private String unInstallFlag;
	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		context = cordova.getActivity().getApplicationContext();
		// 卸载ＡＰＰ
		if ("unInstallApp".equals(action)) {
			String param = args.getString(0);
			String flag = this.unInstallApp(param);
			callbackContext.success(flag);
			return true;
		} else if ("unInstallAppAll".equals(action)) {
			List<String> list = getPackageName();
			for (int i = 0;i<list.size() ; i++) {

				Install(list.get(i));
			}
			return true;
		}else if (action.equals("uninstallcast")) {
			othercast = new GetUnInstallcast();
			IntentFilter filter = new IntentFilter("UNINSTALLAPPLOCATION");
			context.registerReceiver(othercast, filter);
			return true;
		}else if(action.equals("uninstallflag")){
			callbackContext.success(unInstallFlag);
			return true;
		}

		return false;
	}

	/**
	 * 卸载App
	 * 
	 * @param param
	 */
	private String unInstallApp(String param) {
		// this.Dielog(param);
		String flag = uninstallApplication(param);
		return flag;
	}

	/**
	 * 卸载确认提示框
	 * 
	 * @param param
	 */
	public void Dielog(String param) {
		final String unInstallApp = param;
		new AlertDialog.Builder(cordova.getActivity())
				.setTitle("卸载")
				// 设置标题
				.setMessage("您确定要卸载此应用？")
				// 设置提示消息
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {// 设置确定的按键
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// do something
								uninstallApplication(unInstallApp);
								dialog.dismiss();
							}
						})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {// 设置取消按键
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// do something
								dialog.dismiss();
							}
						})

				.setCancelable(false)// 设置按返回键是否响应返回，这是是不响应
				.show();// 显示

	}

	/**
	 * 卸载
	 */
	private String uninstallApplication(String unInstallApp) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:" + unInstallApp));
		cordova.getActivity().startActivityForResult(intent, 0);
		return "success";
	}

	public void Install(String packname) {
		try {
			packageInfo = context.getPackageManager().getPackageInfo(packname,
					0);
		} catch (NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		if (packageInfo == null) {
			System.out.println("没有安装");
		} else {
			uninstallApplication(packname);
		}

	}

	// 获取所有Appbao包名
	public List<String> getPackageName() {
		list = new ArrayList<String>();
		list.add("com.sinosoft.gyicPlat");
		DatabaseHelper dbOpenHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("prpmApplication",
				new String[] { "PackageName" }, null, null, null, null, null);

		while (cursor.moveToNext()) {
			String PackageName = cursor.getString(cursor
					.getColumnIndex("PackageName"));
			list.add(PackageName);
		}
		cursor.close();
		db.close();
		return list;

	}
	
	private class GetUnInstallcast extends BroadcastReceiver {

		
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals("UNINSTALLAPPLOCATION")) {
			unInstallFlag = intent.getStringExtra("unInstallFlag");

			}
		}

	}
	
	@Override
	public void onDestroy() {
		if(othercast!=null){
		context.unregisterReceiver(othercast);
	}
		super.onDestroy();
	}

}
