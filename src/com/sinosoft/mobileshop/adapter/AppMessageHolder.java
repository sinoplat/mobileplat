package com.sinosoft.mobileshop.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.activity.NewsDetailActivity;
import com.sinosoft.mobileshop.appwidget.recycler.adapter.BaseViewHolder;
import com.sinosoft.mobileshop.bean.AppMessage;
import com.sinosoft.progressdialog.RollProgressbar;

public class AppMessageHolder extends BaseViewHolder<AppMessage> {

	private ImageView msgRead;
	private ImageView msgIcon;
	private TextView msgTitle;
	private TextView msgDate;
	private TextView msgContent;
	private RollProgressbar rollProgressbar;
	
    public AppMessageHolder(ViewGroup parent) {
		super(parent, R.layout.holder_appmessage);
	}

	@Override
	public void onInitializeView() {
		super.onInitializeView();
		msgRead = findViewById(R.id.msg_read_iv);
		msgIcon = findViewById(R.id.msg_icon_iv);
		msgTitle = findViewById(R.id.msg_title_tv);
		msgDate = findViewById(R.id.msg_date_tv);
		msgContent = findViewById(R.id.msg_content_tv);
	}

	@Override
	public void setData(final AppMessage object) {
		super.setData(object);
		
		if(object != null && "0".equals(object.getReadFlag())) {
			msgRead.setVisibility(View.VISIBLE);
		} else {
			msgRead.setVisibility(View.INVISIBLE);
		}
		msgTitle.setText(object.getMessageTitle());
		msgDate.setText(object.getOperateDate());
		msgContent.setText(object.getMessageContent());

	}

	@Override
	public void onItemViewClick(AppMessage object) {
		super.onItemViewClick(object);
		
		Intent intent = new Intent(itemView.getContext(), NewsDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("appMessage", object);
		intent.putExtras(bundle);
		itemView.getContext().startActivity(intent);
	}
	
	
}