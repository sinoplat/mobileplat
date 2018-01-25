/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidpn.client;

import java.io.IOException;
import java.util.Random;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.sinosoft.gyicPlat.R;
import com.sinosoft.httpclient.HttpClientService;
import com.sinosoft.phoneGapPlugins.android.SmsActivity;

/**
 * This class is to notify the user of messages with NotificationManager.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class Notifier {

	private static final String LOGTAG = LogUtil.makeLogTag(Notifier.class);

	private static final Random random = new Random(System.currentTimeMillis());

	public static TimerTask task;

	private Context context;
	
	private String messageTitle;

	private String messageContent;

	public static MediaPlayer player = null;

	private SharedPreferences sharedPrefs;

	private NotificationManager notificationManager;

	private JSONObject jsonObject;

	private JSONObject jsonObject2;

	public Notifier(Context context) {
		this.context = context;
		this.sharedPrefs = context.getSharedPreferences(
				Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		this.notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void notify(String notificationId, String apiKey, String title,
			String message, String uri) {
		Log.d(LOGTAG, "notify()...");

		Log.d(LOGTAG, "notificationId=" + notificationId);
		Log.d(LOGTAG, "notificationApiKey=" + apiKey);
		Log.d(LOGTAG, "notificationTitle=" + title);
		Log.d(LOGTAG, "notificationMessage=" + message);
		Log.d(LOGTAG, "notificationUri=" + uri);

		// 定时器执行音乐播放
		if (task == null) {
			task = new TimerTask() {
				public void run() {
					// 音乐来源
					player = MediaPlayer.create(context, R.raw.wonotifi);
					if (player != null) {
						player.stop();
					}
					try {
						// 执行播放前先执行暂停
						player.prepare();
						player.start();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
		}

		if (isNotificationEnabled()) {
			// Show the toast
			if (isNotificationToastEnabled()) {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}

			// Notification
			Notification notification = new Notification();
			notification.icon = getNotificationIcon();
			notification.defaults = Notification.DEFAULT_LIGHTS;
			if (isNotificationSoundEnabled()) {
				notification.defaults |= Notification.DEFAULT_SOUND;
			}
			if (isNotificationVibrateEnabled()) {
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.when = System.currentTimeMillis();
			notification.tickerText = message;
			Intent intent = null;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			intent = new Intent(context, SmsActivity.class);
			intent.putExtra(Constants.NOTIFICATION_ID, notificationId);
			intent.putExtra(Constants.NOTIFICATION_API_KEY, apiKey);
			intent.putExtra(Constants.NOTIFICATION_TITLE, title);
			intent.putExtra(Constants.NOTIFICATION_MESSAGE, message);
			intent.putExtra(Constants.NOTIFICATION_URI, uri);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			try {
				JSONObject json1 = new JSONObject(message)
						.getJSONObject("jsonstr");
				String json2 = json1.getString("flag");
				String sendMessageId =json1.getString("sendMessageId");
				if(json2.equals("1")){
					JSONObject prpMfeedReply = json1.getJSONObject("prpMfeedReply");
					String replyContent = prpMfeedReply.getString("replyContent");
					messageTitle = "意见反馈";
					messageContent =replyContent;
					intent.putExtra("newsTitle", "意见回复");
					intent.putExtra("newsContent", replyContent);
					String feedBackId =json1.getString("feedBackId");
					//向服务器返回信息
					jsonObject2 = new JSONObject();
					jsonObject2.put("type",json2 );
					jsonObject2.put("sendMessageId",sendMessageId );
					jsonObject2.put("taskNos","0000" );
					jsonObject2.put("feedBackID",feedBackId );					
					jsonObject2.put("taskNo","00" );
					jsonObject2.put("result","1");
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							HttpClientService http = new HttpClientService(context);
							http.httpClient(jsonObject2);
						}
					});   
					thread.start();
				}else{
				JSONObject json = json1.getJSONObject("prpmmessage");
				messageTitle = json.getString("messageTitle");
				messageContent = json.getString("messageContent");												
					intent.putExtra("newsTitle", messageTitle);
					intent.putExtra("newsContent", messageContent);			
				intent.putExtra(Constants.NOTIFICATION_MESSAGE, message);
				//向服务器返回信息
				String messageId =json1.getString("messageId");
				jsonObject = new JSONObject();
				jsonObject.put("messageId",messageId );
				jsonObject.put("type",json2 );
				jsonObject.put("sendMessageId",sendMessageId );
				jsonObject.put("taskNos","0000" );					
				jsonObject.put("taskNo","00" );
				jsonObject.put("result","1" );
				
				Thread thread1 = new Thread(new Runnable() {
					@Override
					public void run() {
						HttpClientService http = new HttpClientService(context);
						http.httpClient(jsonObject);
					}
				});
				thread1.start();
				}

				System.out.println(messageTitle + "..." + messageContent);

				messageContent = (String) ((messageContent.length() > 8) ? messageContent
						.subSequence(0, 7) + "..."
						: messageContent);
				notification.tickerText = title + messageContent;
				title = (String) notification.tickerText;
			} catch (JSONException e1) {

				e1.printStackTrace();
			}
			PendingIntent contentIntent = PendingIntent.getActivity(context, random.nextInt(),
					intent, PendingIntent.FLAG_UPDATE_CURRENT);

			notification.setLatestEventInfo(context, title, "", contentIntent);
			notificationManager.notify(random.nextInt(), notification);

			// Intent clickIntent = new Intent(
			// Constants.ACTION_NOTIFICATION_CLICKED);
			// clickIntent.putExtra(Constants.NOTIFICATION_ID, notificationId);
			// clickIntent.putExtra(Constants.NOTIFICATION_API_KEY, apiKey);
			// clickIntent.putExtra(Constants.NOTIFICATION_TITLE, title);
			// clickIntent.putExtra(Constants.NOTIFICATION_MESSAGE, message);
			// clickIntent.putExtra(Constants.NOTIFICATION_URI, uri);
			// // positiveIntent.setData(Uri.parse((new StringBuilder(
			// // "notif://notification.adroidpn.org/")).append(apiKey).append(
			// // "/").append(System.currentTimeMillis()).toString()));
			// PendingIntent clickPendingIntent = PendingIntent.getBroadcast(
			// context, 0, clickIntent, 0);
			//
			// notification.setLatestEventInfo(context, title, message,
			// clickPendingIntent);
			//
			// Intent clearIntent = new Intent(
			// Constants.ACTION_NOTIFICATION_CLEARED);
			// clearIntent.putExtra(Constants.NOTIFICATION_ID, notificationId);
			// clearIntent.putExtra(Constants.NOTIFICATION_API_KEY, apiKey);
			// // negativeIntent.setData(Uri.parse((new StringBuilder(
			// // "notif://notification.adroidpn.org/")).append(apiKey).append(
			// // "/").append(System.currentTimeMillis()).toString()));
			// PendingIntent clearPendingIntent = PendingIntent.getBroadcast(
			// context, 0, clearIntent, 0);
			// notification.deleteIntent = clearPendingIntent;
			//
			// notificationManager.notify(random.nextInt(), notification);

		} else {
			Log.w(LOGTAG, "Notificaitons disabled.");
		}
	}

	private int getNotificationIcon() {
		return sharedPrefs.getInt(Constants.NOTIFICATION_ICON, 0);
	}

	private boolean isNotificationEnabled() {
		return sharedPrefs.getBoolean(Constants.SETTINGS_NOTIFICATION_ENABLED,
				true);
	}

	private boolean isNotificationSoundEnabled() {
		return sharedPrefs.getBoolean(Constants.SETTINGS_SOUND_ENABLED, true);
	}

	private boolean isNotificationVibrateEnabled() {
		return sharedPrefs.getBoolean(Constants.SETTINGS_VIBRATE_ENABLED, true);
	}

	private boolean isNotificationToastEnabled() {
		return sharedPrefs.getBoolean(Constants.SETTINGS_TOAST_ENABLED, false);
	}

}
