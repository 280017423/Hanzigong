package com.zshq.hanzigong.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.zshq.hanzigong.R;
import com.zshq.hanzigong.util.FileUtil;
import com.zshq.hanzigong.util.SharedPreferenceUtil;

public class CheckUdiskActivity extends ActivityBase {

	private BroadcastReceiver mBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_udisk);
		initViews();
		registerReceiver();
	}

	private void initViews() {

	}

	public void registerReceiver() {
		mBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, final Intent intent) {
				if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)
						|| intent.getAction().equals(Intent.ACTION_MEDIA_CHECKING)) {
					String dir = intent.getData().getPath() + "/新汉字宫/w/";
					if (FileUtil.isFileExist(dir)) {
						jumpToMain();
						SharedPreferenceUtil.saveValue(CheckUdiskActivity.this, "config_file", "config_file_dir", dir);
						finish();
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_CHECKING);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		// 必须要有此行，否则无法收到广播
		filter.addDataScheme("file");
		registerReceiver(mBroadcastReceiver, filter);
	}

	private void jumpToMain() {
		startActivity(new Intent(CheckUdiskActivity.this, MainActivity.class));
		finish();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

}
