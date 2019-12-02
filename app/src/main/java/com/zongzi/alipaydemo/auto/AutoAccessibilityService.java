package com.zongzi.alipaydemo.auto;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.zongzi.alipaydemo.utils.Utils;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class AutoAccessibilityService extends AccessibilityService {

    private static final String TAG = "AutoAccessibilityService";


    public static AutoAccessibilityService mService;
    private static BaseAccessibilityService realAccessibilityService;

    public static void start(BaseAccessibilityService accessibilityService) {
        realAccessibilityService = accessibilityService;
    }

    //初始化
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mService = this;
        if (realAccessibilityService != null) realAccessibilityService.onServiceConnected();
    }

    //实现辅助功能
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (realAccessibilityService != null)
            realAccessibilityService.onAccessibilityEvent(this, event);
    }

    @Override
    public void onInterrupt() {
        if (realAccessibilityService != null) realAccessibilityService.onInterrupt();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mService = null;
        if (realAccessibilityService != null) realAccessibilityService.onDestroy();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 公共方法
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 辅助功能是否启动
     */
    public static boolean isStart() {
        return mService != null;
    }

    public static boolean stopService() {
        if (mService != null) {
            mService.disableSelf();
            return true;
        }
        return false;
    }

    /**
     * 点击该控件
     *
     * @return true表示点击成功
     */
    public boolean clickView(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            } else {
                AccessibilityNodeInfo parent = nodeInfo.getParent();
                if (parent != null) {
                    boolean b = clickView(parent);
                    parent.recycle();
                    if (b) return true;
                }
            }
        }
        return false;
    }

    /**
     * 查找第一个匹配的控件
     *
     * @param tfs 匹配条件，多个AbstractTF是&&的关系，如：
     *            AbstractTF.newContentDescription("表情", true),AbstractTF.newClassName(AbstractTF.ST_IMAGEVIEW)
     *            表示描述内容是'表情'并且是imageview的控件
     */
    @Nullable
    public AccessibilityNodeInfo findFirst(@NonNull AbstractTF... tfs) {
        if (tfs.length == 0) throw new InvalidParameterException("AbstractTF不允许传空");

        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        if (rootInfo == null) return null;

        int idTextTFCount = 0, idTextIndex = 0;
        for (int i = 0; i < tfs.length; i++) {
            if (tfs[i] instanceof AbstractTF.IdTextTF) {
                idTextTFCount++;
                idTextIndex = i;
            }
        }
        switch (idTextTFCount) {
            case 0://id或text数量为0，直接循环查找
                AccessibilityNodeInfo returnInfo = findFirstRecursive(rootInfo, tfs);
                rootInfo.recycle();
                return returnInfo;
            case 1://id或text数量为1，先查出对应的id或text，然后再查其他条件
                if (tfs.length == 1) {
                    AccessibilityNodeInfo returnInfo2 = ((AbstractTF.IdTextTF) tfs[idTextIndex]).findFirst(rootInfo);
                    rootInfo.recycle();
                    return returnInfo2;
                } else {
                    List<AccessibilityNodeInfo> listIdText = ((AbstractTF.IdTextTF) tfs[idTextIndex]).findAll(rootInfo);
                    if (Utils.isEmptyArray(listIdText)) {
                        break;
                    }
                    AccessibilityNodeInfo returnInfo3 = null;
                    for (AccessibilityNodeInfo info : listIdText) {//遍历找到匹配的
                        if (returnInfo3 == null) {
                            boolean isOk = true;
                            for (AbstractTF tf : tfs) {
                                if (!tf.checkOk(info)) {
                                    isOk = false;
                                    break;
                                }
                            }
                            if (isOk) {
                                returnInfo3 = info;
                            } else {
                                info.recycle();
                            }
                        } else {
                            info.recycle();
                        }
                    }
                    rootInfo.recycle();
                    return returnInfo3;
                }
            default:
                throw new RuntimeException("由于时间有限，并且多了也没什么用，所以IdTF和TextTF只能有一个");
        }
        rootInfo.recycle();
        return null;
    }

    /**
     * @param tfs 由于是递归循环，会忽略IdTF和TextTF
     */
    public static AccessibilityNodeInfo findFirstRecursive(AccessibilityNodeInfo parent, @NonNull AbstractTF... tfs) {
        if (parent == null) return null;
        if (tfs.length == 0) throw new InvalidParameterException("AbstractTF不允许传空");

        for (int i = 0; i < parent.getChildCount(); i++) {
            AccessibilityNodeInfo child = parent.getChild(i);
            if (child == null) continue;
//            LogUtils.d(TAG, "class name: " + child.getClassName());
            boolean isOk = true;
            for (AbstractTF tf : tfs) {
                if (!tf.checkOk(child)) {
                    isOk = false;
                    break;
                }
            }
            if (isOk) {
                return child;
            } else {
                AccessibilityNodeInfo childChild = findFirstRecursive(child, tfs);
                child.recycle();
                if (childChild != null) {
                    return childChild;
                }
            }
        }
        return null;
    }

    /**
     * 查找全部匹配的控件
     *
     * @param tfs 匹配条件，多个AbstractTF是&&的关系，如：
     *            AbstractTF.newContentDescription("表情", true),AbstractTF.newClassName(AbstractTF.ST_IMAGEVIEW)
     *            表示描述内容是'表情'并且是imageview的控件
     */
    @NonNull
    public List<AccessibilityNodeInfo> findAll(@NonNull AbstractTF... tfs) {
        if (tfs.length == 0) throw new InvalidParameterException("AbstractTF不允许传空");

        ArrayList<AccessibilityNodeInfo> list = new ArrayList<>();
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        if (rootInfo == null) return list;

        int idTextTFCount = 0, idTextIndex = 0;
        for (int i = 0; i < tfs.length; i++) {
            if (tfs[i] instanceof AbstractTF.IdTextTF) {
                idTextTFCount++;
                idTextIndex = i;
            }
        }
        switch (idTextTFCount) {
            case 0://id或text数量为0，直接循环查找
                findAllRecursive(list, rootInfo, tfs);
                break;
            case 1://id或text数量为1，先查出对应的id或text，然后再循环
                List<AccessibilityNodeInfo> listIdText = ((AbstractTF.IdTextTF) tfs[idTextIndex]).findAll(rootInfo);
                if (Utils.isEmptyArray(listIdText)) {
                    break;
                }
                if (tfs.length == 1) {
                    list.addAll(listIdText);
                } else {
                    for (AccessibilityNodeInfo info : listIdText) {
                        boolean isOk = true;
                        for (AbstractTF tf : tfs) {
                            if (!tf.checkOk(info)) {
                                isOk = false;
                                break;
                            }
                        }
                        if (isOk) {
                            list.add(info);
                        } else {
                            info.recycle();
                        }
                    }
                }
                break;
            default:
                throw new RuntimeException("由于时间有限，并且多了也没什么用，所以IdTF和TextTF只能有一个");
        }
        rootInfo.recycle();
        return list;
    }

    /**
     * @param tfs 由于是递归循环，会忽略IdTF和TextTF
     */
    public static void findAllRecursive(List<AccessibilityNodeInfo> list, AccessibilityNodeInfo parent, @NonNull AbstractTF... tfs) {
        if (parent == null || list == null) return;
        if (tfs.length == 0) throw new InvalidParameterException("AbstractTF不允许传空");

        for (int i = 0; i < parent.getChildCount(); i++) {
            AccessibilityNodeInfo child = parent.getChild(i);
            if (child == null) continue;
            boolean isOk = true;
            for (AbstractTF tf : tfs) {
                if (!tf.checkOk(child)) {
                    isOk = false;
                    break;
                }
            }
            if (isOk) {
                list.add(child);
            } else {
                findAllRecursive(list, child, tfs);
                child.recycle();
            }
        }
    }

    /**
     * 立即发送移动的手势
     * 注意7.0以上的手机才有此方法，请确保运行在7.0手机上
     *
     * @param path  移动路径
     * @param mills 持续总时间
     */
    @RequiresApi(24)
    public void dispatchGestureMove(Path path, long mills) {
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription
                (path, 0, mills)).build(), null, null);
    }

    /**
     * 点击指定位置
     * 注意7.0以上的手机才有此方法，请确保运行在7.0手机上
     */
    @RequiresApi(24)
    public void dispatchGestureClick(int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription
                (path, 0, 100)).build(), null, null);
    }

    @RequiresApi(24)
    public void dispatchGestureClick(AccessibilityNodeInfo nodeInfo) {
        Rect outBounds = new Rect();
        nodeInfo.getBoundsInScreen(outBounds);
        dispatchGestureClick((outBounds.left + outBounds.right) / 2, (outBounds.top + outBounds.bottom) / 2);
    }

    /**
     * 由于太多,最好回收这些AccessibilityNodeInfo
     */
    public static void recycleAccessibilityNodeInfo(List<AccessibilityNodeInfo> listInfo) {
        if (Utils.isEmptyArray(listInfo)) return;

        for (AccessibilityNodeInfo info : listInfo) {
            info.recycle();
        }
    }

    /**
     * Android 5.0 版本及以上
     *
     * @param nodeInfo
     * @param text
     */
    @RequiresApi(21)
    public void setEditText(AccessibilityNodeInfo nodeInfo, String text) {
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

    // 调用手机的back、home、最近任务、拉出通知栏
    // AccessibilityService.GLOBAL_ACTION_BACK
    // GLOBAL_ACTION_HOME
    // GLOBAL_ACTION_NOTIFICATIONS
    // GLOBAL_ACTION_RECENTS
    // service.performGlobalAction(mAction)
}