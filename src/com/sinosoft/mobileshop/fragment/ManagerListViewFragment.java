package com.sinosoft.mobileshop.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.adapter.AppInfoAdapter;
import com.sinosoft.mobileshop.appwidget.recycler.RefreshRecyclerView;
import com.sinosoft.mobileshop.base.BaseFragment;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.JsonUtil;
import com.sinosoft.mobileshop.util.LiteOrmUtil;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;

public class ManagerListViewFragment extends BaseFragment {

	private RefreshRecyclerView mRecyclerView;
	private AppInfoAdapter mAdapter;
	private Handler handler;
	private Context context;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_managerlist, container,
				false);
		initView(view);
		initData();
		
		IntentFilter install = new IntentFilter();
		install.addAction("com.sinosoft.msg.install");//添加动态广播的Action
		view.getContext().registerReceiver(broadcastReceiver, install);
		
		return view;
	}

	@Override
	public void initView(View view) {
		super.initView(view);

		handler = new Handler();
		context = view.getContext();
		mAdapter = new AppInfoAdapter(view.getContext());
		mRecyclerView = (RefreshRecyclerView) view.findViewById(R.id.appinfo_recycler_view);
		mRecyclerView.setSwipeRefreshColors(0xFF437845, 0xFFE44F98, 0xFF2FAC21);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
		mRecyclerView.setAdapter(mAdapter);
//		mRecyclerView.setRefreshAction(new Action() {
//			@Override
//			public void onAction() {
//				doPost();
//			}
//		});

//		mRecyclerView.setLoadMoreAction(new Action() {
//			@Override
//			public void onAction() {
//				getData(false);
//				page++;
//			}
//		});
//
		mRecyclerView.post(new Runnable() {
			@Override
			public void run() {
				mRecyclerView.showSwipeRefresh();
				doPost();
			}
		});
	}

	@Override
	public void initData() {
		super.initData();
	}

	private void getData() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mAdapter.clear();
				List<AppVersionInfo> appInfoList = LiteOrmUtil.getLiteOrm(context).query(AppVersionInfo.class);
				List<AppVersionInfo> appList = new ArrayList<AppVersionInfo>();
				for (AppVersionInfo appVersionInfo : appInfoList) {
        			if(Constant.GYICPACKAGE.equals(appVersionInfo.getPackageName())) {
        				continue;
        			}
        			appList.add(appVersionInfo);
				}
				mAdapter.addAll(appList);
				mAdapter.isLoadEnd = true;
				mAdapter.showNoMoreAndHidden();
				mRecyclerView.dismissSwipeRefresh();
			}
		}, 1000);
	}

	private void doPost() {
		String url = Constant.GETAPPLIST + 
				"jsonstr={\"UserCode\":\"0000000000\",\"OptUserCode\":\"0000000000\",\"OptPackageName\":\"\",\"OS\":\"1\"}";
		RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(context).getRequestQueue();
		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,  new Response.Listener<JSONArray>(){
            private List<AppVersionInfo> appVersionList;
            private List<AppVersionInfo> appList = new ArrayList<AppVersionInfo>();

			@Override
            public void onResponse(JSONArray response) {
            	if(response != null && response.length() > 0) {
            		appVersionList = JsonUtil.jsonToBeanList(response, AppVersionInfo.class);
            		LiteOrmUtil.getLiteOrm(context).deleteAll(AppVersionInfo.class);
            		LiteOrmUtil.getLiteOrm(context).save(appVersionList);
            	}
            	getData();
			}
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                CommonUtil.showToast(context, "网络或服务器异常，请检查");
                getData();
            }
        });
		jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(1000*60*2,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		mRequestQueue.add(jsonArrayRequest);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(broadcastReceiver);
	}
	
	// 消息接收器
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			getData();
		};
	};

}
