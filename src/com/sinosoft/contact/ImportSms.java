package com.sinosoft.contact;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.phoneGapPlugins.util.GetUserCode;
import com.sinosoft.progressdialog.RollProgressbar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;
 //短信还原
public class ImportSms {
 
    private Context context;
 
    private List<SmsItem> smsItems;
    private ContentResolver conResolver;
    private String code = Constant.USERCODE;
    private RollProgressbar rollProgressbar;
    public ImportSms(Context context) {
        this.context = context;
        conResolver = context.getContentResolver();
    }
 
    @SuppressWarnings("unchecked")
    public boolean textInsertSMS() {
        /**
         * 放一个解析xml文件的模块
         */
    	rollProgressbar = new RollProgressbar(context);
        smsItems = this.getSmsItemsFromXml();
        if(!smsItems.isEmpty()){
        Log.d("sqk", "after smsItems");
        //for (SmsItem item : smsItems) {
        
        for (SmsItem item : smsItems) {
 
            // 判断短信数据库中是否已包含该条短信，如果有，则不需要恢复
            Cursor cursor = conResolver.query(Uri.parse("content://sms"), new String[] { SmsField.DATE }, SmsField.DATE + "=?",
                    new String[] { item.getDate() }, null);
            System.out.println(item.getDate());
            if (!cursor.moveToFirst()) {// 没有该条短信
            	 System.out.println("--------------"+item.getDate());
                ContentValues values = new ContentValues();
                values.put(SmsField.ADDRESS, item.getAddress());
                // 如果是空字符串说明原来的值是null，所以这里还原为null存入数据库
                values.put(SmsField.PERSON, item.getPerson().equals("") ? null : item.getPerson());
                values.put(SmsField.DATE, item.getDate());
                values.put(SmsField.PROTOCOL, item.getProtocol().equals("") ? null : item.getProtocol());
                values.put(SmsField.READ, item.getRead());
                values.put(SmsField.STATUS, item.getStatus());
                values.put(SmsField.TYPE, item.getType());
                values.put(SmsField.REPLY_PATH_PRESENT, item.getReply_path_present().equals("") ? null : item.getReply_path_present());
                values.put(SmsField.BODY, item.getBody());
                values.put(SmsField.LOCKED, item.getLocked());
                values.put(SmsField.ERROR_CODE, item.getError_code());
                values.put(SmsField.SEEN, item.getSeen());
                conResolver.insert(Uri.parse("content://sms"), values);
            }
            cursor.close();
        }
        return true;
        }
        return false;
    }
 
    public List<SmsItem> getSmsItemsFromXml(){
        SmsItem smsItem = null;
        XmlPullParser parser = Xml.newPullParser();
        String absolutePath = Environment.getExternalStorageDirectory() + "/RestoreText/"+"Sms"+code+".txt";
        File file = new File(absolutePath);
        if (!file.exists()) {
 
            Looper.prepare();
            Toast.makeText(context, "message.xml短信备份文件不在sd卡中", 1).show();
            Looper.loop();//退出线程
//          return null;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            parser.setInput(fis, "UTF-8");
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                case XmlPullParser.START_DOCUMENT:
                    smsItems = new ArrayList<SmsItem>();
                    break;
 
                case XmlPullParser.START_TAG: // 如果遇到开始标记，如<smsItems>,<smsItem>等
                    if ("item".equals(parser.getName())) {
                        smsItem = new SmsItem();
 
                        smsItem.setAddress(parser.getAttributeValue(0));
                        smsItem.setPerson(parser.getAttributeValue(1));
                        smsItem.setDate(parser.getAttributeValue(2));
                        smsItem.setProtocol(parser.getAttributeValue(3));
                        smsItem.setRead(parser.getAttributeValue(4));
                        smsItem.setStatus(parser.getAttributeValue(5));
                        smsItem.setType(parser.getAttributeValue(6));
                        smsItem.setReply_path_present(parser.getAttributeValue(7));
                        smsItem.setBody(parser.getAttributeValue(8));
                        smsItem.setLocked(parser.getAttributeValue(9));
                        smsItem.setError_code(parser.getAttributeValue(10));
                        smsItem.setSeen(parser.getAttributeValue(11));
 
                    }
                    break;
                case XmlPullParser.END_TAG:// 结束标记,如</smsItems>,</smsItem>等
                    if ("item".equals(parser.getName())) {
                        smsItems.add(smsItem);
                        smsItem = null;
                    }
                    break;
                }
                event = parser.next();
            }
        } catch (FileNotFoundException e) {
        	deleteFile("/sdcard/RestoreText/" + "Sms" + code+ ".txt");
            rollProgressbar.disProgressBar();
            Looper.prepare();
            System.out.println(e.toString()); 
            Toast.makeText(context, "短信恢复出错", 1).show();
            Looper.loop();
            e.printStackTrace();
             
        } catch (XmlPullParserException e) {
        	deleteFile("/sdcard/RestoreText/" + "Sms" + code+ ".txt");
            rollProgressbar.disProgressBar();
            Looper.prepare();
            System.out.println(e.toString()); 
            Toast.makeText(context, "短信恢复出错", 1).show();
            Looper.loop();
            e.printStackTrace();       
        } catch (IOException e) {
        	deleteFile("/sdcard/RestoreText/" + "Sms" + code+ ".txt");
            rollProgressbar.disProgressBar();
            Looper.prepare();
            System.out.println(e.toString()); 
            Toast.makeText(context, "短信恢复出错", 1).show();
            Looper.loop();
            e.printStackTrace();
        }
        return smsItems;
    }
    
    public void deleteFile(String localFile){
		  File file = new File(localFile);
		  file.delete();  
	}
 
}