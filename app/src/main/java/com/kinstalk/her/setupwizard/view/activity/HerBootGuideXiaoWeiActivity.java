package com.kinstalk.her.setupwizard.view.activity;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
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
import com.kinstalk.her.setupwizard.view.fragment.GuideXiaoWeiActivateFailFragment;
import com.kinstalk.her.setupwizard.view.fragment.GuideActivatingFragment;
import com.kinstalk.her.setupwizard.view.fragment.GuideBindFragment;
import com.kinstalk.her.setupwizard.view.fragment.GuideRebindFragment;
import com.kinstalk.her.setupwizard.view.fragment.HerXiaoWeiWifiFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.SystemProperties;

import kinstalk.com.qloveaicore.IAICoreInterface;
import ly.count.android.sdk.Countly;
import android.app.ActivityManager;
import android.content.pm.IPackageDataObserver;

public class HerBootGuideXiaoWeiActivity extends FragmentActivity {

    private static final int MIN_ACTIVATING_TIME = 2000;
    private static final int MAX_ACTIVATING_TIME = 15000;

    public static final int SYSTEM_UI_FLAG_FULLSCREEN_CAN_NOT_SWIPE = 0x00000006;

    private static final int MSG_BIND_AI_SERIVCE = 0;
    private static final int MSG_UNBIND_ACCOUNT = 1;

    private Context mContext;
    private boolean mWifiPassed = false;
    private boolean mBindStatusValid = false;
    private boolean mBindStatus = false;
    private boolean mWiFiConnected = false;

    private boolean mNeedToActivate = true;
    private final Handler mHandler = new BootGuideHandler();
    private IAICoreInterface mService;
    private long mActivatingDlgShowTime = 0;

    private boolean mUnbind = false;
    private boolean mIsActivating = false;
    private boolean mDataCleared = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        fotaCheck();
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

        boolean ifUnbind = getIntent().getBooleanExtra(Constants.EXTRA_UNBIND_ACTION, false);
        if(!ifUnbind) {
            toGuideWifiFragment();
        } else {
            //if the unbind from administration, show unbind tip first
            toGuideRebindFragment();
            mBindStatusValid = true;
            mBindStatus = false;
        }

        registerBindStatusReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        Countly.sharedInstance().onStart(this);
        WifiHelper.getInstance().turnOnWiFiIfNeeded();
        if (SystemProperties.getBoolean("persist.sys.fisrtboot", true)) {
            long buildTime = SystemProperties.getLong("ro.build.date.utc", 86400) * 1000;
            SystemClock.setCurrentTimeMillis(buildTime);
            SystemProperties.set("persist.sys.fisrtboot","false");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Countly.sharedInstance().onStop();
    }

    @Override
    public void onResumeFragments() {
        super.onResumeFragments();
    }

    private void registerBindStatusReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.KINSTALK_TXSDK_BIND_STATUS);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        mContext.registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterBindStatusReceiver() {
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataEventBus.getEventBus().unregister(this);

        if(mService != null) {
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

    private void toGuideRebindFragment() {
        mIsActivating = false;
        mUnbind = mBindStatusValid;
        getSupportFragmentManager().beginTransaction().replace(R.id.content, new GuideRebindFragment()).commitAllowingStateLoss();
    }

    public void toGuideWifiFragment() {
        mIsActivating = false;
        getSupportFragmentManager().beginTransaction().replace(R.id.content, new HerXiaoWeiWifiFragment()).commitAllowingStateLoss();
    }

    private void toGuideActivatingFragment() {
        GuideActivatingFragment fragment = new GuideActivatingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("MODE","xiaowei");
        fragment.setArguments(bundle);
        mIsActivating = true;
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commitAllowingStateLoss();
    }

    private void toGuideActivateFailFragment() {
        mIsActivating = false;
        getSupportFragmentManager().beginTransaction().replace(R.id.content, new GuideXiaoWeiActivateFailFragment()).commitAllowingStateLoss();
    }

    public void toGuideBindFragment() {
        mIsActivating = false;
        if(mNeedToActivate) {
            startUnbindAccount();
        } else {
            mWifiPassed = true;
            getSupportFragmentManager().beginTransaction().replace(R.id.content, new GuideBindFragment()).commitAllowingStateLoss();
        }
    }

    public void doRebind() {
        if(WifiHelper.getInstance().isWifiConnected()) {
            mWiFiConnected = true;
            toGuideBindFragment();
        } else {
            mWiFiConnected = false;
            toGuideWifiFragment();
        }
    }

    public boolean isWiFiConnected() {
        return mWiFiConnected;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiConnectSuccess(WifiConnectSuccessEntity successEntity) {
        if(!mWiFiConnected) {
            if (mNeedToActivate) {
                if (mWiFiConnected) {
                    //already receive wifi connected status, skip activate process
                    return;
                }
                startUnbindAccount();
            } else if (!mWifiPassed) {
                toGuideBindFragment();
            }
            mWiFiConnected = true;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiNetworkStatusChange(WifiConnectStatusChangeEntity statusEntity) {
        switch (statusEntity.getState()) {
            case CONNECTED:
                if(!mWiFiConnected) {
                    if (mNeedToActivate) {
                        if (mWiFiConnected) {
                            //already receive wifi connected status, skip activate process
                            return;
                        }
                        startUnbindAccount();
                    } else if (!mWifiPassed) {
                        toGuideBindFragment();
                    }
                    mWiFiConnected = true;
                }
                break;
            case DISCONNECTED:
                //TODO, how to make sure the scenario which backing from bind screen?
                //防止wifi获取的连接信息不可靠
                if(isNetworkAvailable()){
                    Log.i("HerBootGuideXiaoWei", "wifi DISCONNECTED,but isNetworkAvailable: is connected");
                    if(!mWiFiConnected) {
                        if (mNeedToActivate) {
                            if (mWiFiConnected) {
                                //already receive wifi connected status, skip activate process
                                return;
                            }
                            startUnbindAccount();
                        } else if (!mWifiPassed) {
                            toGuideBindFragment();
                        }
                        mWiFiConnected = true;
                    }
                }else {
                    mWifiPassed = false;
                    mWiFiConnected = false;
                }
                break;
        }
    }


    public static boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) HerSetupWizardApplication.getApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (cm.getActiveNetworkInfo() != null) {
                return cm.getActiveNetworkInfo().isAvailable();
            }
        }
        return false;
    }

    private void startUnbindAccount() {
        if (!PackageUtil.getXiaoWeiBindStatus(mContext)) {
            if (mBindStatusValid) {
                DebugUtils.LogD("bind status valid:" + mBindStatusValid + " bind status:" + mBindStatus);
                if (mBindStatus) {
                    startActivating(MAX_ACTIVATING_TIME);
                    unBindSDK();
                } else {
                    //do not need to unbind, show dialog "activating" for 2 seconds
                    startActivating(MIN_ACTIVATING_TIME);
                }
            } else {
                //not receive bind status, waiting for 15 seconds for activating
                if (!mUnbind) {
                    startActivating(MAX_ACTIVATING_TIME / 3);
                } else {
                    startActivating(MAX_ACTIVATING_TIME);
                }
            }
        }
    }

    public void tryUnbindAgain() {
        if (!PackageUtil.getXiaoWeiBindStatus(mContext)) {
            if (mBindStatusValid) {
                if (!mBindStatus) {
                    //goto bind screen immediately
                    mHandler.post(mActivatingRunnable);
                } else {
                    startActivating(MAX_ACTIVATING_TIME);
                    unBindSDK();
                }
            } else {
                startActivating(MAX_ACTIVATING_TIME);
                unBindSDK();
            }
        }
    }

    private void unBindSDK() {
        mUnbind = true;

        //do unbind
        DebugUtils.LogD("start to unbind");
        if(mService == null) {
            mHandler.sendEmptyMessage(MSG_BIND_AI_SERIVCE);
        } else {
            mHandler.sendEmptyMessage(MSG_UNBIND_ACCOUNT);
        }
        //clearData("com.kinstalk.her.baby");
    }

    private void startActivating(long duration) {
        toGuideActivatingFragment();

        mHandler.removeCallbacks(mActivatingRunnable);
        if(duration != 0) {
            mActivatingDlgShowTime = SystemClock.elapsedRealtime();
            mHandler.postDelayed(mActivatingRunnable, duration);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            DebugUtils.LogD("HerBootGuideActivity receiver : " + action);

            if (Constants.KINSTALK_TXSDK_BIND_STATUS.equals(action)) {
                boolean bindStatus = intent.getBooleanExtra(Constants.EXTRA_BIND_STATUS, false);
                DebugUtils.LogD("HerBootGuideActivity bind status : " + bindStatus);

                boolean processBindStatus = false;
                if(mIsActivating) {
                    if(!mBindStatusValid) {
                        //first time to receive bind status
                        processBindStatus = true;
                    } else if(bindStatus != mBindStatus) {
                        processBindStatus = true;
                    }

                    if(processBindStatus) {
                        if (bindStatus) {
                            startActivating(MAX_ACTIVATING_TIME);
                            unBindSDK();
                        } else {
                            //remove the activating dialog immediately
                            mHandler.removeCallbacks(mActivatingRunnable);
                            long elapseTime = MIN_ACTIVATING_TIME - (SystemClock.elapsedRealtime() - mActivatingDlgShowTime);
                            if(elapseTime > 0) {
                                mHandler.postDelayed(mActivatingRunnable, elapseTime);
                            } else {
                                mHandler.post(mActivatingRunnable);
                            }
                        }
                    }
                }

                mBindStatus = bindStatus;
                mBindStatusValid = true;
            } else if(Intent.ACTION_TIME_CHANGED.equals(action)) {
                DebugUtils.LogD("time changed");
                if(mIsActivating && mUnbind) {
                    //may have sent unbind command with wrong time, send it again
                    unBindSDK();
                }
            }
        }
    };

    private final Runnable mActivatingRunnable = new Runnable() {
        @Override
        public void run() {
            DebugUtils.LogD("runnable bind status valid:" + mBindStatusValid + " bind status:" + mBindStatus);
            if(!mBindStatusValid || mBindStatus) {
                if(!mUnbind) {
                    unBindSDK();
                    startActivating(MAX_ACTIVATING_TIME);
                } else {
                    toGuideActivateFailFragment();
                }
            } else {
                //unbind, move the next screen - bind screen
                mNeedToActivate = false;
                toGuideBindFragment();
            }
        }
    };

    private boolean bindAiService() {
        DebugUtils.LogD("start to bind AI service");
        Intent intent = new Intent();
        intent.setClassName(Constants.REMOTE_AI_SERVICE, Constants.REMOTE_AI_SERVICE_CLASS);
        return mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    private void eraseBinders() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.GET_DATA_CMD_STR, Constants.GET_DATA_CMD_ERASE_ALL_BINDERS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(mService != null) {
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
            DebugUtils.LogD("onServiceConnected");
            mService = IAICoreInterface.Stub.asInterface(service);

            mHandler.sendEmptyMessage(MSG_UNBIND_ACCOUNT);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DebugUtils.LogD("onServiceDisconnected");
            mService = null;
        }
    };

    private class BootGuideHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
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

    public void fotaCheck() {
        if (PackageUtil.getXiaoWeiBindStatus(mContext)) {
            DebugUtils.LogD("a22417 in fotaCheck begin to disableBootGuideXiaoWeiActivity");
            PackageUtil.disableBootGuideXiaoWeiActivity(mContext);
            finish();
        }
    }
    /*
    private ClearUserDataObserver mClearDataObserver;

    private void clearData(String packageName) {
        if(mDataCleared) {
            return;
        }
        mDataCleared = true;

        if (mClearDataObserver == null) {
            mClearDataObserver = new ClearUserDataObserver();
        }
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean res = am.clearApplicationUserData(packageName, mClearDataObserver);
        if (!res) {
            // Clearing data failed for some obscure reason. Just log error for now
            Log.i("HerBootGuideActivity", "Couldnt clear application user data for package:" + packageName);

        } else {

        }
    }
    class ClearUserDataObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            Log.d("HerBootGuideActivity", "packageName "+packageName +"   succeeded  "+succeeded);
        }
    }*/
}
