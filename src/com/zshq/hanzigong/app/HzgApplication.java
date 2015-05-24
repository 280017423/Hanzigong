package com.zshq.hanzigong.app;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * 
 * Description the class 全局应用程序
 * 
 * @version 1.0
 * @author zou.sq
 * 
 */
public class HzgApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		String appId = "900002971"; // 上Bugly(bugly.qq.com)注册产品获取的AppId
		boolean isDebug = false; // true代表App处于调试阶段，false代表App发布阶段
		CrashReport.initCrashReport(this, appId, isDebug); // 初始化SDK
	}

}
