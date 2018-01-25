package com.sinosoft.Progressbar;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;

import com.sinosoft.progressdialog.RollProgressbar;

public class DialogPlugn extends CordovaPlugin {
	private RollProgressbar rollProgressbar;
	private Context context;
	private String text;
	private Boolean isshow;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {

		context = cordova.getActivity();
		if ("dialogshow".equals(action)) {
			text = args.getString(0);
			isshow = args.getBoolean(1);
			cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {

					rollProgressbar = new RollProgressbar(context);
					rollProgressbar.showProgressBar(text, isshow);
				}
			});

			return true;
		} else if ("closedialog".equals(action)) {
			rollProgressbar.disProgressBar();
		}

		return false;
	}

}
