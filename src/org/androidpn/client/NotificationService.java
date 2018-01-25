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

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sinosoft.gyicPlat.R;
import com.sinosoft.phoneGapPlugins.util.GetUserCode;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * 一直运行在后台的服务，响应服务端推送事件 需要在AndroidManifest.xml中进行注册 Service that continues to
 * run in background and respond to the push notification events from the
 * server. This should be registered as service in AndroidManifest.xml.
 * 
 */
public class NotificationService extends Service {

	private static final String LOGTAG = LogUtil
			.makeLogTag(NotificationService.class);

	public static final String SERVICE_NAME = "org.androidpn.client.NotificationService";

	private TelephonyManager telephonyManager;

	private BroadcastReceiver notificationReceiver;

	private BroadcastReceiver connectivityReceiver;

	private PhoneStateListener phoneStateListener;

	private ExecutorService executorService;

	private TaskSubmitter taskSubmitter;

	private TaskTracker taskTracker;

	private XmppManager xmppManager;

	private SharedPreferences sharedPrefs;
	
	private String deviceId;

	AlarmManager mAlarmManager = null;
	PendingIntent mPendingIntent = null;



	public NotificationService() {
		// 用于接收推送广播并用NotificationManager通知用户(系统通知栏的通知)
		notificationReceiver = new NotificationReceiver();
		// 接收手机网络状态的广播,来管理xmppManager与服务端的连接与断开
		connectivityReceiver = new ConnectivityReceiver(this);
		// 集成于android.telephony.PhoneStateListener,同上,用于监听数据链接的状态
		phoneStateListener = new PhoneStateChangeListener(this);
		// 线程池
		executorService = Executors.newSingleThreadExecutor();
		// 向线程池提交一个task任务
		taskSubmitter = new TaskSubmitter(this);
		// 任务计数器,用以维护当前工作的task任务
		taskTracker = new TaskTracker(this);
	}

	@Override
	public void onCreate() {
		Log.d(LOGTAG, "onCreate()...");
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// connectivityManager = (ConnectivityManager)
		// getSystemService(Context.CONNECTIVITY_SERVICE);

		sharedPrefs = getSharedPreferences(Constants.SHARED_PREFERENCE_NAME,
				Context.MODE_PRIVATE);

		// 获取设备ID,放入sharedPrefs中
		deviceId = telephonyManager.getDeviceId();
		// Log.d(LOGTAG, "deviceId=" + deviceId);
		Editor editor = sharedPrefs.edit();
		editor.putString(Constants.DEVICE_ID, deviceId);
		editor.commit();

		Intent intent = new Intent(getApplicationContext(),
				NotificationService.class);
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		mPendingIntent = PendingIntent.getService(this, 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);
		long now = System.currentTimeMillis();
		mAlarmManager.setInexactRepeating(AlarmManager.RTC, now, 6000,
				mPendingIntent);

		// 如果设备运行在模拟器,将模拟器放到sharedPrefs中
		if (deviceId == null || deviceId.trim().length() == 0
				|| deviceId.matches("0+")) {
			if (sharedPrefs.contains("EMULATOR_DEVICE_ID")) {
				deviceId = sharedPrefs.getString(Constants.EMULATOR_DEVICE_ID,
						"");
			} else {
				deviceId = (new StringBuilder("EMU")).append(
						(new Random(System.currentTimeMillis())).nextLong())
						.toString();
				editor.putString(Constants.EMULATOR_DEVICE_ID, deviceId);
				editor.commit();
			}
		}

		Log.d(LOGTAG, "deviceId=" + deviceId);

		xmppManager = new XmppManager(this);
		// 将xmppManager对象放入全局变量中，方便其他地方使用
		Constants.xmppManager = xmppManager;
		taskSubmitter.submit(new Runnable() {
			public void run() {
				NotificationService.this.start();
			}
		});
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(LOGTAG, "onStart()...");
	}

	@Override
	public void onDestroy() {
		Log.d(LOGTAG, "onDestroy()...");
		stop();
		System.out.println("onDestroy()...");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(LOGTAG, "onBind()...");
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return START_STICKY;
	}

	@Override
	public void onRebind(Intent intent) {
		Log.d(LOGTAG, "onRebind()...");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(LOGTAG, "onUnbind()...");
		return true;
	}

	public static Intent getIntent() {
		return new Intent(SERVICE_NAME);
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public TaskSubmitter getTaskSubmitter() {
		return taskSubmitter;
	}

	public TaskTracker getTaskTracker() {
		return taskTracker;
	}

	public XmppManager getXmppManager() {
		return xmppManager;
	}

	public SharedPreferences getSharedPreferences() {
		return sharedPrefs;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void connect() {
		Log.d(LOGTAG, "connect()...");
		taskSubmitter.submit(new Runnable() {
			public void run() {
				NotificationService.this.getXmppManager().connect();
			}
		});
	}

	public void disconnect() {
		Log.d(LOGTAG, "disconnect()...");
		taskSubmitter.submit(new Runnable() {
			public void run() {
				NotificationService.this.getXmppManager().disconnect();
			}
		});
	}

	/**
	 * 注册通知接受者
	 */
	private void registerNotificationReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_SHOW_NOTIFICATION);
		filter.addAction(Constants.ACTION_NOTIFICATION_CLICKED);
		filter.addAction(Constants.ACTION_NOTIFICATION_CLEARED);
		registerReceiver(notificationReceiver, filter);
	}

	/**
	 * 注销通知接受者
	 */
	private void unregisterNotificationReceiver() {
		unregisterReceiver(notificationReceiver);
	}

	/**
	 * 注册连接
	 */
	private void registerConnectivityReceiver() {
		Log.d(LOGTAG, "registerConnectivityReceiver()...");
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
		IntentFilter filter = new IntentFilter();
		// filter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectivityReceiver, filter);
	}

	/**
	 * 注销连接
	 */
	private void unregisterConnectivityReceiver() {
		Log.d(LOGTAG, "unregisterConnectivityReceiver()...");
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_NONE);
		unregisterReceiver(connectivityReceiver);
	}

	private void start() {
		Log.d(LOGTAG, "start()...");
		// 注册通知广播接受者
		registerNotificationReceiver();
		// 注册手机网络连接状态接受者
		registerConnectivityReceiver();
		// Intent intent = getIntent();
		// startService(intent);
		xmppManager.connect();
	}

	private void stop() {
		Log.d(LOGTAG, "stop()...");
		unregisterNotificationReceiver();
		unregisterConnectivityReceiver();
		xmppManager.disconnect();
		executorService.shutdown();
		System.out.println("LOGTAG,stop()...");
	}

	/**
	 * 提交一个新运行的任务 Class for summiting a new runnable task.
	 */
	public class TaskSubmitter {

		final NotificationService notificationService;

		public TaskSubmitter(NotificationService notificationService) {
			this.notificationService = notificationService;
		}

		@SuppressWarnings("unchecked")
		public Future submit(Runnable task) {
			Future result = null;
			if (!notificationService.getExecutorService().isTerminated()
					&& !notificationService.getExecutorService().isShutdown()
					&& task != null) {
				result = notificationService.getExecutorService().submit(task);
			}
			return result;
		}
	}

	/**
	 * 监控运行的任务数量 Class for monitoring the running task count.
	 */
	public class TaskTracker {

		final NotificationService notificationService;

		public int count;

		public TaskTracker(NotificationService notificationService) {
			this.notificationService = notificationService;
			this.count = 0;
		}

		/**
		 * 增加一个任务
		 */
		public void increase() {
			synchronized (notificationService.getTaskTracker()) {
				notificationService.getTaskTracker().count++;
				Log.d(LOGTAG, "Incremented task count to " + count);
			}
		}

		/**
		 * 减少一个任务
		 */
		public void decrease() {
			synchronized (notificationService.getTaskTracker()) {
				notificationService.getTaskTracker().count--;
				Log.d(LOGTAG, "Decremented task count to " + count);
			}
		}
	}

}
