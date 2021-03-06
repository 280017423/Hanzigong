package com.zshq.hanzigong.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.zshq.hanzigong.listener.IDialogProtocol;
import com.zshq.hanzigong.util.DialogManager;
import com.zshq.hanzigong.util.StringUtil;
import com.zshq.hanzigong.widget.CustomDialog.Builder;
import com.zshq.hanzigong.widget.LoadingUpView;

public class ActivityBase extends Activity implements IDialogProtocol {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 默认的toast方法，该方法封装下面的两点特性：<br>
	 * 1、只有当前activity所属应用处于顶层时，才会弹出toast；<br>
	 * 2、默认弹出时间为 Toast.LENGTH_SHORT;
	 * 
	 * @param msg
	 *            弹出的信息内容
	 */
	public void toast(final String msg) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!StringUtil.isNullOrEmpty(msg)) {
					Toast toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT);
					TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
					// 用来防止某些系统自定义了消息框
					if (tv != null) {
						tv.setGravity(Gravity.CENTER);
					}
					toast.show();
				}
			}
		});
	}

	protected boolean showLoadingUpView(LoadingUpView loadingUpView) {
		return showLoadingUpView(loadingUpView, "");
	}

	protected boolean showLoadingUpView(LoadingUpView loadingUpView, String info) {
		if (loadingUpView != null && !loadingUpView.isShowing()) {
			if (null == info) {
				info = "";
			}
			loadingUpView.showPopup(info);
			return true;
		}
		return false;
	}

	protected boolean dismissLoadingUpView(LoadingUpView loadingUpView) {
		if (loadingUpView != null && loadingUpView.isShowing()) {
			loadingUpView.dismiss();
			return true;
		}
		return false;
	}

	@Override
	public Builder createDialogBuilder(Context context, String title, String message, String positiveBtnName,
			String negativeBtnName) {
		return DialogManager
				.createMessageDialogBuilder(context, title, message, positiveBtnName, negativeBtnName, this);
	}

	@Override
	public void onPositiveBtnClick(int id, DialogInterface dialog, int which) {

	}

	@Override
	public void onNegativeBtnClick(int id, DialogInterface dialog, int which) {

	}
}
