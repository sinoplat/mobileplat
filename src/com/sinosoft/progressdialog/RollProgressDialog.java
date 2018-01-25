package com.sinosoft.progressdialog;

import java.util.HashMap;
import java.util.Iterator;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.sinosoft.gyicPlat.R;

public class RollProgressDialog extends ProgressDialog {

	private String str;
	private Context context;
	public static HashMap<Context, Boolean> mapsKillHttp = new HashMap<Context, Boolean>();

	public void setStr(String str) {
		this.str = str;
	}

	public RollProgressDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			RollProgressbar.ISSHOW = false;
			mapsKillHttp.put(context, false);
			return super.onKeyDown(keyCode, event);
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public RollProgressDialog(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mapsKillHttp != null && mapsKillHttp.size() > 0) {
			mapsKillHttp.remove(context);
		}
		setContentView(R.layout.rollprogress);
		TextView rollprpgresstv = (TextView) findViewById(R.id.rollprpgresstv);
		rollprpgresstv.setText(str);
	}

	public static boolean getKillValue(Context context) {
		boolean i = true;
		if (mapsKillHttp != null && mapsKillHttp.size() > 0) {
			for (Iterator iterator = mapsKillHttp.keySet().iterator(); iterator
					.hasNext();) {
				Context type = (Context) iterator.next();
				if (type.equals(context)) {
					i = RollProgressDialog.mapsKillHttp.get(context);
				}
			}
		} else {
		}
		return i;
	}
}