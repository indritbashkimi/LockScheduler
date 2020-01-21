/*
 * Copyright (c) 2016 Indrit Bashkimi <indrit.bashkimi@gmail.com>.
 * All rights reserved.
 */

package com.ibashkimi.lockscheduler.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;

public class ThemeUtils {

    @ColorInt
    public static int obtainColor(Context context, int attr, @ColorInt int defaultColor) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{attr});
        int color = a.getColor(0, defaultColor);
        a.recycle();
        return color;
    }

    @Deprecated
    public static float getDimensionPixelSize(float dots, float actualDpi) {
        return dots * actualDpi / 160;
    }

    public static float dpToPx(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}
