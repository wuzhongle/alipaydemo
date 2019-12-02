package com.zongzi.alipaydemo.auto;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Path;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import com.zongzi.alipaydemo.AutoApplication;
import com.zongzi.alipaydemo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseAccessibilityService {

    private static final String TAG = "BaseAccessibilityService";
    protected static final long DEFAULT_SLEEP_TIME = 2 * 1000;
    protected static final int DEFAULT_RETRY_COUNT = 5;

    protected ExecutorService mExecutor;
    protected Random random = new Random(100);

    protected boolean needStart = true;
    protected boolean needStop = false;

    public void setStop(boolean stop) {
        needStop = stop;
        needStart = !stop;
        if (stop && mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdownNow();
        }
    }

    protected AutoAccessibilityService autoAccessibilityService = null;

    public void onServiceConnected() {

    }

    public void onAccessibilityEvent(AutoAccessibilityService autoAccessibilityService, AccessibilityEvent event) {
        this.autoAccessibilityService = autoAccessibilityService;

        if (needStop) {
            LogUtils.d(TAG, "needStop = true, stop");
            return;
        }

        if (!AutoAccessibilityInit.packageName.equals(event.getPackageName().toString())) {
            LogUtils.d(TAG, "onAccessibilityEvent not target packageName, target packageName: " + AutoAccessibilityInit.packageName
                    + ", current packageName: " + event.getPackageName());
            return;
        }

        if (!needStart) return;

        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdownNow();
        }
        mExecutor = Executors.newSingleThreadExecutor();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                doTask();
            }
        });
        needStart = false;
    }

    public void onInterrupt() {

    }

    public void onDestroy() {

    }

    public void doTask() {

    }

    protected boolean isInPackage() {
        int tryCount = 0;
        while (tryCount < DEFAULT_RETRY_COUNT) {
            if (autoAccessibilityService != null && autoAccessibilityService.getRootInActiveWindow() != null
                    && autoAccessibilityService.getRootInActiveWindow().getPackageName() != null) {
                return AutoAccessibilityInit.packageName.equals(autoAccessibilityService.getRootInActiveWindow().getPackageName().toString());
            }
            SystemClock.sleep(1000);
            tryCount++;
            LogUtils.d(TAG, "isInPackage, try time: " + tryCount);
        }

        return false;
    }

    protected void doSmallScroll(boolean down) {
        if (!isInPackage()) return;
        Path path = new Path();
        int startX = Utils.dp2px(AutoApplication.context, random.nextInt(100) + 100);
        int startY = Utils.dp2px(AutoApplication.context, random.nextInt(100) + 300);
        path.moveTo(startX, startY + (down ? random.nextInt(150) : -random.nextInt(150)));
        path.lineTo(startX + random.nextInt(50), startY);
        autoAccessibilityService.dispatchGestureMove(path, 500);
    }

    protected void doLargeScroll(boolean down) {
        if (!isInPackage()) return;
        Path path = new Path();
        int startX = Utils.dp2px(AutoApplication.context, random.nextInt(100) + 100);
        int startY = Utils.dp2px(AutoApplication.context, random.nextInt(50) + 100);
        path.moveTo(startX, startY + (down ? Utils.dp2px(AutoApplication.context, 400) : -Utils.dp2px(AutoApplication.context, 400)));
        path.lineTo(startX + random.nextInt(50), startY);
        autoAccessibilityService.dispatchGestureMove(path, 300);
    }

    protected AccessibilityNodeInfo loopFindFirst(@NonNull AbstractTF... tfs) {
        return loopFindFirst(false, DEFAULT_RETRY_COUNT, tfs);
    }

    protected AccessibilityNodeInfo loopFindFirst(boolean loopForever, @NonNull AbstractTF... tfs) {
        return loopFindFirst(loopForever, DEFAULT_RETRY_COUNT, tfs);
    }

    protected AccessibilityNodeInfo loopFindFirst(int loopCount, @NonNull AbstractTF... tfs) {
        return loopFindFirst(false, loopCount, tfs);
    }

    protected AccessibilityNodeInfo loopFindFirst(boolean loopForever, int loopCount, @NonNull AbstractTF... tfs) {
        AccessibilityNodeInfo findNode = null;
        int tryCount = 0;
        while (loopForever || tryCount < loopCount) {
            findNode = autoAccessibilityService.findFirst(tfs);
            if (findNode != null) {
                return findNode;
            }
            SystemClock.sleep(DEFAULT_SLEEP_TIME);
            tryCount++;
            LogUtils.d(TAG, "loopFindFirst, try time: " + tryCount);
        }

        return findNode;
    }

    protected List<AccessibilityNodeInfo> loopFindAll(@NonNull AbstractTF... tfs) {
        List<AccessibilityNodeInfo> findNodes = new ArrayList<>();
        int tryCount = 0;
        while (tryCount < DEFAULT_RETRY_COUNT) {
            findNodes = autoAccessibilityService.findAll(tfs);
            if (findNodes.size() > 0) {
                return findNodes;
            }
            SystemClock.sleep(DEFAULT_SLEEP_TIME);
            tryCount++;
        }

        return findNodes;
    }

    protected void inPackageBack() {
        if (!isInPackage()) return;
        back();
    }

    protected void back() {
        autoAccessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public static void startLaunch(Activity activity, BaseAccessibilityService accessibilityService, String packageName) {
        AutoAccessibilityInit.start(accessibilityService, packageName);
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } else {
            Utils.toast("自动打开失败");
        }
    }

}
