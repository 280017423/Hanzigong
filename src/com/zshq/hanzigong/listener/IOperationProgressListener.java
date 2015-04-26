package com.zshq.hanzigong.listener;

import java.io.File;

public interface IOperationProgressListener {
	void onFinish();

	void onFileChanged(File file);
}