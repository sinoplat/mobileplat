package com.sinosoft.mobileshop.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.sinosoft.mobileshop.appwidget.recycler.adapter.BaseViewHolder;
import com.sinosoft.mobileshop.appwidget.recycler.adapter.RecyclerAdapter;
import com.sinosoft.mobileshop.bean.AppVersionInfo;

public class AppInfoAdapter extends RecyclerAdapter<AppVersionInfo> {
	private Context context;

	public AppInfoAdapter(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public BaseViewHolder<AppVersionInfo> onCreateBaseViewHolder(
			ViewGroup parent, int viewType) {
		return new AppInfoHolder(parent,context);
	}

}