package com.sinosoft.contact;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.phoneGapPlugins.util.GetUserCode;
import com.sinosoft.progressdialog.RollProgressbar;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;

public class GetContact {
	/** Called when the activity is first created. */
	public String str;
	public Context context;
	private String userCode = Constant.USERCODE;
	private String[] nameAndTels;
	private String id;

	public GetContact(Context context) {
		this.context = context;
	}

	public void getContact() {
		str = "";
		// 获得所有的联系人
		Cursor cur = context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		// 循环遍历
		if (cur.moveToFirst()) {
			int idColumn = cur.getColumnIndex(ContactsContract.Contacts._ID);

			int displayNameColumn = cur
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			do {
				// 获得联系人的ID号
				String contactId = cur.getString(idColumn);
				// 获得联系人姓名
				String disPlayName = cur.getString(displayNameColumn);
				str += disPlayName;
				// 查看该联系人有多少个电话号码。如果没有这返回值为0
				int phoneCount = cur
						.getInt(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				if (phoneCount > 0) {
					// 获得联系人的电话号码
					Cursor phones = context.getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = " + contactId, null, null);
					int i = 0;
					String phoneNumber;
					if (phones.moveToFirst()) {
						do {
							i++;
							phoneNumber = phones
									.getString(phones
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							// if (i == 1)
							str = str + "," + phoneNumber;
							// System.out.println(phoneNumber);
						} while (phones.moveToNext());

					}
					phones.close();
				}
				str += "\r\n";
			} while (cur.moveToNext());

			cur.close();
		}
	}

	public void addContacts(String name, String num) {
		ContentValues values = new ContentValues();
		Uri rawContactUri = context.getContentResolver().insert(
				RawContacts.CONTENT_URI, values);
		long rawContactId = ContentUris.parseId(rawContactUri);
		System.out.println("rawContactId:" + rawContactId);
		values.clear();
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		values.put(StructuredName.GIVEN_NAME, name);

		context.getContentResolver().insert(Data.CONTENT_URI, values);
		values.clear();
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		values.put(Phone.NUMBER, num);
		values.put(Phone.TYPE, Phone.TYPE_HOME);
		context.getContentResolver().insert(Data.CONTENT_URI, values);
		if (nameAndTels.length > 2) {
			for (int c = 2; nameAndTels.length > c; c++) {
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.NUMBER, nameAndTels[c]);
				values.put(Phone.TYPE, Phone.TYPE_HOME);
				context.getContentResolver().insert(Data.CONTENT_URI, values);
			}
		}
	}

	public static String getContactId(Context context, String number) {
		Cursor c = null;
		try {
			c = context.getContentResolver().query(Phone.CONTENT_URI,
					new String[] { Phone.CONTACT_ID, Phone.NUMBER, }, null,
					null, null);
			if (c != null && c.moveToFirst()) {
				while (!c.isAfterLast()) {
					if (PhoneNumberUtils.compare(number, c.getString(1))) {
						return c.getString(0);
					}
					c.moveToNext();
				}
			}
		} catch (Exception e) {
			e.toString();
		} finally {
			if (c != null) {
				c.close();
			}

		}
		return null;
	}

	// 联系人备份
	public boolean backUp() {
		getContact();
		File saveFile = new File("/sdcard/crash/constant" + userCode + ".txt");
		FileOutputStream outStream;
		try {
			outStream = new FileOutputStream(saveFile);
			outStream.write(str.getBytes());
			outStream.close();
			return true;
		} catch (Exception e) {
			System.out.println(e.toString());
			return false;
		}
	}

	// 联系人还原
	public boolean restore() {
		try {
			userCode = Constant.USERCODE;
			File file = new File("/sdcard/RestoreText/constant" + userCode + ".txt");
			FileInputStream inStream = new FileInputStream(file);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024 * 5];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}
			outStream.close();
			inStream.close();
			String txt = outStream.toString();
			String[] str = txt.split("\n");
			for (int i = 0; i < str.length; i++) {
				if (str[i].indexOf(",") >= 0) {
					nameAndTels = str[i].split(",");
					id = getContactId(context, nameAndTels[1]);
//					System.out.println("11111" + id);
					if (id == null) {
						addContacts(nameAndTels[0], nameAndTels[1]);
					}
				}
			}
			return true;
		} catch (IOException e) {
			System.out.println(e.toString());
			return false;
		}
	}

}
