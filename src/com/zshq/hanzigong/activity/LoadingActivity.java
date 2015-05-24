package com.zshq.hanzigong.activity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.zshq.hanzigong.R;
import com.zshq.hanzigong.util.FileUtil;
import com.zshq.hanzigong.util.SharedPreferenceUtil;
import com.zshq.hanzigong.util.UIUtil;

public class LoadingActivity extends ActivityBase {
	private static final int DISPLAY_TIME = 3000;

	private View mLogoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		initViews();
		checkUdisk();
	}

	private void initViews() {
		mLogoView = findViewById(R.id.iv_loading_bg);
		UIUtil.setViewHeight(mLogoView, UIUtil.getScreenHeight(this) / 2);
		UIUtil.setViewWidth(mLogoView, UIUtil.getScreenHeight(this) * 447 / 432 / 2);
	}

	private void checkUdisk() {
		List<String> dirList = getExternalStorageDirectory();
		boolean hasUdisk = false;
		for (int i = 0; i < dirList.size(); i++) {
			String dir = dirList.get(i) + "/新汉字宫/w/";
			Log.d("bbb", "dir:+ " + dir);
			if (FileUtil.isFileExist(dir)) {
				hasUdisk = true;
				jumpToMain();
				SharedPreferenceUtil.saveValue(this, "config_file", "config_file_dir", dir);
			}
		}
		if (!hasUdisk) {
			startActivity(new Intent(LoadingActivity.this, CheckUdiskActivity.class));
			finish();
		}
	}

	private void jumpToMain() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				startActivity(new Intent(LoadingActivity.this, MainActivity.class));
				finish();
			}
		}, DISPLAY_TIME);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 获取扩展存储路径，TF卡、U盘
	 */
	public static List<String> getExternalStorageDirectory() {
		List<String> dirList = new ArrayList<String>();
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec("mount");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			String line;
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				// if (line.contains("secure"))
				// continue;
				// if (line.contains("asec"))
				// continue;

				// if (line.contains("fat") || line.contains("fuse")) {
				String columns[] = line.split(" ");
				if (columns != null && columns.length > 1) {
					dirList.add(columns[1]);
				}
				// }
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dirList;
	}
}
