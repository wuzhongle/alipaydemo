package com.zongzi.alipaydemo.auto;

import android.app.Application;

import com.zongzi.alipaydemo.utils.Utils;

public class AutoAccessibilityInit {

    public static String packageName = "";


    public static void init(Application mApplication) {
        Utils.init(mApplication);
    }

    public static void start(BaseAccessibilityService accessibilityService, String packName) {
        AutoAccessibilityService.start(accessibilityService);
        packageName = packName;
    }

    public static void onDestroy() {
        Utils.onDestroy();
    }

}
