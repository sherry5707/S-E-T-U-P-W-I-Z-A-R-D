package com.kinstalk.her.setupwizard.data.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.kinstalk.her.httpsdk.rx.RxUtil;
import com.kinstalk.her.setupwizard.HerSetupWizardApplication;
import com.kinstalk.her.setupwizard.data.retrofit.HerApiService;
import com.kinstalk.her.setupwizard.data.retrofit.entity.AccountResponse;
import com.kinstalk.her.setupwizard.data.retrofit.entity.AccountSubscriber;
import com.kinstalk.her.setupwizard.data.retrofit.entity.UnregisterSubscriber;
import com.kinstalk.her.setupwizard.data.retrofit.entity.UnRegisterResponse;
import com.kinstalk.her.setupwizard.util.Constants;
import com.kinstalk.her.setupwizard.util.DebugUtils;
import com.kinstalk.her.setupwizard.util.DeviceUtils;
import com.kinstalk.her.setupwizard.util.PackageUtil;
import com.kinstalk.her.setupwizard.view.activity.HerBootGuideActivity;
import com.kinstalk.her.setupwizard.view.activity.HerBootGuideXiaoWeiActivity;
import com.kinstalk.m4.publicownerlib.OwnerColumn;
import com.kinstalk.m4.publicownerlib.OwnerProviderLib;
import com.kinstalk.m4.publicownerlib.OwnerUri;
import com.kinstalk.qloveaicore.AIManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Zhang Zhigang on 2017/6/23.
 */


public class BindActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i("BindActionReceiver", "a22417 debug received the intent = " + intent.getAction());
        if (intent.getAction().equals(Constants.KINSTALK_QCHAT_BIND_STATUS)) {
            boolean bind_status = intent.getBooleanExtra(Constants.EXTRA_QCHAT_BIND_STATUS, false);
            Log.i("BindActionReceiver", "a22417 debug the qchat bind status = " + bind_status);
            if (!bind_status) {
                clearData(context);
                PackageUtil.dismissGuide(context);
                PackageUtil.setDeviceProvisioned(context, false);
                PackageUtil.setWechatBindStatus(context, false);
                PackageUtil.setShowGuideStatus(context, true);
                if (PackageUtil.getXiaoWeiBindStatus(context) &&
                        !PackageUtil.isTopActivityBootWechatActivity() &&
                        !PackageUtil.isTopActivityQrCodeActivity()) {
                    PackageUtil.enableBootGuideActivity(context);
                    PackageUtil.switchPrivacy(context, true);
                    Intent newIntent = new Intent(context, HerBootGuideActivity.class);
                    newIntent.putExtra(Constants.EXTRA_QCHAT_BIND_STATUS, true);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(newIntent);
                }
            } else {
                PackageUtil.disableBootGuideActivity(context);
                PackageUtil.setWechatBindStatus(context, true);
                //PackageUtil.switchPrivacy(context, false);
                if (!PackageUtil.isTopActivityGuideActivity()) {
                    PackageUtil.showGuide(context);
                }
            }
        } else if (Constants.KINSTALK_QCHAT_QRCODE_READY.equals(intent.getAction())) {
            if (PackageUtil.getXiaoWeiBindStatus(context) &&
                    !PackageUtil.getWechatBindStatus(context)) {
                Intent LaunchIntent = new Intent();
                LaunchIntent.setPackage("com.kinstalk.her.qchat");
                LaunchIntent.setClassName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.QRCodeActivity");
                LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(LaunchIntent);
            }
        }
    }

    public void clearData(Context context) {
        Intent clearDataIntent = new Intent();
        clearDataIntent.setAction("com.kinstalk.her.qchat.clear.data");
        clearDataIntent.setPackage("com.kinstalk.her.setupwizard");
        context.sendStickyBroadcast(clearDataIntent);
    }
}
