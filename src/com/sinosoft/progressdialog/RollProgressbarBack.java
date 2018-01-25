package com.sinosoft.progressdialog;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;

/**
 * 
 * @author back不可用的progress
 * 
 */
public class RollProgressbarBack {

	private Context context;

	private RollProgressDialogBack rpd;

	public static boolean ISSHOWBack = false;
//	private Timer timer;

	public RollProgressbarBack(Context context) {
		this.context = context;
	}

	public void showProgressBarBack(String str) {
		rpd = new RollProgressDialogBack(context);
		rpd.setIndeterminate(false);
		rpd.setCanceledOnTouchOutside(false);
		// pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		rpd.setStr(str);
		// 返回键是否有效
		// rpd.setCancelable(true);
		if (!ISSHOWBack) {
			ISSHOWBack = true;
			rpd.show();

//			timer = new Timer();

//			timer.schedule(new TimerTask() {
//
//				public void run() {
//					if (rpd.isShowing()) {
//						rpd.dismiss();
//						ISSHOWBack = false;
//						Intent intent = new Intent();
//						intent.setAction("content_pass_thirtyseconds_tosat");
//						context.sendBroadcast(intent);
//					}
//					timer.cancel();
//				}
//
//			}, 1000 * 30);
		}
	}

	public void disProgressBarBack() {
		if (ISSHOWBack) {
			ISSHOWBack = false;
			if (rpd == null) {
				return;
			}
			if (rpd.isShowing()) {
				rpd.dismiss();
			}

		}
//		if (timer != null) {
//			timer.cancel();
//		}
	}
}
