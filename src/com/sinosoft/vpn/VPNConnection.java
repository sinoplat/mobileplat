package com.sinosoft.vpn;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.sinosoft.bean.VPNBean;
import com.sinosoft.phoneGapPlugins.pgsqliteplugin.DatabaseHelper;
import com.sinosoft.phoneGapPlugins.util.Constant;

/**
 * 远程获取深信服用户名、密码、连接地址
 */
public class VPNConnection {
	public SharedPreferences sp;
	private static final int DOWN_OVER = 2;
	private Handler ParentHandler;
	private Context context;
	private Integer id;

	public VPNConnection(Context context, Handler handler) {
		this.context = context;
		ParentHandler = handler;
	}

	public void vpnConnection() {
		new Thread() {
			public void run() {
				getVPNAddress();
			}
		}.start();
	}

	public void getVPNAddress() {
		/* URL可以随意改 */
		String uri = Constant.GETVPN + "/meap/service/getVPN.do";
		// 第一步，创建HttpPost对象
		HttpPost httpPost = new HttpPost(uri);
		HttpResponse httpResponse = null;
		try {
			httpResponse = new DefaultHttpClient().execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// 第三步，使用getEntity方法活得返回结果
				String result = EntityUtils.toString(httpResponse.getEntity());
				System.out.println("result:" + result);
				try {
					JSONObject json = new JSONObject(result);
					// 深信服用户名、密码、连接地址
					String VPNAddress1 = json.getString("VPNAddress1");
					String VPNAddress2 = json.getString("VPNAddress2");
					String VPNUserName = json.getString("VPNUserName")
							.substring(6,
									json.getString("VPNUserName").length() - 5);
					String VPNPassword = json.getString("VPNPassword")
							.substring(4,
									json.getString("VPNPassword").length() - 7);

					// 天融信用户名、密码、连接地址
					String tianVPNAddress1 = json.getString("tianVPNAddress1");
					String tianVPNAddress2 = json.getString("tianVPNAddress2");
					String tianVPNUserName = json
							.getString("tianVPNUserName")
							.substring(
									6,
									json.getString("tianVPNUserName").length() - 5);
					String tianVPNPassword = json
							.getString("tianVPNPassword")
							.substring(
									4,
									json.getString("tianVPNPassword").length() - 7);

					//接入应用后台服务地址
					JSONArray arr = json.getJSONArray("JoinedAPPList");
					deleteJoinedAppInfo();
					if(arr!=null && arr.length()>0){
						for(int i=0;i<arr.length();i++){
							JSONObject joinedAppInfo = arr.getJSONObject(i);
							insertJoinedAppInfo(joinedAppInfo.getString("PackageName"),joinedAppInfo.getString("Address"));
						}
					}
					
					//辅助应用
					JSONArray assistAppArr = json.getJSONArray("AssistAPPList");
					deleteAssistAppInfo();
					if(assistAppArr!=null && assistAppArr.length()>0){
						for(int i=0;i<assistAppArr.length();i++){
							JSONObject joinedAppInfo = assistAppArr.getJSONObject(i);
							insertAssistAppInfo(joinedAppInfo.getString("PackageName"));
						}
					}
					
					// 深信服用户名、密码、访问地址维护
					VPNBean vpnb = new VPNBean();
					vpnb.setId(1);
					vpnb.setAddress(VPNAddress1);
					vpnb.setUserName(VPNUserName);
					vpnb.setPassword(VPNPassword);
					vpnb.setAddress1(VPNAddress2);
					// Integer t = getVPNid();
					deleteVPNid(1);
					insertVPN(vpnb);

					// 天融信用户名、密码、访问地址维护
					VPNBean tianVpnb = new VPNBean();
					tianVpnb.setId(2);
					tianVpnb.setAddress(tianVPNAddress1);
					tianVpnb.setUserName(tianVPNUserName);
					tianVpnb.setPassword(tianVPNPassword);
					tianVpnb.setAddress1(tianVPNAddress2);
					// Integer t = getVPNid();
					deleteVPNid(2);
					insertVPN(tianVpnb);

					mHandler.sendEmptyMessage(DOWN_OVER);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (ClientProtocolException e) {
			System.out.println("没有网络了" + e.toString());
			sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putBoolean("conn", false);
			editor.commit();
			e.printStackTrace();
		} catch (IOException e) {
			sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putBoolean("conn", false);
			editor.commit();
			System.out.println("没有网络了111" + e.toString());
			e.printStackTrace();
		}

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
	
	public long insertAssistAppInfo(String packageName) {
		DatabaseHelper help = new DatabaseHelper(context);
		SQLiteDatabase db = help.getWritableDatabase();
		db.beginTransaction();
		ContentValues values = new ContentValues();
		values.put("packageName", packageName);
		long v = db.insert("assistApp", null, values);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return v;
	}
	
	// 删除辅助应用
	public Integer deleteAssistAppInfo() {
		DatabaseHelper dbOpenHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.beginTransaction();
		int count = db.delete("assistApp", null, null);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return count;
	}

	public long insertVPN(VPNBean vpnb) {
		DatabaseHelper help = new DatabaseHelper(context);
		SQLiteDatabase db = help.getWritableDatabase();
		db.beginTransaction();
		ContentValues values = new ContentValues();
		values.put("id", vpnb.getId());
		values.put("address", vpnb.getAddress());
		values.put("address1", vpnb.getAddress1());
		values.put("userName", vpnb.getUserName());
		values.put("password", vpnb.getPassword());
		long v = db.insert("VPNConnect", null, values);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return v;
	}

	// 获取flag
	public Integer getVPNid() {
		DatabaseHelper dbOpenHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.beginTransaction();
		Cursor cursor = db.query("VPNConnect", new String[] { "id" }, null,
				null, null, null, null);
		if (cursor.moveToFirst()) {
			id = cursor.getInt(cursor.getColumnIndex("id"));		
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		cursor.close();
		db.close();
		return id;

	}

	// 删除数据
	public Integer deleteVPNid(int id) {
		DatabaseHelper dbOpenHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.beginTransaction();
		int count = db.delete("VPNConnect", "id=?", new String[] { id + "" });
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return count;

	}

	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_OVER:
				ParentHandler.sendEmptyMessage(DOWN_OVER);
				break;

			default:
				break;
			}
		};
	};

}
