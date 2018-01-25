package com.sinosoft.phoneGapPlugins.download;


import java.io.File;
import java.util.Random;
import java.util.UUID;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.sinosoft.gyicPlat.R;

@SuppressLint("NewApi")
public class DownloadLib {
    public static final Uri CONTENT_URI   = Uri.parse("content://downloads/my_downloads");
    DownloadManager downloadmanager;
    DownloadManager.Request request;
    DownloadChangeObserver download_observer;
    Handler handler;
    Context context;
    String download_url;
    File file_save_dir;
    Long download_id;
    String save_file_path;

    Uri uri;
    int filesize;

    Class activity_class;
    Bundle intent_extras;

    UpdateListener listener;
    boolean stop_download = false;
    
    CallbackContext callbackContext;

    public DownloadLib(final Context context, String download_url, File file_save_dir,CallbackContext callbackContext) {
        try {
			this.context = context;
			this.download_url = download_url;
			this.file_save_dir = file_save_dir;
			this.callbackContext=callbackContext;

			handler = new MyHandler();
			downloadmanager = (DownloadManager) context.
			        getSystemService(Context.DOWNLOAD_SERVICE);
			download_observer = new DownloadChangeObserver();

			// ��������Ŀ¼���ļ���
			String dir = file_save_dir.getPath();
			String name = get_filename();

			if (!create_dir(file_save_dir)) return;

			Log.i("����dir ", dir);
			Log.i("����name ", name);
			save_file_path = dir + "/" + name;


			// ��ʼ������ URL ·��
			uri = Uri.parse(download_url);

			request = new DownloadManager.Request(uri);
			request.setDestinationInExternalPublicDir(dir, name);

			// ����ֻ������WIFI������������
			//request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

			// �������ض���, ��ʼ����
			download_id = downloadmanager.enqueue(request);

			context.getContentResolver().registerContentObserver(
			        CONTENT_URI, true, download_observer);
		} catch (Exception e) {
			// TODO Auto-generated catch block7
			e.printStackTrace();
		}
    }
    public DownloadLib(final Context context, String download_url, File file_save_dir,String fileName,CallbackContext callbackContext) {
        try {
			this.context = context;
			this.download_url = download_url;
			this.file_save_dir = file_save_dir;
			this.callbackContext=callbackContext;

			handler = new MyHandler();
			downloadmanager = (DownloadManager) context.
			        getSystemService(Context.DOWNLOAD_SERVICE);
			download_observer = new DownloadChangeObserver();

			// ��������Ŀ¼���ļ���
			String dir =file_save_dir.getPath();
			String name =fileName;
			if (!file_save_dir.exists()) {
				if (!create_dir(file_save_dir)) return;
			}
			

			Log.i("����dir ", dir);
			Log.i("����name ", name);
			save_file_path = dir + "/" + name;


			// ��ʼ������ URL ·��
			uri = Uri.parse(download_url);
			
			request = new DownloadManager.Request(uri);
			
			request.setDestinationInExternalPublicDir(dir, name);

			// ����ֻ������WIFI������������
			//request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

			// �������ض���, ��ʼ����
			download_id = downloadmanager.enqueue(request);

			context.getContentResolver().registerContentObserver(
			        CONTENT_URI, true, download_observer);
		} catch (Exception e) {
			// TODO Auto-generated catch block7
			e.printStackTrace();
		}
    }

    public void download(UpdateListener listener) {

        this.listener = listener;


        // ����֪ͨ������¼�
        context.registerReceiver(on_notification_click,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));


        // ������ɺ��¼�
        context.registerReceiver(on_complete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }


    public String get_filename() {
        String filename = this.download_url.substring(download_url.lastIndexOf('/') + 1);
        if(filename==null || "".equals(filename.trim())){
            filename = UUID.randomUUID()+ ".tmp";
        }

        return filename;
    }


    public void remove_download() {
        downloadmanager.remove(download_id);

        stop_download = true;
    }

    public void pause_download() {
    }


    public void set_notification(Class activity_class, Bundle intent_extras) {
        this.activity_class = activity_class;
        this.intent_extras = intent_extras;
    }

    private boolean create_dir(File file_dir) {
        file_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + file_dir.getPath());
        if (file_dir.exists()) {
            Log.i("Ŀ¼�Ѿ����� ", file_dir.getAbsolutePath());
            return true;
        }

        Log.i("Ŀ¼������ ��ʼ����Ŀ¼ ", "true");

        try{
            boolean result = file_dir.mkdirs();
            if (result) {
                Log.i("Ŀ¼�����ɹ� ", "true");
                return true;
            }
        } catch(SecurityException se){
            Log.i("Ŀ¼����ʧ�� ", se.toString());
        }

        return false;
    }


    private String regenerate_filename(String filename) {
        int size = filename.length();
        if (size <= 24) {
            return filename;
        }

        String short_filename = filename.substring(0, 8) + "..." +
                filename.substring(size - 5);
        return short_filename;
    }


    // ֪ͨ������߼��¼�����
    BroadcastReceiver on_notification_click = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Intent i = new Intent(ctxt, activity_class);
            i.putExtras(intent_extras);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctxt.startActivity(i);
        }
    };


    // ������غ�֪ͨ���߼�
    BroadcastReceiver on_complete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            if (stop_download) {
                Log.i("����ֹͣ���� ����Ҫ���ļ� ", "true");
                context.unregisterReceiver(on_complete);
                return;
            }
            String filename = regenerate_filename(get_filename());

            Notification n;

            String full_file_path = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + save_file_path;
            Log.i("Ҫ�򿪵��ļ� ", full_file_path);
            File file = new File(full_file_path);

            Intent notice_intent = new Intent();
            notice_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notice_intent.setAction(android.content.Intent.ACTION_VIEW);
            notice_intent.setDataAndType(Uri.fromFile(file),
                    get_mime_type(file.getAbsolutePath()));


            PendingIntent pIntent = PendingIntent.getActivity(context, 0, notice_intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            n  = new NotificationCompat.Builder(context)
                    .setContentTitle(filename)
                    .setContentText("�ļ��Ѿ��������")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true).getNotification();


            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);


            Random rand = new Random();
            int notice_id = rand.nextInt(999999999);
            Log.i("֪ͨ id ", Integer.toString(notice_id));
            
            notificationManager.notify(notice_id, n);

            context.unregisterReceiver(on_complete);
            Intent intentx = new Intent(Intent.ACTION_VIEW);  
            intentx.setDataAndType(Uri.parse(file.getAbsolutePath()),
             "application/vnd.android.package-archive");  
            ctxt.startActivity(intent);
            
           /* new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    Intent i = new Intent(ctxt, activity_class);
                    i.putExtras(intent_extras);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctxt.startActivity(i);
                }
            };*/
            JSONObject jsonObject=new JSONObject();
            try {
				jsonObject.put("fileName", file.getName());
				jsonObject.put("fileSize", file.length());
				jsonObject.put("fullPath", file.getAbsolutePath());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            callbackContext.success(jsonObject);
        }
    };


//    protected void open_file() {
//        String full_file_path = Environment.getExternalStorageDirectory().getAbsolutePath()
//                + save_file_path;
//        Log.i("Ҫ�򿪵��ļ� ", full_file_path);
//        File file = new File(full_file_path);
//
//        try {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            // intent.setAction(Intent.ACTION_VIEW);
//            intent.setDataAndType(Uri.fromFile(file), get_mime_type(file.getAbsolutePath()));
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//        } catch (Exception e) {
//            Log.i("�ļ��򿪴��� ", e.getMessage());
//        }
//    }

    private String get_mime_type(String url) {
        String parts[]=url.split("\\.");
        String extension=parts[parts.length-1];
        String type = null;
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }



    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {

            int downloaded_size = get_downloaded_size();
            filesize = get_filesize();

            Log.i("�Ѿ����صĴ�С ", Integer.toString(downloaded_size));
            handler.sendMessage(handler.obtainMessage(0, downloaded_size, filesize));
        }

    }


    private int[] get_bytes_and_status() {
        int[] bytes_and_status = new int[] {-1, -1, 0};
        DownloadManager.Query query =
                new DownloadManager.Query().setFilterById(download_id);
        Cursor c = null;
        try {
            c = downloadmanager.query(query);
            if (c != null && c.moveToFirst()) {
                bytes_and_status[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                Log.i("��ĿǰΪֹ���صĴ�С ", Integer.toString(bytes_and_status[0]));
                bytes_and_status[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                Log.i("�ܴ�С ", Integer.toString(bytes_and_status[0]));
                bytes_and_status[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                Log.i("����״̬ ", Integer.toString(bytes_and_status[2]));
            }
            return bytes_and_status;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }


    private int get_downloaded_size() {
        int[] bytes_and_status = get_bytes_and_status();
        return bytes_and_status[0];
    }

    public int get_filesize() {
        int size = -1;
        // if (stop_download) return size;
        int[] bytes_and_status = get_bytes_and_status();

        Log.i("�ڲ��ܴ�С ", Integer.toString(bytes_and_status[1]));
        size = bytes_and_status[1];

        return size;
    }


    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                Log.i("�ڶ�������ֵ ", Integer.toString(msg.arg1));

                DownloadLib.this.listener.on_update(msg.arg1);
            } catch (Exception e) {

            }

        }
    }




}
