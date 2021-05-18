package com.remoteit.sdk_android.helpers;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class AppContext extends Application {
    @SuppressLint("StaticFieldLeak")
    private static AppContext context;

    public static Context getContext() {
        return context;
    }

    public AppContext() {
        context = this;
    }
}
