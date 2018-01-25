package com.sinosoft.mobileshop.adapter;

import java.io.File;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.activity.AppDetailActivity;
import com.sinosoft.mobileshop.activity.NewsDetailActivity;
import com.sinosoft.mobileshop.appwidget.recycler.adapter.BaseViewHolder;
import com.sinosoft.mobileshop.bean.AppUploadFile;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.download.MainActivity;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;
import com.way.pattern.App;

public class AppInfoHolder extends BaseViewHolder<AppVersionInfo> {

	private ImageView appInfoIcon;
	private TextView appInfoName;
	private TextView appInfoRemark;
	private Button appInfoOpen;
	private RollProgressbar rollProgressbar;
	private Context context;
	
    public AppInfoHolder(ViewGroup parent,Context context) {
		super(parent, R.layout.holder_appinfo);
		this.context = context;
	}

	@Override
	public void onInitializeView() {
		super.onInitializeView();
		appInfoIcon = findViewById(R.id.appicon_iv);
		appInfoName = findViewById(R.id.appinfo_name_tv);
		appInfoRemark = findViewById(R.id.appinfo_remark_tv);
		appInfoOpen = findViewById(R.id.appopen_btn);
	}

	@Override
	public void setData(final AppVersionInfo object) {
		super.setData(object);
		appInfoName.setText(object.getApplicationName());
		appInfoRemark.setText(object.getApplicationTag());

		List<AppUploadFile> reFileList = object.getReFileList();
		String iconUrl = "";
		if (reFileList != null && reFileList.size() > 0) {
			for (AppUploadFile appUploadFile : reFileList) {
				if ("1".equals(appUploadFile.getFileType())) {
					iconUrl = CommonUtil.getImageUrl(appUploadFile);
					break;
				}
			}
		}
		
		
		// 加载图片
		Glide.with(context).load(iconUrl)
		// 加载过程中的图片显示
		.placeholder(R.drawable.icon)
		// 加载失败中的图片显示
		// 如果重试3次（下载源代码可以根据需要修改）还是无法成功加载图片，则用错误占位符图片显示。
		.error(R.drawable.icon).into(appInfoIcon);

		// 加载图片
//		ImageLoader.getInstance().loadImage(iconUrl, CommonUtil.getImageConfig(),
//				new SimpleImageLoadingListener() {
//					@Override
//					public void onLoadingComplete(String imageUri, View view,
//							Bitmap loadedImage) {
//						super.onLoadingComplete(imageUri, view, loadedImage);
//						appInfoIcon.setImageBitmap(loadedImage);
//					}
//
//					public void onLoadingFailed(String imageUri, View view,
//							FailReason failReason) {
//						appInfoIcon.setImageBitmap(BitmapFactory
//								.decodeResource(itemView.getResources(),
//										R.drawable.icon));
//					};
//				});

		final int status = CommonUtil.getVersionStatus(object.getApplicationNewVersion(), object.getPackageName());
		if(status == 0) {
			appInfoOpen.setText("安装");
		}
		if(status == 1) {
			appInfoOpen.setText("更新");
		}
		if(status == 2) {
			appInfoOpen.setText("打开");
		}
		
		appInfoOpen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String downloadSet = App.get("DownloadSetvalue", "0");
				if(TDevice.is3GOpen() && "1".equals(downloadSet)) {
					AlertDialog.Builder builder=new AlertDialog.Builder(itemView.getContext());  //先得到构造器
			        builder.setTitle("提示"); //设置标题
			        builder.setMessage("当前处于移动网络下，确定要下载吗？"); //设置内容
			        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
			            @Override
			            public void onClick(DialogInterface dialog, int which) {
			            	if(status == 0) { // 安装
								rollProgressbar = CommonUtil.showDialog(itemView.getContext(), "正在获取下载资源", true);
								doGetVersion(itemView.getContext(), object);
							}
							if(status == 1) { // 更新
								rollProgressbar = CommonUtil.showDialog(itemView.getContext(), "正在获取下载资源", true);
								doGetVersion(itemView.getContext(), object);
							}
			            }
			        });
			        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
			            @Override
			            public void onClick(DialogInterface dialog, int which) {
			                dialog.dismiss();
			            }
			        });
			        builder.create().show();
				} else {
					if(status == 0) { // 安装
						rollProgressbar = CommonUtil.showDialog(itemView.getContext(), "正在获取下载资源", true);
						doGetVersion(itemView.getContext(), object);
					}
					if(status == 1) { // 更新
						rollProgressbar = CommonUtil.showDialog(itemView.getContext(), "正在获取下载资源", true);
						doGetVersion(itemView.getContext(), object);
					}
				}
				
				if(status == 2) { // 打开
					TDevice.openApp(itemView.getContext(), object.getPackageName(), 
							object.getPackageName() + object.getApplicationLaunch());
				}
			}
		});

	}
	
	/**
	 * 获取版本
	 * @param context
	 * @param appVersionInfo
	 */
	private void doGetVersion(final Context context, AppVersionInfo appVersionInfo) {
		String currentVersion = TDevice.getVersionName(appVersionInfo.getPackageName());
		String applicationNo = appVersionInfo.getApplicationNo();
		String jsonStr = "jsonstr={\"UserCode\":\"0000000000\",\"OptUserCode\":\"0000000000\",\"OptPackageName\":\"\","
				+ "\"OS\":\"1\","
				+ "\"CurrentVersion\":\"" + currentVersion + "\","
				+ "\"ApplicationNo\":\"" + applicationNo + "\""
				+ "}";
		String url = Constant.GETVERSIONURL + jsonStr;
		
		RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(context).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						try {
							String filePath = response.getString("FilePath");
							String fileName = response.getString("FileName");
							showDownload(filePath, fileName);
						} catch (JSONException e) {
							e.printStackTrace();
						} finally {
							if (rollProgressbar != null) {
								rollProgressbar.disProgressBar();
							}
						}
					};
				}, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						if (rollProgressbar != null) {
							rollProgressbar.disProgressBar();
						}
						CommonUtil.showToast(context, "网络或服务器异常，请检查");
					};
				});
		
		mRequestQueue.add(jsonObjectRequest);
	}
	
	/**
	 * 获取远程文件
	 * @param remoteFile
	 */
	private void showDownload(String remoteFile, String fileName) {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		String localFile = "";
		File sdDir = null;
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		if(sdDir != null) {
			localFile = sdDir.getPath() + "/" + fileName + ".apk";
		} else {
			localFile = "/" + fileName + ".apk";
		}
		Intent i = new Intent();  
		i.putExtra("remoteFile", remoteFile);  
		i.putExtra("localFile", localFile); 
		i.setClass(itemView.getContext(),MainActivity.class);
		itemView.getContext().startActivity(i);
		
		App.set("localFile", localFile);
	}

	@Override
	public void onItemViewClick(AppVersionInfo object) {
		super.onItemViewClick(object);
		// 点击事件
		Intent intent = new Intent(itemView.getContext(), AppDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("appInfo", object);
		intent.putExtras(bundle);
		itemView.getContext().startActivity(intent);
		
	}
}