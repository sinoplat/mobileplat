package com.sinosoft.mobileshop.fragment;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.adapter.AppMessageAdapter;
import com.sinosoft.mobileshop.appwidget.recycler.RefreshRecyclerView;
import com.sinosoft.mobileshop.base.BaseFragment;
import com.sinosoft.mobileshop.bean.AppMessage;
import com.sinosoft.mobileshop.util.JsonUtil;
import com.sinosoft.mobileshop.util.LiteOrmUtil;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.way.pattern.App;

public class NewsListViewFragment extends BaseFragment {

	private RefreshRecyclerView mRecyclerView;
	private AppMessageAdapter mAdapter;
	private Handler handler;
	private Context context;
	private boolean isReload = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_newslist, container, false);
		initView(view);
		initData();
		IntentFilter clearFile = new IntentFilter();
		clearFile.addAction("com.sinosoft.msg.clear");//添加动态广播的Action
		view.getContext().registerReceiver(broadcastReceiver, clearFile);
		
		return view;
	}

	@Override
	public void initView(View view) {
		super.initView(view);
		handler = new Handler();
		context = view.getContext();
		mAdapter = new AppMessageAdapter(view.getContext());
		mRecyclerView = (RefreshRecyclerView) view.findViewById(R.id.news_recycler_view);
		mRecyclerView.setSwipeRefreshColors(0xFF437845, 0xFFE44F98, 0xFF2FAC21);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
		mRecyclerView.setAdapter(mAdapter);
	}

	@Override
	public void initData() {
		super.initData();
		mRecyclerView.post(new Runnable() {
			@Override
			public void run() {
				mRecyclerView.showSwipeRefresh();
				doPost();
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(isReload = true) {
			getData();
		}
	}
	
	private void doPost() {
		String emptyTime = App.getPreferences().getString("emptyTime", "2015-01-01 00:00:00");
		String url = Constant.GETMESSAGEURL + 
				"userCode=0000000000&emptyTime=" + emptyTime;
		RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(context).getRequestQueue();
		
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						try {
							String data = response.getString("data");
							List<AppMessage> appMsgList = JsonUtil.strToBeanList(data, AppMessage.class);
							LiteOrmUtil.getLiteOrm(context).save(appMsgList);
						} catch (JSONException e) {
							e.printStackTrace();
						} finally {
							getData();
						}
					};
				}, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						getData();
					};
				});
		mRequestQueue.add(jsonObjectRequest);
	}
	
	private void getData() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mAdapter.clear();
				List<AppMessage> appMsgList = LiteOrmUtil.getLiteOrm(context).query(AppMessage.class);
				mAdapter.addAll(appMsgList);
				mAdapter.isLoadEnd = true;
				mAdapter.showNoMoreAndHidden();
				mRecyclerView.dismissSwipeRefresh();
				isReload = true;
			}
		}, 500);
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
