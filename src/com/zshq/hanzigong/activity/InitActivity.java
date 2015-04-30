package com.zshq.hanzigong.activity;

import io.vov.vitamio.Vitamio;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import com.zshq.hanzigong.R;
import com.zshq.hanzigong.widget.LoadingUpView;

public class InitActivity extends ActivityBase {
	public static final String FROM_ME = "fromVitamioInitActivity";
	public static final String EXTRA_MSG = "EXTRA_MSG";
	public static final String EXTRA_FILE = "EXTRA_FILE";
	private ProgressDialog mPD;
	private LoadingUpView mLoadingUpView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		new AsyncTask<Object, Object, Object>() {
			@Override
			protected void onPreExecute() {
				mLoadingUpView = new LoadingUpView(InitActivity.this, true);
				showLoadingUpView(mLoadingUpView, getString(R.string.init_decoders));
			}

			@Override
			protected Object doInBackground(Object... params) {
				Vitamio.initialize(getApplicationContext());
				uiHandler.sendEmptyMessage(0);
				return null;
			}
		}.execute();
	}

	private Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			dismissLoadingUpView(mLoadingUpView);
			Intent src = getIntent();
			Intent i = new Intent();
			i.setClassName(src.getStringExtra("package"), src.getStringExtra("className"));
			i.setData(src.getData());
			i.putExtras(src);
			i.putExtra(FROM_ME, true);
			startActivity(i);
			finish();
		}
	};
}
