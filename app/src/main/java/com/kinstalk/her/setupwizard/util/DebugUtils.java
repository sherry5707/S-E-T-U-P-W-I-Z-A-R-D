package com.kinstalk.her.setupwizard.util;

import android.util.Log;

/**
 * Created by mamingzhang on 2017/5/12.
 */

public class DebugUtils {
    public static final boolean bDebug = true;

    private static final String Tag = "SetupWizard";

    public static void LogV(String msg) {
        if (bDebug) {
            Log.v(Tag, msg);
        }
    }

    public static void LogD(String msg) {
        if (bDebug) {
            Log.d(Tag, msg);
        }
    }

    public static void LogE(String msg) {
        if (bDebug) {
            Log.v(Tag, msg);
        }
    }
}
