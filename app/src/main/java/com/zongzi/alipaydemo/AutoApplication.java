package com.zongzi.alipaydemo;

import android.app.Application;

import com.zongzi.alipaydemo.auto.AutoAccessibilityInit;

public class AutoApplication extends Application {

    public static Application context = null;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        AutoAccessibilityInit.init(this);
    }

    @Override
    public void onTerminate() {
        AutoAccessibilityInit.onDestroy();
        super.onTerminate();
    }

}
