package com.sinosoft.contact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.sinosoft.phoneGapPlugins.util.VpnAddressIp;
import com.way.pattern.ResetGesturePasswordActivity;

public class HttpGetLocation {
	
	private String usercode;
	private String networkaddress;
	private Handler mHandler;
	public HttpGetLocation(String usercode,Context context){
		this.usercode=usercode;
		VpnAddressIp vpnip = new VpnAddressIp(context);
		networkaddress = vpnip.VPNAddress();
	}
	
	public void getLocation(){
	mHandler = new Handler(){
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle b = msg.getData();
			String smsinfo = b.getString("getBackupFileInfo");
			
		};
	};	
	
	}
	
	/** 获取下载路径 */
	public class LocationThread extends Thread {
		@Override
		public void run() {
			/* URL可以随意改 */
			String uri = "http://" + networkaddress
					+ "/meap/service/login.do";
			// 第一步，创建HttpPost对象
			HttpPost httpPost = new HttpPost(uri);
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("UserCode","0000000");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}

			// 设置HTTP POST请求参数必须用NameValuePair对象
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("jsonstr", jsonObject.toString()));
			HttpResponse httpResponse = null;
			try {
				// 设置httpPost请求参数
				httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				httpResponse = new DefaultHttpClient().execute(httpPost);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					// 第三步，使用getEntity方法活得返回结果
					String result = EntityUtils.toString(httpResponse
							.getEntity());
					System.out.println("result:" + result);
					
					try {
						JSONObject	obj = new JSONObject(result);
						String	getBackupFileInfo = (String) obj.get("getBackupFileInfo");
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("getBackupFileInfo", getBackupFileInfo);
						message.setData(bundle);
						mHandler.sendMessage(message);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
