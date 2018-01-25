package com.sinosoft.phoneGapPlugins.download;

import java.io.File;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mindpin.android.filedownloader.FileDownloader;
import com.mindpin.android.filedownloader.ui.TargetActivity;
import com.squareup.okhttp.internal.DiskLruCache.Editor;

public class DownloadPlugin extends CordovaPlugin {

	CallbackContext callbackContext;
	Context context;
	DownloadLib fd2;
	FileDownloader fileDownloader;
	public TextView result_view;
	public ProgressBar progress_bar;
	String stored_dir;
	String downloaded_file;
	public FTPContinue mFTPContinue;
	private Othercast othercast;
	public SharedPreferences sp;
	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		context = cordova.getActivity().getApplicationContext();
		this.callbackContext = callbackContext;	
		sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE);
		if("sdcard".equals(action)){
			String sdcard = getSDPath();
			callbackContext.success(sdcard);
			return true;
		}else if ("download".equals(action)) {
			othercast = new Othercast();
			IntentFilter filter = new IntentFilter("NEW_LIFEFORMM"); 
			 context.registerReceiver(othercast, filter);
			String remoteFile = args.getString(0);// 下载
			String localFile=args.getString(1);	
			android.content.SharedPreferences.Editor editor = sp.edit();
			editor.putString("localFile", localFile);
			editor.commit();
			 Intent intent1 = new Intent();
			 intent1.setAction("NEW_LIFEFORMM");  
             intent1.putExtra("localFile", localFile);  
             context.sendBroadcast(intent1);
			Intent i = new Intent();  
			i.putExtra("remoteFile", remoteFile);  
			i.putExtra("localFile", localFile); 
			 i.setClass(context,MainActivity.class);
			cordova.getActivity().startActivity(i);
			callbackContext.success("success");

			return true;
		} 

		return false;
	}

	/**
	 * 文件下载(弃用)
	 * 
	 * @param url
	 */
	private void downloadOld(String download_url) {
		File save_file_path = Environment.getExternalStorageDirectory();
		fd2 = new DownloadLib(context, download_url, save_file_path,
				callbackContext);

		Bundle b = new Bundle();
		b.putString("param_name1", "param_value1");
		fd2.set_notification(TargetActivity.class, b);
		fd2.download(new UpdateListener() {
			public void on_update(int downloaded_size) {
				Log.i("UI界面已经下载的大小 ", Integer.toString(downloaded_size));
			}
		});
	}

	
	public String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		return sdDir.toString();
	}
	
	@Override
	public void onDestroy() {
		if(othercast!=null){
		context.unregisterReceiver(othercast);
		}
		super.onDestroy();
	}
	
	
}
