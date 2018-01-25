package com.way.pattern;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Build;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sinosoft.crashHandler.CrashHandler;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.util.Utils;
import com.tencent.bugly.crashreport.CrashReport;
import com.way.view.LockPatternUtils;

public class App extends Application {
	private static String PREF_NAME = "gyicplatuserpre";
	private static App mInstance;
	private LockPatternUtils mLockPatternUtils;
	static Context _context;
	private Resources _resource;

	public static App getInstance() {
		return mInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		mLockPatternUtils = new LockPatternUtils(this);
		_context = getApplicationContext();
		_resource = _context.getResources();
		App.set("set_quit", "0");
		
		// 工具类初始化
		Utils.initialize(_context);
		
		//创建默认的ImageLoader配置参数  
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration  
                .createDefault(this);  
        ImageLoader.getInstance().init(configuration);  
        
        CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(this);
        
        CommonUtil.initData(_context);
        //检测APP异常（生产）
        CrashReport.initCrashReport(getApplicationContext(), "900051879", false);
	}

	public LockPatternUtils getLockPatternUtils() {
		return mLockPatternUtils;
	}

	public static synchronized App context() {
		return (App) _context;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static SharedPreferences getPreferences() {
		SharedPreferences pre = context().getSharedPreferences(PREF_NAME,
				Context.MODE_MULTI_PROCESS);
		return pre;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static SharedPreferences getPreferences(String prefName) {
		return context().getSharedPreferences(prefName,
				Context.MODE_MULTI_PROCESS);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static void apply(SharedPreferences.Editor editor) {
		boolean sIsAtLeastGB = false;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			sIsAtLeastGB = true;
		}
		if (sIsAtLeastGB) {
			editor.apply();
		} else {
			editor.commit();
		}
	}

	public static void set(String key, boolean value) {
		Editor editor = getPreferences().edit();
		editor.putBoolean(key, value);
		apply(editor);
	}

	public static void set(String key, String value) {
		Editor editor = getPreferences().edit();
		editor.putString(key, value);
		apply(editor);
	}

	public static boolean get(String key, boolean defValue) {
		return getPreferences().getBoolean(key, defValue);
	}

	public static String get(String key, String defValue) {
		return getPreferences().getString(key, defValue);
	}

	public static int get(String key, int defValue) {
		return getPreferences().getInt(key, defValue);
	}

	public static long get(String key, long defValue) {
		return getPreferences().getLong(key, defValue);
	}

	public static float get(String key, float defValue) {
		return getPreferences().getFloat(key, defValue);
	}


}
