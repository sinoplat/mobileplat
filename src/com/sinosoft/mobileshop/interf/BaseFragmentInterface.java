package com.sinosoft.mobileshop.interf;

import android.view.View;

/**
 * 基类fragment实现接口
 * 
 * @created 2014年9月25日 上午11:00:25
 *
 */
public interface BaseFragmentInterface {

	/**
	 * 初始化视图
	 * 
	 * @param view
	 */
	public void initView(View view);

	/**
	 * 初始化数据
	 */
	public void initData();
}
