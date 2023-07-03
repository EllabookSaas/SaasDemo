package com.ellabook.saasdemo;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

/**
 * Created by tiandehua on 2021/1/23
 *
 * @description:
 */
public class ReaderUtils {
    private static boolean Debug = true;

    public static int dip2px(Context context, float dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()) + 0.5f);
    }

    public static void LogD(String tag, String msg) {
        if (Debug) {
            Log.d(tag, msg);
        }
    }

    public static void LogI(String tag, String msg) {
        if (Debug) {
            Log.i(tag, msg);
        }
    }

    public static void LogE(String tag, String msg) {
        Log.e(tag, msg);
    }

}
