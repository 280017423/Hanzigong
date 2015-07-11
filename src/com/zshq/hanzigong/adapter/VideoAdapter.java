/**
 * @Title: DishEmptyAdapter.java
 * @Project DCB
 * @Package com.pdw.dcb.ui.adapter
 * @Description: 沽清列表
 * @author zeng.ww
 * @date 2012-12-10 下午04:37:29
 * @version V1.0
 */
package com.zshq.hanzigong.adapter;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zshq.hanzigong.R;
import com.zshq.hanzigong.listener.ImageLoadListener;
import com.zshq.hanzigong.util.AsyncImageLoader;
import com.zshq.hanzigong.util.UIUtil;
import com.zshq.hanzigong.widget.RoundCornerImageView;

/**
 * 文件列表适配器
 * 
 * @author zou.sq
 * @since 2013-03-12 下午04:37:29
 * @version 1.0
 */
public class VideoAdapter extends BaseAdapter {
	private LruCache<String, Bitmap> mLruCache;
	private static final int SPACE_VALUE = 10;
	private int NUM_COLUMNS = 6;
	private List<File> mFilesList;
	private Activity mContext;
	private int mImgSize;
	private GridView mGridView;
	private AsyncImageLoader mImageLoader;

	/**
	 * 实例化对象
	 * 
	 * @param context
	 *            上下文
	 * @param dataList
	 *            数据列表
	 */
	public VideoAdapter(Activity context, List<File> dataList, GridView gridView) {
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		this.mContext = context;
		this.mFilesList = dataList;
		mGridView = gridView;
		mLruCache = new LruCache<String, Bitmap>(maxMemory);
		mImageLoader = new AsyncImageLoader(context);
		mImgSize = (UIUtil.getScreenWidth(mContext) - UIUtil.dip2px(mContext, SPACE_VALUE) * (NUM_COLUMNS + 1))
				/ NUM_COLUMNS;
	}

	public void setColumns(int columns) {
		NUM_COLUMNS = columns;
		mImgSize = (UIUtil.getScreenWidth(mContext) - UIUtil.dip2px(mContext, SPACE_VALUE) * (NUM_COLUMNS + 1))
				/ NUM_COLUMNS;
	}

	@Override
	public int getCount() {
		if (mFilesList != null && !mFilesList.isEmpty()) {
			return mFilesList.size();
		}
		return 0;
	}

	@Override
	public File getItem(int position) {
		if (mFilesList != null) {
			return mFilesList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		viewHode view = new viewHode();
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.view_folder_item, null);
			view.mName = (TextView) convertView.findViewById(R.id.tv_folder_name);
			view.mIcon = (RoundCornerImageView) convertView.findViewById(R.id.iv_folder_img);
			convertView.setTag(view);
		} else {
			view = (viewHode) convertView.getTag();
		}

		UIUtil.setViewWidth(view.mIcon, mImgSize);
		UIUtil.setViewHeight(view.mIcon, mImgSize);

		final File file = mFilesList.get(position);
		view.mIcon.setTag(file.getAbsolutePath());
		if (null != file) {
			if (file.isDirectory()) {
				view.mIcon.setBackgroundResource(R.drawable.format_folder);
			} else {
				view.mIcon.setBackgroundResource(R.drawable.format_media);
				if (null != mLruCache.get(file.getAbsolutePath())) {
					view.mIcon.setImageBitmap(mLruCache.get(file.getAbsolutePath()));
				} else {
					mImageLoader.loadDrawable(file, mImgSize, new ImageLoadListener() {

						@Override
						public void imageLoaded(Bitmap bitmap, String imageUrl) {
							ImageView imageViewByTag = (ImageView) mGridView.findViewWithTag(imageUrl);
							mLruCache.put(file.getAbsolutePath(), bitmap);
							if (imageViewByTag != null) {
								imageViewByTag.setImageBitmap(bitmap);
							}
						}
					});
				}
			}
			view.mName.setText(file.getName());
			view.mName.setTextColor(Color.WHITE);
		}
		return convertView;
	}

	class viewHode {
		TextView mName;
		RoundCornerImageView mIcon;
	}
}
