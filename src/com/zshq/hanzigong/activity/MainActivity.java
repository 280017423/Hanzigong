package com.zshq.hanzigong.activity;

import io.vov.vitamio.LibsChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.umeng.update.UmengUpdateAgent;
import com.zshq.hanzigong.R;
import com.zshq.hanzigong.adapter.FolderAdapter;
import com.zshq.hanzigong.util.FileUtil;

public class MainActivity extends ActivityBase implements OnClickListener, OnItemClickListener {

	private GridView mGvRootFolder;
	private ArrayList<File> mFileList;
	private FolderAdapter mFilAdapter;
	private File mResDir;
	private File mCurrentFile;
	private long mExitTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initVariables();
		initView();
		setListener();
		getFileList();
	}

	private void initVariables() {
		if (!LibsChecker.checkVitamioLibs(this)) {
			return;
		}
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.update(this);
		mFileList = new ArrayList<File>();
		mResDir = FileUtil.getResDir(this);
		mCurrentFile = mResDir;
	}

	private void initView() {
		mGvRootFolder = (GridView) findViewById(R.id.gv_root_folder);
		mFilAdapter = new FolderAdapter(this, mFileList, mGvRootFolder);
		mGvRootFolder.setAdapter(mFilAdapter);
	}

	private void setListener() {
		mGvRootFolder.setOnItemClickListener(this);
	}

	private void getFileList() {
		if (null == mCurrentFile) {
			return;
		}
		File[] files = FileUtil.listFiles(this, mCurrentFile);
		if (files == null) {
			return;
		} else {
			mFileList.clear();
			mFileList.addAll(Arrays.asList(files));
			Collections.sort(mFileList, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					if (o1.isDirectory() && o2.isFile())
						return -1;
					if (o1.isFile() && o2.isDirectory())
						return 1;
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		mFilAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		File tempFile = (File) parent.getAdapter().getItem(position);
		if (null == tempFile || !tempFile.exists()) {
			return;
		}
		if (tempFile.isDirectory()) {
			mCurrentFile = tempFile;
			getFileList();
		} else if (tempFile.isFile()) {
			Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
			intent.putExtra("videoList", mFileList);
			intent.putExtra("position", position);
			startActivity(intent);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			default:
				break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (null != mResDir && null != mCurrentFile && !mResDir.getAbsolutePath().equals(mCurrentFile.getAbsolutePath())) {
				mCurrentFile = mCurrentFile.getParentFile();
				getFileList();
				return true;
			}
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				toast("再按一次退出程序");
				mExitTime = System.currentTimeMillis();
			} else {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
