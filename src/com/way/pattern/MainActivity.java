package com.way.pattern;

import org.apache.cordova.CordovaActivity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;


public class MainActivity extends CordovaActivity{
	
	@JavascriptInterface
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
         super.init();
       // this.appView.setBackgroundResource(R.drawable.scrollbar_handle_vertical_99);//设置背景图片
       // super.setIntegerProperty("splashscreen",R.drawable.scrollbar_handle_vertical_99); //设置闪屏背景图片
        super.loadUrl("file:///android_asset/www/index.html");    //经过测试500毫秒比较合适
               //在该方法中增加js操作java的接口,this为当前对象，js1为操作java文件的javascript的名字
                appView.addJavascriptInterface(this, "js1");
	}

	//跳转到设置手势密码页面
	@JavascriptInterface
	public void createGesturePassWord() {
		Intent intent = new Intent();
		//intent.putExtra("name", str);
		//intent.putExtra("pass", str);
		intent.setClass(MainActivity.this, GuideGesturePasswordActivity.class);
		startActivity(intent);
	}
	
	//跳转到手势密码验证页面
	@JavascriptInterface
	public void unlockGesturePassWord() {
		Intent intent = new Intent();
		//intent.putExtra("name", str);
		//intent.putExtra("pass", str);
		intent.setClass(MainActivity.this, UnlockGesturePasswordActivity.class);
		startActivity(intent);
	}
	
	//跳转到重新设置手势密码页面
	@JavascriptInterface
	public void resetPassWord() {
		Intent intent = new Intent();
		//intent.putExtra("name", str);
		//intent.putExtra("pass", str);
		intent.setClass(MainActivity.this, ResetGesturePasswordActivity.class);
		startActivity(intent);
	}
}

