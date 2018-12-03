package com.kinstalk.her.setupwizard.allapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GetAllApps {

    private Context mContext;
    private PackageManager packageManager;
    private int mIconDpi;
    private String TAG = "GetAllApps";
    private List<PakageMod> datas = new ArrayList<>();

    public GetAllApps(Context mContext){
        this.mContext = mContext;
         ActivityManager activityManager =
                    (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        packageManager = mContext.getPackageManager();
        mIconDpi = activityManager.getLauncherLargeIconDensity();
    }

    public void loadAllAppsByBatch() {
        datas.clear();
        List<ResolveInfo> apps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        apps = packageManager.queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < apps.size(); i++) {
            String packageName = apps.get(i).activityInfo.applicationInfo.packageName;
            //Log.d(TAG,"a22417 the packageName = " +packageName);
            if (TextUtils.equals(packageName,"com.kinstalk.m4") ||
                    TextUtils.equals(packageName,"com.tinnotech.factorytest") ||
                    TextUtils.equals(packageName,"com.android.calculator2") ||
                    TextUtils.equals(packageName,"com.android.settings") ||
                    TextUtils.equals(packageName,"com.thunderst.update") ||
                    TextUtils.equals(packageName,"kinstalk.com.qloveaicore") ||
                    TextUtils.equals(packageName,"kinstalk.com.wateranimapp")) {
                continue;
            }
            String title = apps.get(i).loadLabel(packageManager).toString();
            Drawable icon = null;
            if(title == null){
                title = apps.get(i).activityInfo.name;
            }
            ActivityInfo info = apps.get(i).activityInfo;
            icon = getFullResIcon(info);
            if (TextUtils.equals(packageName,"com.kinstalk.her.help")) {
                datas.add(0,new PakageMod(packageName, title, icon));
            } else {
                datas.add(new PakageMod(packageName, title, icon));
            }
        }
    }

    public Drawable getFullResIcon(ActivityInfo info) {
        Resources resources;
        try {
            resources = packageManager.getResourcesForApplication(
                    info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.getIconResource();
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    public Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(), android.R.mipmap.sym_def_app_icon);
    }

    public Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            // requires API level 15 (current min is 14):
            d = resources.getDrawableForDensity(iconId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            d = null;
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }

    public List<PakageMod> getDatas() {
        loadAllAppsByBatch();
        return datas;
    }
}
