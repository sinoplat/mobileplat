package com.sinosoft.httpclient;

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
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.sinosoft.bean.VPNAddressBean;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.phoneGapPlugins.util.VpnAddressIp;
import com.sinosoft.vpn.VPNAddress;

public class HttpClientService {

	public SharedPreferences sp;
	private String networkaddress;

	public HttpClientService(Context context) {
		VpnAddressIp vpnip = new VpnAddressIp(context);
		networkaddress = vpnip.VPNAddress();
	}

	public void httpClient(JSONObject jsonObject) {
		/* URL可以随意改 */
		String uri = "http://" + networkaddress
				+ "/meap/android/sendMessageBack.do";
		// 第一步，创建HttpPost对象
		HttpPost httpPost = new HttpPost(uri);

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
			}
		} catch (ClientProtocolException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}

	}

}
