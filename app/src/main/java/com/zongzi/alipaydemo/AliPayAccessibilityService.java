package com.zongzi.alipaydemo;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.zongzi.alipaydemo.auto.AbstractTF;
import com.zongzi.alipaydemo.auto.AutoAccessibilityService;
import com.zongzi.alipaydemo.auto.BaseAccessibilityService;
import com.zongzi.alipaydemo.auto.LogUtils;
import com.zongzi.alipaydemo.utils.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: wuzhongle
 * E-mail: wuzhongle@qiyi.com
 * Date: 2019/11/18
 * Description:
 */
public class AliPayAccessibilityService extends BaseAccessibilityService {

    private static final String TAG = "AliPayAccessibilityService";

    private static AliPayAccessibilityService instance = null;

    public static AliPayAccessibilityService getInstance() {
        if (instance == null) {
            synchronized (AliPayAccessibilityService.class) {
                if (instance == null) {
                    instance = new AliPayAccessibilityService();
                }
            }
        }
        return instance;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AutoAccessibilityService autoAccessibilityService, AccessibilityEvent event) {
        super.onAccessibilityEvent(autoAccessibilityService, event);
        needStart = false;
    }

    @Override
    public void doTask() {
        super.doTask();
        collectEnergy();
    }

    @Override
    public void onInterrupt() {
        super.onInterrupt();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private Set<String> collectedNames = new HashSet<>();

    private void collectEnergy() {
        SystemClock.sleep(2000);
        // 确保在首页
        if (!goToHome()) {
            LogUtils.d(TAG, "collectPoint, go home fail");
            needStart = true;
            return;
        }

        AccessibilityNodeInfo homeAntTreesNode = loopFindFirst(AbstractTF.newText("蚂蚁森林", true));

        if (homeAntTreesNode == null) {
            LogUtils.d(TAG, "collectEnergy, homeAntTreesNode == null");
            needStart = true;
            return;
        }

        // 进入蚂蚁森林页面
        autoAccessibilityService.clickView(homeAntTreesNode);

        SystemClock.sleep(2000);

        AccessibilityNodeInfo plantTreeNode = loopFindFirst(10, AbstractTF.newWebText("种树", true));

        if (plantTreeNode == null) {
            LogUtils.d(TAG, "collectEnergy, plantTreeNode == null");
            needStart = true;
            return;
        } else {
            LogUtils.d(TAG, "collectEnergy, plantTreeNode != null");
        }

        List<AccessibilityNodeInfo> energyNode = autoAccessibilityService.findAll(AbstractTF.newWebText("收集能量", false));
        for (int i = 0; i < energyNode.size(); i++) {
            autoAccessibilityService.dispatchGestureClick(energyNode.get(i));
            SystemClock.sleep(500);
        }

        for (int i = 0; i < 5; i++) {
            doLargeScroll(true);
            SystemClock.sleep(500);
        }

        // 点击查看更多好友
        AccessibilityNodeInfo checkAllFriendNode = loopFindFirst(10, AbstractTF.newWebText("查看更多好友", true));
        autoAccessibilityService.clickView(checkAllFriendNode);

        SystemClock.sleep(2000);


        collectedNames.clear();

        AccessibilityNodeInfo noMoreNode = null;
        Rect noMoreNodeOutBounds = new Rect();

        while (noMoreNode == null || (noMoreNodeOutBounds.bottom - noMoreNodeOutBounds.top) < Utils.dp2px(AutoApplication.context, 10)) {
            if (!isInPackage()) {
                LogUtils.d(TAG, "collectEnergy, not in package return");
                return;
            }

            List<AccessibilityNodeInfo> specialNodes = loopFindAll(AbstractTF.newWebText("g", false));
            AccessibilityNodeInfo listItems = null;
            try {
                listItems = specialNodes.get(1).getParent().getParent().getParent();
            } catch (Exception e) {
                LogUtils.d(TAG, "collectEnergy, can not find lists, can not collect friend energy");
            }

            LogUtils.d(TAG, "collectEnergy, collect friend energy: " + (listItems == null ? 0 : listItems.getChildCount()));

            for (int i = 0; listItems != null && i < listItems.getChildCount(); i++) {
                if (!isInPackage()) {
                    LogUtils.d(TAG, "collectEnergy, not in package return");
                    return;
                }
                AccessibilityNodeInfo item = listItems.getChild(i);
                Rect outBounds = new Rect();
                item.getBoundsInScreen(outBounds);
                String friendName = "";
                try {
                    friendName = item.getChild(2).getChild(0).getChild(0).getText().toString();
                    String friendNameContent = item.getChild(2).getChild(0).getChild(0).getContentDescription().toString();
                    LogUtils.d(TAG, "collectEnergy, get friend name text: " + friendName + ", ContentDescription: " + friendNameContent);
                } catch (Exception e) {
                    LogUtils.d(TAG, "collectEnergy, get friend name error");
                }
                String hasNoEnergy = "";
                try {
                    hasNoEnergy = item.getChild(3).getChild(0).getChild(0).getText().toString();
                } catch (Exception e) {
                    LogUtils.d(TAG, "collectEnergy, get has no energy error");
                }
                if (!TextUtils.isEmpty(friendName) && !"邀请".equals(hasNoEnergy) && !collectedNames.contains(friendName)
                        && outBounds.top > 0 && outBounds.bottom < AutoApplication.context.getResources().getDisplayMetrics().heightPixels && outBounds.top < outBounds.bottom) {
                    LogUtils.d(TAG, "collectEnergy, try collect friend energy name: " + friendName);
                    collectFriendEnergy(item);
                    collectedNames.add(friendName);
                    SystemClock.sleep(1000);
                } else {
                    LogUtils.d(TAG, "collectEnergy, can not collect friend energy name: " + friendName);
                }
            }

            noMoreNode = autoAccessibilityService.findFirst(AbstractTF.newWebText("没有更多了", false));
            if (noMoreNode != null) {
                noMoreNode.getBoundsInScreen(noMoreNodeOutBounds);
                LogUtils.d(TAG, "collectEnergy, has no more node left: " + noMoreNodeOutBounds.left + ", right: " + noMoreNodeOutBounds.right
                        + ", top: " + noMoreNodeOutBounds.top + ", bottom: " + noMoreNodeOutBounds.bottom);
            }

            LogUtils.d(TAG, "collectEnergy, do scroll to collect other friends");
            doLargeScroll(true);
        }

        LogUtils.d(TAG, "collectEnergy, collect friend energy end");


        SystemClock.sleep(2000);
        collectPoint();
    }

    private void collectFriendEnergy(AccessibilityNodeInfo friendItem) {
        autoAccessibilityService.dispatchGestureClick(friendItem);

        AccessibilityNodeInfo wateringNode = loopFindFirst(10, AbstractTF.newWebText("浇水", true));

        if (wateringNode == null) {
            LogUtils.d(TAG, "collectFriendEnergy, wateringNode == null");
            return;
        } else {
            LogUtils.d(TAG, "collectFriendEnergy, wateringNode != null");
        }

        List<AccessibilityNodeInfo> energyNode = autoAccessibilityService.findAll(AbstractTF.newWebText("收集能量", false));
        for (int i = 0; i < energyNode.size(); i++) {
            autoAccessibilityService.dispatchGestureClick(energyNode.get(i));
            SystemClock.sleep(500);
        }

        inPackageBack();
    }

    private void collectPoint() {
        if (!goToHome()) {
            LogUtils.d(TAG, "collectPoint, go home fail");
            needStart = true;
            return;
        }

        AccessibilityNodeInfo homeMineNode = loopFindFirst(AbstractTF.newText("我的", true));

        if (homeMineNode == null) {
            LogUtils.d(TAG, "collectPoint, homeMineNode == null");
            needStart = true;
            return;
        }

        // 进入我的页面
        autoAccessibilityService.clickView(homeMineNode);

        SystemClock.sleep(2000);
        AccessibilityNodeInfo enterPointNode = loopFindFirst(AbstractTF.newText("积分待领取", false));
        if (enterPointNode == null) {
            LogUtils.d(TAG, "collectPoint, enterPointNode == null");
            return;
        }
        // 进入积分页
        autoAccessibilityService.clickView(enterPointNode);

        SystemClock.sleep(2000);
        AccessibilityNodeInfo getPointNode = loopFindFirst(AbstractTF.newWebText("领积分", true));
        if (getPointNode == null) {
            LogUtils.d(TAG, "collectPoint, getPointNode == null");
            needStart = true;
            return;
        }
        // 进入领取积分页
        autoAccessibilityService.clickView(getPointNode);

        SystemClock.sleep(2000);
        AccessibilityNodeInfo realGetPointNode = loopFindFirst(AbstractTF.newWebText("点击领取", true));

        while (realGetPointNode != null) {
            // 领取积分
            for (int i = 0; i < 5; i++) {
                autoAccessibilityService.clickView(realGetPointNode);
                SystemClock.sleep(500);
            }
            SystemClock.sleep(2000);
            realGetPointNode = loopFindFirst(2, AbstractTF.newWebText("点击领取", true));
        }

        LogUtils.d(TAG, "collectPoint, already get all point");

        AccessibilityNodeInfo familyGetPointNode = loopFindFirst(AbstractTF.newWebText("家庭积分", false));
        if (familyGetPointNode == null) {
            LogUtils.d(TAG, "collectPoint, familyGetPointNode == null");
            return;
        }

        // 进入家庭积分页
        autoAccessibilityService.clickView(familyGetPointNode);

        SystemClock.sleep(2000);
        AccessibilityNodeInfo getFamilyPointNode = loopFindFirst(AbstractTF.newWebText("领积分", true));
        if (getFamilyPointNode == null) {
            LogUtils.d(TAG, "collectPoint, getFamilyPointNode == null");
            return;
        }

        // 领取家庭积分
        autoAccessibilityService.clickView(getFamilyPointNode);
        LogUtils.d(TAG, "collectPoint, already get all family point");
    }

    private boolean goToHome() {
        AccessibilityNodeInfo homeNode = null;
        // 确保在首页
        while (isInPackage()) {
            homeNode = autoAccessibilityService.findFirst(AbstractTF.newText("首页", true));
            if (homeNode == null) {
                LogUtils.d(TAG, "goToHome, homeNode == null");
            } else {
                LogUtils.d(TAG, "goToHome, in home page");
                break;
            }
            inPackageBack();
            SystemClock.sleep(1000);
        }

        if (homeNode != null) {
            autoAccessibilityService.clickView(homeNode);
            LogUtils.d(TAG, "goToHome, go to home");
            return true;
        } else {
            LogUtils.d(TAG, "goToHome, go home fail");
            return false;
        }
    }

}
