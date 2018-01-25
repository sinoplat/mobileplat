package com.sinosoft.progressdialog;

import android.content.Context;

public class RollProgressbar {

	private Context context;

	private RollProgressDialog rpd;

	public static boolean ISSHOW = false;

	public RollProgressbar(Context context) {
		this.context = context;
	}

	public void showProgressBar(String str,Boolean  invalid) {
		if (!ISSHOW) {
			rpd = new RollProgressDialog(context);
			rpd.setIndeterminate(false);
			rpd.setCanceledOnTouchOutside(false);
			// pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			rpd.setStr(str);
			// 返回键是否有效
			rpd.setCancelable(invalid);

			ISSHOW = true;
			rpd.show();
		}
	}

	public void disProgressBar() {
		if (ISSHOW) {
			ISSHOW = false;
			if (rpd == null) {
				return;
			}
			if (rpd.isShowing()) {
				rpd.dismiss();
			}
		}
	}
}
