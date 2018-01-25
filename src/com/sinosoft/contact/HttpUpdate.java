package com.sinosoft.contact;

import java.io.File;
import java.io.FileNotFoundException;

import org.androidpn.client.NotificationService;
import org.androidpn.client.ServiceManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.phoneGapPlugins.util.GetUserCode;
import com.sinosoft.phoneGapPlugins.util.VpnAddressIp;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Environment;
import android.widget.Toast;

public class HttpUpdate {

	private Context context;
	private String networkaddress;
	public String filename;
	private String fileurl;
	private String back;

	public HttpUpdate(Context context, String filename) {
		this.filename = filename;
		this.context = context;
		VpnAddressIp vpnip = new VpnAddressIp(context);
		networkaddress = vpnip.VPNAddress();
	}

	public String Init() {
		fileurl = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator + "crash" + '/' + filename;
		String RequestURL = "http://" + networkaddress
				+ "/meap/servlet/UploadFileServlet";
		try {
		 back =	upload(fileurl, RequestURL);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return back;

	}

	public String upload(String filePath, String RequestURL)
			throws FileNotFoundException {

		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		File file = new File(filePath);
		// if (!file.exists()) {
		// return;
		// }
		params.put("file", file);
		asyncHttpClient.post(RequestURL, params,
				new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				System.out.println("上传失败");
				System.out.println("arg0" + arg0);
				back = "上传失败";
				super.onFailure(arg0, arg1);
				}

			@Override
			public void onSuccess(String arg0) {
				System.out.println(filename + "上传成功");
				back = "上传成功";
				super.onSuccess(arg0);
				}
			});
		return back;

	}

}