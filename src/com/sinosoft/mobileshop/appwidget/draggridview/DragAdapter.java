package com.sinosoft.mobileshop.appwidget.draggridview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.bean.AppOrderRel;
import com.sinosoft.mobileshop.bean.AppUploadFile;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.LiteOrmUtil;

/**
 * @author Administrator
 */
public class DragAdapter extends BaseAdapter implements DragGridBaseAdapter{
	private List<HashMap<String, Object>> list;
	private LayoutInflater mInflater;
	private int mHidePosition = -1;
	private Context context;
	
	public DragAdapter(Context context, List<HashMap<String, Object>> list){
		this.list = list;
		mInflater = LayoutInflater.from(context);
		this.context = context;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 由于复用convertView导致某些item消失了，所以这里不复用item，
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(R.layout.grid_item, null);
		final ImageView mImageView = (ImageView) convertView.findViewById(R.id.item_image);
		TextView mTextView = (TextView) convertView.findViewById(R.id.item_text);
		
		mTextView.setText((CharSequence) list.get(position).get("item_text"));
		
		// 显示图片
		HashMap<String, Object> dataMap = list.get(position);
		AppVersionInfo appInfo = (AppVersionInfo)dataMap.get("app_info");
		if(appInfo != null) {
			List<AppUploadFile> reFileList = appInfo.getReFileList();
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
			.error(R.drawable.icon).into(mImageView);

			// 加载图片
//			ImageLoader.getInstance().loadImage(iconUrl, CommonUtil.getImageConfig(),
//					new SimpleImageLoadingListener() {
//						@Override
//						public void onLoadingComplete(String imageUri, View view,
//								Bitmap loadedImage) {
//							super.onLoadingComplete(imageUri, view, loadedImage);
//							mImageView.setImageBitmap(loadedImage);
//						}
//
//						public void onLoadingFailed(String imageUri, View view,
//								FailReason failReason) {
//							mImageView.setImageBitmap(BitmapFactory
//									.decodeResource(view.getResources(),
//											R.drawable.icon));
//							mImageView.setImageResource(R.drawable.icon);
//						};
//					});
		}
		
		if(position == mHidePosition){
			convertView.setVisibility(View.INVISIBLE);
		}
		
		return convertView;
	}

	@Override
	public void reorderItems(int oldPosition, int newPosition) {
		HashMap<String, Object> temp = list.get(oldPosition);
		if(oldPosition < newPosition){
			for(int i=oldPosition; i<newPosition; i++){
				Collections.swap(list, i, i+1);
			}
		}else if(oldPosition > newPosition){
			for(int i=oldPosition; i>newPosition; i--){
				Collections.swap(list, i, i-1);
			}
		}
		
		list.set(newPosition, temp);
		
		List<AppOrderRel> appOrderList = new ArrayList<AppOrderRel>();
		// 重新排序后，更新排序表
		for(int i=0; i<list.size(); i++) {
			AppVersionInfo appInfo = (AppVersionInfo)list.get(i).get("app_info");
			AppOrderRel appOrderRel = new AppOrderRel();
			appOrderRel.setApplicationNo(appInfo.getApplicationNo());
			appOrderRel.setSerialNo((i+1)+"");
			appOrderList.add(appOrderRel);
		}
		
		LiteOrmUtil.getLiteOrm(context).deleteAll(AppOrderRel.class);
		LiteOrmUtil.getLiteOrm(context).save(appOrderList);
	}

	@Override
	public void setHideItem(int hidePosition) {
		this.mHidePosition = hidePosition; 
		notifyDataSetChanged();
	}


}
