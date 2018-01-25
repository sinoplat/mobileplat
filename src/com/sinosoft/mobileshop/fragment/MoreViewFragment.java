package com.sinosoft.mobileshop.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.activity.AdvicesActivity;
import com.sinosoft.mobileshop.activity.QrcodeActivity;
import com.sinosoft.mobileshop.activity.SettingActivity;
import com.sinosoft.mobileshop.base.BaseFragment;

public class MoreViewFragment extends BaseFragment{
	
	private LinearLayout moreSetting;
	private LinearLayout moreAdvices;
	private LinearLayout moreQrcode;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_more, container, false);
		initView(view);
		initData();
		return view;
	}

	
	@Override
	public void initView(View view) {
		super.initView(view);
		moreSetting = (LinearLayout)view.findViewById(R.id.more_setting);
		moreAdvices = (LinearLayout)view.findViewById(R.id.more_advices);
		moreQrcode = (LinearLayout)view.findViewById(R.id.more_qrcode);
		
		moreSetting.setOnClickListener(this);
		moreAdvices.setOnClickListener(this);
		moreQrcode.setOnClickListener(this);
		
	}
	
	@Override
	public void initData() {
		super.initData();
	}	
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.more_setting:
			showSetting();
			break;
		case R.id.more_advices:
			showAdvices();
			break;
		case R.id.more_qrcode:
			showQrCode();
			break;
		default:
			break;
		}
	}
	
	private void showSetting() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), SettingActivity.class);
        getActivity().startActivity(intent);
    }
	
	private void showAdvices() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), AdvicesActivity.class);
        getActivity().startActivity(intent);
    }
	
	private void showQrCode() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), QrcodeActivity.class);
        getActivity().startActivity(intent);
    }
	
}
