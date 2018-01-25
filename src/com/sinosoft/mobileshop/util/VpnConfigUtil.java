package com.sinosoft.mobileshop.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sinosoft.mobileshop.bean.AssistApp;
import com.sinosoft.mobileshop.bean.VpnBean;
import com.sinosoft.phoneGapPlugins.pgsqliteplugin.DatabaseHelper;
import com.sinosoft.phoneGapPlugins.util.Constant;

/**
 * 远程获取深信服用户名、密码、连接地址
 */
public class VpnConfigUtil {
	public SharedPreferences sp;
	public static final int DOWN_OVER = 2;
	public static final int DOWN_EXCEPTION = -1;
	public static final int DOWN_FAIL = -2;
	private Handler parentHandler;
	private Context context;

	public VpnConfigUtil(Context context, Handler handler) {
		this.context = context;
		parentHandler = handler;
	}

	public void vpnConfigInit() {
		getVpnAddress();
	}

	private void getVpnAddress() {
		String url = Constant.GETVPNURL;
		RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(context).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						try {
							List<VpnBean> vpnBeans = processData(response);
							if(vpnBeans != null && vpnBeans.size() > 0) {
								LiteOrmUtil.getLiteOrm(context).deleteAll(VpnBean.class);
								LiteOrmUtil.getLiteOrm(context).save(vpnBeans);
							}
							parentHandler.sendEmptyMessage(DOWN_OVER);
						} catch (Exception e) {
							e.printStackTrace();
							parentHandler.sendEmptyMessage(DOWN_EXCEPTION);
						}
					};
				}, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						parentHandler.sendEmptyMessage(DOWN_FAIL);
					};
				});
		mRequestQueue.add(jsonObjectRequest);
	}
	
	private List<VpnBean> processData(JSONObject json) throws Exception {
		List<VpnBean> vpnBeans = null;
		String resultCode = json.getString("ResultCode");
		if (!"1".equals(resultCode)) {
			return null;
		}
		// 深信服用户名、密码、连接地址
		String VPNAddress1 = json.getString("VPNAddress1");
		String VPNAddress2 = json.getString("VPNAddress2");
		String VPNUserName = json.getString("VPNUserName").substring(6,json.getString("VPNUserName").length() - 5);
		String VPNPassword = json.getString("VPNPassword").substring(4,json.getString("VPNPassword").length() - 7);

		// 天融信用户名、密码、连接地址
		String tianVPNAddress1 = json.getString("tianVPNAddress1");
		String tianVPNAddress2 = json.getString("tianVPNAddress2");
		String tianVPNUserName = json.getString("tianVPNUserName").substring(6,json.getString("tianVPNUserName").length() - 5);
		String tianVPNPassword = json.getString("tianVPNPassword").substring(4,json.getString("tianVPNPassword").length() - 7);

		//接入应用后台服务地址
		JSONArray arr = json.getJSONArray("JoinedAPPList");
		if(arr!=null && arr.length()>0){
			deleteJoinedAppInfo();
			for(int i=0;i<arr.length();i++){
				JSONObject joinedAppInfo = arr.getJSONObject(i);
				insertJoinedAppInfo(joinedAppInfo.getString("PackageName"),joinedAppInfo.getString("Address"));
			}
		}
		
		deleteVPNAdress();
		insertVPNAdress();
		
		//辅助应用
		List<AssistApp> assistApps = new ArrayList<AssistApp>();
		JSONArray assistAppArr = json.getJSONArray("AssistAPPList");
		if(assistAppArr!=null && assistAppArr.length()>0){
			for(int i=0;i<assistAppArr.length();i++){
				AssistApp assistApp = new AssistApp();
				JSONObject jappInfo = assistAppArr.getJSONObject(i);
				assistApp.setPackageName(jappInfo.getString("PackageName"));
				assistApps.add(assistApp);
			}
		}
		AssistApp assistApp = new AssistApp();
		assistApp.setPackageName(Constant.GYICPACKAGE);
		assistApps.add(assistApp);
		
		if(assistApps != null && assistApps.size() > 0) {
			LiteOrmUtil.getLiteOrm(context).deleteAll(AssistApp.class);
			LiteOrmUtil.getLiteOrm(context).save(assistApps);
		}
		
		vpnBeans = new ArrayList<VpnBean>();
		// 深信服用户名、密码、访问地址维护
		VpnBean vpnb = new VpnBean();
		vpnb.setId(1);
		vpnb.setAddress(VPNAddress1);
		vpnb.setUserName(VPNUserName);
		vpnb.setPassword(VPNPassword);
		vpnb.setAddress1(VPNAddress2);
		vpnBeans.add(vpnb);

		// 天融信用户名、密码、访问地址维护
		VpnBean tianVpnb = new VpnBean();
		tianVpnb.setId(2);
		tianVpnb.setAddress(tianVPNAddress1);
		tianVpnb.setUserName(tianVPNUserName);
		tianVpnb.setPassword(tianVPNPassword);
		tianVpnb.setAddress1(tianVPNAddress2);
		vpnBeans.add(tianVpnb);
		return vpnBeans;
	}

	public long insertJoinedAppInfo(String packageName,String address) {
		DatabaseHelper help = new DatabaseHelper(context);
		SQLiteDatabase db = help.getWritableDatabase();
		db.beginTransaction();
		ContentValues values = new ContentValues();
		values.put("packageName", packageName);
		values.put("address", address);
		long v = db.insert("joinedAppServiceAddress", null, values);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return v;
	}
	
	// 删除数据
	public Integer deleteJoinedAppInfo() {
		DatabaseHelper dbOpenHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.beginTransaction();
		int count = db.delete("joinedAppServiceAddress", null, null);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return count;

	}
	
	public long insertVPNAdress() {
		DatabaseHelper help = new DatabaseHelper(context);
		SQLiteDatabase db = help.getWritableDatabase();
		db.beginTransaction();
		ContentValues values = new ContentValues();
		values.put("id", "1");
		values.put("xmpp", Constant.XMPPURL);
		values.put("ftp", Constant.FTPURL);
		values.put("network", Constant.NETURL);
		long v = db.insert("VPNAdress", null, values);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return v;
	}
	
	// 删除数据
	public Integer deleteVPNAdress() {
		DatabaseHelper dbOpenHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.beginTransaction();
		int count = db.delete("VPNAdress", null, null);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return count;

	}
}
