package com.sinosoft.vpn;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import topsec.sslvpn.svsdklib.SVSDKLib;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.sangfor.ssl.IVpnDelegate;
import com.sangfor.ssl.SFException;
import com.sangfor.ssl.SangforAuth;
import com.sangfor.ssl.common.VpnCommon;
import com.sinosoft.bean.VPNAddressBean;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.phoneGapPlugins.pgsqliteplugin.DatabaseHelper;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.phoneGapPlugins.util.VpnAddressIp;
import com.sinosoft.progressdialog.RollProgressbar;
import com.sinosoft.traffic.TrafficHttp;

/**
 * VPN初始化连接插件类
 */
public class VPN extends CordovaPlugin implements IVpnDelegate {

	// 获取远程用户名、密码消息通知
	private static final int DOWN_OVER = 2;
	// 向html返回地址的消息通知
	private static final int VPN_ADDRESS = 1;
	private SharedPreferences sp;
	private Context context;
	private int flag;
	private static final String TAG = "VPN";
	private VPNAddress vpnaddress;
	private VPNAddressBean bean;
	public Handler vpnHandler;
	private RollProgressbar rollProgressbar;
	
	// -----深信服start-----
	private InetAddress mAddr = null;
	private String address;
	private String userName;
	private String password;
	// -----深信服end-----

	// -----天融信start-----
	private static final int VPN_MSG_STATUS_UPDATE = 100; // 天融信VPN状态通知消息号
	private String AddressFTP;// 天融信Ftp地址
	private String AddressNETWORK;// 天融信内网地址
	private String AddressXMPP;// 天融信XMPP地址
	private HashMap<String, String> map;// 天融信获取地址集合
	// -----天融信end-----
	
	// 登录成功标识
	boolean isLoginSuccess = false;
	// 点击退出标识
	private boolean quitFlag = false;
	// 显示标识
	private boolean showFlag = false;
	// 退出次数
	private int quitCount = 0;
	// 重连
	private boolean tryLink = false;
	// 认证失败次数
	private int authFailCount = 0;
	
	// 定时器
	Timer mTimer;
	
	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		context = cordova.getActivity().getApplicationContext();
		sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//		context.registerReceiver(NetWorksBroadCast, intentFilter);
		
		// 初始化Log
		try {
			com.sangfor.ssl.service.utils.logger.Log.init(context);
			com.sangfor.ssl.service.utils.logger.Log.LEVEL = com.sangfor.ssl.service.utils.logger.Log.DEBUG;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		flag = sp.getInt("VPNFlag", 1);
		// 初始化连接
		if (action.equals("login")) {
//			if (flag == 2) { // 天融信
//				cordova.getActivity().runOnUiThread(new Runnable() {
//					public void run() {
//						rollProgressbar = new RollProgressbar(cordova
//								.getActivity());
//						rollProgressbar.showProgressBar("网络初始化中，请稍候...", true);
//					}
//				});
//			}
			
			VPNConnection vpnconn = new VPNConnection(context, mHandler);
			vpnconn.vpnConnection();
			vpnaddress = new VPNAddress(context);
			bean = new VPNAddressBean();
			vpnHandler = new Handler() {
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case VPN_ADDRESS:
						if (flag == 1) {
							bean = vpnaddress.queryVPN(1);
							String address = bean.getNetwork();
							callbackContext.success(address);
						}
						if (flag == 2) {
							callbackContext.success(AddressNETWORK);
						}
						break;
					default:
						break;
					}
				};
			};
			return true;
		} else if (action.equals("logout")) { // 退出
			if (flag == 1) {
				quitFlag = true;
				tryLink = false;
				cordova.getActivity().runOnUiThread(new Runnable() {
					public void run() {
						if(SangforAuth.getInstance() != null) {
							SangforAuth.getInstance().vpnLogout();
						}
					}
				});
				if(SangforAuth.getInstance() != null && SangforAuth.getInstance().vpnQueryStatus() != 5) {
					System.out.println("----退出VPN----"+ SangforAuth.getInstance().vpnQueryStatus());
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(0); 
				}
			}
			if (flag == 2) {
				doStop();
			}
			return true;
		} else if ("conn".equals(action)) { // 重连
			boolean conn = sp.getBoolean("conn", true);
			if (conn == false) {
				if (flag == 1) {
					VPNConnection vpnconn = new VPNConnection(context, mHandler);
					vpnconn.vpnConnection();
				} else {
					InitSVSDKLib();
					doStart();
				}
				android.content.SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("conn", true);
				editor.commit();
			}
			return true;
		} else if ("VPNflag".equals(action)) { // 查询vpn状态
			SangforAuth sfAuth = SangforAuth.getInstance();
			String result = "0";
			if(sfAuth != null) {
				try {
					int status = sfAuth.vpnQueryStatus();	
					System.out.println("********VPN状态**********" + status);
					if(status == 5) {
						result = "1";
					} else {
						result = "0";
						// 之前未登录成功，则先注销，在登录
						if(!isLoginSuccess) {
							if(status == 3) {
								authFailCount ++;
							}
							
							// 初始状态
							if(status == 3) {
								authFailCount = 0;
								if(!tryLink) {
									tryLink = true; // 重连
								}
								// 直接进行登录
								cordova.getActivity().runOnUiThread(new Runnable() {
									public void run() {
										if(SangforAuth.getInstance() != null) {
											doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
										}
									}
								});
								
							} 
							
							if(status == 2 || status ==9 || status == 10 ) {
								if(!tryLink) {
									tryLink = true; // 重连
								}
								// 进行重连， 先调用退出
								cordova.getActivity().runOnUiThread(new Runnable() {
									public void run() {
										if(SangforAuth.getInstance() != null) {
											SangforAuth.getInstance().vpnLogout();
										}
									}
								});
							}
							
						} else { // 登录成功，直接访问
							cordova.getThreadPool().execute(new Runnable() {
								@Override
								public void run() {
									netReq();
								}
							});
						}
					}
				} catch(Exception e) {
					VPNConnection vpnconn = new VPNConnection(context, mHandler);
					vpnconn.vpnConnection();
				}
				
			}
			callbackContext.success(result);
			return true;
		}
		return false;
	}

	/**
	 * 处理连接VPN消息
	 */
	public Handler mHandler = new Handler() {
		private Boolean b;
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_OVER:
				if (flag == 1) { // 深信服
					b = getVPNInfo(1);
				}
				if (flag == 2) { // 天融信
					b = getVPNInfo(2);
				}
				if (b == true) {
					cordova.getActivity().runOnUiThread(new Runnable() {
						public void run() {
							if (flag == 1) {// 1：深信服初始化。
								SangforAuth sfAuth = SangforAuth.getInstance();
								try {
									// sfAuth.init(this, this,
									// SangforAuth.AUTH_MODULE_EASYAPP);
									// sfAuth.init(context, VPN.this);
									// sfAuth.setLoginParam(AUTH_CONNECT_TIME_OUT, String.valueOf(5));
									// sfAuth.init(this, this, SangforAuth.AUTH_MODULE_L3VPN);
									// sfAuth.init(context, VPN.this, SangforAuth.AUTH_MODULE_EASYAPP);
//									sfAuth.init(cordova.getActivity(), VPN.this, SangforAuth.AUTH_MODULE_L3VPN);
//									sfAuth.init(cordova.getActivity(), VPN.this, SangforAuth.AUTH_MODULE_L3VPN);
//									sfAuth.init(cordova.getActivity(), VPN.this, SangforAuth.AUTH_MODULE_EASYAPP);
									System.out.println("初始化状态:" + sfAuth.vpnQueryStatus());
									Log.i("sysy","初始化状态:" + sfAuth.vpnQueryStatus());
									sfAuth.setLoginParam(AUTH_CONNECT_TIME_OUT, String.valueOf(5));
								} catch (Exception e) {
									e.printStackTrace();
								}
								if (address != null && userName != null && password != null) {
									// 开始初始化VPN
									if (initSslVpn() == false) {
										Log.e(TAG, "init ssl vpn fail.");
									}
									//doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
								}
								vpnHandler.sendEmptyMessage(VPN_ADDRESS);
							}
							if (flag == 2) {// 2：天融信
								InitSVSDKLib();// 天融信初始化
								doStart();
							}
						}
					});
				}
				break;
			default:
				break;
			}
		};
	};

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

	@Override
	public void vpnCallback(int vpnResult, int authType) {
		SangforAuth sfAuth = SangforAuth.getInstance();

		switch (vpnResult) {
			case IVpnDelegate.RESULT_VPN_INIT_FAIL:
				/**
				 * 初始化vpn失败
				 */
				if(sfAuth != null) {
					Log.i(TAG, "RESULT_VPN_INIT_FAIL, error is " + sfAuth.vpnGeterr());
				}
				break;

			case IVpnDelegate.RESULT_VPN_INIT_SUCCESS:
				/**
				 * 初始化vpn成功，接下来就需要开始认证工作了
				 */
				if(sfAuth != null) {
					Log.i(TAG,
							"RESULT_VPN_INIT_SUCCESS, current vpn status is " + sfAuth.vpnQueryStatus());
					Log.i(TAG, "vpnResult===================" + vpnResult  + "\nauthType ==================" + authType);
				}
				
				// 初始化成功，进行认证操作
				doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
//				doVpnLogin(IVpnDelegate.AUTH_TYPE_CERTIFICATE)
//				doVpnLogin(authType);
//				String hardidString = sfAuth.vpnQueryHardID();
//				Log.w(TAG, "vpn hardid ============================ " + hardidString);
				break;

			case IVpnDelegate.RESULT_VPN_AUTH_FAIL:
				/**
				 * 认证失败，有可能是传入参数有误，具体信息可通过sfAuth.vpnGeterr()获取
				 */
				if(sfAuth != null) {
					String errString = sfAuth.vpnGeterr();
					Log.i(TAG, "RESULT_VPN_AUTH_FAIL, error is " + sfAuth.vpnGeterr());
				}
				break;

			case IVpnDelegate.RESULT_VPN_AUTH_SUCCESS:
				/**
				 * 认证成功，认证成功有两种情况，一种是认证通过，可以使用sslvpn功能了，另一种是前一个认证（如：用户名密码认证）通过，
				 * 但需要继续认证（如：需要继续证书认证）
				 */
				if (authType == IVpnDelegate.AUTH_TYPE_NONE) {
					Log.i(TAG, "welcom to sangfor sslvpn!");
					Toast.makeText(context, "恭喜您，登录VPN成功", Toast.LENGTH_LONG).show();
                    // 若为L3vpn流程，认证成功后开启自动开启l3vpn服务
                    if (SangforAuth.getInstance() != null && 
                    		SangforAuth.getInstance().getModuleUsed() == SangforAuth.AUTH_MODULE_EASYAPP) {
                        // EasyApp流程，认证流程结束，可访问资源。
                        //doResourceRequest();
                    }

				} else {
					Log.i(TAG, "auth success, and need next auth, next auth type is " + authType);
					//displayToast("auth success, and need next auth, next auth type is " + authType);

					if (authType == IVpnDelegate.AUTH_TYPE_SMS) {
						// 输入短信验证码
					} else {
						doVpnLogin(authType);
					}
				}
				isLoginSuccess = true;
				// 认证成功，开启检测VPN状态
				mTimer = new Timer();
				setTimeTask();
				break;
			case IVpnDelegate.RESULT_VPN_AUTH_CANCEL:
				Toast.makeText(context, "初始化网络失败，请重启移动应用平台或联系管理员", Toast.LENGTH_LONG).show();
				Log.i(TAG, "RESULT_VPN_AUTH_CANCEL");
				break;
			case IVpnDelegate.RESULT_VPN_AUTH_LOGOUT:
				//quitCount++;
				/**
				 * 主动注销（自己主动调用logout接口）或者被动注销（通过控制台把用户踢掉）均会调用该接口
				 */
				//tunObserver.stopWatching();
				if(!quitFlag && !showFlag) {
//					quitCount = 0;
//					showFlag = true;
//					new AlertDialog.Builder(cordova.getActivity()).setTitle("提示").setMessage("VPN已断开，是否进行重新连接？")
//					.setPositiveButton("确定", new OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							cordova.getActivity().runOnUiThread(new Runnable() {
//								public void run() {
//									initSslVpn();
//									showFlag = false;
//								}
//							});
//						}
//					}).setNegativeButton("取消", new OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							showFlag = false;
//							exit();
//						}
//					}).show();
				}
				Log.i(TAG, "RESULT_VPN_AUTH_LOGOUT");
				// 退出回调
				if(quitFlag) {
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(0); 
				}
				
				// 重连
				if(!quitFlag && tryLink) {
					cordova.getActivity().runOnUiThread(new Runnable() {
						public void run() {
							initSslVpn();
						}
					});
					tryLink = false;
				}
				
				break;
			case IVpnDelegate.RESULT_VPN_L3VPN_FAIL:
				/**
				 * L3vpn启动失败，有可能是没有l3vpn资源，具体信息可通过sfAuth.vpnGeterr()获取
				 */
				Toast.makeText(context, "初始化网络失败，请重启移动应用平台或联系管理员", Toast.LENGTH_LONG).show();
				if(sfAuth != null) {
					Log.i(TAG, "RESULT_VPN_L3VPN_FAIL, error is " + sfAuth.vpnGeterr());
				}
				break;
			case IVpnDelegate.RESULT_VPN_L3VPN_SUCCESS:
				/**
				 * L3vpn启动成功
				 */
//				registerNetWorkBroadcasts(); //注册网络监听广播
//				if (tunObserver == null) {
//					tunObserver = new TunObserver();
//				}
				Log.i(TAG, "start tun0 observe");
//				tunObserver.startWatching();
//				Log.i(TAG, "RESULT_VPN_L3VPN_SUCCESS ===== " + SystemConfiguration.getInstance().getSessionId() );
                // L3vpn流程，认证流程结束，可访问资源。
//                doResourceRequest();
				break;
			case IVpnDelegate.VPN_STATUS_ONLINE:
				/**
				 * 与设备连接建立
				 */
				Log.i(TAG, "online");
				break;
			case IVpnDelegate.VPN_STATUS_OFFLINE:
				/**
				 * 与设备连接断开
				 */
				Log.i(TAG, "offline");
				Toast.makeText(context, "初始化网络失败，请重启移动应用平台或联系管理员", Toast.LENGTH_LONG).show();
				break;
			default:
				/**
				 * 其它情况，不会发生，如果到该分支说明代码逻辑有误
				 */
				Log.i(TAG, "default result, vpn result is " + vpnResult);
				break;
		}
	}

	@Override
	public void vpnRndCodeCallback(byte[] arg0) {
	}

	
	/**
	 * 开始初始化VPN，该初始化为异步接口，后续动作通过回调函数通知结果
	 * 
	 * @return 成功返回true，失败返回false，一般情况下返回true
	 */
	private boolean initSslVpn() {
		SangforAuth sfAuth = SangforAuth.getInstance();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mAddr = InetAddress.getByName(address);
					Log.i(TAG, "ip Addr is : " + mAddr.getHostAddress());
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

		if (mAddr == null || mAddr.getHostAddress() == null) {
			Log.d(TAG, "vpn host error");
			return false;
		}
		long host = VpnCommon.ipToLong(mAddr.getHostAddress());
		int port = 443;

		if (sfAuth != null && sfAuth.vpnInit(host, port) == false) {
			Log.d(TAG, "vpn init fail, errno is " + sfAuth.vpnGeterr());
			return false;
		}

		return true;
	}

	/**
	 * 处理认证，通过传入认证类型（需要的话可以改变该接口传入一个hashmap的参数用户传入认证参数）.
	 * 也可以一次性把认证参数设入，这样就如果认证参数全满足的话就可以一次性认证通过，可见下面屏蔽代码
	 * 
	 * @param authType
	 *            认证类型
	 */
	private void doVpnLogin(int authType) {
		Log.d(TAG, "doVpnLogin authType " + authType);

		boolean ret = false;
		SangforAuth sForward = SangforAuth.getInstance();

		switch (authType) {
		case IVpnDelegate.AUTH_TYPE_CERTIFICATE:
			sForward.setLoginParam(IVpnDelegate.CERT_PASSWORD, "123456");
			sForward.setLoginParam(IVpnDelegate.CERT_P12_FILE_NAME,
					"/sdcard/csh/csh.p12");
			ret = sForward.vpnLogin(IVpnDelegate.AUTH_TYPE_CERTIFICATE);
			break;
		case IVpnDelegate.AUTH_TYPE_PASSWORD:
			// 填写具体的VPN认证的用户名和密码
			sForward.setLoginParam(IVpnDelegate.PASSWORD_AUTH_USERNAME,
					userName);
			sForward.setLoginParam(IVpnDelegate.PASSWORD_AUTH_PASSWORD,
					password);
			ret = sForward.vpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
			break;
		case IVpnDelegate.AUTH_TYPE_SMS:
//			String smsCode = edt_sms.getText().toString();
//			sfAuth.setLoginParam(IVpnDelegate.SMS_AUTH_CODE, smsCode);
//			ret = sfAuth.vpnLogin(IVpnDelegate.AUTH_TYPE_SMS);
			break;
		case IVpnDelegate.AUTH_TYPE_SMS1:
//			ret = sfAuth.vpnLogin(IVpnDelegate.AUTH_TYPE_SMS1);
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
	
	

	/**
	 * 查询VPN数据 1：深信服 2：天融信
	 * 
	 * @param id
	 * @return
	 */
	public boolean getVPNInfo(int id) {
		DatabaseHelper dbOpenHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor c = db.query("VPNConnect", new String[] { "address", "userName",
				"password" }, "id=?", new String[] { id + "" }, null, null,
				null);
		boolean i = false;
		if (c.moveToFirst()) {
			address = c.getString(c.getColumnIndex("address"));
			userName = c.getString(c.getColumnIndex("userName"));
			password = c.getString(c.getColumnIndex("password"));
			i = true;
		}
		c.close();
		db.close();
		return i;
	}

	// 天融信VPN
	// 通过vpnlib.setMsgHandler(MsgHandler, VPN_MSG_STATUS_UPDATE)传给SDK,
	// VPN SDK通过此handler和MSG ID通知VPN隧道状态信息。
	// 请参考此handler中如何解析VPN隧道状态信息
	public Handler mmHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case VPN_MSG_STATUS_UPDATE: {
				Bundle bundle = (Bundle) msg.obj;
				if (null == bundle) {
					Log.e(TAG, "Error***VPN handler, bundle is null");
					return;
				}

				String vpnStatus = bundle.getString("vpnstatus");
				String vpnErr = bundle.getString("vpnerror");

				if (vpnStatus.equalsIgnoreCase("6")) {// VPN隧道建立成功

					Log.i(TAG, "VPN库消息通知：VPN隧道建立成功");
//					Toast.makeText(context, "VPN库消息通知：VPN隧道建立成功",Toast.LENGTH_SHORT).show();
					rollProgressbar.disProgressBar();
					doGetVPNStatus();
					showResource();

				}

				if (vpnStatus.equalsIgnoreCase("200")) { // VPN隧道超时

					Log.i(TAG, "VPN库消息通知：VPN隧道超时");
//					Toast.makeText(context, "VPN库消息通知：VPN隧道超时",
//							Toast.LENGTH_SHORT).show();
				}

				if (vpnStatus.equalsIgnoreCase("255")) { // VPN隧道已关闭
					Log.i(TAG, "VPN库消息通知：VPN隧道已关闭");
//					Toast.makeText(context, "VPN库消息通知：VPN隧道已关闭",
//							Toast.LENGTH_SHORT).show();
				}

				if (!vpnErr.equalsIgnoreCase("0")) {

					if (vpnErr.equalsIgnoreCase("10")) {
						Log.i(TAG,
								"VPN库消息通知：VPN需要重新登陆，可提示用户进行选择是否踢出上一个用户，现在是强行踢出上一个用户");
						SVSDKLib vpnlib = SVSDKLib.getInstance();
						vpnlib.reLoginVPN();

					} else {
						int errnum = Integer.parseInt(vpnErr);
						int stringId = 0;
						if (errnum < 17) {
							stringId = R.string.vpn_error_40000 + errnum;
						} else if (errnum < 88 && errnum > 17) {
							stringId = R.string.vpn_error_40000 + errnum - 1;
						} else if (errnum < 96 && errnum > 89) {
							stringId = R.string.vpn_error_40000 + errnum - 3;
						} else if (errnum < 118 && errnum > 105) {
							stringId = R.string.vpn_error_40000 + errnum - 13;
						} else if (errnum < 124 && errnum > 118) {
							stringId = R.string.vpn_error_40000 + errnum - 14;
						} else if (errnum == 125) {
							stringId = R.string.vpn_error_40000 + errnum - 15;
						} else if (errnum == 130) {
							stringId = R.string.vpn_error_40000 + errnum - 19;
						} else if (errnum == 133) {
							stringId = R.string.vpn_error_40000 + errnum - 21;
						} else if (errnum < 138 && errnum > 134) {
							stringId = R.string.vpn_error_40000 + errnum - 22;
						} else if (errnum == 255) {
							stringId = R.string.vpn_error_40000 + errnum - 139;
						} else {
							stringId = 0;
						}

						if (stringId == 0) {
//							Toast.makeText(
//									context,
//									"VPN库消息通知：错误码为"
//											+ (40000 + Integer.parseInt(vpnErr)),
//									Toast.LENGTH_SHORT).show();
						} else {
//							Toast.makeText(context,
//									"VPN库消息通知：" + context.getString(stringId),
//									Toast.LENGTH_SHORT).show();
						}

					}
				}

			}
				break;
			}
		}
	};

	// 天融信初始化VPN库
	private void InitSVSDKLib() {

		Log.i(TAG, "InitSVSDKLib called");

		// 获取VPN库实例
		SVSDKLib vpnlib = SVSDKLib.getInstance();

		// 设置VPN隧道客户端程序的释放目录。应用必须有权限rwx此目录，如 appContext.getFilesDir().getPath()
		Context appContext = context;
		vpnlib.setSVClientPath(appContext.getFilesDir().getPath());

		// 设置应用程序的资产管理器
		vpnlib.setAssetManager(context.getAssets());

		// 设置消息处理器和MsgID。VPN库通过此handler和MsgID向调用者发送VPN隧道状态信息。
		vpnlib.setMsgHandler(mmHandler, VPN_MSG_STATUS_UPDATE);

		// 设置VPN连接信息
		// 参数1 String VPN网关地址
		// 参数2 int VPN网关端口
		// 参数3 String 登陆VPN网关的用户名
		// 参数4 String 登陆VPN网关的口令
		vpnlib.setVPNInfo("192.168.95.84", 443, "1", "111111");

		// 设置VPN客户端做连接前的准备
		vpnlib.prepareVPNSettings();
	}

	// 天融信启动VPN连接
	private void doStart() {
		Log.i(TAG, "doStart VPN");
		SVSDKLib vpnlib = SVSDKLib.getInstance();
		int port;
		String sIP = address;
		String sUname = userName;
		String sUpwd = password;
		port = Integer.parseInt("443");

		Log.i(TAG, "ip= " + sIP + " port= " + port + " uname= " + sUname);

		// 设置VPN连接信息
		vpnlib.setVPNInfo(sIP, port, sUname, sUpwd);

		// 设置VPN客户端做连接前的准备
		vpnlib.prepareVPNSettings();

		// 关闭VPN库连接
		vpnlib.stopVPN();

		// 启动VPN连接
		vpnlib.startVPN();

	}

	// 天融信关闭VPN连接
	private void doStop() {
		SVSDKLib vpnlib = SVSDKLib.getInstance();
		vpnlib.stopVPN();
	}

	// 天融信获取VPN状态
	private void doGetVPNStatus() {
		SVSDKLib vpnlib = SVSDKLib.getInstance();
		String sVPNStatus = vpnlib.getVPNStatus();
//		Toast.makeText(context, "当前VPN状态为：" + sVPNStatus, Toast.LENGTH_SHORT)
//				.show();
	}

	// 天融信获取资源信息：IP, Port, 本地端口
	private void showResource() {
		SVSDKLib vpnlib = SVSDKLib.getInstance();
		ArrayList<HashMap<String, String>> list = vpnlib.getResList();
		if (list == null) {
			// 此时没有已建立的隧道
		} else {
			VPNAddressBean bean = new VPNAddressBean();
			// 此时有已建立的隧道，且资源信息为
			Log.d(TAG, "resList=" + list);
			System.out.println(list.toString());
			for (int i = 0; i < list.size(); i++) {
				map = list.get(i);
				String port = map.get("resport");
				if (port.equals(Constant.FTPPORT)) {
					AddressFTP = "127.0.0.1:" + map.get("reslocalport");
					System.out.println("AddressFTP:" + AddressFTP);
				} else if (port.equals(Constant.NETPORT)) {
					AddressNETWORK = "127.0.0.1:" + map.get("reslocalport");
					System.out.println("AddressNETWORK:" + AddressNETWORK);
				} else if (port.equals(Constant.XMPPPPORT)) {
					AddressXMPP = "127.0.0.1:" + map.get("reslocalport");
					System.out.println("AddressXMPP:" + AddressXMPP);
				}
				vpnHandler.sendEmptyMessage(VPN_ADDRESS);
			}
			bean.setId(2);
			bean.setXmpp(AddressXMPP);
			bean.setFtp(AddressFTP);
			bean.setNetwork(AddressNETWORK);
			VPNAddress vpnaddress = new VPNAddress(context);
			vpnaddress.delete(2);
			vpnaddress.intert(bean);
		}
	}

	// 天融信获取证书CN
	private void doGetCertCN() {

		SVSDKLib vpnlib = SVSDKLib.getInstance();
		String certCN = vpnlib.getCertCN();
		Toast.makeText(context, "当前证书CN为：" + certCN, Toast.LENGTH_SHORT).show();
	}
	
	// ---------------vpn状态检测---------------
	// 定时器，检测vpn的状态
	// =============================VPN状态码 ===================================
	/** 未启动 **/
//	public static final int VPN_STATUS_UNSTART = 0;
//	/** 正在初始化 **/
//	public static final int VPN_STATUS_INITING = 1;
//	/** 初始化完成 **/
//	public static final int VPN_STATUS_INIT_OK = 2;
//	/** 正在进行认证 **/
//	public static final int VPN_STATUS_LOGINING = 3;
//	/** 正在进行自动重新认证 **/
//	public static final int VPN_STATUS_RELOGIN = 4;
//	/** 认证成功,正常运行中 **/
//	public static final int VPN_STATUS_AUTH_OK = 5;
//	/** 正在退出VPN的状态 **/
//	public static final int VPN_STATUS_EXITING = 6;
//	/** 非主线程错误 **/
//	public static final int VPN_STATUS_ERR_THREAD = 7;
//	/** 用户已经注销 **/
//	public static final int VPN_STATUS_LOGOUT = 8;
//	/** 查询VPN状态超时或网络错误 **/
//	public static final int VPN_STATUS_TIME_OUT = 9;
//	/** VPN查询时出现错误 **/
//	public static final int VPN_STATUS_QUERY_ERR = 10;
//	/** 正取消认证的状态 **/
//	public static final int VPN_STATUS_CANCELING = 11;
//
//	/** 在线离线状态 **/
//	public static final int VPN_STATUS_ONLINE = 12;
//	public static final int VPN_STATUS_OFFLINE = 13;
	private void setTimeTask() {
		System.out.println("----------------------" + mTimer); 
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					SangforAuth sfAuth = SangforAuth.getInstance();
					if(sfAuth != null) {
						int vpnStatus = sfAuth.vpnQueryStatus();
						System.out.println("=======vpn状态========" + vpnStatus);
						if(8 == vpnStatus) {
							Message msg = new Message();
							msg.what = vpnStatus;
							doActionHandler.sendMessage(msg);
						}
					}
				} catch(Exception e) {
				}
			}
		}, 5*1000, 5*1000);
	}

	private Handler doActionHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			System.out.println("mTimer:" + mTimer);
			mTimer.cancel();
			// 先注销
			if(SangforAuth.getInstance() != null) {
				SangforAuth.getInstance().vpnLogout();
			}
//			new AlertDialog.Builder(cordova.getActivity()).setTitle("提示").setMessage("VPN断开，是否进行重新连接?")
//					.setPositiveButton("确定", new OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							// 先注销
//							SangforAuth.getInstance().vpnLogout();
//							// 再登录
//							initSslVpn();
//						}
//					}).setNegativeButton("取消", new OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							exit();
//						}
//					}).show();
		};
	};
	
	/**
	 * 退出系统
	 */
	private void exit() {
		if(SangforAuth.getInstance() != null) {
			SangforAuth.getInstance().vpnLogout();
		}
		cordova.getActivity().finish();
	}
	
//	public final Handler vpnConnHandler = new Handler() {
//		@Override
//		public void handleMessage(android.os.Message msg) {
//			Bundle bundle = (Bundle) msg.getData();
//			String message = bundle.getString("messagecode");
//			if("1".equals(message) && isLoginSuccess) {
//				if(mTimer != null) {
//					mTimer.cancel();
//					mTimer = null;
//				}
//				new AlertDialog.Builder(cordova.getActivity()).setTitle("提示").setMessage("VPN断开，是否进行重新连接?")
//				.setPositiveButton("确定", new OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						VPNConnection vpnconn = new VPNConnection(context, mHandler);
//						vpnconn.vpnConnection();
//					}
//				}).setNegativeButton("取消", null).show();
//			}
//		}
//	};
//	
//	
//	public BroadcastReceiver NetWorksBroadCast = new BroadcastReceiver() {
//		State wifiState = null;
//		State mobileState = null;
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			ConnectivityManager cm = (ConnectivityManager) context
//					.getSystemService(Context.CONNECTIVITY_SERVICE);
//			wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
//			if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
//				mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
//						.getState();
//			}
//			String flag = "";
//			// 有网时启动服务
//			if ((wifiState != null && State.CONNECTED == wifiState)
//					|| (mobileState != null && State.CONNECTED == mobileState)) {
//				flag = "1";
//			}
//			if (wifiState != null && State.CONNECTED != wifiState
//					&& State.CONNECTED != mobileState) {
//				flag = "0";
//			}
//			Message msg=new Message();
//	        msg.what=1;
//	        Bundle bundle=new Bundle();
//	        bundle.putString("messagecode", flag);
//	        msg.setData(bundle);
//	        vpnConnHandler.sendMessage(msg);
//		}
//	};
	
	
	/**
	 * 网络访问，进行vpn连接
	 */
	public void netReq() {
		VpnAddressIp vpnip = new VpnAddressIp(context);
		String networkaddress = vpnip.VPNAddress();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = formatter.format(new Date());
		String uuid = "1-1-1-1-1-1-2-2-2-2-2";
		String nowtime = time;
		JSONObject obj = new JSONObject();
		try {
			obj.put("UserCode", "9999");
			obj.put("ApplicationNo", "APP20140911110645");
			obj.put("IMEI", "-------");
			obj.put("OS", "1");
			obj.put("LocateTime", nowtime);
			obj.put("Longitude", "00");
			obj.put("Latitude", "00");
			obj.put("Locate", "00");
			obj.put("RunningSpeed", "0");
			obj.put("TrafficStatistics", "0");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		TrafficHttp ht = new TrafficHttp();
		String url = "http://" + networkaddress
				+ "/meap/service/savaRealtimeMonitor.do?";
		ht.httpClient(obj, url);
	}
	
}
