package com.sinosoft.traffic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.sinosoft.phoneGapPlugins.util.Constant;

public class HttpLocation {

	private String location;

	public String httpClient(double lat, double lon) {
		/* URL可以随意改 */
		String urlBaidu = String.format(Constant.LOCATIONURL, lat, lon);
		// 第一步，创建HttpPost对象
		HttpPost httpPost = new HttpPost(urlBaidu);
		// 设置HTTP POST请求参数必须用NameValuePair对象
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		HttpResponse httpResponse = null;
		try {
			// 设置httpPost请求参数
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			httpResponse = new DefaultHttpClient().execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// 第三步，使用getEntity方法活得返回结果
				String result = EntityUtils.toString(httpResponse.getEntity());
				try {
					JSONObject obj = new JSONObject(result)
							.getJSONObject("result");
					location = obj.getString("formatted_address");
					 System.out.println(location);
				} catch (JSONException e) {
					e.printStackTrace();
					return "";
				}

			}
		} catch (ClientProtocolException e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		}
		return location;
	}

}
