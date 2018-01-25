package com.sinosoft.progressdialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.sinosoft.gyicPlat.R;

/**
 * 
 * @author Back 不可用的ProgressDialog
 * 
 */
public class RollProgressDialogBack extends ProgressDialog {

	private String str;
	private Context context;

	public void setStr(String str) {
		this.str = str;
	}

	public RollProgressDialogBack(Context context, int theme) {
		super(context, theme);
		this.context = context;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			return true;
		} else {
			return false; // 默认返回 false
		}
	}

	public RollProgressDialogBack(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rollprogress);
		TextView rollprpgresstv = (TextView) findViewById(R.id.rollprpgresstv);
		rollprpgresstv.setText(str);
	}

}