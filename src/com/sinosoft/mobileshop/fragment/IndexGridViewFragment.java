package com.sinosoft.mobileshop.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.sangfor.ssl.SangforAuth;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.appwidget.dialog.StytledDialog;
import com.sinosoft.mobileshop.appwidget.draggridview.DragAdapter;
import com.sinosoft.mobileshop.appwidget.draggridview.DragGridView;
import com.sinosoft.mobileshop.base.BaseFragment;
import com.sinosoft.mobileshop.bean.AppOrderRel;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.bean.AssistApp;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.LiteOrmUtil;
import com.sinosoft.mobileshop.util.TDevice;
import com.sinosoft.phoneGapPlugins.util.Constant;

@SuppressLint("NewApi")
public class IndexGridViewFragment extends BaseFragment {

	private List<HashMap<String, Object>> dataSourceList = new ArrayList<HashMap<String, Object>>();
	private DragGridView mDragGridView = null;
	private Context context;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_main, container,
				false);
		initView(view);
		initData();
		return view;
	}
	
	@Override
	public void initView(View view) {
		super.initView(view);
		context = view.getContext();
		mDragGridView = (DragGridView) view.findViewById(R.id.dragGridView);
		
		mDragGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				HashMap<String, Object> dataMap = dataSourceList.get(position);
				AppVersionInfo appInfo = (AppVersionInfo)dataMap.get("app_info");
				int status = (Integer)dataMap.get("app_status");
				
				if("3".equals(appInfo.getApplicationStatus())) {
					StytledDialog.showMdAlert(context, "提醒", "该应用已下线，请卸载。", "确定", true, true, null);
					return;
				}
				if("4".equals(appInfo.getApplicationStatus())) {
					StytledDialog.showMdAlert(context, "提醒", "该应用已锁定，不能使用。", "确定", true, true, null);
					return;
				}
				if(status == 1) { //更新
					StytledDialog.showMdAlert(context, "提醒", "APP有新版本，请进行更新", "确定", true, true, null);
					return;
				}
				if(status == 2) { //打开
					if(Constant.VPNSTATUS == 5) {
						TDevice.openApp(view.getContext(), appInfo.getPackageName(), 
								appInfo.getPackageName() + appInfo.getApplicationLaunch());
					} else {
						StytledDialog.showMdAlert(context, "提醒", "VPN未连接，请手工连接或重新启动移动应用平台", "确定", true, true, null);
					}
				}
			}
		});
	}

	@Override
	public void initData() {
		super.initData();
		if(dataSourceList != null) {
			dataSourceList.clear();
		}
		List<AppVersionInfo> appInfoList = new ArrayList<AppVersionInfo>();
		List<AppVersionInfo> appInfoNewList = LiteOrmUtil.getLiteOrm(context).query(AppVersionInfo.class);
		List<AppOrderRel> appOrderRelList = LiteOrmUtil.getLiteOrm(context).query(AppOrderRel.class);
		List<AssistApp> assistAppList = LiteOrmUtil.getLiteOrm(context).query(AssistApp.class);
		Map<String, Integer> assAppMap = new HashMap<String, Integer>();
		if(assistAppList != null && assistAppList.size() > 0) {
			for (AssistApp assistApp : assistAppList) {
				assAppMap.put(assistApp.getPackageName(), assistApp.getId());
				Log.i("syso", assistApp.getPackageName()+"--------------:"+assistApp.getId());
			}
		}
		Map<String, String> orderAppMap = new HashMap<String, String>();
		if(appOrderRelList != null && appOrderRelList.size() > 0) {
			for (AppOrderRel appOrderRel : appOrderRelList) {
				orderAppMap.put(appOrderRel.getApplicationNo(), appOrderRel.getSerialNo());
			}
		}
		
        if(appInfoNewList != null && appInfoNewList.size() > 0) {
        	for (AppVersionInfo appVersionInfo : appInfoNewList) {
        		if(orderAppMap.containsKey(appVersionInfo.getApplicationNo())) {
        			appVersionInfo.setResultCode(orderAppMap.get(appVersionInfo.getApplicationNo()));
        		} else {
        			appVersionInfo.setResultCode("100");
        		}
        		appInfoList.add(appVersionInfo);
			}
        }
        
        //排序
		Comparator<AppVersionInfo> comparator = new Comparator<AppVersionInfo>() {
			public int compare(AppVersionInfo s1, AppVersionInfo s2) {
				// 先排年龄
				if (!s1.getResultCode().equals(s2.getResultCode())) {
					return s1.getResultCode().compareTo(s2.getResultCode());
				} else {
					return s1.getApplicationName().compareTo(s2.getApplicationName());
				}
			}
		};
		Collections.sort(appInfoList,comparator);
        
		if(appInfoList != null && appInfoList.size() > 0) {
			for (AppVersionInfo appVersionInfo : appInfoList) {
				int status = CommonUtil.getVersionStatus(appVersionInfo.getApplicationNewVersion(), 
						appVersionInfo.getPackageName());
				if((status == 1 || status == 2) && !assAppMap.containsKey(appVersionInfo.getPackageName())) { //1 更新   2打开
					HashMap<String, Object> itemHashMap = new HashMap<String, Object>();
					itemHashMap.put("item_image",R.drawable.icon);
					itemHashMap.put("item_text", appVersionInfo.getApplicationName());
					itemHashMap.put("app_info", appVersionInfo);
					itemHashMap.put("app_status", status);
					dataSourceList.add(itemHashMap);
				}
			}
		}
		
		
		DragAdapter mDragAdapter = new DragAdapter(context, dataSourceList);
		mDragGridView.setAdapter(mDragAdapter);
		
		mDragGridView.setOnDragListener(new OnDragListener() {
			@Override
			public boolean onDrag(View v, DragEvent event) {
				return false;
			}
		});
	}


}
