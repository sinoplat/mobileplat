package com.sinosoft.phoneGapPlugins.pgsqliteplugin;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class VPNAddressProvider extends ContentProvider {

	private static final UriMatcher MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);
	// 匹配码
	private static final int ADDRESS = 1;
	private static final int ADDRESS1 = 2;
	private DatabaseHelper dBHelper;
	static {
		MATCHER.addURI(
				"com.sinosoft.phoneGapPlugins.pgsqliteplugin.VPNAddressProvider",
				"VPNAdress", ADDRESS);
		// 在android中,#代表数字,*代表任意字符
		MATCHER.addURI(
				"com.sinosoft.phoneGapPlugins.pgsqliteplugin.VPNAddressProvider",
				"VPNAdress/#", ADDRESS1);
	}

	@Override
	public boolean onCreate() {
		dBHelper = new DatabaseHelper(this.getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = dBHelper.getReadableDatabase();
		Cursor cursor = null;
		switch (MATCHER.match(uri)) {
		case ADDRESS:
			cursor = db.query("VPNAdress", projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		case ADDRESS1:
			// 解析出/VPNAdress/10这种形式的id值
			long rowid = ContentUris.parseId(uri);
			String where = " personid = " + rowid;
			if (selection != null && selection.length() > 0) {
				where += selection;
			}
			cursor = db.query("VPNAdress", projection, where,
					selectionArgs, null, null, sortOrder);
			break;
		default:
			throw new IllegalArgumentException("this is Unknown Uri:" + uri);
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
