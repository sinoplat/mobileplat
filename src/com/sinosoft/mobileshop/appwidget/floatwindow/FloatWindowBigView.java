package com.sinosoft.mobileshop.appwidget.floatwindow;


import com.sangfor.ssl.SangforAuth;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.service.VpnWorkService;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.util.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class FloatWindowBigView extends LinearLayout{

	/**
	 * 记录大悬浮窗的宽度
	 */
	public static int viewWidth;

	/**
	 * 记录大悬浮窗的高度
	 */
	public static int viewHeight;
	
	private Context context;

	public FloatWindowBigView(final Context context) {
		super(context);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.float_window_big, this);
		View view = findViewById(R.id.big_window_layout);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		Button closeFloat = (Button) findViewById(R.id.float_closeFloat);
		Button controlVPN = (Button) findViewById(R.id.float_controlVPN);
		Button back = (Button) findViewById(R.id.float_back);
		if(Constant.VPNSTATUS == 5){
			controlVPN.setText("关闭VPN");
		}else {
			controlVPN.setText("连接VPN");
		}
		controlVPN.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(Constant.VPNSTATUS == 5){
					//关闭VPN
					if (SangforAuth.getInstance() != null) {
						SangforAuth.getInstance().vpnLogout();
					}
				}else {
					//重连VPN
					Intent intent = new Intent();
					intent.setAction("com.sinosoft.msg.vpnreconnect");
					context.sendBroadcast(intent);
				}
			}
		});
		
		closeFloat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击关闭悬浮窗的时候，移除所有悬浮窗
				MyWindowManager.closeFloat(context);
				MyWindowManager.setIsOpenFloat(false);
			}
		});
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击返回的时候，移除大悬浮窗，创建小悬浮窗
				MyWindowManager.removeBigWindow(context);
				MyWindowManager.createSmallWindow(context);
			}
		});
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		Rect rect = new Rect();
		this.getGlobalVisibleRect(rect);
		if(!rect.contains(x, y) || (x==0 && y==0)){
			MyWindowManager.removeBigWindow(context);
			MyWindowManager.createSmallWindow(context);
		}
		
		return false;
	}
}
