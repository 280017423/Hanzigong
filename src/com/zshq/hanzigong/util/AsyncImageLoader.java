package com.zshq.hanzigong.util;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import com.zshq.hanzigong.listener.ImageLoadListener;

public class AsyncImageLoader {

	private File[] mThumbnails;

	public AsyncImageLoader(Context context) {
		mThumbnails = FileUtil.getThumbnails(context);
	}

	public Drawable loadDrawable(final File videoFile, final int size, final ImageLoadListener imageCallback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Bitmap) message.obj, videoFile.getAbsolutePath());
			}
		};
		new Thread() {
			@Override
			public void run() {
				String fileName = FileUtil.getFileNameNoEx(videoFile.getName());
				if (null != mThumbnails) {
					for (int i = 0; i < mThumbnails.length; i++) {
						if (fileName.equalsIgnoreCase(FileUtil.getFileNameNoEx(mThumbnails[i].getName()))) {
							Bitmap bitmap = ImageUtil.getImageThumbnail(mThumbnails[i].getAbsolutePath(), size, size);
							Message message = handler.obtainMessage(0, bitmap);
							handler.sendMessage(message);
						}
					}
				}
			}
		}.start();
		return null;
	}

}