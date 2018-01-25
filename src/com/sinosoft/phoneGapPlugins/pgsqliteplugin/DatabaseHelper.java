package com.sinosoft.phoneGapPlugins.pgsqliteplugin;

import com.sinosoft.phoneGapPlugins.util.Constant;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 生成数据库和表的内部类 context 上下文（哪个类使用这个内部类） name 数据库名字 version 数据库 版本号
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private String SQL = "CREATE TABLE tableTemp(key VARCHAR(1) PRIMARY KEY)";
	private String SQL_v2 = "CREATE TABLE prpmApplicationRel(ApplicationNo VARCHAR(20) PRIMARY KEY, SerialNo VARCHAR(5), Remark VARCHAR(100), Flag VARCHAR(10))";
	private String SQL_v3 = "CREATE TABLE assistApp(packageName VARCHAR(300) PRIMARY KEY)";
	
	public DatabaseHelper(Context context) {
		super(context, "gyic.db", null, 3);
	}

	public DatabaseHelper(Context context, int version, String sql) {
		super(context, "gyic.db", null, version);
		this.SQL = sql;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql1 = "CREATE TABLE prpmApplication(ApplicationNo VARCHAR(20) PRIMARY KEY, ApplicationName VARCHAR(100), ApplicationType VARCHAR(10), ApplicationNewVersion VARCHAR(10), ApplicationTag VARCHAR(100), ApplicationVersionContent text, ApplicationRefer text, PackageName VARCHAR(2000), ApplicationLaunch VARCHAR(100),ReFileList text, NewViewUpdateTime VARCHAR(20),ApplicationStatus VARCHAR(1))";
		String sql2 = "CREATE TABLE config(id INTEGER PRIMARY KEY, config VARCHAR(2), pattern VARCHAR(2), flag VARCHAR(2), emptyTime DATE)";
		String sql3 = "CREATE TABLE prpmUser(jobNumber VARCHAR(20) PRIMARY KEY, phoneNumber VARCHAR(20), email VARCHAR(50), timeNum INTEGER)";
		String sql4 = "CREATE TABLE prpmRegistorUser(userCode VARCHAR(20) PRIMARY KEY, password VARCHAR(64), userName VARCHAR(30), comCode VARCHAR(50))";
		String sql5 = "CREATE TABLE prpmMessage(messageID VARCHAR(20) , userCode VARCHAR(20), messageTitle VARCHAR(100), messageContent VARCHAR(1000), comCode VARCHAR(50), validDate DATE, operateCode VARCHAR(20), operateDate DATE, messageStatus VARCHAR(1), validStatus VARCHAR(1), remark VARCHAR2(255), receiveUserFlag VARCHAR(2), userGroup VARCHAR(4000),deleteFlag VARCHAR(1),readFlag VARCHAR(1))";
		String sql6 = "CREATE TABLE installedApplication(ApplicationNo VARCHAR(20) PRIMARY KEY, ApplicationName VARCHAR(100), ApplicationType VARCHAR(10), ApplicationNewVersion VARCHAR(10), ApplicationTag VARCHAR(100), ApplicationVersionContent text, ApplicationRefer text, PackageName VARCHAR(2000), ReFileList text, NewViewUpdateTime VARCHAR(20),installTime DATE,ApplicationStatus VARCHAR(1))";
		String sql7 = "CREATE TABLE VPNConnect(id INTEGER PRIMARY KEY, address VARCHAR(50),address1 VARCHAR(50), userName VARCHAR(50), password VARCHAR(50))";
		String sql8 = "INSERT INTO config values(1, '1', '1', '0', '2010-01-01 00:00:00')";
		String sql9 = "CREATE TABLE prpmFlowCount(UserCode VARCHAR(20), time VARCHAR(20),id INTEGER PRIMARY KEY, rapidity VARCHAR(20), totalNetWork VARCHAR(20))";
		String sql10 = "CREATE TABLE VPNAdress(id INTEGER PRIMARY KEY, xmpp VARCHAR(20), ftp VARCHAR(20), network VARCHAR(20))";
		// 初始维护深信服VPN地址
		String sql11 = "INSERT INTO VPNAdress values(1, '" + Constant.XMPPURL + "','" + Constant.FTPURL + "','" + Constant.NETURL + "')";
		//接入应用服务地址
		String sql12 = "CREATE TABLE joinedAppServiceAddress(packageName VARCHAR(300) PRIMARY KEY, address VARCHAR(300))";
		//平台主页应用排序
		String sql13 = "CREATE TABLE prpmApplicationRel(ApplicationNo VARCHAR(20) PRIMARY KEY, SerialNo VARCHAR(5), Remark VARCHAR(100), Flag VARCHAR(10))";
		//辅助应用
		String sql14 = "CREATE TABLE assistApp(packageName VARCHAR(300) PRIMARY KEY)";

		db.execSQL(sql1);
		db.execSQL(sql2);
		db.execSQL(sql3);
		db.execSQL(sql4);
		db.execSQL(sql5);
		db.execSQL(sql6);
		db.execSQL(sql7);
		db.execSQL(sql8);
		db.execSQL(sql9);
		db.execSQL(sql10);
		db.execSQL(sql11);
		db.execSQL(sql12);
		db.execSQL(sql13);
		db.execSQL(sql14);
//		db.execSQL("INSERT INTO joinedAppServiceAddress values('com.jh.jcs.activity','http://9.0.2.13:7002/jc6')");
		// System.out.println("onCreate");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {// 版本更新时调用此方法
		if(oldVersion == 1) {
			db.execSQL(SQL);
			db.execSQL(SQL_v2);
			db.execSQL(SQL_v3);
		}else if(oldVersion == 2) {
			db.execSQL(SQL);
			db.execSQL(SQL_v3);
		}
		System.out.println("onUpgrade");
	}
}
