package com.sinosoft.mobileshop.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.sinosoft.gyicPlat.MainActivity;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.appwidget.dialog.StytledDialog;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.util.Utils;

/**
 * 应用启动界面
 * 
 */
public class WelcomeActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final View view = View.inflate(this, R.layout.activity_welcome, null);
		setContentView(view);
		// 渐变展示启动屏
		AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
		aa.setDuration(800);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				if(!Utils.isNetConnect()) {
					AlertDialog.Builder builder=new AlertDialog.Builder(WelcomeActivity.this);  //先得到构造器
			        builder.setTitle("提示"); //设置标题
			        builder.setMessage("当前设备没有开启任何网络，请开启后重新启动移动应用平台。"); //设置内容
			        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
			            @Override
			            public void onClick(DialogInterface dialog, int which) {
			            	finish();
			            }
			        });
			        builder.create().show();				
				} else {
					redirectTo();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * 跳转到...
	 */
	private void redirectTo() {
//		String readTxtFile = CommonUtil.ReadTxtFile();
//		if(TextUtils.isEmpty(readTxtFile)){
//			Intent intent = new Intent(this, RegisterActivity.class);
//			startActivity(intent);
//			finish();
//		}else {
//			userLogin();
//		}
		
		// 开启服务
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * 自动登陆
	 */
	private void userLogin() {
		
	}
}
