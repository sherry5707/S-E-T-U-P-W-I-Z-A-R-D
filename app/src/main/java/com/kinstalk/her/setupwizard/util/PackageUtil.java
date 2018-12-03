package com.kinstalk.her.setupwizard.util;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import com.kinstalk.her.setupwizard.view.activity.HerBootGuideActivity;
import com.kinstalk.her.setupwizard.view.activity.HerBootGuideXiaoWeiActivity;
import com.kinstalk.her.setupwizard.view.dialog.GuidePageDialog;

import com.kinstalk.util.AppUtils;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by lenovo on 2017/8/15.
 */

public class PackageUtil {
    public static GuidePageDialog mGuidePageDialog;

    public static void disableBootGuideActivity(Context context) {
        // remove this activity from the package manager.
        PackageManager pm = context.getPackageManager();
        ComponentName name = new ComponentName(context, HerBootGuideActivity.class);
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void enableBootGuideActivity(Context context) {
        // add this activity to the package manager.
        PackageManager pm = context.getPackageManager();
        ComponentName name = new ComponentName(context, HerBootGuideActivity.class);
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP);
    }

    public static void disableBootGuideXiaoWeiActivity(Context context) {
        // remove this activity from the package manager.
        PackageManager pm = context.getPackageManager();
        ComponentName name = new ComponentName(context, HerBootGuideXiaoWeiActivity.class);
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void enableBootGuideXiaoWeiActivity(Context context) {
        // add this activity to the package manager.
        PackageManager pm = context.getPackageManager();
        ComponentName name = new ComponentName(context, HerBootGuideXiaoWeiActivity.class);
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP);
    }


        public static void setDeviceProvisioned(Context context, boolean provision) {
        //Settings.Secure.USER_SETUP_COMPLETE
        final String USER_SETUP_COMPLETE = "user_setup_complete";
        if(provision) {
            Settings.Global.putInt(context.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
            Settings.Secure.putInt(context.getContentResolver(), USER_SETUP_COMPLETE, 1);
        } else {
            Settings.Global.putInt(context.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
            Settings.Secure.putInt(context.getContentResolver(), USER_SETUP_COMPLETE, 0);
        }
    }

    public static boolean getWechatBindStatus(Context context) {
        int status = Settings.Secure.getInt(context.getContentResolver(),"wechat_bind_status",0);
        if (status == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean getMessageStatus(Context context) {
        int status = Settings.Secure.getInt(context.getContentResolver(),"new_message_status",0);
        if (status == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean getHomeworkStatus(Context context) {
        int status = Settings.Secure.getInt(context.getContentResolver(),"new_homework_status",0);
        if (status == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static void setMessageStatus(Context context, Boolean status) {
        if (status) {
            Settings.Secure.putInt(context.getContentResolver(),"new_message_status",1);
        } else {
            Settings.Secure.putInt(context.getContentResolver(),"new_message_status",0);
        }
    }

    public static void setHomeworkStatus(Context context, Boolean status) {
        if (status) {
            Settings.Secure.putInt(context.getContentResolver(),"new_homework_status",1);
        } else {
            Settings.Secure.putInt(context.getContentResolver(),"new_homework_status",0);
        }
    }

    public static void setWechatBindStatus(Context context, Boolean status) {
        if (status) {
            Settings.Secure.putInt(context.getContentResolver(),"wechat_bind_status",1);
        } else {
            Settings.Secure.putInt(context.getContentResolver(),"wechat_bind_status",0);
        }
    }

    public static void setScreenOffTimeout(Context context, Integer timeout) {
        Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT,timeout);
    }

    public static void setAccountBindMobile(Context context, String mobile) {
        Settings.Secure.putString(context.getContentResolver(),"bind_mobile",mobile);
    }

    public static void setXiaoWeiTinyId(Context context, String tinyID) {
        Settings.Secure.putString(context.getContentResolver(),"xiaowei_tinyid",tinyID);
    }

    public static String getXiaoWeiTinyId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),"xiaowei_tinyid");
    }

    public static boolean getXiaoWeiBindStatus(Context context) {
        int status = Settings.Secure.getInt(context.getContentResolver(),"xiaowei_bind_status",0);
        if (status == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static void setXiaoWeiBindStatus(Context context, Boolean status) {
        Log.i("AllAPPList","setXiaoWeiBindStatus status = "+status);
        if (status) {
            Settings.Secure.putInt(context.getContentResolver(),"xiaowei_bind_status",1);
        } else {
            Settings.Secure.putInt(context.getContentResolver(),"xiaowei_bind_status",0);
        }
    }

    public static boolean getShowGuideStatus(Context context) {
        int status = Settings.Secure.getInt(context.getContentResolver(),"show_guide_status",1);
        if (status == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static void setShowGuideStatus(Context context, Boolean status) {
        Log.i("AllAPPList","setShowGuideStatus status = "+status);
        if (status) {
            Settings.Secure.putInt(context.getContentResolver(),"show_guide_status",1);
        } else {
            Settings.Secure.putInt(context.getContentResolver(),"show_guide_status",0);
        }
    }

    public static boolean isTopActivity(String pkgName) {
        try {
            boolean is = AppUtils.isTopAcitivity(pkgName);
            Log.e("PackageUtil", "isTopActivity " + pkgName + " " + is);
            return is;
        } catch (Exception e) {
            //
        }
        return false;
    }

    public static void setStars(Context context, int stars) {
        Settings.Secure.putInt(context.getContentResolver(),"all_stars_number",stars);
    }

    public static int getStars(Context context) {
        int stars = Settings.Secure.getInt(context.getContentResolver(),"all_stars_number",3);
        return stars;
    }
    public static void showGuide(Context context) {
//        mGuidePageDialog = new GuidePageDialog(context);
//        if (!mGuidePageDialog.isShowing()) {
//            mGuidePageDialog.show();
//        }
        GuidePageDialog.actionStart(context);
    }

    public static void dismissGuide(Context context) {
//        if ((mGuidePageDialog != null) && (mGuidePageDialog.isShowing())) {
//            mGuidePageDialog.dismiss();
//        }
        GuidePageDialog.actionFinish(context);
    }

    public static boolean isTopActivity(ComponentName name) {
        try {
            boolean is = AppUtils.isTopAcitivity(name);
            Log.e("PackageUtil", "isTopActivity " + name + " " + is);
            return is;
        } catch (Exception e) {
            //
        }
        return false;
    }

    public static boolean isTopActivitySleepActivity() {
        ComponentName name = new ComponentName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.activity.SleepActivity");
        return isTopActivity(name);
    }

    public static boolean isTopActivityBootWechatActivity() {
        ComponentName name = new ComponentName("com.kinstalk.her.setupwizard", "com.kinstalk.her.setupwizard.view.activity.HerBootGuideActivity");
        return isTopActivity(name);
    }

    public static boolean isTopActivityQrCodeActivity() {
        ComponentName name = new ComponentName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.QRCodeActivity");
        return isTopActivity(name);
    }

    public static boolean isTopActivityWakeUpActivity() {
        ComponentName name = new ComponentName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.activity.WakeUpActivity");
        return isTopActivity(name);
    }

    public static boolean isTopActivityGuideActivity() {
        ComponentName name = new ComponentName("com.kinstalk.her.setupwizard", "com.kinstalk.her.setupwizard.view.dialog.GuidePageDialog");
        return isTopActivity(name);
    }

    public static void switchPrivacy(Context context,boolean privacyMode) {
        try {
            AppUtils.setPrivacyMode(context, privacyMode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
