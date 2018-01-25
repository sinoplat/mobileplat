package com.sinosoft.mobileshop.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.sinosoft.gyicPlat.R;
import com.sinosoft.mobileshop.appwidget.CommonToast;
import com.sinosoft.mobileshop.appwidget.TitleBar;
import com.sinosoft.mobileshop.interf.BaseViewInterface;
import com.sinosoft.mobileshop.util.CommonUtil;

/**
 * 基础Activity
 */
@SuppressLint("NewApi")
public abstract class BaseActivity extends Activity implements
        View.OnClickListener, BaseViewInterface {

    private boolean _isVisible;
    private ProgressDialog _waitDialog;
    protected TitleBar titleBar;
    
    protected LayoutInflater mInflater;
    
	private int textColor = Color.parseColor("#f2f2f2");

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        onBeforeSetContentLayout();
		if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }
        mInflater = getLayoutInflater();
        if (hasActionBar()) {
        }
        init(savedInstanceState);
        initTitleBar();
        initView();
        initData();
        _isVisible = true;
    }
    
    private void initTitleBar() {
		boolean isImmersive = false;
//		if (CommonUtil.hasKitKat() && !CommonUtil.hasLollipop()) {
//			isImmersive = true;
//			// 透明状态栏
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//		} else if (CommonUtil.hasLollipop()) {
//			Window window = getWindow();
//			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//			isImmersive = true;
//		}
		if(titleBar == null) {
			titleBar = (TitleBar) findViewById(R.id.title_bar);
		}
		titleBar.setImmersive(isImmersive);
		titleBar.setBackgroundColor(Color.parseColor("#28282a"));
		
	}

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onBeforeSetContentLayout() {
    }

    protected boolean hasActionBar() {
        return true;
    }

    protected int getLayoutId() {
        return 0;
    }

    protected View inflateView(int resId) {
        return mInflater.inflate(resId, null);
    }

    protected boolean hasBackButton() {
        return false;
    }

    protected void init(Bundle savedInstanceState) {
    }

    public void setActionBarTitle(int resId) {
        if (resId != 0) {
            setActionBarTitle(getString(resId));
        }
    }
    
    /**
     * 设置标题栏
     * @param title
     * @param hasBackBtn
     */
    public void setTitleBar(String title, boolean hasBackBtn) {
    	if(hasBackBtn) {
	    	titleBar.setLeftImageResource(R.drawable.back_green);
			titleBar.setLeftText("返回");
			titleBar.setLeftTextColor(textColor);
			titleBar.setLeftClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
    	}
		titleBar.setTitle(title);
		titleBar.setTitleColor(textColor);
    }

    /**
     * 增加标题栏按钮
     */
    public void addTitleButton() {
		titleBar.setActionTextColor(textColor);
		
//		mCollectView = (ImageView) titleBar.addAction(new TitleBar.ImageAction(R.drawable.collect) {
//		@Override
//		public void performAction(View view) {
//			Toast.makeText(MainActivity.this, "点击了收藏", Toast.LENGTH_SHORT).show();
//			mCollectView.setImageResource(R.drawable.fabu);
//			titleBar.setTitle(mIsSelected ? "文章详情\n朋友圈" : "帖子详情");
//			mIsSelected = !mIsSelected;
//		}
//	});

//	titleBar.addAction(new TitleBar.TextAction("连接VPN") {
//		@Override
//		public void performAction(View view) {
//			Toast.makeText(MainActivity.this, "连接VPN", Toast.LENGTH_SHORT).show();
//		}
//	});
    }
    
    /**
     * 设置TitleBar标题
     * @param title
     */
    public void setActionBarTitle(String title) {
        if (hasActionBar() && titleBar != null) {
        	
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showToast(String message) {
        Toast.makeText(BaseActivity.this, message, Toast.LENGTH_LONG).show();
    }
    
    public void showToast(int msgResid, int icon, int gravity) {
        showToast(getString(msgResid), icon, gravity);
    }

    public void showToast(String message, int icon, int gravity) {
        CommonToast toast = new CommonToast(this);
        toast.setMessage(message);
        toast.setMessageIc(icon);
        toast.setLayoutGravity(gravity);
        toast.show();
    }

    public ProgressDialog showWaitDialog() {
//        return showWaitDialog(R.string.loading);
    	return null;
    }

    public ProgressDialog showWaitDialog(int resid) {
//        return showWaitDialog(getString(resid));
    	return null;
    }

    public ProgressDialog showWaitDialog(String message) {
//        if (_isVisible) {
//            if (_waitDialog == null) {
//                _waitDialog = DialogHelp.getWaitDialog(this, message);
//            }
//            if (_waitDialog != null) {
//                _waitDialog.setMessage(message);
//                _waitDialog.show();
//            }
//            return _waitDialog;
//        }
        return null;
    }

    public void hideWaitDialog() {
//        if (_isVisible && _waitDialog != null) {
//            try {
//                _waitDialog.dismiss();
//                _waitDialog = null;
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
    }
}
