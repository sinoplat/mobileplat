package com.sinosoft.mobileshop.activity;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.appwidget.TitleBar;
import com.sinosoft.mobileshop.base.BaseActivity;
import com.sinosoft.mobileshop.bean.AppMessage;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.LiteOrmUtil;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;

/**
 * 消息详细页面
 */
public class NewsDetailActivity extends BaseActivity {

	private AppMessage appMessage;
	private TextView msgDetailTitle;
	private TextView msgDetailContent;
	
	private RollProgressbar rollBar;
	
    @Override
    protected int getLayoutId() {
        return R.layout.activity_news_detail;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    public void initView() {
    	setTitleBar("消息详情", true);
    	Intent intent = this.getIntent(); 
    	appMessage = (AppMessage)intent.getSerializableExtra("appMessage");
    	msgDetailTitle = (TextView)findViewById(R.id.msg_detail_title_tv);
    	msgDetailContent = (TextView)findViewById(R.id.msg_detail_context_tv);
    	
    	msgDetailTitle.setText(appMessage.getMessageTitle());
    	msgDetailContent.setText("\u3000\u3000" + appMessage.getMessageContent());
    	
    	addDelBtn();
    }

    @Override
    public void initData() {
    	if("0".equals(appMessage.getReadFlag())) {
    		appMessage.setReadFlag("1");
    		LiteOrmUtil.getLiteOrm(getApplicationContext()).update(appMessage);
    		operateReadMsg(appMessage);
    	}
    }
    
    private void operateReadMsg(AppMessage appMessage) {
		String url = Constant.OPERMSGURL + 
					"messageId="+appMessage.getMessageID()+"&userCode=0000000000&operateType=read";
		RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(getApplicationContext()).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
					};
				}, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {
					};
				});
		mRequestQueue.add(jsonObjectRequest);
	}
    
    private void operateDelMsg(AppMessage appMessage) {
		String url = Constant.OPERMSGURL + 
					"messageId="+appMessage.getMessageID()+"&userCode=0000000000&operateType=delete";
		RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(getApplicationContext()).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					String result = "";
					public void onResponse(JSONObject response) {
						try {
							result = response.getString("ResultCode");
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if(rollBar != null) {
								rollBar.disProgressBar();
							}
							if("1".equals(result)) {
								showToast("删除成功");
								finish();
							} else {
								showToast("网络或服务器异常，请稍后重试");
							}
						}
					};
				}, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						if(rollBar != null) {
							rollBar.disProgressBar();
						}
						showToast("网络或服务器异常，请稍后重试");
					};
				});
		mRequestQueue.add(jsonObjectRequest);
	}
    
    private void addDelBtn() {
    	titleBar.setActionTextColor(Color.RED);
		titleBar.addAction(new TitleBar.TextAction("删 除") {
			@Override
			public void performAction(View view) {
				rollBar = CommonUtil.showDialog(NewsDetailActivity.this, "正在删除，请稍后..", true);
				LiteOrmUtil.getLiteOrm(getApplicationContext()).delete(appMessage);
				operateDelMsg(appMessage);
			}
		});
    }

	@Override
	public void onClick(View v) {
	}

}
