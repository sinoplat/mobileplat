package com.sinosoft.phoneGapPlugins.download;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sinosoft.gyicPlat.R;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */
	FTPContinue mFTPContinue;
	private static final int DOWN_OVER = 2;
	private String remoteFile = "";
	// private static final String remoteFile="/1.mp3";
	private String localFile = "";
	private Othercast othercast;
	private String apkname;
	private TextView btn_break;
	private TextView mTextView;
	private Button bt_break;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainload);

		ProgressBar mProgress = (ProgressBar) findViewById(R.id.progressbar);
		mTextView = (TextView) findViewById(R.id.tv_progress);
//		othercast = new Othercast();
//		IntentFilter filter = new IntentFilter("NEW_LIFEFORMM"); 
//		 registerReceiver(othercast, filter);
		btn_break = (TextView) findViewById(R.id.btn_break);
		bt_break = (Button) findViewById(R.id.bt_break);
		bt_break.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mFTPContinue.BreakDownload();
				
			}
		});
		
		Intent intent =getIntent();
		String remoteFile = intent.getStringExtra("remoteFile");
		String localFile = intent.getStringExtra("localFile");
		this.remoteFile = remoteFile;
		this.localFile = localFile;
		String spStr[] = localFile.split("/");
		File f = new File(localFile);
//		// 本地存在文件则删除
		if (f.exists()) {
			f.delete();
		}
		
		apkname = spStr[spStr.length - 1];
		btn_break.setText(apkname);
		mFTPContinue = new FTPContinue(this, mProgress, mTextView,bt_break, mHandler);
		
		DownloadFile();
	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
////		if(keyCode == KeyEvent.KEYCODE_BACK && mFTPContinue != null) {
////			mFTPContinue.cancelDownload();
////		}
////		return super.onKeyDown(keyCode, event);
//	}
	
	// 下载
	private void DownloadFile() {

		try {		
			mFTPContinue.download(remoteFile, localFile);
		} catch (IOException e) {
		
			e.printStackTrace();
		}
	}

	// 下载完成自动调用
	private void downloadOver() {
		Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
		install();
	}

	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_OVER:
				bt_break.setText("完成");
				downloadOver();
				break;
			default:
				break;
			}
		};
	};
	
	
	
	public void  install(){
		Intent intent = new Intent(Intent.ACTION_VIEW);  
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
		intent.setDataAndType(Uri.parse("file://" + localFile),  
		"application/vnd.android.package-archive");  
		startActivity(intent);  
	}
	
//	public void DeleteApk(){
//		  File file = new File(localFile);
//		  file.delete();  
//
//	}	
//	@Override
//	protected void onDestroy() {
//		unregisterReceiver(othercast);
//		super.onDestroy();
//	}
	@Override
	protected void onPause() {
		finish();
		super.onPause();
	}
//	private class Othercast extends BroadcastReceiver{
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//		String qudao =	intent.getStringExtra("qudao");
//		System.out.println("quxiao"+"111111111");
//			if(qudao!=null){
//				DeleteApk();
//			}
//		}
//		
//	} 
	
}