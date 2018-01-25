package com.sinosoft.phoneGapPlugins.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sinosoft.phoneGapPlugins.pgsqliteplugin.DatabaseHelper;

public class GetUserCode {
	private Context context;
	private String userCode="";
	private String password;

	public GetUserCode(Context context) {
		this.context = context;
	}

	// 获取usercode
	public String getUsercode() {
		DatabaseHelper dbOpenHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.beginTransaction();
		Cursor cursor = db.query("prpmRegistorUser", new String[] { "userCode",
				"userName" }, null, null, null, null, null);

		if (cursor.moveToFirst()) {
			userCode = cursor.getString(cursor.getColumnIndex("userCode"));
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		cursor.close();
		db.close();
		return userCode;
	}
	
	// 获取usercode
		public String getPassWord() {
			DatabaseHelper dbOpenHelper = new DatabaseHelper(context);
			SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
			db.beginTransaction();
			Cursor cursor = db.query("prpmRegistorUser", new String[] { "userCode",
					"password" }, null, null, null, null, null);

			if (cursor.moveToFirst()) {
				password = cursor.getString(cursor.getColumnIndex("password"));
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			cursor.close();
			db.close();
			return password;
		}
}
