package com.zongzi.alipaydemo.auto;

import android.util.Log;

/**
 * Author: wuzhongle
 * E-mail: wuzhongle@qiyi.com
 * Date: 2019/11/13
 * Description:
 */
public class LogUtils {

    private static StringBuilder sb = new StringBuilder();

    public static String getLogs() {
        return sb.toString();
    }

    public static void v(String tag, String msg) {
        write("V", tag, msg);
        Log.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        write("D", tag, msg);
        Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        write("I", tag, msg);
        Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        write("W", tag, msg);
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        write("E", tag, msg);
        Log.e(tag, msg);
    }

    private static void write(String type, String tag, String msg) {
        StringBuilder stringBuilder = new StringBuilder(type).append("/").append(tag).append(": ").append(msg).append("\n");
        sb.insert(0, stringBuilder.toString());
    }
}
