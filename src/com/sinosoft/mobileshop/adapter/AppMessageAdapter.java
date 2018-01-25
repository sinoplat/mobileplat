package com.sinosoft.mobileshop.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.sinosoft.mobileshop.appwidget.recycler.adapter.BaseViewHolder;
import com.sinosoft.mobileshop.appwidget.recycler.adapter.RecyclerAdapter;
import com.sinosoft.mobileshop.bean.AppMessage;
import com.sinosoft.mobileshop.bean.AppVersionInfo;

public class AppMessageAdapter extends RecyclerAdapter<AppMessage> {

	public AppMessageAdapter(Context context) {
		super(context);
	}

	@Override
	public BaseViewHolder<AppMessage> onCreateBaseViewHolder(
			ViewGroup parent, int viewType) {
		return new AppMessageHolder(parent);
	}

}