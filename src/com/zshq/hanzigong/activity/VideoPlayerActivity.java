/*
 * Copyright (C) 2011 VOV IO (http://vov.io/)
 */

package com.zshq.hanzigong.activity;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.MediaController.OnHiddenListener;
import io.vov.vitamio.widget.MediaController.OnShownListener;
import io.vov.vitamio.widget.VideoView;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zshq.hanzigong.R;
import com.zshq.hanzigong.adapter.FolderAdapter;
import com.zshq.hanzigong.util.FileUtil;
import com.zshq.hanzigong.util.PopWindowUtil;
import com.zshq.hanzigong.util.StringUtil;
import com.zshq.hanzigong.util.UIUtil;
import com.zshq.hanzigong.widget.LoadingUpView;

public class VideoPlayerActivity extends ActivityBase implements OnCompletionListener, OnInfoListener, OnClickListener {

	private VideoView mVideoView;
	private View mVolumeBrightnessLayout;
	private ImageView mOperationBg;
	private TextView mTvTitle;
	private ImageView mOperationPercent;
	private AudioManager mAudioManager;
	/** 最大声音 */
	private int mMaxVolume;
	/** 当前声音 */
	private int mVolume = -1;
	/** 当前亮度 */
	private float mBrightness = -1f;
	/** 当前缩放模式 */
	private int mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
	private GestureDetector mGestureDetector;
	private MediaController mMediaController;
	private LoadingUpView mLoadingUpView;
	private int mPosition;
	private ArrayList<File> mFileList;
	private PopWindowUtil mMuluPop;
	private PopWindowUtil mWebPop;
	private PopWindowUtil mToatstPop;
	private View mLeftView;
	private View mRightView;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_video_view);
		initVariables();
		initViews();
		play(mFileList.get(mPosition));
	}

	private void initViews() {
		mLeftView = findViewById(R.id.ll_left_view);
		mRightView = findViewById(R.id.ll_right_view);
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mTvTitle = (TextView) findViewById(R.id.tv_video_title);
		mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
		mOperationBg = (ImageView) findViewById(R.id.operation_bg);
		mOperationPercent = (ImageView) findViewById(R.id.operation_percent);
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnInfoListener(this);
		initMuluPop();
		initToastPop();
	}

	private void initMuluPop() {
		View popView = View.inflate(this, R.layout.view_mulu_pop, null);
		mMuluPop = new PopWindowUtil(popView, null);
		GridView mGvRootFolder = (GridView) popView.findViewById(R.id.gv_root_folder);
		FolderAdapter mFilAdapter = new FolderAdapter(this, mFileList, mGvRootFolder);
		mFilAdapter.setColumns(6);
		mGvRootFolder.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mPosition = position;
				play(mFileList.get(mPosition));
				mMuluPop.dissmiss();
			}
		});
		mGvRootFolder.setAdapter(mFilAdapter);
	}

	private void initToastPop() {
		View popView = View.inflate(this, R.layout.view_toast, null);
		mToatstPop = new PopWindowUtil(popView, null);
		View replay = popView.findViewById(R.id.replay);
		View playlast = popView.findViewById(R.id.playlast);
		View playnext = popView.findViewById(R.id.playnext);
		replay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mToatstPop.dissmiss();
				if (mPosition == -1) {
					play(new File(FileUtil.getResDir(VideoPlayerActivity.this), "header.flv"));
				} else if (mPosition == mFileList.size()) {
					play(new File(FileUtil.getResDir(VideoPlayerActivity.this), "footer.flv"));
				}
			}
		});
		playlast.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPosition <= 0) {
					toast("没有上一集了");
					return;
				}
				mToatstPop.dissmiss();
				mPosition--;
				play(mFileList.get(mPosition));
			}
		});
		playnext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPosition >= mFileList.size()) {
					toast("没有下一集了");
					return;
				}
				mToatstPop.dissmiss();
				mPosition++;
				play(mFileList.get(mPosition));
			}
		});
	}

	private void showWebPop(String url) {
		View popView = View.inflate(this, R.layout.view_web, null);
		mWebPop = new PopWindowUtil(popView, null);
		WebView mWebView = (WebView) popView.findViewById(R.id.wv_content);
		mWebView.loadUrl(url);
		mWebPop.show();
	}

	private void initVariables() {
		mLoadingUpView = new LoadingUpView(this);
		mGestureDetector = new GestureDetector(this, new MyGestureListener());
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		Intent intent = getIntent();
		mFileList = (ArrayList<File>) intent.getSerializableExtra("videoList");
		mPosition = intent.getIntExtra("position", 0);
	}

	private void play(File videoFile) {
		if (!videoFile.exists()) {
			Toast.makeText(this, "视频文件路径错误", Toast.LENGTH_LONG).show();
			return;
		}
		mVideoView.setVideoPath(videoFile.getAbsolutePath());
		// 设置显示名称
		mMediaController = new MediaController(this);
		mVideoView.setMediaController(mMediaController);
		mVideoView.requestFocus();
		if (!StringUtil.isNullOrEmpty(videoFile.getName())) {
			UIUtil.setViewVisible(mTvTitle);
			mTvTitle.setText(videoFile.getName());
		}
		mMediaController.setOnHiddenListener(new OnHiddenListener() {

			@Override
			public void onHidden() {
				UIUtil.setViewGone(mTvTitle);
				UIUtil.setViewGone(mLeftView);
				UIUtil.setViewGone(mRightView);
			}
		});
		mMediaController.setOnShownListener(new OnShownListener() {

			@Override
			public void onShown() {
				UIUtil.setViewVisible(mLeftView);
				UIUtil.setViewVisible(mRightView);
				UIUtil.setViewVisible(mTvTitle);
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mVideoView != null)
			mVideoView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mVideoView != null)
			mVideoView.resume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mVideoView != null)
			mVideoView.stopPlayback();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event))
			return true;

		// 处理手势结束
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_UP:
				endGesture();
				break;
		}

		return super.onTouchEvent(event);
	}

	/** 手势结束 */
	private void endGesture() {
		mVolume = -1;
		mBrightness = -1f;

		// 隐藏
		mDismissHandler.removeMessages(0);
		mDismissHandler.sendEmptyMessageDelayed(0, 1000);
	}

	private class MyGestureListener extends SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mLayout == VideoView.VIDEO_LAYOUT_SCALE) {
				mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
			} else {
				mLayout = VideoView.VIDEO_LAYOUT_SCALE;
			}
			if (mVideoView != null) {
				mVideoView.setVideoLayout(mLayout, 0);
			}
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			float mOldX = e1.getX(), mOldY = e1.getY();
			int y = (int) e2.getRawY();
			int windowWidth = UIUtil.getScreenWidth(VideoPlayerActivity.this);
			int windowHeight = UIUtil.getScreenHeight(VideoPlayerActivity.this);

			if (mOldX > windowWidth * 4.0 / 5)// 右边滑动
				onVolumeSlide((mOldY - y) / windowHeight);
			else if (mOldX < windowWidth / 5.0)// 左边滑动
				onBrightnessSlide((mOldY - y) / windowHeight);

			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	}

	/** 定时隐藏 */
	private Handler mDismissHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (0 == msg.what) {
				mVolumeBrightnessLayout.setVisibility(View.GONE);
			} else if (1 == msg.what) {
				UIUtil.setViewGone(mTvTitle);
			}
		}
	};

	/**
	 * 滑动改变声音大小
	 * 
	 * @param percent
	 */
	private void onVolumeSlide(float percent) {
		if (mVolume == -1) {
			mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (mVolume < 0)
				mVolume = 0;

			// 显示
			mOperationBg.setImageResource(R.drawable.video_volumn_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}

		int index = (int) (percent * mMaxVolume) + mVolume;
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;

		// 变更声音
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

		// 变更进度条
		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = findViewById(R.id.operation_full).getLayoutParams().width * index / mMaxVolume;
		mOperationPercent.setLayoutParams(lp);
	}

	/**
	 * 滑动改变亮度
	 * 
	 * @param percent
	 */
	private void onBrightnessSlide(float percent) {
		if (mBrightness < 0) {
			mBrightness = getWindow().getAttributes().screenBrightness;
			if (mBrightness <= 0.00f)
				mBrightness = 0.50f;
			if (mBrightness < 0.01f)
				mBrightness = 0.01f;

			// 显示
			mOperationBg.setImageResource(R.drawable.video_brightness_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}
		WindowManager.LayoutParams lpa = getWindow().getAttributes();
		lpa.screenBrightness = mBrightness + percent;
		if (lpa.screenBrightness > 1.0f)
			lpa.screenBrightness = 1.0f;
		else if (lpa.screenBrightness < 0.01f)
			lpa.screenBrightness = 0.01f;
		getWindow().setAttributes(lpa);

		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
		mOperationPercent.setLayoutParams(lp);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (mVideoView != null)
			mVideoView.setVideoLayout(mLayout, 0);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCompletion(MediaPlayer player) {
		mToatstPop.show();
	}

	private void stopPlayer() {
		if (mVideoView != null)
			mVideoView.pause();
	}

	private void startPlayer() {
		if (mVideoView != null)
			mVideoView.start();
	}

	private boolean isPlaying() {
		return mVideoView != null && mVideoView.isPlaying();
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
		switch (arg1) {
			case MediaPlayer.MEDIA_INFO_BUFFERING_START:
				// 开始缓存，暂停播放
				if (isPlaying()) {
					stopPlayer();
				}
				showLoadingUpView(mLoadingUpView);
				break;
			case MediaPlayer.MEDIA_INFO_BUFFERING_END:
				startPlayer();
				dismissLoadingUpView(mLoadingUpView);
				break;
			case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
				break;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ibtn_mulu:
				mMuluPop.show();
				break;
			case R.id.ibtn_piantou:
				playHeaderFooter("header.flv");
				break;
			case R.id.ibtn_pianwei:
				playHeaderFooter("footer.flv");
				break;
			case R.id.ibtn_jianjie:
				showWebPop("file:///android_asset/dz.html");
				// Intent jianjieIntent = new Intent(VideoPlayerActivity.this,
				// WebActivity.class);
				// jianjieIntent.putExtra("path",
				// "file:///android_asset/dz.html");
				// startActivity(jianjieIntent);
				break;
			case R.id.ibtn_quanwenzimu:
				showWebPop("file:///android_asset/analects.html");
				// Intent quanwenIntent = new Intent(VideoPlayerActivity.this,
				// WebActivity.class);
				// quanwenIntent.putExtra("path",
				// "file:///android_asset/analects.html");
				// startActivity(quanwenIntent);
				break;
			case R.id.ibtn_meijizimu:
				showWebPop("file:///android_asset/gc.html");
				// Intent shengziIntent = new Intent(VideoPlayerActivity.this,
				// WebActivity.class);
				// shengziIntent.putExtra("path",
				// "file:///android_asset/gc.html");
				// startActivity(shengziIntent);
				break;

			default:
				break;
		}
	}

	private void playHeaderFooter(String name) {
		if ("header.flv".equals(name)) {
			mPosition = -1;
		} else if ("footer.flv".equals(name)) {
			mPosition = mFileList.size();
		}
		play(new File(FileUtil.getResDir(this), name));
	}
}
