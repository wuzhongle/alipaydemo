package com.zongzi.alipaydemo.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.Collection;

public class Utils {

    private static Context mContext;
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void init(Context context) {
        mContext = context;
    }

    public static void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public static void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean isEmptyArray(Collection list) {
        return list == null || list.size() == 0;
    }

    public static <T> boolean isEmptyArray(T[] list) {
        return list == null || list.length == 0;
    }

    public static int dp2px(Context context, int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * (dp + 0.5));
    }

}
