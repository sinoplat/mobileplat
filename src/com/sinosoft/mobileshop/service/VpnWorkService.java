package com.sinosoft.mobileshop.service;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.sangfor.ssl.SangforAuth;
import com.sinosoft.mobileshop.appwidget.floatwindow.MyWindowManager;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.service.receiver.WakeUpReceiver;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.LiteOrmUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;

public class VpnWorkService extends Service implements OnSharedPreferenceChangeListener{

    private static final int sHashCode = VpnWorkService.class.getName().hashCode();

    public static boolean startFlag = false;
    
    private boolean isStop = false;
    private boolean isSure = false;
    private boolean isWait = false;
    public static final int SHOW_RECONNECTION = 1;//展示重连的对话框
    public static final int SHOW_SUCCESS = 2;//展示成功的对话框
    public static final int SHOW_FAIL = 3;//展示失败的对话框
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private String packageNames = "";
    private int nineCount = 0;
    private AlertDialog waitDialog;//等待连接对话框
    private AlertDialog reconnectionDialog;//是否重连对话框
	private SharedPreferences preferences;
	private static HomeWatcherReceiver mHomeKeyReceiver = null;

    /**
     * 1.防止重复启动，可以任意调用startService(Intent i);
     * 2.利用漏洞启动前台服务而不显示通知;
     * 3.在子线程中运行定时任务，处理了运行前检查和销毁时保存的问题;
     * 4.启动守护服务.
     * 5.简单守护开机广播.
     */
    private int onStart(Intent intent, int flags, int startId) {
        //利用漏洞在 API Level 17 及以下的 Android 系统中，启动前台服务而不显示通知
        startForeground(sHashCode, new Notification());
        preferences = getSharedPreferences("Config", Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
        final Editor edit = preferences.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
            startService(new Intent(this, WorkNotificationService.class));
        }

        //启动守护服务，运行在:watch子进程中
        startService(new Intent(this, WatchDaemonService.class));

        //若还没有取消订阅，说明任务仍在运行，为防止重复启动，直接返回START_STICKY
        if (startFlag) return START_STICKY;

        //----------业务逻辑----------
        startFlag = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
					while(true) {
						try {
							Thread.sleep(4000);
						} catch (Exception e) {
							e.printStackTrace();
						}
						int vpnStatus = -1;
						SangforAuth sfAuth = SangforAuth.getInstance();
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								 boolean isQuit = preferences.getBoolean("isQuit", false);
							      //创建悬浮窗
							        if (!MyWindowManager.isWindowShowing() && !isQuit && MyWindowManager.getIsOpenFloat()){
										 MyWindowManager.createSmallWindow(getApplicationContext());
							        }
							}
						});
						try {
							if(sfAuth != null) {
								vpnStatus = sfAuth.vpnQueryStatus();
								Constant.VPNSTATUS = vpnStatus;
							} else {
								Constant.VPNSTATUS = -1;
							}
						} catch (Exception e) {
							e.printStackTrace();
							Constant.VPNSTATUS = -1;
						}
						if(vpnStatus == -1) {
							System.out.println("vpn需要重新初始化");
						}
						if(vpnStatus == 5){
							if(MyWindowManager.isSmallWindowShowing()){
								handler.post(new Runnable() {
									@Override
									public void run() {
										MyWindowManager.updateSmallVpnStatus(getApplicationContext(), true);
									}
								});
							}
							if(MyWindowManager.isBigWindowShowing()){
								handler.post(new Runnable() {
									@Override
									public void run() {
										MyWindowManager.updateBigVpnStatus(getApplicationContext(), "VPN已连接", "关闭VPN");
									}
								});
							}
						 }else {
							if(MyWindowManager.isSmallWindowShowing()){
								handler.post(new Runnable() {
									@Override
									public void run() {
										MyWindowManager.updateSmallVpnStatus(getApplicationContext(), false);
									}
								});
							}
							if(MyWindowManager.isBigWindowShowing()){
								handler.post(new Runnable() {
									@Override
									public void run() {
										MyWindowManager.updateBigVpnStatus(getApplicationContext(), "VPN未连接", "连接VPN");
									}
								});
							}
						}
						if(vpnStatus == 9) {
							nineCount++;
						}
						if(vpnStatus !=5 && !isAppOnForegroud() && isAppOnForegroudForShop()) {
							Message message = Message.obtain();
							message.what = SHOW_RECONNECTION;
							handler.sendMessage(message);
						}
//						if(vpnStatus == 9 && nineCount >= 3 && !isAppOnForegroud() && isAppOnForegroudForShop() ) {
//							nineCount = 0;
//							handler.sendEmptyMessage(0);
//						}
//						if(vpnStatus == 5 && Constant.isAlert) {
//							Constant.isAlert = false;
//						}
						System.out.println("vpn状态--"+vpnStatus);
//						Log.i("syso", "vpn状态--"+vpnStatus);
						edit.putInt("vpnStatus", vpnStatus);
						edit.commit();
					}
				}
			}).start();
        //----------业务逻辑----------
        //简单守护开机广播
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(getPackageName(), WakeUpReceiver.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(getPackageName(), WakeUpReceiver.WakeUpAutoStartReceiver.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        //注册监听home键广播
        mHomeKeyReceiver = new HomeWatcherReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeKeyReceiver, homeFilter);
        return START_STICKY;
    }
    
    private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case SHOW_RECONNECTION:
				showVpnPrompt();
				break;
				
			case SHOW_SUCCESS:
				if(isWait){
					CommonUtil.showTimeDialog(VpnWorkService.this, "VPN已经恢复连接", 1000);
				}
				if (reconnectionDialog != null && reconnectionDialog.isShowing()) {
					reconnectionDialog.dismiss();
				}
				if (waitDialog != null && waitDialog.isShowing()) {
					waitDialog.dismiss();
					isWait = false;
				}
				break;
				
			case SHOW_FAIL:
				if(isWait){
					CommonUtil.showTimeDialog(VpnWorkService.this, "VPN连接失败，请确认网络是否通畅并稍候尝试", 1000);
				}
				if (waitDialog != null && waitDialog.isShowing()) {
					waitDialog.dismiss();
					isWait = false;
				}
				break;

			default:
				break;
			}
			
		}
	};
	
	/**
	 * 展示重连窗口
	 */
	private void showVpnPrompt() {
		boolean isQuit = preferences.getBoolean("isQuit", false);
		if(isStop) {
			return;
		}
//		if(Constant.isAlert) {
//			return;
//		}
		if(reconnectionDialog != null && reconnectionDialog.isShowing()){
			return;
		}
		if(isQuit){
			return;
		}
//		Constant.isAlert = true;
		
		reconnectionDialog = new AlertDialog.Builder(this)
				.setTitle("重要提醒")
				.setMessage("网络信号不佳，VPN已断开，请确认是否尝试重新连接？")
				.setPositiveButton("确认", null)
				.setNegativeButton("取消", null)
				.create();
		//在服务中使用dialog必须加上这句
		reconnectionDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		reconnectionDialog.show();
		//为了不让对话框点击后消失，重写点击方法，覆盖原生的代码
		if (reconnectionDialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
			reconnectionDialog.getButton(AlertDialog.BUTTON_POSITIVE)
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							isSure = true;
							Log.i("syso", "-----------------重连-----------------------");
							// 重连
							Intent intent = new Intent();
							intent.setAction("com.sinosoft.msg.vpnreconnect");
							VpnWorkService.this.sendBroadcast(intent);
							showWaitDialog();
						}

					});
		}
		if (reconnectionDialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
			reconnectionDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							AlertDialog f = new AlertDialog.Builder(
									VpnWorkService.this)
									.setTitle("重要提醒")
									.setMessage(
											"取消连接VPN会导致移动应用平台中所有APP都无法正常使用，请再次确认是否"
													+ "取消并不再提醒？\r\n特别提醒：可以通过移动应用平台中手工连接按钮重新连接VPN。")
									.setPositiveButton(
											"重连VPN",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													isSure = true;
													// 重连
													Intent intent = new Intent();
													intent.setAction("com.sinosoft.msg.vpnreconnect");
													VpnWorkService.this
															.sendBroadcast(intent);
													showWaitDialog();
												}
											})
									.setNegativeButton(
											"确认取消",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													isStop = true;
													if (reconnectionDialog != null && reconnectionDialog.isShowing()) {
														reconnectionDialog.dismiss();
													}
												}
											}).create();

							f.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
							f.show();
						}
					});
		}
		
	}

	public boolean isAppOnForegroud() {
		ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = this.getPackageName();
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();   
		if (appProcesses == null) {
			return false;   
		}
		for (RunningAppProcessInfo appProcess : appProcesses) {   
			if (appProcess.processName.equals(packageName) && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {   
				return true;   
			}
		}
		return false;
	}
	
	private void showWaitDialog() {
		AlertDialog.Builder builder = new Builder(VpnWorkService.this);
    	builder.setTitle("提示");
    	builder.setMessage("正在连接VPN,请稍候.....");
    	builder.setNegativeButton("取消", null);
    	waitDialog = builder.create();
    	waitDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	waitDialog.setCancelable(false);
    	isWait=true;
    	waitDialog.show();
    	if(waitDialog.getButton(AlertDialog.BUTTON_NEGATIVE)!=null) {
    		waitDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog f = new AlertDialog.Builder(VpnWorkService.this)
					.setTitle("提示")
					.setMessage("已经取消VPN连接,再次连接可以通过移动应用平台中手工连接按钮重新连接VPN。点击确定关闭窗口")
					.setPositiveButton("取消", null)
					.setNegativeButton("确认",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							isStop = true;
							if(waitDialog != null && waitDialog.isShowing()){
								isWait = false;
								waitDialog.dismiss();
							}
							if(reconnectionDialog != null && reconnectionDialog.isShowing()){
								reconnectionDialog.dismiss();
							}
						}
					}).create();
					f.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					f.show();
				}
			});
    		
    	}
    	//开启线程，30秒后需要一个结果
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(1000*30);
				} catch (Exception e) {
					e.printStackTrace();
				}
				int vpnStatus = -1;
				SangforAuth sfAuth = SangforAuth.getInstance();
				try {
					if(sfAuth != null) {
						 vpnStatus = sfAuth.vpnQueryStatus();
					} else {
						vpnStatus = -1;
					}
				} catch (Exception e) {
					e.printStackTrace();
					vpnStatus = -1;
				}
				Message message = Message.obtain();
				if(vpnStatus == 5){
					message.what = SHOW_SUCCESS;
				}else {
					message.what = SHOW_FAIL;
				}
				handler.sendMessage(message);
			}
		}).start();
	}
	
	public boolean isAppOnForegroudForShop() {
		ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		if(packageNames == null || "".equals(packageNames)) {
			ArrayList<AppVersionInfo> appList = new ArrayList<AppVersionInfo>();
			try {
				appList = LiteOrmUtil.getLiteOrm(getApplicationContext()).query(AppVersionInfo.class);
			} catch(Exception e) {
			}
			if(appList != null && appList.size() > 0) {
				for (AppVersionInfo appInfo : appList) {
					packageNames += packageNames +=appInfo.getPackageName() + ",";
				}
			}
		}

//		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//	        UsageStatsManager usm = (UsageStatsManager)this.getSystemService("usagestats");
//	        long time = System.currentTimeMillis();
//	        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
//	        if (appList != null && appList.size() > 0) {
//	            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
//	            for (UsageStats usageStats : appList) {
//	                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
//	            }
//	            if (mySortedMap != null && !mySortedMap.isEmpty()) {
//	                currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
//	            }
//	        }
//	    } else {
//	        ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
//	        List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
//	        currentApp = tasks.get(0).processName;
//	    }
		
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();   
		if (appProcesses == null) {
			return false;   
		}
		
		if(android.os.Build.VERSION.SDK_INT >= 21) {
			for (RunningAppProcessInfo appProcess : appProcesses) {   
				if (packageNames.indexOf(appProcess.processName) > -1 ) {   
					return true;   
				}
			}
		} else {
			for (RunningAppProcessInfo appProcess : appProcesses) {   
				if (packageNames.indexOf(appProcess.processName) > -1 && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {   
					return true;   
				}
			}
		}
		return false;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return onStart(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        onStart(intent, 0, 0);
        return null;
    }

    private void onEnd(Intent rootIntent) {
        System.out.println("保存数据到磁盘。");
        startService(new Intent(this, VpnWorkService.class));
        startService(new Intent(this, WatchDaemonService.class));
    }
    
    
    /**
     * 最近任务列表中划掉卡片时回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd(rootIntent);
    }

    /**
     * 设置-正在运行中停止服务时回调
     */
    @Override
    public void onDestroy() {
    	if (null != mHomeKeyReceiver) {
            unregisterReceiver(mHomeKeyReceiver);
        }
        onEnd(null);
    }
	

    public static class WorkNotificationService extends Service {

        /**
         * 利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(VpnWorkService.sHashCode, new Notification());
            stopSelf();
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		if (key.equals("vpnStatus") && !isAppOnForegroud() && isAppOnForegroudForShop()) {
			int vpnStatus = sharedPreferences.getInt("vpnStatus", -1);
			switch (vpnStatus) {
			case 5:
				if(waitDialog != null && waitDialog.isShowing()){
					waitDialog.dismiss();
					isWait = false;
				}
				if(reconnectionDialog != null && reconnectionDialog.isShowing()){
					reconnectionDialog.dismiss();
				}
				CommonUtil.showTimeDialog(VpnWorkService.this, "VPN已恢复连接", 1000);
				MyWindowManager.updateSmallVpnStatus(getApplicationContext(), true);
				break;
				
			default:
				break;
			}
		}
	}
	
	
	
	
	
	/**
	 * 监听点击home键的广播
	 * @author dell
	 *
	 */
	class HomeWatcherReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (reconnectionDialog != null && reconnectionDialog.isShowing()) {
					reconnectionDialog.dismiss();
				}
				if (waitDialog != null && waitDialog.isShowing()) {
					waitDialog.dismiss();
					isWait = false;
				}
			}
			
		}
		
	}

	
}
