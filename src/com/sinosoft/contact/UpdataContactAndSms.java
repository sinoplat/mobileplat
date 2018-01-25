package com.sinosoft.contact;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sinosoft.phoneGapPlugins.download.FTPContinue;
import com.sinosoft.phoneGapPlugins.util.Constant;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

/**
 * 联系人短信备份还原
 */
public class UpdataContactAndSms{

	private Context context;
	private ExportSmsXml exportsmsxml;
	private ImportSms importsms;
	private GetContact contact;
	private Thread thread;
	private String code = "";
	private Handler mHandler;
	private Handler mmHandler;
	private Handler pHandler;
	private String networkaddress;
	private String fileurl = "";
	private String back = "";
	private Handler conHandler;
	private String conAddress = "";
	private String smsAddress = "";
	private FTPContinue mFTPContinue;
	private static final int DOWN_OVER = 2;
	private static final int OTHER_OVER = 3;
	private static final int TWODWON_OVER = 6;
	private String path;
	private String smsAddress2;
	private Handler constantHandler;
	private String flag;
	
	public boolean execute(String action, final Context context, final Handler pHandler) throws JSONException {
		this.context = context;
		code = Constant.USERCODE;
		networkaddress = Constant.NETURL;
		this.pHandler = pHandler;
		importsms = new ImportSms(context);
		if (action.equals("updataContact")) {// 联系人短信备份
			mmHandler = new Handler() {
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					Bundle b = msg.getData();
					String smsinfo = b.getString("smsinfo");
					returnMsg(smsinfo);
				};
			};
			contact = new GetContact(context);
			boolean c = contact.backUp();
			if (c == true) {
				thread = new Thread(new Runnable() {
					public void run() {
						contactInit("constant" + code + ".txt");
					}
				});
				thread.start();
			} else {
				returnMsg("0");
			}
			return true;
		} else if (action.equals("restoreContact")) {// 联系人还原
			conHandler = new Handler() {
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					Bundle bundle = msg.getData();
					smsAddress2 = bundle.getString("SmsAddress");
					String conAddress = bundle.getString("ConAddress");
					flag = "1";
					if (smsAddress2 == "" || conAddress == "") {
						returnMsg("0");
					} else {
						if (Environment.getExternalStorageState().equals(
								Environment.MEDIA_MOUNTED)) {
							path = "/sdcard/RestoreText/";
							File dir = new File(path);
							if (!dir.exists()) {
								dir.mkdirs();
							}
						}
						mFTPContinue = new FTPContinue(context, DownHandler, 1);
						try {
							mFTPContinue.download(conAddress, path + "constant" + code + ".txt");
						} catch (IOException e) {
							returnMsg("0");
							e.printStackTrace();
						}
					}
				};
			};
			new AddressThread().start();
			constantHandler = new Handler() {
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					Bundle b = msg.getData();
					boolean downover = b.getBoolean("downover");
					if (downover == true) {
						Thread thread = new Thread(new Runnable() {
							@Override
							public void run() {
								contact = new GetContact(context);
								boolean con = contact.restore();								
								importsms = new ImportSms(context);
								boolean sms = importsms.textInsertSMS();
								if (con == true && sms == true) {
									DeleteApk( path + "constant"+ code + ".txt");
									DeleteApk(path + "Sms" + code+ ".txt");
									returnMsg("1");
								} else {
									returnMsg("0");
								}
							}
						});
						thread.start();
					}
				};
			};
			return true;
		} else if (action.equals("UpdataSms")) {// 短信备份
			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					Bundle b = msg.getData();
					String smsinfo = b.getString("smsinfo");
					returnMsg(smsinfo);
				};
			};
			try {
				exportsmsxml = new ExportSmsXml(context);
				boolean sms = exportsmsxml.createXml();
				if (sms == true) {
					thread = new Thread(new Runnable() {
						public void run() {
							init("Sms" + code + ".xml");
						}
					});
					thread.start();
				} else {
					returnMsg("0");
				}
			} catch (Exception e) {
				returnMsg("0");
				e.printStackTrace();
			}
			return true;
		} 
		return false;
	}

	public String init(String filename) {
		fileurl = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "crash" + '/' + filename;
		String RequestURL = "http://" + networkaddress + "/meap/servlet/UploadFileServlet";
		try {
			back = upload(fileurl, RequestURL);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return back;
	}

	public String upload(String filePath, String RequestURL)
			throws FileNotFoundException {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.setTimeout(30000);
		RequestParams params = new RequestParams();
		params.put("textname", "Sms" + code + ".xml");
		params.put("usercode", code);
		File file = new File(filePath);
		params.put("file", file);
		asyncHttpClient.post(RequestURL, params,
				new AsyncHttpResponseHandler() {
					@Override
					public void onFailure(Throwable arg0, String arg1) {
						System.out.println("短信上传失败");
						back = "上传失败";
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("smsinfo", "0");
						message.setData(bundle);
						mHandler.sendMessage(message);
						super.onFailure(arg0, arg1);
					}

					@Override
					public void onSuccess(String arg0) {
						System.out.println("短信上传成功");
						back = "上传成功";
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("smsinfo", "1");
						message.setData(bundle);
						mHandler.sendMessage(message);
						super.onSuccess(arg0);
					}
				});

		return back;

	}

	public String contactInit(String filename) {
		String fileurl1 = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + "crash" + '/' + filename;
		String RequestURL = "http://" + networkaddress + "/meap/servlet/UploadFileServlet";
		try {
			back = contactupload(fileurl1, RequestURL);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return back;
	}

	public String contactupload(String filePath, String RequestURL)
			throws FileNotFoundException {

		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.setTimeout(30000);
		RequestParams params = new RequestParams();
		params.put("usercode", code);
		params.put("textname", "constant" + code + ".txt");
		File file = new File(filePath);
		params.put("file", file);
		asyncHttpClient.post(RequestURL, params,
				new AsyncHttpResponseHandler() {
					@Override
					public void onFailure(Throwable arg0, String arg1) {
						System.out.println("联系人上传失败");
						System.out.println("arg0" + arg0);
						back = "上传失败";
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("smsinfo", "0");
						message.setData(bundle);
						mmHandler.sendMessage(message);
						super.onFailure(arg0, arg1);
					}

					@Override
					public void onSuccess(String arg0) {
						System.out.println(arg0);
						back = "上传成功";
						Message message = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("smsinfo", "1");
						message.setData(bundle);
						mmHandler.sendMessage(message);
						super.onSuccess(arg0);
					}
				});

		return back;
	}

	/** 获取Resultaddress */
	public class AddressThread extends Thread {

		@Override
		public void run() {
			String uri = "http://" + networkaddress
					+ "/meap/service/getBackupFileInfo.do";
			HttpPost httpPost = new HttpPost(uri);
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("usercode", code);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("usercode", code));
			HttpResponse httpResponse = null;
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				httpResponse = new DefaultHttpClient().execute(httpPost);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					String result = EntityUtils.toString(httpResponse
							.getEntity());
					System.out.println("result:" + result);
					try {
						JSONArray json = new JSONArray(result);
						JSONObject obj1 = json.getJSONObject(0);
						String FileType = obj1.getString("FileType");
						if (FileType.equals("02")) {
							String FilePath2 = obj1.getString("FilePath");
							String smsname = obj1.getString("Name");
							String file2 =FilePath2.substring(1,FilePath2.length());
							smsAddress =  file2 + "/"
									+ smsname;
							System.out.println("SmsAddress:" + smsAddress);
						}
						JSONObject obj2 = json.getJSONObject(1);
						String FileType2 = obj2.getString("FileType");
						if (FileType2.equals("01")) {
							String FilePath1 = obj2.getString("FilePath");
							String conname = obj2.getString("Name");
							String file1 =FilePath1.substring(1,FilePath1.length());
							conAddress = file1 + "/"
									+ conname;
							System.out.println("ContantAddress:" + conAddress);
						}
						sendmessage(smsAddress, conAddress);
					} catch (JSONException e) {
						e.printStackTrace();
						sendmessage(smsAddress, conAddress);
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				sendmessage(smsAddress, conAddress);
			} catch (IOException e) {
				e.printStackTrace();
				sendmessage(smsAddress, conAddress);
			}
		}
	}

	private void sendmessage(String smsAddress, String conAddress) {
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("SmsAddress", smsAddress);
		bundle.putString("ConAddress", conAddress);
		message.setData(bundle);
		conHandler.sendMessage(message);
	}

	public Handler DownHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TWODWON_OVER:
				if (flag.equals("1")) {
					flag = "2";
					DownSmsHandler.sendEmptyMessage(OTHER_OVER);
				} else {
				}
				break;
			default:
				break;
			}
		};
	};

	public Handler DownSmsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OTHER_OVER:
				flag = "2";
				mFTPContinue = new FTPContinue(context, DownSmsHandler, 1);
				try {
					mFTPContinue.download(smsAddress2, path + "Sms" + code
							+ ".txt");
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case TWODWON_OVER:
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("downover", true);
				message.setData(bundle);
				constantHandler.sendMessage(message);

				break;
			default:
				break;
			}
		}
	};
	
	public void DeleteApk(String localFile){
		  File file = new File(localFile);
		  file.delete();  
	}
	
	private void returnMsg(String res) {
		Message resMsg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("result", res);
		resMsg.setData(bundle);
		pHandler.sendMessage(resMsg);
	}
}
