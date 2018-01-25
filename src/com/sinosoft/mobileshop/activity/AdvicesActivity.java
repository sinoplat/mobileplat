package com.sinosoft.mobileshop.activity;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonObject;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.base.BaseActivity;
import com.sinosoft.mobileshop.bean.AppVersionInfo;
import com.sinosoft.mobileshop.bean.ReturnMsgDto;
import com.sinosoft.mobileshop.util.CommonUtil;
import com.sinosoft.mobileshop.util.DateUtil;
import com.sinosoft.mobileshop.util.JsonUtil;
import com.sinosoft.mobileshop.util.VolleyUtil;
import com.sinosoft.phoneGapPlugins.util.Constant;
import com.sinosoft.progressdialog.RollProgressbar;

/**
 * 意见反馈页面
 */
public class AdvicesActivity extends BaseActivity {

	private EditText advicesContent;
	private Button submitAdv;
	private String content;
	
    @Override
    protected int getLayoutId() {
        return R.layout.activity_advices;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    public void initView() {
    	setTitleBar("意见反馈", true);
    	advicesContent = (EditText) findViewById(R.id.advices_content_et);
    	submitAdv = (Button) findViewById(R.id.submit_adv_btn);
    	submitAdv.setOnClickListener(this);
    }

    @Override
    public void initData() {
    	
    }

    @Override
    public void onClick(View view) {
    	switch (view.getId()) {
		case R.id.submit_adv_btn:
			content = advicesContent.getText().toString();
			if(content == null || "".equals(content)) {
				showToast("请输入您的意见再提交");
				return;
			}
			doSaveAdvicesinfo();
			break;

		default:
			break;
		}
    }
    
    /**
	 * 保存意见反馈信息
	 */
	private void doSaveAdvicesinfo() {
		String feedBackId = DateUtil.formatDate(new Date(), "yyyyMMddHHmmss");
		String content = advicesContent.getText().toString();
		final RollProgressbar rollProgressbar = CommonUtil.showDialog(AdvicesActivity.this, "正在保存反馈信息", true);
		String jsonStr = "jsonstr={\"userCode\":\"0000000000\",\"mobileNo\":\"0000000000\","
				+ "\"feedBackID\":\"" + feedBackId + "\","
				+ "\"content\":\"" + content + "\""
				+ "}";
		String url = Constant.SAVEADVICEINFOURL + jsonStr;
		RequestQueue mRequestQueue = VolleyUtil.getVolleySingleton(getApplicationContext()).getRequestQueue();
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						try {
							String jsonStr = response.getString("jsonstr");
							JSONObject msgDto = new JSONObject(jsonStr);
							ReturnMsgDto res = (ReturnMsgDto)JsonUtil.strToBean(msgDto.get("ReturnMsgDto").toString(), ReturnMsgDto.class);
							if("1".equals(res.getResultCode())) {
								CommonUtil.showToast(AdvicesActivity.this, "意见反馈提交成功");
								advicesContent.setText("");
							} else {
								CommonUtil.showToast(AdvicesActivity.this, "提交失败，网络或服务器异常，请稍后重试");
							}
						} catch (JSONException e) {
							e.printStackTrace();
							CommonUtil.showToast(AdvicesActivity.this, "提交失败，网络或服务器异常，请稍后重试");
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
						}
						CommonUtil.showToast(AdvicesActivity.this, "提交失败，网络或服务器异常，请稍后重试");
					};
				});
		
		mRequestQueue.add(jsonObjectRequest);
	}


}
