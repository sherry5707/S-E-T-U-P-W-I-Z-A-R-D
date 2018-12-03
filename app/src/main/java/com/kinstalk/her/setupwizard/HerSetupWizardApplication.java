package com.kinstalk.her.setupwizard;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.kinstalk.her.httpsdk.HttpManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kinstalk.com.countly.CountlyUtils;
import ly.count.android.sdk.Countly;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import com.kinstalk.her.httpsdk.rx.RxUtil;
import com.kinstalk.her.setupwizard.data.retrofit.HerApiService;
import com.kinstalk.her.setupwizard.data.retrofit.entity.AccountResponse;
import com.kinstalk.her.setupwizard.data.retrofit.entity.AccountSubscriber;
import com.kinstalk.her.setupwizard.data.retrofit.entity.UnRegisterResponse;
import com.kinstalk.her.setupwizard.data.retrofit.entity.UnregisterSubscriber;
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
 * Created by mamingzhang on 2017/4/21.
 */

public class HerSetupWizardApplication extends MultiDexApplication {

    private static HerSetupWizardApplication application;

    private HttpManager httpManager;

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;

        Stetho.initializeWithDefaults(this);

        initHttpManager();
        initStateReporter();
        CountlyUtils.initCountly(this,DebugUtils.bDebug, BuildConfig.IS_RELEASE);
    }

    public static HerSetupWizardApplication getApplication() {
        return application;
    }

    public HttpManager getHttpManager() {
        return httpManager;
    }

    public void initHttpManager() {
        Map<String, String> header = new HashMap<>();

        this.httpManager = new HttpManager.Builder(this)
                .interceptor(new CustomInterceptor())
                .debug(true)
                .stetho(true)
                .header(header)
                .build();
    }

    private class CustomInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            Request.Builder newBuilder = request.newBuilder();
            if (!TextUtils.isEmpty(OwnerProviderLib.getInstance(HerSetupWizardApplication.this).getToken())) {
                newBuilder.header("token", OwnerProviderLib.getInstance(HerSetupWizardApplication.this).getToken());
                newBuilder.header("deviceId", OwnerProviderLib.getInstance(HerSetupWizardApplication.this).getDeviceId());
            }

            //在这里可以添加新的自定义Header，在项目中主要是Token和DeviceId，因为这两个值是变值，刷新发生变化后需要重新赋值，
            //如果要利用公用header来实现，那么就需要重新生成HttpManager

            Request newRequest = newBuilder.method(request.method(), request.body()).build();

            return chain.proceed(newRequest);
        }
    }

    private void initStateReporter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Constants.KINSTALK_TXSDK_BIND_STATUS);
        filter.addAction(Constants.ACTION_ASSIST_KEY);
        registerReceiver(mReceiver, filter);
    }
    public void playWorkContent(String content) {
        try {
            AIManager.getInstance(getApplicationContext()).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (intent != null) {
                String acyion = intent.getAction();
                //Log.d("HerSetting", acyion);
                Log.i("Application"," a22418 get the action = " +acyion);
                switch (acyion) {
                    case Constants.ACTION_ASSIST_KEY:
                        //fix bug 17149 、17021
                        playWorkContent("  ");
                        if (SystemProperties.getBoolean("sys.keypad.test", false) ||
                                PackageUtil.isTopActivitySleepActivity() ||
                                PackageUtil.isTopActivityWakeUpActivity()) {
                            return;
                        }
                        if (!PackageUtil.isTopActivity("com.kinstalk.her.settings") &&
                                !PackageUtil.isTopActivity("com.kinstalk.her.setupwizard")) {
                            Intent i = new Intent(Intent.ACTION_MAIN);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addCategory(Intent.CATEGORY_HOME);
                            context.startActivity(i);

                        }
                        break;
                    case Intent.ACTION_POWER_CONNECTED://接通电源
                        Countly.sharedInstance().recordEvent("power_connected",1);
                        Countly.sharedInstance().startEvent("Time_power_charge");
                        break;
                    case Intent.ACTION_POWER_DISCONNECTED://拔出电源
                        Countly.sharedInstance().recordEvent("power_disconnected",1);
                        Countly.sharedInstance().endEvent("Time_power_charge");
                        break;
                    case Constants.KINSTALK_TXSDK_BIND_STATUS:
                        boolean bind_status = intent.getBooleanExtra(Constants.EXTRA_BIND_STATUS, false);
                        Log.i("Application"," a22418 get the bind_status = " +bind_status);
                        if (bind_status) {
//                            if (PackageUtil.getWechatBindStatus(context)) {
//                                PackageUtil.setDeviceProvisioned(context, true);
//                            } else {
//                                PackageUtil.enableBootGuideActivity(context);
//                                Intent newIntent = new Intent(context, HerBootGuideActivity.class);
//                                newIntent.putExtra(Constants.EXTRA_QCHAT_BIND_STATUS, true);
//                                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                context.startActivity(newIntent);
//                            }
//                            PackageUtil.setXiaoWeiBindStatus(context, true);
//                            PackageUtil.disableBootGuideXiaoWeiActivity(context);

                            getAccountInfo(context);
                            if (!TextUtils.isEmpty(tinyID) &&
                                    (Long.parseLong(tinyID) != 0) &&
                                    !TextUtils.equals(OwnerProviderLib.getInstance(context).getToken(), "adfasdfaskdfatokentest")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        createUser(context);
                                    }
                                }).start();
                            }
                        } else {
                            PackageUtil.dismissGuide(context);
                            PackageUtil.setXiaoWeiBindStatus(context, false);
                            PackageUtil.setDeviceProvisioned(context, false);
                            PackageUtil.setShowGuideStatus(context, true);
                            PackageUtil.enableBootGuideXiaoWeiActivity(context);
                            Intent newIntent = new Intent(context, HerBootGuideXiaoWeiActivity.class);
                            newIntent.putExtra(Constants.EXTRA_UNBIND_ACTION, true);
                            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(newIntent);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    unRegister(context);
                                }
                            }).start();
                        }
                        break;
                }
            }
        }

    };

    public static String getBaseURL() {
        String url;
        String userCenterUrlChoice = SystemProperties.get("persist.sys.user.center.url");
        if ("0".equals(userCenterUrlChoice)) {
                //test or develop env
            url = "https://uc.test.qspeaker.com";
        } else if ("1".equals(userCenterUrlChoice)) {
            //product env
            url = "https://uc.qspeaker.com/";
        } else {
            //product env
            url = "https://uc.qspeaker.com/";
        }
        return url;
    }

    private void unRegister(final Context context) {

        Log.d("a22418", "a22418 unRegister thetinyID = " + PackageUtil.getXiaoWeiTinyId(context));
        HerSetupWizardApplication
                .getApplication()
                .getHttpManager()
                .createService(HerApiService.class)
                .unRegisterJiaYuanUser(PackageUtil.getXiaoWeiTinyId(context), DeviceUtils.getHerSerialNumber())
                .compose(RxUtil.<UnRegisterResponse>defaultSchedulers())
                .subscribe(new UnregisterSubscriber() {
                    @Override
                    public void resultSuccess(UnRegisterResponse response) {
                        if (response != null) {
                            int code = response.getCode();
                            PackageUtil.setAccountBindMobile(context, "");
                            PackageUtil.setXiaoWeiTinyId(context, "");
                            DebugUtils.LogE("a22418 unRegister the code = " + code);
                        } else {
                            DebugUtils.LogE("a22418 unRegister" + "response is null unregister fail");
                        }
                    }

                    @Override
                    public void resultError(Throwable e) {
                        DebugUtils.LogE("" + e.getMessage());
                    }
                });
    }

    private String tinyID;
    private String remark;
    private String din;

    private void getAccountInfo(Context mContext) {
        String jasonStr = AIManager.getInstance(mContext).getAccountInfo(null);
        Log.i("a22418", "a22418 begin to getAccountInfo");
        Log.e("GuideBindFragment", ": jasonStr:" + jasonStr);
        if (!TextUtils.isEmpty(jasonStr)) {
            try {
                JSONObject jsonObject = new JSONObject(jasonStr);
                PackageUtil.setXiaoWeiTinyId(mContext, jsonObject.optString("tinyID"));
                tinyID = jsonObject.optString("tinyID");
                din = jsonObject.optString("din");
                remark = jsonObject.optString("remark");
                Log.i("a22418", "a22418 after get getAccountInfo the tinyID = " + tinyID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void createUser(final Context mContext) {
        DebugUtils.LogE("serial no : " + DeviceUtils.getHerSerialNumber());

        HerSetupWizardApplication
                .getApplication()
                .getHttpManager()
                .createService(HerApiService.class)
                .createJiaYuanUser(tinyID, din, DeviceUtils.getHerSerialNumber(), 1, remark, DeviceUtils.getHerProductVersion())
                .compose(RxUtil.<AccountResponse>defaultSchedulers())
                .subscribe(new AccountSubscriber() {
                    @Override
                    public void resultSuccess(AccountResponse response) {
                        if (response != null) {
                            int code = response.getCode();
                            Log.d("a22418", "a22418 the code = " + code);
                            if (code == 0) {
                                Log.d("a22418", "a22418 the id = " + response.user.getId());
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(OwnerColumn.UID, response.user.getId());
                                contentValues.put(OwnerColumn.USERCODE, response.user.getId());
//                            contentValues.put(OwnerColumn.ACCESSTOKEN, response.token.getAccess_token());
//                            contentValues.put(OwnerColumn.ACCESSEXPIRESIN, response.token.getExpires_in());
//                            contentValues.put(OwnerColumn.REFRESHTOKEN, response.token.getRefresh_token());
//                            contentValues.put(OwnerColumn.REFRESHEXPIRESIN, response.token.getRe_expires_in());
                                contentValues.put(OwnerColumn.ACCESSTOKEN, "adfasdfaskdfatokentest");
                                contentValues.put(OwnerColumn.ACCESSEXPIRESIN, "12348345738479");
                                contentValues.put(OwnerColumn.REFRESHTOKEN, "asdfksjdfhaweur0928");
                                contentValues.put(OwnerColumn.REFRESHEXPIRESIN, "93487529793768");
                                contentValues.put(OwnerColumn.DUDUAPPID, "");
                                contentValues.put(OwnerColumn.DUDUVOIPACCOUNT, "");
                                contentValues.put(OwnerColumn.DUDUVOIPPWD, "");
                                contentValues.put(OwnerColumn.MERCHANTID, response.user.getMobile());
                                PackageUtil.setAccountBindMobile(mContext, response.user.getMobile());

                                Uri insertUri = mContext.getContentResolver().insert(OwnerUri.OWNER_URI, contentValues);
                                Log.i("a22418", "a22418 the insertUri = " + insertUri);
                                if (insertUri != null) {
                                    //mHandler.postDelayed(mCloseRunnable, 1000);
                                    //getActivity().finish();
                                }
                            } else {
                                Toast.makeText(mContext, "出错了 ：" + response.getMsg(), Toast.LENGTH_LONG).show();

                                //LENGTH_LONG is 3.5 seconds, retry to create user after toast disappear
                                //mHandler.postDelayed(mCreateUserRunnable, 3500);
                            }
                        }
                    }

                    @Override
                    public void resultError(Throwable e) {
                        DebugUtils.LogE("" + e.getMessage());

                        Toast.makeText(mContext, "创建异常 ：" + e.getMessage(), Toast.LENGTH_LONG).show();

                        //LENGTH_LONG is 3.5 seconds, retry to create user after toast disappear
                        //mHandler.postDelayed(mCreateUserRunnable, 3500);
                    }
                });
    }
}
