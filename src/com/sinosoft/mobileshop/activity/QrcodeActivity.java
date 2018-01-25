package com.sinosoft.mobileshop.activity;

import android.view.View;

import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.base.BaseActivity;

/**
 * 二维码页面
 */
public class QrcodeActivity extends BaseActivity {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_qrcode;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    public void initView() {
    	setTitleBar("扫描下载", true);
    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View view) {

    }

}
