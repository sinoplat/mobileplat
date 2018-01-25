/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.sinosoft.gyicPlat;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import com.sinosoft.crashHandler.CrashHandler;
import com.sinosoft.phoneGapPlugins.pgsqliteplugin.DatabaseHelper;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.way.pattern.GuideGesturePasswordActivity;
import com.way.pattern.ResetGesturePasswordActivity;
import com.way.pattern.UnlockGesturePasswordActivity;

public class gyicPlat extends CordovaActivity {

	public SharedPreferences sp;
	private primarySource primarysource;
	private HomeKeyEventBroadCastReceiver homeKeyReceiver;
	public String a = "";
	private String flag;
	public String uuid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.init();
		// Set by <content src="index.html" /> in config.xml
		super.loadUrl(Config.getStartUrl());
		// super.loadUrl("file:///android_asset/www/index.html");
		// 在该方法中增加js操作java的接口,this为当前对象，js1为操作java文件的javascript的名字
		vpnFlag();
		appView.addJavascriptInterface(this, "js1");

		homeKeyReceiver = new HomeKeyEventBroadCastReceiver();
		registerReceiver(homeKeyReceiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		primarysource = new primarySource();
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(primarysource, filter);

		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(this);

	}

	// 跳转到设置手势密码页面
	@JavascriptInterface
	public void createGesturePassWord() {
		Intent intent = new Intent();
		// intent.putExtra("name", str);
		// intent.putExtra("pass", str);
		intent.setClass(gyicPlat.this, GuideGesturePasswordActivity.class);
		startActivity(intent);
	}

	// 跳转到手势密码验证页面
	@JavascriptInterface
	public void unlockGesturePassWord() {
		Intent intent = new Intent();
		// intent.putExtra("name", str);
		// intent.putExtra("pass", str);
		intent.setClass(gyicPlat.this, UnlockGesturePasswordActivity.class);
		startActivity(intent);
	}

	// 登陆验证手势密码
	@JavascriptInterface
	public void loginPassWord() {
		SharedPreferences sp = getSharedPreferences("SP", Context.MODE_PRIVATE);
		boolean cancel = sp.getBoolean("cancel", true);
		if (cancel == true) {
			unlockGesturePassWord();
		} else {

		}
	}

	// 跳转到重新设置手势密码页面
	@JavascriptInterface
	public void resetPassWord() {
		Intent intent = new Intent();
		// intent.putExtra("name", str);
		// intent.putExtra("pass", str);
		intent.setClass(gyicPlat.this, ResetGesturePasswordActivity.class);
		startActivity(intent);
	}

	@JavascriptInterface
	public void regian() {
		SharedPreferences sp = getSharedPreferences("SP", Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean("cancel", true);
		editor.commit();
		Toast.makeText(this, "手势密码已恢复", 2000).show();
	}

	class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {
		static final String SYSTEM_REASON = "reason";
		static final String SYSTEM_HOME_KEY = "homekey";// home key
		static final String SYSTEM_RECENT_APPS = "recentapps";// long home key

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (reason != null) {
					if (reason.equals(SYSTEM_HOME_KEY)) {

						sp = getSharedPreferences("SP", Context.MODE_PRIVATE);
						if (sp.getBoolean("first", true) == false) {
							Editor editor = sp.edit();
							editor.putBoolean("home", false);
							editor.putBoolean("text", false);
							editor.commit();
						} else {

						}

						// home key处理点

					} else if (reason.equals(SYSTEM_RECENT_APPS)) {
						sp = getSharedPreferences("SP", Context.MODE_PRIVATE);
						if (sp.getBoolean("first", true) == false) {
							Editor editor = sp.edit();
							editor.putBoolean("home", false);
							editor.putBoolean("text", false);
							editor.commit();
						} else {

						}

					}
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sp = getSharedPreferences("SP", Context.MODE_PRIVATE);
		String flag = getFlag();
		boolean cancel = sp.getBoolean("cancel", true);
		if (flag.equals("1")) {
			if (cancel == false) {

			} else {
				if (sp.getBoolean("text", true) == false) {

					if (sp.getBoolean("home", true) == false) {
						unlockGesturePassWord();
					} else {

					}
				} else {

				}
			}
		}
	}

	class primarySource extends BroadcastReceiver {
		private SharedPreferences sp;

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_SCREEN_OFF.equals(action)) {

				sp = getSharedPreferences("SP", Context.MODE_PRIVATE);
				if (sp.getBoolean("first", true) == false) {
					Editor editor = sp.edit();
					editor.putBoolean("home", false);
					editor.putBoolean("text", false);
					editor.commit();

				}
			}
		}

	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		unregisterReceiver(homeKeyReceiver);
		unregisterReceiver(primarysource);
	}

	@JavascriptInterface
	public void cancel() {
		SharedPreferences sp = getSharedPreferences("SP", Context.MODE_PRIVATE);

		Editor editor = sp.edit();

		editor.putBoolean("cancel", false);
		editor.commit();
		Toast.makeText(this, "手势密码已取消", 2000).show();
	}

	// 获取flag
	public String getFlag() {
		DatabaseHelper dbOpenHelper = new DatabaseHelper(this);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("config", new String[] { "flag" }, null, null,
				null, null, null);

		if (cursor.moveToFirst()) {
			flag = cursor.getString(cursor.getColumnIndex("flag"));
			cursor.close();
			db.close();
			return flag;
		}
		return "";

	}

	// 选择连接哪个vpn,1：表示深信服。2：表示天融信
	public void vpnFlag() {
		SharedPreferences sp = getSharedPreferences("SP", Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt("VPNFlag", Constant.VPNFLAG);
		editor.commit();
	}


}
