package com.sinosoft.mobileshop.activity;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.sinosoft.contact.UpdataContactAndSms;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.appwidget.floatwindow.MyWindowManager;
import com.sinosoft.mobileshop.base.BaseActivity;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.LiteOrmUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.download.MainActivity;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;
import com.way.pattern.App;

/**
 * 设置页面
 */
public class SettingActivity extends BaseActivity implements OnSharedPreferenceChangeListener {

	private LinearLayout settingDownload;
	private LinearLayout settingCheckNew;
	private LinearLayout settingModifyPass;
	private LinearLayout settingGesturePass;
	private LinearLayout settingGestureLock;
	private LinearLayout settingLinkerBackup;
	private LinearLayout settingLinkerRecover;
	private LinearLayout is_open_float_ll;
	private LinearLayout settingHelp;
	private LinearLayout settingQuit;
	private TextView versionNo;
	private ImageView downloadSwtich;
	private ImageView gestureLockSwtich;
	
	
	private String downloadSet;
	private String gestureSet;
	
	private boolean isBackup = true;
	
    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    public void initView() {
    	setTitleBar("设置", true);
    	settingDownload = (LinearLayout)findViewById(R.id.setting_download);
    	settingCheckNew = (LinearLayout)findViewById(R.id.setting_checknew);
    	settingModifyPass = (LinearLayout)findViewById(R.id.setting_modify_passord);
    	settingGesturePass = (LinearLayout)findViewById(R.id.setting_gesture_pass);
    	settingGestureLock = (LinearLayout)findViewById(R.id.setting_gesture_pass_lock);
    	settingLinkerBackup = (LinearLayout)findViewById(R.id.setting_linker_backup);
    	settingLinkerRecover = (LinearLayout)findViewById(R.id.setting_linker_recover);
    	settingHelp = (LinearLayout)findViewById(R.id.setting_help);
    	settingQuit = (LinearLayout)findViewById(R.id.setting_quit);
    	is_open_float_ll = (LinearLayout) findViewById(R.id.setting_is_open_float_ll);
    	
    	versionNo = (TextView)findViewById(R.id.versionno);
    	downloadSwtich = (ImageView) findViewById(R.id.setting_download_switch);
    	gestureLockSwtich = (ImageView) findViewById(R.id.setting_guesture_lockswitch);
    	is_open_float_iv = (ImageView) findViewById(R.id.setting_is_open_float_iv);
    	
    	settingDownload.setOnClickListener(this);
    	settingCheckNew.setOnClickListener(this);
    	settingModifyPass.setOnClickListener(this);
    	settingGesturePass.setOnClickListener(this);
    	settingGestureLock.setOnClickListener(this);
    	settingLinkerBackup.setOnClickListener(this);
    	settingLinkerRecover.setOnClickListener(this);
    	settingHelp.setOnClickListener(this);
    	settingQuit.setOnClickListener(this);
    	is_open_float_ll.setOnClickListener(this);
    	getSharedPreferences("Config", Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
	protected void onResume() {
		super.onResume();
		setFloatImage(MyWindowManager.getIsOpenFloat());
	}
    public void initData() {
    	versionNo.setText(TDevice.getVersionName() + "");
    	downloadSet = App.get("DownloadSetvalue", "1");
    	gestureSet = App.get("GestureSetvalue", "0");
    	
    	setDownloadImage();
    	setGustureLockImage();
    }

    @Override
    public void onClick(View view) {
    	switch (view.getId()) {
		case R.id.setting_download:
			if("0".equals(downloadSet)) {
				App.set("DownloadSetvalue", "1");
			} else if("1".equals(downloadSet)) {
				App.set("DownloadSetvalue", "0");
			}
			downloadSet = App.get("DownloadSetvalue", "0");
			setDownloadImage();
			break;
		case R.id.setting_checknew:
			doGetVersion();
			break;
		case R.id.setting_modify_passord:
			showToast("功能暂关闭");
			break;
		case R.id.setting_gesture_pass:
			showToast("功能暂关闭");
//			gestureSet = App.get("GestureSetvalue", "0");
//			if("0".equals(gestureSet)) {
//				Intent intent = new Intent(SettingActivity.this, GuideGesturePasswordActivity.class);
//				startActivity(intent);
//			}
			break;
		case R.id.setting_gesture_pass_lock:
			showToast("功能暂关闭");
//			if("0".equals(gestureSet)) {
//	    		App.set("GestureSetvalue", "1");
//	    	} else if("1".equals(gestureSet)) {
//	    		App.set("GestureSetvalue", "0");
//	    	}
//			gestureSet = App.get("GestureSetvalue", "0");
//			setGustureLockImage();
			break;
		case R.id.setting_linker_backup:
			backup();
			break;
		case R.id.setting_linker_recover:
			restore();
			break;
		case R.id.setting_help:
			
			break;
		case R.id.setting_quit:
			showDialog(SettingActivity.this, "移动应用平台退出之后，其他接入应用将不能继续访问，您确定要退出吗？");
			break;
			
		case R.id.setting_is_open_float_ll:
			isOpenFloat();
			break;

		default:
			break;
		}
    }
    
    private void isOpenFloat() {
    	boolean isOpenFloat = MyWindowManager.getIsOpenFloat();
    	if(isOpenFloat){
    		MyWindowManager.closeFloat(getApplicationContext());
    		MyWindowManager.setIsOpenFloat(false);
    	}else {
    		MyWindowManager.createSmallWindow(getApplicationContext());
    	}
    	setFloatImage(!isOpenFloat);
	}

	private void setFloatImage(boolean isOpenFloat) {
		if(isOpenFloat){
    		is_open_float_iv.setImageResource(R.drawable.on);
    	}else {
    		is_open_float_iv.setImageResource(R.drawable.off);
    	}
		
	}

	public void showDialog(Context context, String msg){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage(msg); //设置内容
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	Editor edit = getSharedPreferences("Config", Context.MODE_PRIVATE).edit();
				edit.putBoolean("isQuit", true);
				edit.commit();
            	App.set("set_quit", "1");
            	finish();
            	Intent intent = new Intent(); 
            	intent.setAction("com.sinosoft.msg.quit");  
            	MyWindowManager.closeFloat(getApplicationContext());
            	SettingActivity.this.sendBroadcast(intent); 
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //参数都设置完成了，创建并显示出来
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void setDownloadImage() {
    	if("0".equals(downloadSet)) {
			downloadSwtich.setImageResource(R.drawable.off);
		} else if("1".equals(downloadSet)) {
			downloadSwtich.setImageResource(R.drawable.on);
		}
    }
    
    private void setGustureLockImage() {
    	if("0".equals(gestureSet)) {
    		gestureLockSwtich.setImageResource(R.drawable.off);
    	} else if("1".equals(gestureSet)) {
    		gestureLockSwtich.setImageResource(R.drawable.on);
    	}
    }
    
    /**
	 * 获取版本
	 * @param context
	 * @param appVersionInfo
	 */
	private void doGetVersion() {
		QueryBuilder<AppVersionInfo> builder = new QueryBuilder<AppVersionInfo>(AppVersionInfo.class);
		builder.whereEquals("PackageName", Constant.GYICPACKAGE);
		ArrayList<AppVersionInfo> appInfos = LiteOrmUtil.getLiteOrm(getApplicationContext()).query(builder);
		AppVersionInfo appVersionInfo = null;
		if(appInfos != null && appInfos.size() > 0) {
			appVersionInfo = appInfos.get(0);
		}
		if(appVersionInfo == null) {
			return;
		}
		String newVersion = appVersionInfo.getApplicationNewVersion();
		String applicationNo = appVersionInfo.getApplicationNo();
		if(newVersion != null && !"".equals(newVersion)
				&& !newVersion.equals(TDevice.getVersionName())) {
			final RollProgressbar rollProgressbar = CommonUtil.showDialog(SettingActivity.this, "移动应用平台有新版本，正在获取下载资源", true);
			String jsonStr = "jsonstr={\"UserCode\":\"0000000000\",\"OptUserCode\":\"0000000000\",\"OptPackageName\":\"\","
					+ "\"OS\":\"1\","
					+ "\"CurrentVersion\":\"" + newVersion + "\","
					+ "\"ApplicationNo\":\"" + applicationNo + "\""
					+ "}";
			String url = Constant.GETVERSIONURL + jsonStr;
			RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(getApplicationContext()).getRequestQueue();
			JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
					new Response.Listener<JSONObject>() {
						public void onResponse(JSONObject response) {
							try {
								String filePath = response.getString("FilePath");
								String fileName = response.getString("FileName");
								showDownload(filePath, fileName);
							} catch (JSONException e) {
								e.printStackTrace();
							} finally {
								if (rollProgressbar != null) {
									rollProgressbar.disProgressBar();
								}
							}
						};
					}, new Response.ErrorListener() {
						public void onErrorResponse(VolleyError error) {
							if (rollProgressbar != null) {
								rollProgressbar.disProgressBar();
							}
							CommonUtil.showToast(SettingActivity.this, "网络或服务器异常，请检查");
						};
					});
			
			mRequestQueue.add(jsonObjectRequest);
		} else {
			CommonUtil.showToast(SettingActivity.this, "当前已是最新版本");
		}
		
	}
	
	/**
	 * 获取远程文件
	 * @param remoteFile
	 */
	private void showDownload(String remoteFile, String fileName) {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		String localFile = "";
		File sdDir = null;
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		if(sdDir != null) {
			localFile = sdDir.getPath() + "/" + fileName + ".apk";
		} else {
			localFile = "/" + fileName + ".apk";
		}
		Intent i = new Intent();  
		i.putExtra("remoteFile", remoteFile);  
		i.putExtra("localFile", localFile); 
		i.setClass(SettingActivity.this,MainActivity.class);
		startActivity(i);
		
		App.set("localFile", localFile);
	}

	
	private void backup() {
		isBackup = true;
		UpdataContactAndSms ucas = new UpdataContactAndSms();
		try {
			ucas.execute("updataContact", getApplicationContext(), pHandler);
			ucas.execute("UpdataSms", getApplicationContext(), pHandler);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void restore() {
		isBackup = false;
		UpdataContactAndSms ucas = new UpdataContactAndSms();
		try {
			ucas.execute("restoreContact", getApplicationContext(), pHandler);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private Handler pHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Bundle b = msg.getData();
			String resultCode = b.getString("result");
			if(isBackup) {
				if("1".equals(resultCode)) {
					showToast("备份成功");
				} else {
					showToast("备份失败");
				}
			} else {
				if("1".equals(resultCode)) {
					showToast("恢复成功");
				} else {
					showToast("恢复失败");
				}
			}
		};
	};
	private ImageView is_open_float_iv;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(!TextUtils.isEmpty(key) && key.equals("isOpenFloat")){
			boolean boolean1 = sharedPreferences.getBoolean(key, true);
			setFloatImage(boolean1);
		}
		
	}
	
}
