package com.kinstalk.her.setupwizard.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.kinstalk.util.SystemProperty;

/**
 * Created by mamingzhang on 2017/4/21.
 */

public class DeviceUtils {
    public static String getHerSerialNumber(){
        String serial = null;
        try {
            serial = SystemProperty.get("persist.qlove.sn");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(serial)) {
            serial = getSerialNumber();
        }

        return serial;
    }

    public static String getHerProductVersion(){
        String productVersion = null;
        try {
            productVersion = SystemProperty.get("ro.timotech.ota.version");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productVersion;
    }

    private static String getSerialNumber(){
        String serial = null;
        try {
            serial = SystemProperty.get("ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

}
