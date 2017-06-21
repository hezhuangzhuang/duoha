package com.zxwl.duoha.utils;

import android.view.View;

/**
 * author：hw
 * data:2017/5/11 17:03
 * view的帮助类
 */
public class ViewUtil {
    /**
     * 判断某个点是否在view上
     *
     * @param view
     * @param x
     * @param y
     * @return
     */
    public static boolean isTouchPointInView(View view, int x, int y) {
        if (null == view) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getWidth();
        int bottom = top + view.getHeight();
        if (y >= top && y <= bottom && x >= left && x <= right) {
            return true;
        }
        return false;
    }
}
