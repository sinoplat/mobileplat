package com.sinosoft.phoneGapPlugins.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.sinosoft.gyicPlat.R;

public class SmsActivity extends Activity implements OnClickListener {

	private ImageView newsCancel;
	private TextView newsContent, newsTitle;
	private String newsContent1;
	private String messageTitle1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.news);
		setupView();

		if (this.getIntent().getStringExtra("newsTitle") != null) {
			messageTitle1 = (String) (this.getIntent()
					.getStringExtra("newsTitle"));
		}

		if (this.getIntent().getStringExtra("newsContent") != null) {
			newsContent1 = (String) (this.getIntent()
					.getStringExtra("newsContent"));
		}

		newsTitle.setText(messageTitle1);
		newsContent.setText(newsContent1);

	}

	private void setupView() {
		newsTitle = (TextView) findViewById(R.id.newsTitle);
		newsContent = (TextView) findViewById(R.id.newsContent);
		newsCancel = (ImageView) findViewById(R.id.newsCancel);
		newsCancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.newsCancel:
			finish();
			break;
		}
	}
}
