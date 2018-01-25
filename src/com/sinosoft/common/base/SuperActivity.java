package com.sinosoft.common.base;

import java.lang.annotation.Annotation;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.sinosoft.common.base.presenter.RequirePresenter;
import com.sinosoft.common.base.presenter.SuperPresenter;
import com.sinosoft.common.base.widget.MaterialDialog;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.util.Utils;

/**
 * Activity顶级父类 : 添加各种状态(数据错误，数据为空，数据加载中)页的展示， 自定义的MaterialDialog的显示，进度条dialog显示
 *
 * MVP模型中把Activity作为view层，可通过getPresenter()调用对应的presenter实例
 *
 * Created by on 2016/8/3.
 */

public class SuperActivity<P extends SuperPresenter> extends Activity {

	private boolean isUseStatusPages = false;

	protected TextView mEmptyPage;
	protected TextView mErrorPage;
	protected LinearLayout mLoadingPage;
	protected FrameLayout mContent;
	protected View mCurrentShowView;

	private MaterialDialog mDialog;
	private AlertDialog mLoadingDialog;

	private ObjectAnimator mShowAnimator;
	private ObjectAnimator mHideAnimator;

	private P mPresenter;

	// 在setContentView()之前调用，如果activity的toolbar功能会受到其他view影响则不能使用状态页，如：带DrawLayout的toolbar
	public void useStatusPages(boolean show) {
		isUseStatusPages = show;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		attachPresenter();
	}

	// 在onStart之后回调
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (mPresenter != null) {
			mPresenter.onCreate();
		}

	}

	// 在onResume之后回调
	@Override
	protected void onPostResume() {
		super.onPostResume();
		if (mPresenter != null) {
			mPresenter.onResume();
		}
	}

	public void attachPresenter() {
		Annotation[] annotations = getClass().getAnnotations();
		if (annotations.length > 0) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof RequirePresenter) {
					RequirePresenter presenter = (RequirePresenter) annotation;
					try {
						mPresenter = (P) presenter.value().newInstance();
						mPresenter.attachView(this);
					} catch (InstantiationException e) {
						e.printStackTrace();
						Utils.Log("SuperActivity : " + e.getMessage());
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						Utils.Log("SuperActivity : " + e.getMessage());
					}
				}
			}
		}
	}

	public P getPresenter() {
		return mPresenter;
	}

	@Override
	public void setContentView(int layoutResID) {
		if (isUseStatusPages) {
			addStatusPage(layoutResID);
		} else
			super.setContentView(layoutResID);
	}

	/**
	 * 添加各种状态页
	 *
	 * @param contentID
	 */
	private void addStatusPage(int contentID) {
		FrameLayout mDecorContent = (FrameLayout) getWindow().getDecorView()
				.findViewById(android.R.id.content);
		getLayoutInflater().inflate(R.layout.base_status_page, mDecorContent,
				true);
		mContent = (FrameLayout) mDecorContent.findViewById(R.id.content); // Activity的content
		getLayoutInflater().inflate(contentID, mContent, true); // 把activity要显示的xml加载到mContent布局里

		mEmptyPage = (TextView) mDecorContent.findViewById(R.id.empty_page); // 事实说明view状态时GONE也可以findViewById()
		mErrorPage = (TextView) mDecorContent.findViewById(R.id.error_page);
		mLoadingPage = (LinearLayout) mDecorContent
				.findViewById(R.id.loading_page);
		mCurrentShowView = mLoadingPage;
	}

	public boolean isUseStatusPages() {
		return isUseStatusPages;
	}

	public void showEmpty() {
		showView(mEmptyPage);
	}

	public void showError() {
		showView(mErrorPage);
	}

	public void showLoading() {
		showView(mLoadingPage);
	}

	public void showContent() {
		showView(mContent);
	}

	public void showView(View view) {
		hideViewWithAnimation(mCurrentShowView);
		mCurrentShowView = view;
		view.setVisibility(View.VISIBLE);
		showViewWithAnimation(view);
	}

	/**
	 * 展示状态页添加动画
	 *
	 * @param view
	 */
	@SuppressLint("NewApi")
	public void showViewWithAnimation(View view) {
		if (mShowAnimator != null) {
			mShowAnimator.end();
			mShowAnimator.cancel();
			mShowAnimator = null;
		}
		mShowAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
		mShowAnimator.setDuration(400);
		mShowAnimator.start();
	}

	/**
	 * 隐藏状态页添加动画
	 *
	 * @param view
	 */
	@SuppressLint("NewApi")
	public void hideViewWithAnimation(View view) {
		if (mHideAnimator != null) {
			mHideAnimator.end();
			mHideAnimator.cancel();
			mHideAnimator = null;
		}
		mHideAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
		mHideAnimator.setDuration(400);
		mHideAnimator.start();
		view.setVisibility(View.GONE);
	}

	/**
	 * 显示进度条的dialog
	 */
	public void showLoadingDialog() {
		if (mLoadingDialog == null) {
			ProgressBar progressBar = new ProgressBar(this);
			progressBar.setPadding(Utils.dip2px(16), Utils.dip2px(16),
					Utils.dip2px(16), Utils.dip2px(16));
			progressBar.setBackgroundResource(android.R.color.transparent);
			mLoadingDialog = new AlertDialog.Builder(this).setView(progressBar)
					.create();
		}
		mLoadingDialog.show();
	}

	public void dismissLoadingDialog() {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
			mLoadingDialog = null;
		}
	}

	/**
	 * 展示一个对话框 : title,content,确定按钮，取消按钮
	 */
	public void showDialog(String title,
			DialogInterface.OnClickListener positiveListener) {
		showDialog(title, null, null, false, positiveListener, null);
	}

	public void showDialog(String title, boolean cancleAble,
			DialogInterface.OnClickListener positiveListener) {
		showDialog(title, null, null, cancleAble, positiveListener, null);
	}

	public void showDialog(String title,
			DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener passiveListener) {
		showDialog(title, null, null, false, positiveListener, passiveListener);
	}

	public void showDialog(String title, boolean cancleAble,
			DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener passiveListener) {
		showDialog(title, null, null, cancleAble, positiveListener,
				passiveListener);
	}

	public void showDialog(String title, String positiveText,
			String passiveText, boolean cancelAble,
			DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener passiveListener) {
		if (mDialog == null) {
			mDialog = new MaterialDialog.Builder(this).setTitle(title)
					.setPositiveText(positiveText).setPassiveText(passiveText)
					.setOnPositiveClickListener(positiveListener)
					.setOnPassiveClickListener(passiveListener)
					.setCancelable(cancelAble).create();
		}
		mDialog.show();
	}

	public void dismissDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	@Override
	@SuppressLint("NewApi")
	protected void onDestroy() {
		super.onDestroy();
		if (mShowAnimator != null) {
			mShowAnimator.cancel();
		}
		if (mHideAnimator != null) {
			mHideAnimator.cancel();
		}
		if (mPresenter != null) {
			mPresenter.onDestroy();
		}
		mPresenter = null;

	}
}