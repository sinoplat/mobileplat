package com.sinosoft.traffic;

import java.util.ArrayList;
import java.util.List;

import com.sinosoft.phoneGapPlugins.pgsqliteplugin.DatabaseHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NetWorkSQL {

	private DatabaseHelper dbOpenHelper;
	private String count;
	private int count2;
	private String time = "";
	private int lastid;
	private List<String> list;
	private String count1;

	public NetWorkSQL(Context context) {
		this.dbOpenHelper = new DatabaseHelper(context);
	}

	public void intert(prpmFlowCount flow) {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.execSQL(
				"insert into prpmFlowCount (id,time,rapidity,totalNetWork) values(?,?,?,?)",
				new Object[] { flow.getId(), flow.getTime(),
						flow.getRapidity(), flow.getTotalNetWork() });
		db.close();
	}

	public List<String> lastTotalNetWork() {
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		list = new ArrayList<String>();
		Cursor c = db.query("prpmFlowCount", new String[] { "rapidity",
				"totalNetWork" }, null, null, null, null, null);
		c.moveToLast();
		if (c != null) {
			count1 = c.getString(0);
			count = c.getString(1);
            list.add(count1);
            list.add(count);
		}
		c.close();
		db.close();
		return list;
	}

	public int queryCount() {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.beginTransaction();
		Cursor c = db.query("prpmFlowCount", new String[] { "COUNT(*)" }, null,
				null, null, null, null);
		if (c.moveToNext()) {
			count2 = c.getInt(0);
		}
		c.close();
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return count2;
	}

	public int queryLast() {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.beginTransaction();
		Cursor c = db.query("prpmFlowCount", new String[] { "id" }, null, null,
				null, null, null);
		if (c.moveToLast()) {
			lastid = c.getInt(0);
		}
		c.close();
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return lastid;
	}

	public int delete(String time) {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		int count = db.delete("prpmFlowCount", "time=?", new String[] { time });
		db.close();
		return count;
	}

	public String getTime() {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("prpmFlowCount", new String[] { "time" },
				null, null, null, null, null);
		if (cursor.moveToFirst()) {
			time = cursor.getString(cursor.getColumnIndex("time"));
		}
		cursor.close();
		db.close();
		return time;
	}

}
