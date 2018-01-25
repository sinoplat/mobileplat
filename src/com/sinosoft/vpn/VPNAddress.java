package com.sinosoft.vpn;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.sinosoft.bean.VPNAddressBean;
import com.sinosoft.phoneGapPlugins.pgsqliteplugin.DatabaseHelper;
import com.sinosoft.traffic.prpmFlowCount;

public class VPNAddress {

	private DatabaseHelper dbOpenHelper;

	public VPNAddress(Context context) {
		this.dbOpenHelper = new DatabaseHelper(context);
	}

	/**
	 * 插入VPN数据
	 * @param address
	 */
	public void intert(VPNAddressBean address) {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.execSQL(
				"insert into VPNAdress (id,xmpp,ftp,network) values(?,?,?,?)",
				new Object[] { address.getId(), address.getXmpp(),
						address.getFtp(), address.getNetwork() });
		db.close();
	}

	/**
	 * 删除VPN
	 * @param id
	 * @return
	 */
	public int delete(int id) {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		int count = db.delete("VPNAdress", "id=?", new String[] { id + "" });
		db.close();
		return count;
	}

	/**
	 * 查询VPN数据	1：深信服  2：天融信
	 * @param id
	 * @return
	 */
	public VPNAddressBean queryVPN(int id) {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		Cursor c = db.query("VPNAdress", new String[] { "id", "xmpp", "ftp",
						"network" }, "id=?", new String[] { id + "" }, null,
						null, null);
		VPNAddressBean vpn = null;
		if (c.moveToNext()) {
			Integer count = c.getInt(0);
			String xmpp = c.getString(1);
			String ftp = c.getString(2);
			String network = c.getString(3);
			vpn = new VPNAddressBean(count, xmpp, ftp, network);
		}
		c.close();
		db.close();
		return vpn;
	}

}
