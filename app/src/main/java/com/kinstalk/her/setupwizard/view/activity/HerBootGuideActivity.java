package com.kinstalk.her.setupwizard.view.activity;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.kinstalk.her.setupwizard.HerSetupWizardApplication;
import com.kinstalk.her.setupwizard.R;
import com.kinstalk.her.setupwizard.data.eventbus.DataEventBus;
import com.kinstalk.her.setupwizard.data.eventbus.entity.WifiConnectStatusChangeEntity;
import com.kinstalk.her.setupwizard.data.eventbus.entity.WifiConnectSuccessEntity;
import com.kinstalk.her.setupwizard.data.wifi.WifiHelper;
import com.kinstalk.her.setupwizard.util.Constants;
import com.kinstalk.her.setupwizard.util.DebugUtils;
import com.kinstalk.her.setupwizard.util.PackageUtil;
import com.kinstalk.her.setupwizard.view.fragment.GuideActivateFailFragment;
import com.kinstalk.her.setupwizard.view.fragment.GuideActivatingFragment;
import com.kinstalk.her.setupwizard.view.fragment.GuideBindFragment;
import com.kinstalk.her.setupwizard.view.fragment.GuideRebindFragment;
import com.kinstalk.her.setupwizard.view.fragment.HerWifiFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemProperties;

import kinstalk.com.qloveaicore.IAICoreInterface;
import ly.count.android.sdk.Countly;

import android.app.ActivityManager;
import android.content.pm.IPackageDataObserver;

public class HerBootGuideActivity extends FragmentActivity {

    private static final int MIN_ACTIVATING_TIME = 5000;
    private static final int MAX_ACTIVATING_TIME = 30000;

    public static final int SYSTEM_UI_FLAG_FULLSCREEN_CAN_NOT_SWIPE = 0x00000006;

    private static final int MSG_BIND_AI_SERIVCE = 0;
    private static final int MSG_UNBIND_ACCOUNT = 1;

    private Context mContext;
    private boolean mWiFiConnected = false;

    private final Handler mHandler = new BootGuideHandler();
    private IAICoreInterface mService;
    private boolean mQrCodeReady = false;
    private boolean mFirstSetup = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                        // bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | SYSTEM_UI_FLAG_FULLSCREEN_CAN_NOT_SWIPE);

        setContentView(R.layout.activity_her_boot_guide);

        DataEventBus.getEventBus().register(this);
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();
        registerBindStatusReceiver();
        Log.d("HerBootGuideActivity", "a22417 debug in onCreate");

    }

    @Override
    public void onPause() {
        Log.d("HerBootGuideActivity", "a22417 debug in onPause");
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("HerBootGuideActivity", "a22417 debug in onStart");
        Countly.sharedInstance().onStart(this);
        WifiHelper.getInstance().turnOnWiFiIfNeeded();
        if (SystemProperties.getBoolean("persist.sys.fisrtboot", true)) {
            long buildTime = SystemProperties.getLong("ro.build.date.utc", 86400) * 1000;
            SystemClock.setCurrentTimeMillis(buildTime);
            SystemProperties.set("persist.sys.fisrtboot", "false");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("HerBootGuideActivity", "a22417 debug in onStop");
        mHandler.removeCallbacks(mActivatingRunnable);
        Countly.sharedInstance().onStop();
    }

    @Override
    public void onResumeFragments() {
        boolean bind_status = PackageUtil.getWechatBindStatus(mContext);
        Log.d("HerBootGuideActivity", "in onResumeFragments a22417 the wechat bind_status = " + bind_status);
        if (!bind_status) {
            toGuideWifiFragment();
            PackageUtil.switchPrivacy(mContext, true);
        }

        //registerBindStatusReceiver();
        super.onResumeFragments();
    }

    private void registerBindStatusReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.KINSTALK_QCHAT_QRCODE_READY);
        intentFilter.addAction(Constants.KINSTALK_QCHAT_BIND_STATUS);
        mContext.registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterBindStatusReceiver() {
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("HerBootGuideActivity", "a22417 debug in onDestroy");
        DataEventBus.getEventBus().unregister(this);

        if (mService != null) {
            mContext.unbindService(mConn);
        }
        unregisterBindStatusReceiver();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void toGuideWifiFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, new HerWifiFragment()).commitAllowingStateLoss();
    }

    private void toGuideActivatingFragment() {
        GuideActivatingFragment fragment = new GuideActivatingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("MODE", "wechat");
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commitAllowingStateLoss();
    }

    private void toGuideActivateFailFragment() {
        failNotice();
        getSupportFragmentManager().beginTransaction().replace(R.id.content, new GuideActivateFailFragment()).commitAllowingStateLoss();
    }

    public void startQchatBindActivity() {
        Intent LaunchIntent = new Intent();
        LaunchIntent.setPackage("com.kinstalk.her.qchat");
        LaunchIntent.setClassName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.QRCodeActivity");
        LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(LaunchIntent);
    }

    public void failNotice() {
        Intent failIntent = new Intent();
        failIntent.setAction("com.kinstalk.her.qchat.activate.fail");
        mContext.sendBroadcast(failIntent);
    }

    public boolean isWiFiConnected() {
        return mWiFiConnected;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiConnectSuccess(WifiConnectSuccessEntity successEntity) {
        Log.d("HerBootGuideActivity", "in onWifiConnectSuccess a22417 the mWiFiConnected = " + mWiFiConnected);
        if (!mWiFiConnected) {
            mWiFiConnected = true;
        }

        Log.d("HerBootGuideActivity", "PackageUtil.getXiaoWeiBindStatus(mContext) = " + PackageUtil.getXiaoWeiBindStatus(mContext));
//		if (!PackageUtil.getXiaoWeiBindStatus(mContext)) {
//			Log.d("HerBootGuideActivity", "in onWifiConnectSuccess a22417 begin to startUnbindAccount");
//			startUnbindAccount();
//		}

        Log.d("HerBootGuideActivity", "in onWifiConnectSuccess a22417 the !PackageUtil.getWechatBindStatus(mContext) = " + !PackageUtil.getWechatBindStatus(mContext));
        Log.d("HerBootGuideActivity", "in onWifiConnectSuccess a22419 the mFirstSetup = " + mFirstSetup);
        if (!PackageUtil.getWechatBindStatus(mContext) && mFirstSetup) {
            mFirstSetup = false;
            startActivating(MAX_ACTIVATING_TIME);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiNetworkStatusChange(WifiConnectStatusChangeEntity statusEntity) {
        Log.d("HerBootGuideActivity", "in onWifiNetworkStatusChange the statusEntity.getState() = " + statusEntity.getState());
        switch (statusEntity.getState()) {
            case CONNECTED:
                if (!mWiFiConnected) {
                    mWiFiConnected = true;
                }
                break;
            case DISCONNECTED:
                //TODO, how to make sure the scenario which backing from bind screen?
                mWiFiConnected = false;
                break;
        }
    }

    private void startUnbindAccount() {
        unBindSDK();
    }

    public void tryBindAgain() {
        startActivating(MIN_ACTIVATING_TIME);
    }

    private void unBindSDK() {
        //do unbind
        DebugUtils.LogD("a22417 start to unbind the mService = " + mService);
        if (mService == null) {
            mHandler.sendEmptyMessage(MSG_BIND_AI_SERIVCE);
        } else {
            mHandler.sendEmptyMessage(MSG_UNBIND_ACCOUNT);
        }
    }

    public void startActivating(long duration) {
        toGuideActivatingFragment();

        mHandler.removeCallbacks(mActivatingRunnable);
        if (duration != 0) {
            mHandler.postDelayed(mActivatingRunnable, duration);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            DebugUtils.LogD("HerBootGuideActivity a22417 in receiver : " + action);

            if (Constants.KINSTALK_QCHAT_QRCODE_READY.equals(action)) {
                //need to do
                mQrCodeReady = true;
                if (PackageUtil.getXiaoWeiBindStatus(mContext) &&
                        !PackageUtil.getWechatBindStatus(mContext)) {
                    mHandler.removeCallbacks(mActivatingRunnable);
                    startQchatBindActivity();
                }
                //startActivating(1000);
                //startQchatBindActivity();
            } else if (Constants.KINSTALK_QCHAT_BIND_STATUS.equals(action)) {
                boolean bind_status = intent.getBooleanExtra(Constants.EXTRA_QCHAT_BIND_STATUS, false);
                if (bind_status) {
                    //bind
//					PackageUtil.disableBootGuideActivity(mContext);
//					PackageUtil.setDeviceProvisioned(mContext, true);
//					PackageUtil.setWechatBindStatus(mContext, true);
                    finish();
                } else {
                    //unbind
                    //startQchatBindActivity();
                }
            }
        }
    };

    private final Runnable mActivatingRunnable = new Runnable() {
        @Override
        public void run() {
            DebugUtils.LogD("a22417 in runnable the mQrCodeReady = :" + mQrCodeReady);
            if (!mQrCodeReady) {
                toGuideActivateFailFragment();
            } else {
                //unbind, move the next screen - bind screen
                if (PackageUtil.getXiaoWeiBindStatus(mContext) &&
                        !PackageUtil.getWechatBindStatus(mContext)) {
                    startQchatBindActivity();
                }
            }
        }
    };

    private boolean bindAiService() {
        DebugUtils.LogD("HerBootGuideActivity start to bind AI service");
        Intent intent = new Intent();
        intent.setClassName(Constants.REMOTE_AI_SERVICE, Constants.REMOTE_AI_SERVICE_CLASS);
        return mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    private void eraseBinders() {
        DebugUtils.LogD("HerBootGuideActivity start to eraseBinders");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.GET_DATA_CMD_STR, Constants.GET_DATA_CMD_ERASE_ALL_BINDERS);
        } catch (JSONException e) {
            e.printStackTrace();
            DebugUtils.LogD("HerBootGuideActivity start to eraseBinders the e catched");
        }
        if (mService != null) {
            try {
                mService.getData(jsonObject.toString(), null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DebugUtils.LogD("HerBootGuideActivity begin to MSG_UNBIND_ACCOUNT onServiceConnected");
            mService = IAICoreInterface.Stub.asInterface(service);

            mHandler.sendEmptyMessage(MSG_UNBIND_ACCOUNT);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DebugUtils.LogD("HerBootGuideActivity onServiceDisconnected");
            mService = null;
        }
    };

    private class BootGuideHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BIND_AI_SERIVCE:
                    bindAiService();
                    break;
                case MSG_UNBIND_ACCOUNT:
                    eraseBinders();
                    break;
                default:
                    break;
            }
        }
    }

}
