package com.sinosoft.crashHandler;

import java.io.File;
import java.io.FileNotFoundException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sinosoft.bean.VPNAddressBean;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.phoneGapPlugins.util.VpnAddressIp;
import com.sinosoft.vpn.VPNAddress;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class HttpAssist {

	private Context context;
	private String networkaddress;
	public String filename;
	private String fileurl;
	public HttpAssist(Context context,String url) {
		this.filename = url;
		this.context =context;
		VpnAddressIp vpnip = new VpnAddressIp(context);
		networkaddress =  vpnip.VPNAddress();
	}

	public void Init() {
		fileurl = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator + "crash" + '/' + filename;
		String RequestURL ="http://"+ networkaddress+"/meap/servlet/UploadFileServlet";
		try {
			upload(fileurl, RequestURL);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void upload(String filePath, String RequestURL)
			throws FileNotFoundException {

		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		params.put("file", file);
		asyncHttpClient.post(RequestURL, params,
				new AsyncHttpResponseHandler() {
                           
					@Override
					public void onFailure(Throwable arg0, String arg1) {
						System.out.println("上传失败");
						super.onFailure(arg0, arg1);
					}

					@Override
					public void onSuccess(String arg0) {
						System.out.println("上传成功");
						File file = new File(fileurl);
						file.delete();
						super.onSuccess(arg0);
					}
				});
	}


}