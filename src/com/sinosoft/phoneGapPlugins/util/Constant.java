package com.sinosoft.phoneGapPlugins.util;

public class Constant {

	public static final String XMPPPPORT = "5222"; // 推送端口号
	public static final String APIKEY = "1234567890"; // 推送key

	public static final String XMPPHOST = "9.0.1.101";// 服务器地址
	public static final String ACTIONURL = "http://" + XMPPHOST + ":7002";// 服务器内网地址

	public static final int ftpPort = 21;// FTP端口号
	public static final String ftpUserName = "weblogic";// FTP用户名
	public static final String ftpPassword = "weblogic";// FTP密码
//										        220.178.31.50
	public static final String GETVPN = "http://220.178.31.53:8002";// 获取VPN用户名密码地址
	// 获取GPS定位地址（百度API)
	public static final String LOCATIONURL = "http://115.239.210.16/geocoder/v2/?ak=FA0d0942f513ed3a466b7b9dde0c1b6b&location=%f,%f&output=json&pois=1&coordtype=wgs84ll";
	// xmpp 地址
	public static final String XMPPURL = XMPPHOST + ":" + XMPPPPORT;
	// ftp 地址
	public static final String FTPURL = XMPPHOST + ":" + ftpPort;
	// VPN标志 1：深信服 2：天融信
	public static final int VPNFLAG = 1;
	// FTP端口
	public static final String FTPPORT = "21";
	// 内网端口
	public static final String NETPORT = "7002";
	
	public static int VPNSTATUS = -1;
	public static boolean isAlert = false;
	public static boolean firstLoad = true;
	
	public static String USERCODE = "0000000000";
	
	//用户信息储存文件路径
	public static String USERINFO_PATH = "/sdcard/gyicPlat/";
	//用户信息储存文件名称
	public static String USERINFO = "userInfo.text";
	
	// 内网 地址
	public static final String NETURL = XMPPHOST + ":" + NETPORT;

	public static final String GYICPACKAGE = "com.sinosoft.gyicPlat";
	// 获取VPN信息
	public static final String GETVPNURL = Constant.GETVPN + "/meap/service/getVPN.do";
	// 获取应用列表
	public static final String GETAPPLIST = ACTIONURL + "/meap/service/getAppliCations.do?";
	// 获取图片
	public static final String GETIMAGEURL = ACTIONURL + "/meap/service/appimage.do?";
	// 获取版本信息
	public static final String GETVERSIONURL = ACTIONURL + "/meap/service/getVersions.do?";
	// 获取版本信息
	public static final String GETMESSAGEURL = ACTIONURL + "/meap/service/getMessages.do?";
	// 操作消息
	public static final String OPERMSGURL = ACTIONURL + "/meap/service/operateMessage.do?";
	// 保存意见反馈信息
	public static final String SAVEADVICEINFOURL = ACTIONURL + "/meap/android/saveFeedBack.do?";

}
