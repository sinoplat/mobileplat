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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;

import com.sinosoft.bean.VPNAddressBean;
import com.sinosoft.vpn.VPNAddress;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;

/**
 * 管理XMPP连接在客户端与服务端之间 This class is to manage the XMPP connection between client
 * and server.
 * 
 */
public class XmppManager {

	private static final String LOGTAG = LogUtil.makeLogTag(XmppManager.class);

	private static final String XMPP_RESOURCE_NAME = "AndroidpnClient";

	private Context context;

	private NotificationService.TaskSubmitter taskSubmitter;

	private NotificationService.TaskTracker taskTracker;

	private SharedPreferences sharedPrefs;

	private String xmppHost;

	private int xmppPort;
	
	private String xmppHost1;

	private int xmppPort1;

	private XMPPConnection connection;

	private String username;

	private String password;

	private ConnectionListener connectionListener;

	private PacketListener notificationPacketListener;

	private Handler handler;

	private List<Runnable> taskList;

	private boolean running = false;

	private Future<?> futureTask;

	private Thread reconnection;

	private String userCode;
	public SharedPreferences sp;
	private int VPNflag;

	public XmppManager(NotificationService notificationService) {
		context = notificationService;
		// 获取Task提交管理器，用于维护并行任务
		taskSubmitter = notificationService.getTaskSubmitter();
		// Task计数器
		taskTracker = notificationService.getTaskTracker();
		// 配置信息
		sharedPrefs = notificationService.getSharedPreferences();
		xmppHost = sharedPrefs.getString(Constants.XMPP_HOST,xmppHost1);
		xmppPort = sharedPrefs.getInt(Constants.XMPP_PORT,xmppPort1);
		username = sharedPrefs.getString(Constants.XMPP_USERNAME, "");
		password = sharedPrefs.getString(Constants.XMPP_PASSWORD, "");
		userCode = sharedPrefs.getString("USER_CODE", "");
		// XMPP连接状态的监听器
		connectionListener = new PersistentConnectionListener(this);
		// 服务器推送监控器,服务器如果有消息推送,会自己解析,并通过XmppManager发送广播
		notificationPacketListener = new NotificationPacketListener(this);

		// 当XMPP因异常重新连接服务器时,这期间发生异常的话,会在这个handler中处理
		handler = new Handler();
		// 任务队列
		taskList = new ArrayList<Runnable>();
		/**
		 * 当xmppManager因异常与服务器断开连接时 ReconnectionThread会在一定的时间内尝试重新连接
		 * 也就是说，当PersistentConnectionListener监听器监听到异常断开连接
		 * 会调用ReconnectionThread中重新连接的方法以进行连接尝试
		 */
		reconnection = new ReconnectionThread(this);
		VPNAddress();
	}

	public Context getContext() {
		return context;
	}

	/**
	 * 连接
	 */
	public void connect() {
		Log.d(LOGTAG, "connect()...");
		submitLoginTask();
	}

	/**
	 * 中断连接
	 */
	public void disconnect() {
		Log.d(LOGTAG, "disconnect()...");
		removeAccount();
		terminatePersistentConnection();
	}

	/**
	 * 终止持久化连接
	 */
	public void terminatePersistentConnection() {
		Log.d(LOGTAG, "terminatePersistentConnection()...");
		Runnable runnable = new Runnable() {

			final XmppManager xmppManager = XmppManager.this;

			public void run() {
				if (xmppManager.isConnected()) {
					Log.d(LOGTAG, "terminatePersistentConnection()... run()");
					xmppManager.getConnection().removePacketListener(
							xmppManager.getNotificationPacketListener());
					xmppManager.getConnection().disconnect();
				}
				xmppManager.runTask();
			}

		};
		addTask(runnable);
	}

	public XMPPConnection getConnection() {
		return connection;
	}

	public void setConnection(XMPPConnection connection) {
		this.connection = connection;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ConnectionListener getConnectionListener() {
		return connectionListener;
	}

	public PacketListener getNotificationPacketListener() {
		return notificationPacketListener;
	}

	/**
	 * 登录服务端时,如果出错,则进行重新连接
	 */
	public void startReconnectionThread() {
		synchronized (reconnection) {
			if (!reconnection.isAlive()) {
				reconnection.setName("Xmpp Reconnection Thread");
				reconnection.start();
			}
		}
	}

	public Handler getHandler() {
		return handler;
	}

	/**
	 * 重新向服务器注册
	 */
	public void reregisterAccount() {
		removeAccount();
		submitLoginTask();
		runTask();
	}

	public List<Runnable> getTaskList() {
		return taskList;
	}

	public Future<?> getFutureTask() {
		return futureTask;
	}

	/**
	 * 运行任务
	 */
	public void runTask() {
		Log.d(LOGTAG, "runTask()...");
		synchronized (taskList) {
			running = false;
			futureTask = null;
			if (!taskList.isEmpty()) {
				Runnable runnable = (Runnable) taskList.get(0);
				taskList.remove(0);
				running = true;
				futureTask = taskSubmitter.submit(runnable);
				// 执行后,减少一个任务
				if (futureTask == null) {
					taskTracker.decrease();
				}
			}
		}
		taskTracker.decrease();
		Log.d(LOGTAG, "runTask()...done");
	}

	/**
	 * 生成随机UUID
	 * 
	 * @return
	 */
	private String newRandomUUID() {
		String uuidRaw = UUID.randomUUID().toString();
		return uuidRaw.replaceAll("-", "");
	}

	/**
	 * 是否已连接
	 */
	private boolean isConnected() {
		return connection != null && connection.isConnected();
	}

	/**
	 * 是否已认证
	 */
	private boolean isAuthenticated() {
		return connection != null && connection.isConnected()
				&& connection.isAuthenticated();
	}

	/**
	 * 是否已经注册(用户名\密码)
	 */
	private boolean isRegistered() {
		return sharedPrefs.contains(Constants.XMPP_USERNAME)
				&& sharedPrefs.contains(Constants.XMPP_PASSWORD);
	}

	/**
	 * 连接任务
	 */
	private void submitConnectTask() {
		Log.d(LOGTAG, "submitConnectTask()...");
		// 连接
		addTask(new ConnectTask());
	}

	/**
	 * 执行注册任务
	 */
	private void submitRegisterTask() {
		Log.d(LOGTAG, "submitRegisterTask()...");
		// 连接
		submitConnectTask();
		addTask(new RegisterTask());
	}

	/**
	 * 提交登录Task
	 */
	private void submitLoginTask() {
		Log.d(LOGTAG, "submitLoginTask()...");
		// 注册
		submitRegisterTask();
		// 登录
		addTask(new LoginTask());
	}

	/**
	 * 增加线程任务
	 * 
	 * @param runnable
	 */
	private void addTask(Runnable runnable) {
		Log.d(LOGTAG, "addTask(runnable)...");
		taskTracker.increase();
		synchronized (taskList) {
			if (taskList.isEmpty() && !running) {
				running = true;
				futureTask = taskSubmitter.submit(runnable);
				if (futureTask == null) {
					taskTracker.decrease();
				}
			} else {
				// 解决服务器重新启动后,客户端不能成功连接
				runTask();
				taskList.add(runnable);
			}
		}
		Log.d(LOGTAG, "addTask(runnable)... done");
	}

	/**
	 * 移除用户名与密码
	 */
	private void removeAccount() {
		Editor editor = sharedPrefs.edit();
		editor.remove(Constants.XMPP_USERNAME);
		editor.remove(Constants.XMPP_PASSWORD);
		editor.commit();
	}

	/**
	 * 连接服务器任务 A runnable task to connect the server.
	 */
	private class ConnectTask implements Runnable {

		final XmppManager xmppManager;

		private ConnectTask() {
			this.xmppManager = XmppManager.this;
		}

		public void run() {
			Log.i(LOGTAG, "ConnectTask.run()...");

			if (!xmppManager.isConnected()) {
				// Create the configuration for this new connection
				// 连接的配置项
				ConnectionConfiguration connConfig = new ConnectionConfiguration(
						xmppHost, xmppPort);
				// connConfig.setSecurityMode(SecurityMode.disabled);
				connConfig.setSecurityMode(SecurityMode.required);
				connConfig.setSASLAuthenticationEnabled(false);
				connConfig.setCompressionEnabled(false);

				XMPPConnection connection = new XMPPConnection(connConfig);
				xmppManager.setConnection(connection);

				try {
					// Connect to the server
					// 连接服务器
					connection.connect();
					Log.i(LOGTAG, "XMPP connected successfully");

					// packet provider
					ProviderManager.getInstance().addIQProvider("notification",
							"androidpn:iq:notification",
							new NotificationIQProvider());

				} catch (XMPPException e) {
					Log.e(LOGTAG, "XMPP connection failed", e);
				}

				xmppManager.runTask();

			} else {
				Log.i(LOGTAG, "XMPP connected already");
				xmppManager.runTask();
			}
		}
	}

	/**
	 * 在服务器上注册一个新用户 A runnable task to register a new user onto the server.
	 */
	private class RegisterTask implements Runnable {

		final XmppManager xmppManager;

		private RegisterTask() {
			xmppManager = XmppManager.this;
		}

		public void run() {
			Log.i(LOGTAG, "RegisterTask.run()...");

			// 未注册
			if (!xmppManager.isRegistered()) {

				// 用户名，加上userCode
				final String newUsername = newRandomUUID() + userCode;
				System.out.println("newUsername:" + newUsername);
				// final String newUsername = newRandomUUID();
				// 密码
				final String newPassword = newRandomUUID();

				Registration registration = new Registration();

				PacketFilter packetFilter = new AndFilter(new PacketIDFilter(
						registration.getPacketID()), new PacketTypeFilter(
						IQ.class));

				PacketListener packetListener = new PacketListener() {

					public void processPacket(Packet packet) {
						Log.d("RegisterTask.PacketListener",
								"processPacket().....");
						Log.d("RegisterTask.PacketListener",
								"packet=" + packet.toXML());

						if (packet instanceof IQ) {
							IQ response = (IQ) packet;
							if (response.getType() == IQ.Type.ERROR) {
								if (!response.getError().toString()
										.contains("409")) {
									Log.e(LOGTAG,
											"Unknown error while registering XMPP account! "
													+ response.getError()
															.getCondition());
								}
							} else if (response.getType() == IQ.Type.RESULT) {
								xmppManager.setUsername(newUsername);
								xmppManager.setPassword(newPassword);
								Log.d(LOGTAG, "username=" + newUsername);
								Log.d(LOGTAG, "password=" + newPassword);

								// 存储
								Editor editor = sharedPrefs.edit();
								editor.putString(Constants.XMPP_USERNAME,
										newUsername);
								editor.putString(Constants.XMPP_PASSWORD,
										newPassword);
								editor.commit();
								Log.i(LOGTAG, "Account registered successfully");
								xmppManager.runTask();
							}
						}
					}
				};

				connection.addPacketListener(packetListener, packetFilter);

				registration.setType(IQ.Type.SET);
				// registration.setTo(xmppHost);
				// Map<String, String> attributes = new HashMap<String,
				// String>();
				// attributes.put("username", rUsername);
				// attributes.put("password", rPassword);
				// registration.setAttributes(attributes);
				registration.addAttribute("username", newUsername);
				registration.addAttribute("password", newPassword);
				connection.sendPacket(registration);

			} else {
				Log.i(LOGTAG, "Account registered already");
				xmppManager.runTask();
			}
		}
	}

	/**
	 * 登录服务器 A runnable task to log into the server.
	 */
	private class LoginTask implements Runnable {

		final XmppManager xmppManager;

		private LoginTask() {
			this.xmppManager = XmppManager.this;
		}

		public void run() {
			Log.i(LOGTAG, "LoginTask.run()...");

			// 未认证
			if (!xmppManager.isAuthenticated()) {
				Log.d(LOGTAG, "username=" + username);
				Log.d(LOGTAG, "password=" + password);

				try {
					xmppManager.getConnection().login(
							xmppManager.getUsername(),
							xmppManager.getPassword(), XMPP_RESOURCE_NAME);
					Log.d(LOGTAG, "Loggedn in successfully");

					// connection listener
					// 连接监听
					if (xmppManager.getConnectionListener() != null) {
						xmppManager.getConnection().addConnectionListener(
								xmppManager.getConnectionListener());
					}

					// packet filter
					// 数据包过滤器
					PacketFilter packetFilter = new PacketTypeFilter(
							NotificationIQ.class);
					// packet listener
					// 数据包监听
					PacketListener packetListener = xmppManager
							.getNotificationPacketListener();
					connection.addPacketListener(packetListener, packetFilter);

					// 心跳处理
					getConnection().startKeepAliveThread(xmppManager);

				} catch (XMPPException e) {
					Log.e(LOGTAG, "LoginTask.run()... xmpp error");
					Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
							+ e.getMessage());
					String INVALID_CREDENTIALS_ERROR_CODE = "401";
					String errorMessage = e.getMessage();
					if (errorMessage != null
							&& errorMessage
									.contains(INVALID_CREDENTIALS_ERROR_CODE)) {
						xmppManager.reregisterAccount();
						return;
					}
					xmppManager.startReconnectionThread();

				} catch (Exception e) {
					Log.e(LOGTAG, "LoginTask.run()... other error");
					Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
							+ e.getMessage());
					xmppManager.startReconnectionThread();
				}

				xmppManager.runTask();
			} else {
				Log.i(LOGTAG, "Logged in already");
				xmppManager.runTask();
			}

		}
	}

	private void VPNAddress() {
		sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
		VPNflag = sp.getInt("VPNFlag", 1);
		VPNAddress vpnaddress = new VPNAddress(context);
		if (VPNflag == 1) {
			VPNAddressBean bean = vpnaddress.queryVPN(1);
			String xmppaddress = bean.getXmpp();
			String[] tras1 = xmppaddress.split(":");
			if (tras1 != null && tras1.length > 0) {
				xmppHost1 = tras1[0];
				xmppPort1 = Integer.parseInt(tras1[1]);
				System.out.println("xmppHost1:" + xmppHost1 + "xmppPort1"
						+ xmppPort1);
			}
		} else {
			VPNAddressBean bean = vpnaddress.queryVPN(2);
			String xmppaddress = bean.getXmpp();
			String[] tras1 = xmppaddress.split(":");
			if (tras1 != null && tras1.length > 0) {
				xmppHost1 = tras1[0];
				xmppPort1 = Integer.parseInt(tras1[1]);
			}
		}
	}

}