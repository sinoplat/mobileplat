package com.sinosoft.phoneGapPlugins.pgsqliteplugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PGSQLitePlugin extends CordovaPlugin {

	private Context context;
	private static final String ACTION_INSERT = "insert";
	private static final String ACTION_DELETE = "delete";
	private static final String ACTION_UPDATE = "update";
	private static final String ACTION_QUERY = "query";
	private DatabaseHelper dBHelper;
	private JSONArray rows;

	@Override
	public boolean execute(String action, final JSONArray data,
			final CallbackContext callbackContext) throws JSONException {

		context = this.cordova.getActivity().getApplicationContext();
		if ("beep".equals(action)) {

			Createtable(data);
			return true;
		} else if (action.equals(PGSQLitePlugin.ACTION_INSERT)) {
			insertQuery(data);
			return true;
		} else if (action.equals(PGSQLitePlugin.ACTION_DELETE)) {
			deleteQuery(data);
			return true;
		} else if (action.equals(PGSQLitePlugin.ACTION_UPDATE)) {
			updateQuery(data);
			return true;
		} else if (action.equals(PGSQLitePlugin.ACTION_QUERY)) {
			JSONArray result = new JSONArray();
			result = query(data);
			callbackContext.success(result.toString());
			return true;

		}

		return true;
	}

	// 创建表
	private Status Createtable(JSONArray data) {
		try {
			int version = data.getInt(0);
			String sql = data.getString(1);
			dBHelper = new DatabaseHelper(context, version, sql);
			SQLiteDatabase db = dBHelper.getWritableDatabase();
			System.out.println("444444444444444444444" + db);

		} catch (JSONException e) {

		}
		return PluginResult.Status.OK;
	}

	/**
	 * 向数据库插入一条数据
	 * 
	 * @param data
	 *            （1.数据库名字，2表名，3.数据（键值对的JSON））
	 * @return
	 */
	private PluginResult insertQuery(JSONArray data) {
		PluginResult result;
		try {
			Log.d("PGSQLitePlugin", "insertQuery");
			String tableName = data.getString(0);
			JSONObject values = (JSONObject) data.get(1);
			JSONArray names = values.names();
			int vLen = names.length();
			dBHelper = new DatabaseHelper(context);
			SQLiteDatabase db = dBHelper.getWritableDatabase();
			ContentValues _values = new ContentValues();
			String tempAppNo = "";
			for (int i = 0; i < vLen; i++) {
				String name = names.getString(i);
				_values.put(name, values.getString(name));
				
				if("prpmApplication".equals(tableName) && "ApplicationNo".equals(name)) {
					tempAppNo = values.getString(name);
				}
			}
			long id = db.insert(tableName, null, _values);
			db.close();
			if (id == -1) {
				result = new PluginResult(PluginResult.Status.ERROR,
						"Insert error");
			} else {
				result = new PluginResult(PluginResult.Status.OK, id);
				// 应用表
				if("prpmApplication".equals(tableName)) {
					boolean hasEx = hasExistForAppRel(tempAppNo);
					if(!hasEx) {
						int maxNo = getMaxAppRelNo();
						insertAppRel(tempAppNo, maxNo);
					}
				}
			}
			Log.d("PGSQLitePlugin", "insertQuery::id=" + id);

		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}
		return result;
	}

	/**
	 * 删除一条数据
	 */
	private PluginResult deleteQuery(JSONArray data) {
		PluginResult result = null;
		try {
			Log.d("PGSQLitePlugin", "deleteQuery");
			String tableName = data.getString(0);
			String where = getStringAt(data, 1);
			JSONArray whereArgs = getJSONArrayAt(data, 2);
			String[] _whereArgs = null;
			if (whereArgs != null) {
				int vLen = whereArgs.length();
				_whereArgs = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_whereArgs[i] = whereArgs.getString(i);
				}
			}
			dBHelper = new DatabaseHelper(context);
			SQLiteDatabase db = dBHelper.getWritableDatabase();
			long count = db.delete(tableName, where, _whereArgs);
			result = new PluginResult(PluginResult.Status.OK, count);
			Log.d("PGSQLitePlugin", "deleteQuery::count=" + count);
			db.close();
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}
		return result;
	}

	private String getStringAt(JSONArray data, int position) {
		String ret = null;
		try {
			ret = data.getString(position);
			// JSONArray convert JavaScript undefined|null to string "null", fix
			// it
			ret = (ret.equals("null")) ? null : ret;
		} catch (Exception er) {
		}
		return ret;
	}

	private JSONArray getJSONArrayAt(JSONArray data, int position) {
		JSONArray ret = null;
		try {
			ret = (JSONArray) data.get(position);
		} catch (Exception er) {
		}
		;
		return ret;
	}

	private String getStringAt(JSONArray data, int position, String dret) {
		String ret = getStringAt(data, position);
		return (ret == null) ? dret : ret;
	}

	/**
	 * 修改一条数据
	 */
	private PluginResult updateQuery(JSONArray data) {
		PluginResult result = null;
		try {
			Log.d("PGSQLitePlugin", "updateQuery");
			String tableName = data.getString(0);
			JSONObject values = (JSONObject) data.get(1);
			String where = getStringAt(data, 2, "1");
			JSONArray whereArgs = getJSONArrayAt(data, 3);
			String[] _whereArgs = null;
			if (whereArgs != null) {
				int vLen = whereArgs.length();
				_whereArgs = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_whereArgs[i] = whereArgs.getString(i);
				}
			}
			JSONArray names = values.names();
			int vLenVal = names.length();
			ContentValues _values = new ContentValues();
			for (int i = 0; i < vLenVal; i++) {
				String name = names.getString(i);
				_values.put(name, values.getString(name));
			}
			dBHelper = new DatabaseHelper(context);
			SQLiteDatabase db = dBHelper.getWritableDatabase();
			long count = db.update(tableName, _values, where, _whereArgs);
			result = new PluginResult(PluginResult.Status.OK, count);
			Log.d("PGSQLitePlugin", "updateQuery::count=" + count);
			db.close();
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}
		return result;
	}

	/**
	 * 查询全部数据
	 */
	private JSONArray query(JSONArray data) {
		PluginResult result = null;
		try {
			Log.d("PGSQLitePlugin", "query");
			String tableName = data.getString(0);
			JSONArray columns = getJSONArrayAt(data, 1);
			String where = getStringAt(data, 2);
			JSONArray whereArgs = getJSONArrayAt(data, 3);
			String groupBy = getStringAt(data, 4);
			String having = getStringAt(data, 5);
			String orderBy = getStringAt(data, 6);
			String limit = getStringAt(data, 7);
			String[] _whereArgs = null;
			if (whereArgs != null) {
				int vLen = whereArgs.length();
				_whereArgs = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_whereArgs[i] = whereArgs.getString(i);
				}
			}
			String[] _columns = null;
			if (columns != null) {
				int vLen = columns.length();
				_columns = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_columns[i] = columns.getString(i);
				}
			}
			dBHelper = new DatabaseHelper(context);
			SQLiteDatabase db = dBHelper.getWritableDatabase();
			
			Cursor cs = null;
			
			if("prpmApplication".equals(tableName)) {
				String sql = "select "
						+ "a.ApplicationNo, "
						+ "ApplicationName, "
						+ "ApplicationType, "
						+ "ApplicationNewVersion, "
						+ "ApplicationLaunch, "
						+ "ApplicationTag, "
						+ "ApplicationVersionContent,"
						+ "ApplicationRefer, "
						+ "PackageName, "
						+ "ReFileList, "
						+ "NewViewUpdateTime, "
						+ "ApplicationStatus "
						+ "from "
						+ "prpmapplication a left outer JOIN prpmapplicationrel b on a.applicationno = b.applicationno "
						+ " where not exists (select 1 from assistApp s where s.PackageName = a.PackageName)"
						+ " order by b.serialno, a.NewViewUpdateTime";
				
				cs = db.rawQuery(sql, null);
			} else {
				cs = db.query(tableName, _columns, where, _whereArgs,
						groupBy, having, orderBy, limit);
			}
			
			if (cs != null) {
				JSONObject res = new JSONObject();
				rows = new JSONArray();
				if (cs.moveToFirst()) {
					String[] names = cs.getColumnNames();
					int namesCoint = names.length;
					do {
						JSONObject row = new JSONObject();
						for (int i = 0; i < namesCoint; i++) {
							String name = names[i];
							row.put(name, cs.getString(cs.getColumnIndex(name)));
						}
						rows.put(row);
					} while (cs.moveToNext());
				}
				res.put("rows", rows);
				cs.close();
				db.close();
				Log.d("PGSQLitePlugin", "query::count=" + rows.length());
				return rows;

			} else {
				result = new PluginResult(PluginResult.Status.ERROR,
						"Error execute query");
			}
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
		}
		return rows;
	}
	
	/**
	 * 在关系表中是否存在此APP
	 */
	private boolean hasExistForAppRel(String appNo) {
		boolean hasExist = false;
		dBHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dBHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from prpmApplicationRel where ApplicationNo ='" + appNo + "'", null);
		if(cursor.getCount() > 0) {
			hasExist = true;
		} else {
			hasExist = false;
		}
		return hasExist;
	}
	
	/**
	 * 获取最大的App关系序号
	 */
	private int getMaxAppRelNo() {
		int res = 0;
		dBHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dBHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select max(SerialNo) from prpmApplicationRel", null);
		cursor.moveToLast();
		res = cursor.getInt(0);		
		return res+1;
	}
	
	/**
	 * 插入AppRel表
	 */
	private void insertAppRel(String appNo, int maxNo) {
		dBHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dBHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put("ApplicationNo", appNo);
		values.put("SerialNo", maxNo);
		values.put("Remark", "");
		values.put("Flag", "");
		long id = db.insert("prpmApplicationRel", null, values);
	}
	
}
