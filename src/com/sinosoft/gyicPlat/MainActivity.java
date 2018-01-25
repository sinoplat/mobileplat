package com.sinosoft.gyicPlat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.androidpn.client.NotificationService;
import org.androidpn.client.ServiceManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sangfor.ssl.IVpnDelegate;
import com.sangfor.ssl.SFException;
import com.sangfor.ssl.SangforAuth;
import com.sangfor.ssl.common.VpnCommon;
import com.sangfor.ssl.service.setting.SystemConfiguration;
import com.sinosoft.mobileshop.activity.SettingActivity;
import com.sinosoft.mobileshop.appwidget.BadgeView;
import com.sinosoft.mobileshop.appwidget.MainTab;
import com.sinosoft.mobileshop.appwidget.MyFragmentTabHost;
import com.sinosoft.mobileshop.appwidget.TitleBar;
import com.sinosoft.mobileshop.appwidget.floatwindow.MyWindowManager;
import com.sinosoft.mobileshop.bean.AppMessage;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.bean.VpnBean;
import com.sinosoft.mobileshop.service.VpnWorkService;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.DateUtil;
import com.sinosoft.mobileshop.util.JsonUtil;
import com.sinosoft.mobileshop.util.LiteOrmUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.mobileshop.util.VpnConfigUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;
import com.sinosoft.traffic.TrafficService;
import com.sinosoft.util.Utils;
import com.way.pattern.App;

public class MainActivity extends FragmentActivity implements OnClickListener,
		OnTouchListener, OnTabChangeListener,IVpnDelegate {
	
	private boolean hasVpnBtn = false;
	private boolean hasClearBtn = false;
	private boolean hasLeftText = false;

	private MyFragmentTabHost mTabHost;
	private BadgeView mBvNotice;
	private TitleBar titleBar;
	private View vpnBtn;
	private View clearBtn;
	private RollProgressbar rollBar;
	
	private int textColor = Color.parseColor("#f2f2f2");
	private int statusColor = Color.parseColor("#F4D24A");
	private int btnColor = Color.parseColor("#5CC0AA");
	
	private RollProgressbar rollProgressbar;
	
	private boolean isQuit = false;
	
	private int vpnStatus = -1;
	private String vpnBtnText = "连接VPN";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initvpn();
		initView();
		initData();
		registBroadcast();
		openService();
		SharedPreferences preferences = getSharedPreferences("Config", Context.MODE_PRIVATE);
		edit = preferences.edit();
		edit.putBoolean("isQuit", false);
		edit.commit();
	}

	@SuppressLint("NewApi")
	public void initView() {
		mTabHost = (MyFragmentTabHost)findViewById(android.R.id.tabhost);
		// 增加tabhost内容
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		if (android.os.Build.VERSION.SDK_INT > 10) {
			mTabHost.getTabWidget().setShowDividers(0);
		}
		initTabs();
		mTabHost.setCurrentTab(0);
		mTabHost.setOnTabChangedListener(this);
		
		setTitleBar(0);
		TextView textView0 = (TextView)mTabHost.getTabWidget().getChildTabViewAt(0).findViewById(R.id.tab_title);
		textView0.setTextColor(getResources().getColor(R.color.index_Select));
		
	}
	
	private void setTitleBar(int selectId) {
		boolean isImmersive = false;
//		if (CommonUtil.hasKitKat() && !CommonUtil.hasLollipop()) {
//			isImmersive = true;
//			// 透明状态栏
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//			// 透明导航栏
//		} else if (CommonUtil.hasLollipop()) {
//			Window window = getWindow();
//			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//			isImmersive = true;
//		}
		
		String title = "";
		if(titleBar == null) {
			titleBar = (TitleBar) findViewById(R.id.title_bar);
		}
		titleBar.setImmersive(isImmersive);
		titleBar.setBackgroundColor(Color.parseColor("#28282a"));
		
		if(!hasLeftText) {
			titleBar.setLeftImageResource(R.drawable.tip_icon);
			titleBar.setLeftText("VPN未连接");
			titleBar.setLeftTextColor(statusColor);
			hasLeftText = true;
		}
		switch (selectId) {
		case 0:
			title = getString(R.string.index_title);
			break;
		case 1:
			title = getString(R.string.manager_title);
			break;
		case 2:
			title = getString(R.string.news_title);
			break;
		case 3:
			title = getString(R.string.more_title);
			break;
		default:
			break;
		}
		
		titleBar.setTitle(title);
		titleBar.setTitleColor(textColor);
//		titleBar.setSubTitleColor(Color.WHITE);
//		titleBar.setDividerColor(Color.GRAY);

		titleBar.setActionTextColor(textColor);
//		mCollectView = (ImageView) titleBar.addAction(new TitleBar.ImageAction(R.drawable.collect) {
//			@Override
//			public void performAction(View view) {
//				Toast.makeText(MainActivity.this, "点击了收藏", Toast.LENGTH_SHORT).show();
//				mCollectView.setImageResource(R.drawable.fabu);
//				titleBar.setTitle(mIsSelected ? "文章详情\n朋友圈" : "帖子详情");
//				mIsSelected = !mIsSelected;
//			}
//		});

		if(!hasVpnBtn) {
			titleBar.setActionTextColor(btnColor);
			vpnBtn = titleBar.addAction(new TitleBar.TextAction(vpnBtnText) {
				@Override
				public void performAction(View view) {
					doExecVpn();
				}
			});
			hasVpnBtn = true;
		}
		if(!hasClearBtn) {
			titleBar.setActionTextColor(btnColor);
			clearBtn = titleBar.addAction(new TitleBar.TextAction("清 除") {
				@Override
				public void performAction(View view) {
					rollBar = CommonUtil.showDialog(MainActivity.this, "正在清除消息，请稍后", true);
					operateClearMsg();
				}
			});
			hasClearBtn = true;
		}
		
		switch (selectId) {
		case 0:
			vpnBtn.setVisibility(View.VISIBLE);
			clearBtn.setVisibility(View.GONE);
			break;
		case 1:
			vpnBtn.setVisibility(View.GONE);
			clearBtn.setVisibility(View.GONE);
			break;
		case 2:
			vpnBtn.setVisibility(View.GONE);
			clearBtn.setVisibility(View.VISIBLE);
			break;
		case 3:
			vpnBtn.setVisibility(View.GONE);
			clearBtn.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}
	
	private void initTabs() {
		MainTab[] tabs = MainTab.values();
		final int size = tabs.length;
	    for (int i = 0; i < size; i++) {
            MainTab mainTab = tabs[i];
            TabSpec tab = mTabHost.newTabSpec(getString(mainTab.getResName()));
            View indicator = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tab_indicator, null);
            TextView title = (TextView) indicator.findViewById(R.id.tab_title);
            Drawable drawable = this.getResources().getDrawable(mainTab.getResIcon());
            title.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            title.setText(getString(mainTab.getResName()));
            tab.setIndicator(indicator);
            
            tab.setContent(new TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return new View(MainActivity.this);
                }
            });
            
            // 把fragment添加到tabHost中
            mTabHost.addTab(tab, mainTab.getClz(), null);

            if (mainTab.equals(MainTab.MANAGER)) {
                View cn = indicator.findViewById(R.id.tab_mes);
                mBvNotice = new BadgeView(MainActivity.this, cn);
                mBvNotice.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                mBvNotice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                mBvNotice.setBackgroundResource(R.drawable.notification_bg);
                mBvNotice.setGravity(Gravity.CENTER);
            }
            mTabHost.getTabWidget().getChildAt(i).setOnTouchListener(this);
        }
	}

	/**
	 * 初始化数据与VPN链接
	 */
	private void initData() {
		rollProgressbar = CommonUtil.showDialog(this, "正在连接VPN", false);
		VpnConfigUtil config = new VpnConfigUtil(getApplicationContext(), handler);
		config.vpnConfigInit();
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case VpnConfigUtil.DOWN_OVER:
				VpnBean vpnBean = LiteOrmUtil.getLiteOrm(getApplicationContext()).queryById("1", VpnBean.class);
				if(vpnBean != null) {
					USER = vpnBean.getUserName();
					PASSWD = vpnBean.getPassword();
					VPN_IP = vpnBean.getAddress();
//					System.out.println(USER + "---" + PASSWD + "---" + VPN_IP);
					Log.i("syso", "handleMessage:"+USER + "---" + PASSWD + "---" + VPN_IP);
				}
				doVpn();
				break;
			case VpnConfigUtil.DOWN_EXCEPTION:
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
					CommonUtil.showToast(MainActivity.this, "网络或服务器异常，连接VPN失败，请稍后重试.");
				}
				break;
			case VpnConfigUtil.DOWN_FAIL:
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
					CommonUtil.showToast(MainActivity.this, "网络或服务器异常，连接VPN失败，请稍后重试.");
				}
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

	@Override
	public void onTabChanged(String tabId) {
		final int size = mTabHost.getTabWidget().getTabCount();
		int selectId = 0;
        for (int i = 0; i < size; i++) {
            View v = mTabHost.getTabWidget().getChildAt(i);
            TextView title = (TextView)v.findViewById(R.id.tab_title);
            if (i == mTabHost.getCurrentTab()) {
                v.setSelected(true);
                selectId = i;
                switch (i) {
				case 0:
					title.setTextColor(getResources().getColor(R.color.index_Select));
					break;
				case 1:
					title.setTextColor(getResources().getColor(R.color.manager_select));
					break;
				case 2:
					title.setTextColor(getResources().getColor(R.color.news_select));
					break;
				case 3:
					title.setTextColor(getResources().getColor(R.color.more_select));
					break;
				default:
					break;
				}
            } else {
                v.setSelected(false);
                title.setTextColor(getResources().getColor(R.color.textcolor));
            }
        }
        if (tabId.equals(getString(MainTab.MANAGER.getResName()))) {
            mBvNotice.setText("");
            mBvNotice.hide();
        }
        supportInvalidateOptionsMenu();
        setTitleBar(selectId);
	}
	
	private void operateClearMsg() {
		String url = Constant.OPERMSGURL + 
					"messageId=emptyTime&userCode=0000000000&operateType=empty";
		RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(getApplicationContext()).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					String result = "";
					public void onResponse(JSONObject response) {
						try {
							result = response.getString("ResultCode");
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if(rollBar != null) {
								rollBar.disProgressBar();
							}
							if("1".equals(result)) {
								App.set("emptyTime", DateUtil.date24ToStr(new Date()));
								LiteOrmUtil.getLiteOrm(getApplicationContext()).deleteAll(AppMessage.class);
								CommonUtil.showToast(MainActivity.this, "消息清楚成功");
								Intent intent = new Intent();  
							    intent.setAction("com.sinosoft.msg.clear");  
							    MainActivity.this.sendBroadcast(intent); 
							} else {
								CommonUtil.showToast(MainActivity.this, "网络或服务器异常，请稍后重试");
							}
						}
					};
				}, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						if(rollBar != null) {
							rollBar.disProgressBar();
						}
						CommonUtil.showToast(MainActivity.this, "网络或服务器异常，请稍后重试");
					};
				});
		mRequestQueue.add(jsonObjectRequest);
	}
	
	/**
     * 监听返回--是否退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	showDialog(MainActivity.this, "移动应用平台退出之后，其他接入应用将不能继续访问，您确定要退出吗？");
        	return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    public void showDialog(Context context, String msg){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage(msg); //设置内容
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isQuit = true;
                edit.putBoolean("isQuit", isQuit);
                edit.commit();
                MyWindowManager.closeFloat(getApplicationContext());
                // 关闭vpn
                if(SangforAuth.getInstance() != null) {
                	SangforAuth.getInstance().vpnLogout();
				}
				if(SangforAuth.getInstance() != null && (vpnStatus != 2 && vpnStatus !=5 )) {
					dialog.dismiss(); //关闭dialog
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(0); 
				}
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //参数都设置完成了，创建并显示出来
        AlertDialog dialog = builder.create();
        dialog.show();
    }
	
	private static final String TAG = "GyicPlatMobile_VPN";
	//认证所需信息
	private static String VPN_IP = "220.178.31.50"; //VPN设备地址　（也可以使用域名访问）
	private static final int VPN_PORT = 443; //vpn设备端口号，一般为443
	
	//用户名密码认证；用户名和密码
	private static String USER = "ceshi1"; 
	private static String PASSWD = "ceshi1";
	
	//证书认证；导入证书路径和证书密码（如果服务端没有设置证书认证此处可以不设置）
	private static final String CERT_PATH = "/sdcard/hml_test.p12";
	private static final String CERT_PASS = "1";
	private InetAddress m_iAddr = null;
	
	//实例化vpn
	private void initvpn() {
		try {
			com.sangfor.ssl.service.utils.logger.Log.init(getApplicationContext());
			com.sangfor.ssl.service.utils.logger.Log.LEVEL = com.sangfor.ssl.service.utils.logger.Log.DEBUG;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		SangforAuth sfAuth = SangforAuth.getInstance();
		try {
		    //SDK模式初始化，easyapp 模式或者是l3vpn模式，两种模式区别请参考文档。
//			sfAuth.init(this, this, SangforAuth.AUTH_MODULE_EASYAPP);
//			sfAuth.init(this, this, SangforAuth.AUTH_MODULE_L3VPN);
			sfAuth.init(getApplication(), this, this, SangforAuth.AUTH_MODULE_L3VPN);
			sfAuth.setLoginParam(AUTH_CONNECT_TIME_OUT, String.valueOf(2));
		} catch (SFException e) {
			e.printStackTrace();
		}
	}
	
	private void doVpn() {
		// 初始化vpn
		initSslVpn();
	}

	@SuppressLint("NewApi")
	private void doExecVpn() {
		// 初始化vpn
		// 判定VPN状态
		final SangforAuth sfAutha = SangforAuth.getInstance();
		if(!Utils.isNetConnect()) {
			displayToast("手机网络异常，请检测或重新开启");
			return;
		}
		
		if("关闭VPN".equals(vpnBtnText)) {
			AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_DARK);  //先得到构造器
	        builder.setTitle("提示"); //设置标题
	        builder.setMessage("关闭VPN后，其他APP将不能使用"); //设置内容
	        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	if(sfAutha != null) {
	    				rollProgressbar.showProgressBar("正在关闭VPN", true);
	    				sfAutha.vpnLogout();
	    			}
	            }
	        });
	        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                dialog.dismiss();
	            }
	        });
	        //参数都设置完成了，创建并显示出来
	        builder.create().show();
		}
		if("连接VPN".equals(vpnBtnText)) {
			reConnVpn();
		}
	}
	
	/**
	 * 重连VPN
	 */
	private void reConnVpn() {
		if(!Utils.isNetConnect()) {
			displayToast("手机网络异常，请检测或重新开启");
			return;
		}
		SangforAuth sfAutha = SangforAuth.getInstance();
		int doVpnStatus = vpnStatus;
		if(sfAutha != null) {
			if(doVpnStatus == -1) {
				rollProgressbar.showProgressBar("正在连接VPN", true);
				initvpn();
				doVpn();
			} else 
			if(doVpnStatus == 0 || doVpnStatus == 1 ) {
				rollProgressbar.showProgressBar("正在连接VPN", true);
				doVpn();
			} else 
			if(doVpnStatus == 2) {
				rollProgressbar.showProgressBar("正在连接VPN", true);
				doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
			} else 
			if(doVpnStatus == 3 || doVpnStatus == 4) {
				rollProgressbar.showProgressBar("正在连接VPN", true);
				doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
			}
			if(doVpnStatus == 10 || 
					doVpnStatus == 11 || doVpnStatus == 13) {
				rollProgressbar.showProgressBar("正在连接VPN", true);
				doVpn();
			}
			if(doVpnStatus == 8) {
				rollProgressbar.showProgressBar("正在连接VPN", true);
				doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
			}
			if(doVpnStatus == 9) {
				rollProgressbar.showProgressBar("正在连接VPN", true);
//				doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
				displayToast("网络异常查询VPN状态错误，请检测网络后手工连接VPN");
				doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopRefreshVpnStatus();
		unregisterReceiver(broadcastReceiverQuit);
		unregisterReceiver(broadcastReceiverReconnect);
	}

//	@Override
//	public void onClick(View v) {
//		String tempString = "no content";
//		switch (v.getId()) {
//			case R.id.btn_login:
//				initSslVpn();
//				break;
//			case R.id.btn_logout:
//				SangforAuth.getInstance().vpnLogout();
//				break;
//			case R.id.btn_cancel:
//				SangforAuth.getInstance().vpnCancelLogin();
//				break;
//			case R.id.btn_sms:
//				doVpnLogin(IVpnDelegate.AUTH_TYPE_SMS);
//				break;
//			case R.id.btn_reget_sms:
//				doVpnLogin(IVpnDelegate.AUTH_TYPE_SMS1);
//				break;
//			case R.id.imgbtn_rnd_code:
//				break;
//
//			case R.id.btn_test_http:
//				loadPage();
//				break;
//			default:
//				Log.w(TAG, "onClick no process");
//		}
//	}

	
	/**
	 * 开始初始化VPN，该初始化为异步接口，后续动作通过回调函数vpncallback通知结果
	 * 
	 * @return 成功返回true，失败返回false，一般情况下返回true
	 */
	private boolean initSslVpn() {
		SangforAuth sfAuth = SangforAuth.getInstance();
		int vpnQueryStatus = sfAuth.vpnQueryStatus();
		if(vpnQueryStatus == 5){
			if(rollProgressbar != null && rollProgressbar.ISSHOW){
				rollProgressbar.disProgressBar();
			}
			return true;
		}
		m_iAddr = null;
		final String ip = VPN_IP;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					m_iAddr = InetAddress.getByName(ip);
					Log.i(TAG, "ip Addr is : " + m_iAddr.getHostAddress());
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (m_iAddr == null || m_iAddr.getHostAddress() == null) {
			Log.d(TAG, "vpn host error");
			return false;
		}
		long host = VpnCommon.ipToLong(m_iAddr.getHostAddress());
		int port = VPN_PORT;

		if (sfAuth.vpnInit(host, port) == false) {
			Log.d(TAG, "vpn init fail, errno is " + sfAuth.vpnGeterr());
			return false;
		}

		return true;
	}

	/**
	 * 处理认证，通过传入认证类型（需要的话可以改变该接口传入一个hashmap的参数用户传入认证参数）.
	 * 也可以一次性把认证参数设入，这样就如果认证参数全满足的话就可以一次性认证通过，可见下面屏蔽代码
	 * 
	 * @param authType 认证类型
	 * @throws SFException
	 */
	private void doVpnLogin(int authType) {
		Log.d(TAG, "doVpnLogin authType " + authType);

		boolean ret = false;
		SangforAuth sfAuth = SangforAuth.getInstance();

		switch (authType) {
			case IVpnDelegate.AUTH_TYPE_CERTIFICATE:
				String certPasswd = PASSWD;
				String certName = USER;
				sfAuth.setLoginParam(IVpnDelegate.CERT_PASSWORD, certPasswd);
				sfAuth.setLoginParam(IVpnDelegate.CERT_P12_FILE_NAME, certName);
				ret = sfAuth.vpnLogin(IVpnDelegate.AUTH_TYPE_CERTIFICATE);
				break;
			case IVpnDelegate.AUTH_TYPE_PASSWORD:
				String user = USER;
				String passwd = PASSWD;
				String rndcode = "";
				sfAuth.setLoginParam(IVpnDelegate.PASSWORD_AUTH_USERNAME, user);
				sfAuth.setLoginParam(IVpnDelegate.PASSWORD_AUTH_PASSWORD, passwd);
				sfAuth.setLoginParam(IVpnDelegate.SET_RND_CODE_STR, rndcode);
				ret = sfAuth.vpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
				break;
			case IVpnDelegate.AUTH_TYPE_SMS:
				String smsCode = "";
				sfAuth.setLoginParam(IVpnDelegate.SMS_AUTH_CODE, smsCode);
				ret = sfAuth.vpnLogin(IVpnDelegate.AUTH_TYPE_SMS);
				break;
			case IVpnDelegate.AUTH_TYPE_SMS1:
				ret = sfAuth.vpnLogin(IVpnDelegate.AUTH_TYPE_SMS1);
				break;
			default:
				Log.w(TAG, "default authType " + authType);
				break;
		}

		if (ret == true) {
			Log.i(TAG, "success to call login method");
		} else {
			Log.i(TAG, "fail to call login method");
		}

	}

	/*
	 * l3vpn模式必须重写这个函数
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SangforAuth.getInstance().onActivityResult(requestCode, resultCode);
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void displayToast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_LONG).show();
	}

	/*
	 *VPN 初始化和认证过程的回调结果通知
	 * @see com.sangfor.ssl.IVpnDelegate#vpnCallback(int, int)
	 */
	@Override
	public void vpnCallback(int vpnResult, int authType) {
		SangforAuth sfAuth = SangforAuth.getInstance();
		switch (vpnResult) {
			case IVpnDelegate.RESULT_VPN_INIT_FAIL:
				/**
				 * 初始化vpn失败
				 */
				Log.i(TAG, "RESULT_VPN_INIT_FAIL, error is " + sfAuth.vpnGeterr());
//				displayToast("RESULT_VPN_INIT_FAIL, error is " + sfAuth.vpnGeterr());
				displayToast("VPN初始化异常" + sfAuth.vpnGeterr());
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
				}
				break;

			case IVpnDelegate.RESULT_VPN_INIT_SUCCESS:
				/**
				 * 初始化vpn成功，接下来就需要开始认证工作了
				 */
				Log.i(TAG,"RESULT_VPN_INIT_SUCCESS, current vpn status is " + sfAuth.vpnQueryStatus());
//				displayToast("RESULT_VPN_INIT_SUCCESS, current vpn status is "+ sfAuth.vpnQueryStatus());
				displayToast("VPN初始化成功");
				Log.i(TAG, "vpnResult===================" + vpnResult  + "\nauthType ==================" + authType);
			
				// 初始化成功，进行认证操作　（此处采用“用户名密码”认证）
				doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
				break;

			case IVpnDelegate.RESULT_VPN_AUTH_FAIL:
				/**
				 * 认证失败，有可能是传入参数有误，具体信息可通过sfAuth.vpnGeterr()获取
				 */
				String errString = sfAuth.vpnGeterr();
//				Log.i(TAG, "RESULT_VPN_AUTH_FAIL, error is " + sfAuth.vpnGeterr());
//				displayToast("RESULT_VPN_AUTH_FAIL, error is " + sfAuth.vpnGeterr());
				displayToast("VPN认证失败，错误原因:" + errString);
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
				}
				break;

			case IVpnDelegate.RESULT_VPN_AUTH_SUCCESS:
				/**
				 * 认证成功，认证成功有两种情况，一种是认证通过，可以使用sslvpn功能了，另一种是
				 * 前一个认证（如：用户名密码认证）通过，但需要继续认证（如：需要继续证书认证）
				 */
				if (authType == IVpnDelegate.AUTH_TYPE_NONE) {
					Log.i(TAG, "welcom to sangfor sslvpn!");
					displayToast("VPN登录成功");
					if(titleBar != null) {
						vpnBtnText = "关闭VPN";
						titleBar.setLeftText("VPN已连接");
						((TextView)vpnBtn).setText(vpnBtnText);
						Constant.VPNSTATUS = 5;
						if(Constant.firstLoad) {
							Constant.firstLoad = false;
							doPostApps();
						}
					}
                    // 若为L3vpn流程，认证成功后会自动开启l3vpn服务，需等l3vpn服务开启完成后再访问资源
                    if (SangforAuth.getInstance().getModuleUsed() == SangforAuth.AUTH_MODULE_EASYAPP) {
                        // EasyApp流程，认证流程结束，可访问资源。
                        doResourceRequest();
                    }
                    if(rollProgressbar != null) {
    					rollProgressbar.disProgressBar();
    				}
				} else if (authType == IVpnDelegate.VPN_TUNNEL_OK) {
                    //l3vpn流程，l3vpn服务通道建立成功，可访问资源
				    doResourceRequest();
                } else {
					Log.i(TAG, "auth success, and need next auth, next auth type is " + authType);
					displayToast("auth success, and need next auth, next auth type is " + authType);
					if (authType == IVpnDelegate.AUTH_TYPE_SMS) {
						// 输入短信验证码
						Toast.makeText(this, "you need send sms code.", Toast.LENGTH_LONG).show();
					} else {
						doVpnLogin(authType);
					}
				}
				break;
			case IVpnDelegate.RESULT_VPN_AUTH_CANCEL:
				Log.i(TAG, "RESULT_VPN_AUTH_CANCEL");
				displayToast("RESULT_VPN_AUTH_CANCEL");
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
				}
				break;
			case IVpnDelegate.RESULT_VPN_AUTH_LOGOUT:
				/**
				 * 主动注销（自己主动调用logout接口）
				 */
				Log.i(TAG, "RESULT_VPN_AUTH_LOGOUT");
//				displayToast("RESULT_VPN_AUTH_LOGOUT");
				displayToast("VPN注销成功");
				if(isQuit) {
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(0);
				}
				String setQuit = App.get("set_quit", "0");
				if("1".equals(setQuit)) {
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(0);
				}
				
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
				}
				break;
			case IVpnDelegate.RESULT_VPN_L3VPN_FAIL:
				/**
				 * L3vpn启动失败，有可能是没有l3vpn资源，具体信息可通过sfAuth.vpnGeterr()获取
				 */
				Log.i(TAG, "RESULT_VPN_L3VPN_FAIL, error is " + sfAuth.vpnGeterr());
				displayToast("RESULT_VPN_L3VPN_FAIL, error is " + sfAuth.vpnGeterr());
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
				}
				break;
			case IVpnDelegate.RESULT_VPN_L3VPN_SUCCESS:
				/**
				 * L3vpn启动成功
				 */
				registerNetWorkBroadcasts(); //注册网络监听广播
				Log.i(TAG, "RESULT_VPN_L3VPN_SUCCESS ===== " + SystemConfiguration.getInstance().getSessionId() );
				displayToast("RESULT_VPN_L3VPN_SUCCESS");
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
				}
				break;
			case IVpnDelegate.VPN_STATUS_ONLINE:
				/**
				 * 与设备连接建立
				 */
				Log.i(TAG, "online");
				displayToast("online");
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
				}
				break;
			case IVpnDelegate.VPN_STATUS_OFFLINE:
				/**
				 * 与设备连接断开
				 */
				Log.i(TAG, "offline");
				displayToast("offline");
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
				}
				break;
			case IVpnDelegate.VPN_STATUS_TIME_OUT:
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
				}
				
			default:
				/**
				 * 其它情况，不会发生，如果到该分支说明代码逻辑有误
				 */
				Log.i(TAG, "default result, vpn result is " + vpnResult);
				displayToast("default result, vpn result is " + vpnResult);
				
				if(rollProgressbar != null) {
					rollProgressbar.disProgressBar();
				}
				break;
		}

	}
	
	/**我们EasyConnect有网络检测功能，当网络断开的时候直接会logout，然后网络连上的时候重新有login
	 * 但是使用sdk的应用不能保证这种机制，长时间断网的时候，如果服务端发生session切换的操作，会导致
	 * 客户端用了一个老的session，去和服务端握手，导致网络联通后仍然不能访问内网资源
	 * 
	 * 在此函数里面，请检测下网络是否联通，如果联通请调用一下logout，然后再重新进行一次login
	 */
	private void processL3vpnFatalErr() {
	}

    private void doResourceRequest() {
        // 认证结束，可访问资源。
    }

	/**
	 * 认证过程若需要图形校验码，则回调通告图形校验码位图，
	 * 
	 * @param data
	 *            图形校验码位图
	 */
	@Override
	public void vpnRndCodeCallback(byte[] data) {
		Log.d(TAG, "vpnRndCodeCallback data: " + Boolean.toString(data==null));
		if (data != null) {
			Drawable drawable = Drawable.createFromStream(new ByteArrayInputStream(data), "rand_code");
		}
	}
	
	@Override
	public void reloginCallback(int status, int result) {
		switch (status){
		
		case IVpnDelegate.VPN_START_RELOGIN:
			Log.e(TAG, "relogin callback start relogin start ...");
			break;
		case IVpnDelegate.VPN_END_RELOGIN:
			Log.e(TAG, "relogin callback end relogin ...");
			if (result == IVpnDelegate.VPN_RELOGIN_SUCCESS){
				Log.e(TAG, "relogin callback, relogin success!");
			} else {
				Log.e(TAG, "relogin callback, relogin failed");
			}
			break;
		}
		
	}

	private NetWorkBroadcastReceiver mNetWorkReceiver = null;
	/**
	 * 注册网络状态变化广播接收器
	 */
	private void registerNetWorkBroadcasts() {
		Log.d(TAG, "registerNetWorkBroadcasts.");

		// 注册网络广播接收器
		if (mNetWorkReceiver == null) {
			// 创建IntentFilter对象
			IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
			// 注册Broadcast Receiver
			mNetWorkReceiver = new NetWorkBroadcastReceiver();
			registerReceiver(mNetWorkReceiver, networkFilter);
		}
	}
	
	/**
	 * 取消注册网络状态变化广播接收器
	 */
	private void unRegisterNetWorkBroadcasts() {
		Log.d(TAG, "unRegisterBroadcasts.");
		// 取消注册Broadcast Receiver
		if (mNetWorkReceiver != null) {
			unregisterReceiver(mNetWorkReceiver);
			mNetWorkReceiver = null;
		}
	}
	
	/** 接收网络状态广播消息 **/
	private class NetWorkBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mobNetInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if ((mobNetInfo == null || !mobNetInfo.isConnected()) && (wifiInfo == null || !wifiInfo.isConnected())) {
				// 网络断开
				onEthStateChanged(false);   //再此函数里面做判断，如果网络断开做注销操作
				Log.d(TAG, "Network is disconnected.");
			} else if ((mobNetInfo != null && mobNetInfo.isConnected()) || (wifiInfo != null && wifiInfo.isConnected())) {
				// 网络恢复
				onEthStateChanged(true);  //判断正常的话，重新登陆
				Log.d(TAG, "Network is connected.");
			}
		}
	}
	
	/**
	 * 当网络发生变化时通告函数，这个地方无需处理离线情况，因为离线情况下不会注册监听网络的广播接收器
	 */
	private void onEthStateChanged(boolean connected) {
		if (connected) {
			initSslVpn();//登录
		} else {
			SangforAuth.getInstance().vpnLogout(); //注销
		}
	}

	private void registBroadcast() {
		IntentFilter quitFilter = new IntentFilter();
		quitFilter.addAction("com.sinosoft.msg.quit");//添加动态广播的Action
		registerReceiver(broadcastReceiverQuit, quitFilter);
		
		IntentFilter reconnectFilter = new IntentFilter();
		reconnectFilter.addAction("com.sinosoft.msg.vpnreconnect");//添加动态广播的Action
		registerReceiver(broadcastReceiverReconnect, reconnectFilter);
	}
	
	private void openService() {
		Intent intent = new Intent(this, VpnWorkService.class);
		startService(intent);
		
		ServiceManager serviceManager = new ServiceManager(getApplicationContext(), "0000000000");
		serviceManager.setNotificationIcon(R.drawable.desktop_icon);
		serviceManager.startService();
		Intent intent2 = new Intent(MainActivity.this, NotificationService.class);
		startService(intent2);
		
		Intent intent3 = new Intent(MainActivity.this, TrafficService.class);
		startService(intent3);
		
		startRefreshVpnStatus();
	}
	
	// 定时更细vpn状态
	private boolean run = false;
	private Timer timer = new Timer();
	
    private void startRefreshVpnStatus() {
    	run = true;
    	if(timer == null) {
    		timer = new Timer();
    	}
        timer.scheduleAtFixedRate(new MyVpnTask(), 3000, 2000);
    }
	
    private void stopRefreshVpnStatus() {
    	run = false;
    	if(timer != null) {
    		timer.cancel();
    		timer = null;
    	}
    }
    
    private Handler handlerVpn = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 100:
                updateTitle();
                break;
            }
        };
    };
        
    private class MyVpnTask extends TimerTask{
        @Override
        public void run() {
        	try {
        		vpnStatus =	Constant.VPNSTATUS; 
	            Message message = new Message();
	            message.what = 100;
	            handlerVpn.sendMessage(message);
	        } catch(Exception e) {
        		e.printStackTrace();
        		vpnStatus = -1;
        		Constant.VPNSTATUS = -1;
        	}
        }
    }
    
    private void updateTitle() {
        if(titleBar != null && vpnStatus == 5) {
        	vpnBtnText = "关闭VPN";
			titleBar.setLeftText("VPN已连接");
			((TextView)vpnBtn).setText(vpnBtnText);
		} else {
			vpnBtnText = "连接VPN";
			titleBar.setLeftText("VPN未连接");
			((TextView)vpnBtn).setText(vpnBtnText);
		}
    }
    
    //消息接收器
 	private BroadcastReceiver broadcastReceiverQuit = new BroadcastReceiver() {
 		public void onReceive(Context context, android.content.Intent intent) {
            // 关闭vpn
            if(SangforAuth.getInstance() != null) {
            	SangforAuth.getInstance().vpnLogout();
			}
			if(SangforAuth.getInstance() != null && (vpnStatus !=2 && vpnStatus !=5)) {
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
				getSharedPreferences("Config", Context.MODE_PRIVATE).edit().putBoolean("isQuit", true);
				System.exit(0); 
			}
 		};
 	};
 	
 	 //消息接收器
 	private BroadcastReceiver broadcastReceiverReconnect = new BroadcastReceiver() {
 		public void onReceive(Context context, android.content.Intent intent) {
            // 重连VPN
 			reConnVpn();
 		};
 	};
	private Editor edit;
 
	private void doPostApps() {
		String url = Constant.GETAPPLIST + 
				"jsonstr={\"UserCode\":\"0000000000\",\"OptUserCode\":\"0000000000\",\"OptPackageName\":\"\",\"OS\":\"1\"}";
		RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(getApplicationContext()).getRequestQueue();
		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,  new Response.Listener<JSONArray>(){
            private List<AppVersionInfo> appVersionList;
            private List<AppVersionInfo> appList = new ArrayList<AppVersionInfo>();

			@Override
            public void onResponse(JSONArray response) {
            	if(response != null && response.length() > 0) {
            		appVersionList = JsonUtil.jsonToBeanList(response, AppVersionInfo.class);
            		LiteOrmUtil.getLiteOrm(getApplicationContext()).deleteAll(AppVersionInfo.class);
            		LiteOrmUtil.getLiteOrm(getApplicationContext()).save(appVersionList);
            		// 检查版本
            		String gyicPlatVersion = "";
            		String gyicPlatNo = "";
            		for (AppVersionInfo appVersionInfo : appVersionList) {
            			if(Constant.GYICPACKAGE.equals(appVersionInfo.getPackageName())) {
            				gyicPlatVersion = appVersionInfo.getApplicationNewVersion();
            				gyicPlatNo = appVersionInfo.getApplicationNo();
            				break;
            			}
    				}
            		if(gyicPlatVersion != null && !"".equals(gyicPlatVersion)
            				&& !gyicPlatVersion.equals(TDevice.getVersionName())) {
            			doGetVersion(gyicPlatVersion, gyicPlatNo);
            		}
            		
            	}
			}
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                CommonUtil.showToast(MainActivity.this, "网络或服务器异常，请检查");
            }
        });
		mRequestQueue.add(jsonArrayRequest);
	}
	
	private void doGetVersion(String currentVersion, String applicationNo) {
		final RollProgressbar rollProgressbar = CommonUtil.showDialog(MainActivity.this, "移动应用平台有新版本，正在获取下载资源", true);
		String jsonStr = "jsonstr={\"UserCode\":\"0000000000\",\"OptUserCode\":\"0000000000\",\"OptPackageName\":\"\","
				+ "\"OS\":\"1\","
				+ "\"CurrentVersion\":\"" + currentVersion + "\","
				+ "\"ApplicationNo\":\"" + applicationNo + "\""
				+ "}";
		String url = Constant.GETVERSIONURL + jsonStr;
		RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(getApplicationContext()).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						try {
							String filePath = response.getString("FilePath");
							String fileName = response.getString("FileName");
							showDownload(filePath, fileName);
						} catch (JSONException e) {
							e.printStackTrace();
						} finally {
							if (rollProgressbar != null) {
								rollProgressbar.disProgressBar();
							}
						}
					};
				}, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						if (rollProgressbar != null) {
							rollProgressbar.disProgressBar();
						}
						CommonUtil.showToast(MainActivity.this, "网络或服务器异常，请检查");
					};
				});
		
		mRequestQueue.add(jsonObjectRequest);
	}
	
	/**
	 * 获取远程文件
	 * @param remoteFile
	 */
	private void showDownload(String remoteFile, String fileName) {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		String localFile = "";
		File sdDir = null;
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		if(sdDir != null) {
			localFile = sdDir.getPath() + "/" + fileName + ".apk";
		} else {
			localFile = "/" + fileName + ".apk";
		}
		Intent i = new Intent();  
		i.putExtra("remoteFile", remoteFile);  
		i.putExtra("localFile", localFile); 
		i.setClass(MainActivity.this,com.sinosoft.phoneGapPlugins.download.MainActivity.class);
		startActivity(i);
		
		App.set("localFile", localFile);
	}
 	
}
