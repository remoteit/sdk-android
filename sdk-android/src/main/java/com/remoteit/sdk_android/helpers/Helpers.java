package com.remoteit.sdk_android.helpers;

import android.annotation.SuppressLint;
import android.util.Log;

public class Helpers {
	@SuppressLint("LogNotTimber")
	public static void ReportException(String logTag, Exception e) {
		Log.e(logTag, e.toString());
		e.printStackTrace();
	}
}
