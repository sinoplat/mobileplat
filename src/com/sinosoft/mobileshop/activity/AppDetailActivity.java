package com.sinosoft.mobileshop.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.sangfor.ssl.SangforAuth;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.appwidget.ZoomOutPageTransformer;
import com.sinosoft.mobileshop.base.BaseActivity;
import com.sinosoft.mobileshop.bean.AppUploadFile;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.download.MainActivity;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;
import com.way.pattern.App;

/**
 * 应用详细页面
 */
public class AppDetailActivity extends BaseActivity {

	private AppVersionInfo appInfo;
	
	private ImageView appDetailIcon;
	private TextView appDetailName;
	private TextView appDetailRemark;
	private TextView appDetailContent;
	private TextView appDetailDate;
	private TextView appDetailVersion;
	private TextView appUninstall;
	private Button appOperBtn;
	List<String> imgPathList = new ArrayList<String>();
	
	int status = -1;
	
	private RollProgressbar rollProgressbar;
	
    @Override
    protected int getLayoutId() {
        return R.layout.activity_app_detail;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    public void initView() {
    	setTitleBar("应用详情", true);

    	Intent intent = this.getIntent(); 
    	appInfo = (AppVersionInfo)intent.getSerializableExtra("appInfo");
    	
    	appDetailIcon = (ImageView)findViewById(R.id.app_detail_icon_iv);
    	appDetailName = (TextView)findViewById(R.id.app_detail_name_tv);
    	appDetailRemark = (TextView)findViewById(R.id.app_detail_remark_tv);
    	appDetailContent = (TextView)findViewById(R.id.app_detail_content_tv);
    	appDetailDate = (TextView)findViewById(R.id.app_detail_date_tv);
    	appDetailVersion = (TextView)findViewById(R.id.app_detail_version_tv);
    	appUninstall = (TextView)findViewById(R.id.app_detail_uninstall_tv);
    	appOperBtn = (Button)findViewById(R.id.app_detail_oper_btn);
    	
    	appDetailName.setText(appInfo.getApplicationName());
    	appDetailRemark.setText(appInfo.getApplicationTag());
    	appDetailContent.setText("\u3000\u3000" + appInfo.getApplicationRefer());
    	appDetailDate.setText("更新时间：" + appInfo.getNewViewUpdateTime());
    	appDetailVersion.setText("版本号：" + appInfo.getApplicationNewVersion());
    	setImg();
    	
    	ViewPager viewPager = (ViewPager) findViewById(R.id.app_detail_img_vp);
        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.setOffscreenPageLimit(4);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        viewPager.setCurrentItem(1);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	setBtnText();
    }
    
    private void setImg() {
    	List<AppUploadFile> reFileList = appInfo.getReFileList();
    	imgPathList.clear();
		String iconUrl = "";
		if (reFileList != null && reFileList.size() > 0) {
			for (AppUploadFile appUploadFile : reFileList) {
				if ("1".equals(appUploadFile.getFileType())) {
					iconUrl = CommonUtil.getImageUrl(appUploadFile);
				} 
				if ("2".equals(appUploadFile.getFileType())) {
					imgPathList.add(CommonUtil.getImageUrl(appUploadFile));
				}
			}
		}

		// 加载图片
		ImageLoader.getInstance().loadImage(iconUrl, CommonUtil.getImageConfig(),
				new SimpleImageLoadingListener() {
					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						super.onLoadingComplete(imageUri, view, loadedImage);
						appDetailIcon.setImageBitmap(loadedImage);
					}

					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						appDetailIcon.setImageBitmap(BitmapFactory
								.decodeResource(getResources(),
										R.drawable.icon));
					};
				});

		setBtnText();
		
		appOperBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String downloadSet = App.get("DownloadSetvalue", "0");
				if(TDevice.is3GOpen() && "1".equals(downloadSet)) {
					AlertDialog.Builder builder=new AlertDialog.Builder(AppDetailActivity.this);  //先得到构造器
			        builder.setTitle("提示"); //设置标题
			        builder.setMessage("当前处于移动网络下，确定要下载吗？"); //设置内容
			        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
			            @Override
			            public void onClick(DialogInterface dialog, int which) {
			            	if(status == 0) { // 安装
								rollProgressbar = CommonUtil.showDialog(AppDetailActivity.this, "正在获取下载资源", true);
								doGetVersion(AppDetailActivity.this, appInfo);
							}
							if(status == 1) { // 更新
								rollProgressbar = CommonUtil.showDialog(AppDetailActivity.this, "正在获取下载资源", true);
								doGetVersion(AppDetailActivity.this, appInfo);
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
						rollProgressbar = CommonUtil.showDialog(AppDetailActivity.this, "正在获取下载资源", true);
						doGetVersion(AppDetailActivity.this, appInfo);
					}
					if(status == 1) { // 更新
						rollProgressbar = CommonUtil.showDialog(AppDetailActivity.this, "正在获取下载资源", true);
						doGetVersion(AppDetailActivity.this, appInfo);
					}
				}
				
				if(status == 2) { // 打开
					TDevice.openApp(AppDetailActivity.this, appInfo.getPackageName(), 
							appInfo.getPackageName() + appInfo.getApplicationLaunch());
				}
			}
		});
		
		appUninstall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TDevice.uninstallApk(getApplicationContext(), appInfo.getPackageName());
			}
		});
    }
    
    private void setBtnText() {
    	status = CommonUtil.getVersionStatus(appInfo.getApplicationNewVersion(), appInfo.getPackageName());
		if(status == 0) {
			appOperBtn.setText("安装");
			appUninstall.setVisibility(View.INVISIBLE);
		}
		if(status == 1) {
			appOperBtn.setText("更新");
			appUninstall.setVisibility(View.VISIBLE);
		}
		if(status == 2) {
			appOperBtn.setText("打开");
			appUninstall.setVisibility(View.VISIBLE);
		}
    }
    
    /**
	 * 获取版本
	 * @param context
	 * @param appVersionInfo
	 */
	private void doGetVersion(Context context, AppVersionInfo appVersionInfo) {
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
							CommonUtil.showToast(AppDetailActivity.this, "服务器或网络异常，请稍后重试");
						}
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
		i.setClass(AppDetailActivity.this,MainActivity.class);
		startActivity(i);
	}
	
    @Override
    public void initData() {
    }
    
    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imgPathList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final ImageView imageView = new ImageView(AppDetailActivity.this);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            // 加载图片
    		ImageLoader.getInstance().loadImage(imgPathList.get(position), CommonUtil.getImageConfig(),
    				new SimpleImageLoadingListener() {
    					@Override
    					public void onLoadingComplete(String imageUri, View view,
    							Bitmap loadedImage) {
    						super.onLoadingComplete(imageUri, view, loadedImage);
    						imageView.setImageBitmap(loadedImage);
    					}

    					public void onLoadingFailed(String imageUri, View view,
    							FailReason failReason) {
    						imageView.setImageBitmap(BitmapFactory
    								.decodeResource(getResources(),
    										R.drawable.home_bg));
    					};
    				});
            container.addView(imageView);

            
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }
    }
    
	@Override
	public void onClick(View v) {
	}

}
