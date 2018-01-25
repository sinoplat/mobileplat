package com.sinosoft.mobileshop.util;

import android.content.Context;
import android.os.Environment;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;

public class LiteOrmUtil {

	public static final String SD_CARD = Environment
			.getExternalStorageDirectory().getAbsolutePath();
	public static final String DB_NAME = "gyicmobileplat.db";
//	public static final String DB_NAME = SD_CARD + "/gyicmobileplat.db";
	public static LiteOrm liteOrm;

	public static LiteOrm getLiteOrm(Context context) {
		if (liteOrm == null) {
			// 使用级联操作
			DataBaseConfig config = new DataBaseConfig(context, DB_NAME);
			config.debugged = true; // open the log
			config.dbVersion = 1; // set database version
			config.onUpdateListener = null; // set database update listener
			liteOrm = LiteOrm.newCascadeInstance(config);// cascade
		}
		return liteOrm;
	}

}
