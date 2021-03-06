package com.lhc.customprogressbar;

import android.content.Context;

/**
 * 作者：lhc
 * 时间：2017/12/27.
 */

public class ScreenUtil {
    public static float dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (scale * dpValue + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
