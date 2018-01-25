package com.way.pattern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

import com.sinosoft.bean.VPNAddressBean;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.phoneGapPlugins.util.GetUserCode;
import com.sinosoft.phoneGapPlugins.util.VpnAddressIp;
import com.sinosoft.vpn.VPNAddress;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ResetGesturePasswordActivity extends Activity implements
OnClickListener{
	
	private EditText phonenumber;
	private String phonenumberlocal="";
	private Button gesturepwd_reset_btn;
	private Context context;
	public  String resultCode;
	public String uuid;
	private RegistHandler registHandler;
	private ProgressDialog progressDialog = null;
	public SharedPreferences sp;
	private int VPNflag;
	private String networkaddress;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesturepassword_reset);
		context= getApplicationContext();
		VpnAddressIp vpnip = new VpnAddressIp(context);
		networkaddress =  vpnip.VPNAddress();
		phonenumber = (EditText)findViewById(R.id.phonenumber);
		gesturepwd_reset_btn = (Button) findViewById(R.id.gesturepwd_reset_btn);
		gesturepwd_reset_btn.setOnClickListener(this);
		 uuid = Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	}
	
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {

		case R.id.gesturepwd_reset_btn:
			// 防暴击
			if (ButtonUtil.isFastDoubleClick()) {
				return;
			}
			if (TextUtils.isEmpty(phonenumber.getText().toString())) {
				Toast.makeText(context, "员工号不能为空", 2000).show();
			}else{
				Boolean conn = checkNet(this);
				if (conn == true) {
					new TestThread().start();
					progressDialog = ProgressDialog.show(
							ResetGesturePasswordActivity.this, null,
							"访问网络,请稍候！");

					registHandler = new RegistHandler();
				} else {
					Toast.makeText(context, "当前无网络", 2000).show();
				}
			} 	

			break;
		}

		
	}
	/** 获取ResultCode */
	public class TestThread extends Thread {
		
		private String password;

		@Override
		public void run() {
			GetUserCode  usercode = new GetUserCode(context);
			 password = usercode.getPassWord();
			/* URL可以随意改 */
			String uri = "http://"+networkaddress+"/meap/service/login.do";
		        // 第一步，创建HttpPost对象 
		        HttpPost httpPost = new HttpPost(uri);
		        JSONObject jsonObject = new JSONObject();
		        try {
		        	String number =phonenumber.getText().toString().trim();
					jsonObject.put("UserCode",number );
					jsonObject.put("IMEI", uuid);
					jsonObject.put("OptUserCode", number);
					jsonObject.put("OptPackageName","com.sinosoft.gyicPlat");
					jsonObject.put("Password",password);
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
		                String result = EntityUtils.toString(httpResponse.getEntity()); 
		                System.out.println("result:" + result); 
		              try {
						JSONObject obj = new JSONObject(result);
				resultCode = (String) obj.get("ResultCode");
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("resultCode", resultCode);
				message.setData(bundle);
				ResetGesturePasswordActivity.this.registHandler.sendMessage(message);
					} catch (JSONException e) {
					
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
	
	class RegistHandler extends Handler {
		public RegistHandler() {
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Bundle b = msg.getData();
			String resultCode = b.getString("resultCode");
			if (resultCode != null && resultCode.equals("1")) {
				Intent intent = new Intent(ResetGesturePasswordActivity.this,
						CreateGesturePasswordActivity.class);
				startActivity(intent);
				progressDialog.dismiss();
			} else {
				progressDialog.dismiss();
				Toast.makeText(context, "员工号不正确", 2000).show();
			}

		}

	}
	
	/**
	 * 判断Android客户端网络是否连接
	 * 
	 * @param context
	 * @return 真假
	 */
	public boolean checkNet(Context context) {

		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {

				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {

					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	
	
}
