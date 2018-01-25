package com.sinosoft.mobileshop.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.sinosoft.gyicPlat.MainActivity;
import com.sinosoft.mobileshop.bean.AppUploadFile;
import com.sinosoft.phoneGapPlugins.pgsqliteplugin.DatabaseHelper;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;

public class CommonUtil {

	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

	public static boolean hasLollipop() {
		return Build.VERSION.SDK_INT >= 21;
	}

	public static String getImageUrl(AppUploadFile appUploadFile) {
		String pre = "prpmuploadfileId.applicationNo=" + appUploadFile.getApplicationNo()
					+"&prpmuploadfileId.applicationversion=" + appUploadFile.getApplicationVersion()
					+"&prpmuploadfileId.serialNo=" + appUploadFile.getSerialNo();
		
//		String pre = "prpmuploadfileId.applicationNo=APP20150120075906&prpmuploadfileId.applicationversion=0.0.1&prpmuploadfileId.serialNo=1";
		return Constant.GETIMAGEURL + pre;
	}
	
	public static DisplayImageOptions getImageConfig() {
		DisplayImageOptions options = null;
		if (options == null) {
			options = new DisplayImageOptions.Builder().cacheInMemory(true)
					.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565)
					.build();
		}
		return options;
	}
	
	/**
	 * 0 未安装  1-更新  2-打开
	 * @param newVersionNo
	 * @param packageName
	 * @return
	 */
	public static int getVersionStatus(String newVersionNo, String packageName) {
		boolean isExist = TDevice.isPackageExist(packageName);
		if(!isExist) {
			return 0;
		}
		String oldVersion = TDevice.getVersionName(packageName);
		if(oldVersion != null && !oldVersion.equals(newVersionNo)) {
			return 1;
		} else {
			return 2;
		}
	}
	
	public static RollProgressbar showDialog(Context context, String text, boolean isShow) {
		RollProgressbar rollProgressbar = new RollProgressbar(context);
		rollProgressbar.showProgressBar(text, isShow);
		return rollProgressbar;
	}
	
	public static void showToast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
	
	public static AlertDialog showDialog(Context context, String msg){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage(msg); //设置内容
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //关闭dialog
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
        return dialog;
    }
	
	/**
	 * 展示一个规定时间后自动关闭的 对话框
	 * 
	 * @param context
	 *            上下文
	 * @param msg
	 *            提示信息
	 * @param time
	 *            规定时间
	 */
	public static void showTimeDialog(Context context, String msg,
			final long time) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context); // 先得到构造器
		builder.setTitle("提示"); // 设置标题
		builder.setMessage(msg); // 设置内容
		// 参数都设置完成了，创建并显示出来
		final AlertDialog dialog = builder.create();
		dialog.setCancelable(false);
		dialog.getWindow()
				.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		}).start();
	}
	
	/**
	 * 读取文件中的数据
	 * @param strFilePath
	 * @return
	 */
	public static String ReadTxtFile() {
		String content = ""; // 文件内容字符串
		// 打开文件
		File file = new File(Constant.USERINFO_PATH+Constant.USERINFO);
		// 如果path是传递过来的参数，可以做一个非目录的判断
		if (file.isDirectory()) {
			Log.d("TestFile", "The File doesn't not exist.");
		} else {
			try {
				InputStream instream = new FileInputStream(file);
				if (instream != null) {
					InputStreamReader inputreader = new InputStreamReader(
							instream);
					BufferedReader buffreader = new BufferedReader(inputreader);
					String line;
					// 分行读取
					while ((line = buffreader.readLine()) != null) {
						content += line + "\n";
					}
					instream.close();
				}
			} catch (java.io.FileNotFoundException e) {
				Log.d("TestFile", "The File doesn't not exist.");
			} catch (IOException e) {
				Log.d("TestFile", e.getMessage());
			}
		}
		return content;
	}
	
	// 初始化数据
	public static void initData(Context context) {
		DatabaseHelper dBHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dBHelper.getWritableDatabase();
		db.delete("prpmRegistorUser", null, null);
		ContentValues _values = new ContentValues();
		_values.put("userCode", "");
		_values.put("password", "");
		_values.put("userName", "");
		_values.put("comCode", "");
		db.insert("prpmRegistorUser", null, _values);
	}
}
