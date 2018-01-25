package com.sinosoft.mobileshop.base;

import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sinosoft.mobileshop.interf.BaseFragmentInterface;

/**
 * 碎片基类（实现点击事件接口、基础碎片接口）
 * 
 * @created 2014年9月25日 上午11:18:46
 * 
 */
public class BaseFragment extends Fragment implements
        android.view.View.OnClickListener, BaseFragmentInterface {
	// 默认状态
    public static final int STATE_NONE = 0;
    // 刷新
    public static final int STATE_REFRESH = 1;
    // 加载更多
    public static final int STATE_LOADMORE = 2;
    // 没有更多
    public static final int STATE_NOMORE = 3;
    // 正在下拉但还没有到刷新的状态
    public static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    
    public static int mState = STATE_NONE;

    protected LayoutInflater mInflater;

    public Application getApplication() {
        return  getActivity().getApplication();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        this.mInflater = inflater;
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected int getLayoutId() {
        return 0;
    }

    protected View inflateView(int resId) {
        return this.mInflater.inflate(resId, null);
    }

    public boolean onBackPressed() {
        return false;
    }

    /**
     * 隐藏等待对话框
     */
    protected void hideWaitDialog() {
        FragmentActivity activity = getActivity();
//        if (activity instanceof DialogControl) {
//            ((DialogControl) activity).hideWaitDialog();
//        }
    }

//    /**
//     * 显示等待对话框
//     * @return
//     */
//    protected WaitDialog showWaitDialog() {
//        return showWaitDialog(R.string.loading);
//    }
//
//    /**
//     * 显示等待对话框
//     * @param resid
//     * @return
//     */
//    protected WaitDialog showWaitDialog(int resid) {
//        FragmentActivity activity = getActivity();
//        if (activity instanceof DialogControl) {
//            return ((DialogControl) activity).showWaitDialog(resid);
//        }
//        return null;
//    }
//
//    /**
//     * 显示等待对话框
//     * @param resid
//     * @return
//     */
//    protected WaitDialog showWaitDialog(String resid) {
//        FragmentActivity activity = getActivity();
//        if (activity instanceof DialogControl) {
//            return ((DialogControl) activity).showWaitDialog(resid);
//        }
//        return null;
//    }

    /**
     * 初始化视图
     */
    @Override
    public void initView(View view) {
    	
    }

    /**
     * 初始化数据
     */
    @Override
    public void initData() {

    }

    /**
     * 点击事件处理
     */
    @Override
    public void onClick(View v) {

    }
}
