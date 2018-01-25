package com.sinosoft.common.base.fragment;

import java.lang.annotation.Annotation;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sinosoft.common.base.presenter.RequirePresenter;
import com.sinosoft.common.base.presenter.SuperPresenter;
import com.sinosoft.common.base.widget.MaterialDialog;
import com.sinosoft.gyicPlat.R;
import com.sinosoft.util.Utils;

/**
 * Fragment顶级父类 : 添加各种状态(数据错误，数据为空，数据加载中)页的展示， 自定义的MaterialDialog的显示，进度条dialog显示
 *
 * MVP模型中把Fragment作为view层，可通过getPresenter()调用对应的presenter实例
 *
 * Created by 2016/8/6.
 */

public class SuperFragment<T extends SuperPresenter> extends Fragment {

	private final String TAG = "SuperFragment";
	private boolean isUseStatusPages = false;
	private int mLayoutResId;
	private View mView;

	private TextView mEmptyPage;
	private TextView mErrorPage;
	private LinearLayout mLoadingPage;
	private FrameLayout mContent;
	private View mCurrentShowView;
	private MaterialDialog mDialog;

	private ObjectAnimator mShowAnimator;
	private ObjectAnimator mHideAnimator;

	private T mPresenter;

	public SuperFragment() {
	}

	public SuperFragment(View fragment) {
		mView = fragment;
	}

	public SuperFragment(int layoutResID) {
		this.mLayoutResId = layoutResID;
	}

	public SuperFragment(int layoutResID, boolean isUseStatusPages) {
		this.mLayoutResId = layoutResID;
		this.isUseStatusPages = isUseStatusPages;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		attachPresenter();
	}

	// onCreateView之后
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (mPresenter != null)
			mPresenter.onCreate();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mPresenter != null)
			mPresenter.onResume();
	}

	public void attachPresenter() {
		Annotation[] annotations = getClass().getAnnotations();
		if (annotations.length > 0) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof RequirePresenter) {
					RequirePresenter presenter = (RequirePresenter) annotation;
					try {
						mPresenter = (T) presenter.value().newInstance();
						mPresenter.attachView(this);
					} catch (java.lang.InstantiationException e) {
						e.printStackTrace();
						Utils.Log("SuperFragment : " + e.getMessage());
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						Utils.Log("SuperFragment : " + e.getMessage());
					}
				}
			}
		}
	}

	public T getPresenter() {
		return mPresenter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (isUseStatusPages) {
			addStatusPage(inflater, container);
			return mView;
		} else {
			if (mLayoutResId != 0) {
				mView = inflater.inflate(mLayoutResId, container, false);
			}
			return mView;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onDestroy() {
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

	/**
	 * 添加各种状态页
	 */
	private void addStatusPage(LayoutInflater inflater, ViewGroup container) {
		mView = inflater.inflate(R.layout.base_status_page, null);
		mView.setLayoutParams(new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		mContent = (FrameLayout) mView.findViewById(R.id.content);
		inflater.inflate(mLayoutResId, mContent, true);

		mEmptyPage = findViewById(R.id.empty_page);
		mErrorPage = findViewById(R.id.error_page);
		mLoadingPage = findViewById(R.id.loading_page);
		mCurrentShowView = mLoadingPage;
	}

	public <T extends View> T findViewById(int resId) {
		return (T) mView.findViewById(resId);
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
	 * 展示一个对话框 : title,content,确定按钮，取消按钮
	 */
	public void showDialog(String title, String content,
			DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener passiveListener) {
		showDialog(title, content, null, null, positiveListener,
				passiveListener);
	}

	public void showDialog(String content, String positiveText,
			String passiveText,
			DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener passiveListener) {
		showDialog(null, content, positiveText, passiveText, positiveListener,
				passiveListener);
	}

	public void showDialog(String title, String content, String positiveText,
			String passiveText,
			DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener passiveListener) {
		if (mDialog == null) {
			mDialog = new MaterialDialog.Builder(getActivity()).setTitle(title)
					.setPositiveText(positiveText).setPassiveText(passiveText)
					.setOnPositiveClickListener(positiveListener)
					.setOnPassiveClickListener(passiveListener).create();
		}
		mDialog.show();
	}

	public void dismissDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}
}