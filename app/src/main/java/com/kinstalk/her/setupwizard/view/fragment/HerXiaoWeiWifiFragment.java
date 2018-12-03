package com.kinstalk.her.setupwizard.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kinstalk.her.httpsdk.util.DebugUtil;
import com.kinstalk.her.setupwizard.HerSetupWizardApplication;
import com.kinstalk.her.setupwizard.R;
import com.kinstalk.her.setupwizard.data.eventbus.DataEventBus;
import com.kinstalk.her.setupwizard.data.eventbus.entity.WifiConnectStatusChangeEntity;
import com.kinstalk.her.setupwizard.data.eventbus.entity.WifiConnectSuccessEntity;
import com.kinstalk.her.setupwizard.data.eventbus.entity.WifiScanSuccessEntity;
import com.kinstalk.her.setupwizard.data.eventbus.entity.WifiScanTimeoutEntity;
import com.kinstalk.her.setupwizard.data.wifi.ScanResultEntity;
import com.kinstalk.her.setupwizard.data.wifi.WifiHelper;
import com.kinstalk.her.setupwizard.data.wifi.WifiScanTimer;
import com.kinstalk.her.setupwizard.util.Constants;
import com.kinstalk.her.setupwizard.util.ToastHelper;
import com.kinstalk.her.setupwizard.view.activity.HerBootGuideXiaoWeiActivity;
import com.kinstalk.her.setupwizard.view.adapter.WifiListAdapter;
import com.kinstalk.her.setupwizard.view.widget.WifiListHeaderView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Unbinder;

/**
 * Created by Zhigang Zhang on 2017/5/15.
 */

public class HerXiaoWeiWifiFragment extends Fragment {
    private Unbinder unbinder;
    private WifiScanTimer scanTimer;

    @BindView(R.id.wifi_listview)
    ListView mListView;

    @BindView(R.id.wifi_next)
    TextView mNextView;

    WifiListHeaderView mHeaderView;
    ImageView mScanView;
    TextView mScanContentView;
    ImageView mSecretCode;

    WifiListAdapter mAdapter;
    private boolean isAuthDlgShown = false;
    private boolean mConnecting = false;
    protected Activity mActivity;
    long[] mHits = new long[6];

    public static HerXiaoWeiWifiFragment getInstance() {
        return new HerXiaoWeiWifiFragment();
    }

    private void addHeaderView() {
        mHeaderView = new WifiListHeaderView(getActivity());
        mScanView = (ImageView) mHeaderView.findViewById(R.id.wifi_scan);
        mScanContentView = (TextView)mHeaderView.findViewById(R.id.scan_status_content);
        mSecretCode = (ImageView)mHeaderView.findViewById(R.id.secret_code);

        mScanContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.scan_status_content) {
                    startScan();
                }
            }
        });

        mSecretCode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 1000)) {
                    mHits[1] = 0;
                    Intent intent = new Intent();
                    intent.setAction("com.tinnotech.FactoryTest");
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            }
        });
        mHeaderView.setConnectedWifiClickedListener(new WifiListHeaderView.OnConnectedWifiClickedListener() {
            @Override
            public void onClick() {
                startScan();
            }
        });
        mScanView.setVisibility(View.INVISIBLE);
        mListView.addHeaderView(mHeaderView);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_wifi, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        scanTimer = new WifiScanTimer();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DataEventBus.register(this);
        addHeaderView();
        mAdapter = new WifiListAdapter(mListView);
        mListView.setAdapter(mAdapter);

        if(isConnected()) {
            mNextView.setVisibility(View.VISIBLE);
        } else {
            mNextView.setVisibility(View.GONE);
        }
        mAdapter.refreshData(WifiHelper.getInstance().getScanResultEntityList());
        mHeaderView.bindStatus(new WifiConnectStatusChangeEntity(NetworkInfo.State.CONNECTED, NetworkInfo.DetailedState.CONNECTED));
        startScan();
    }

    @Override
    public void onAttach(Context context) {
        // Log.d(TAG,"onAttach");
        super.onAttach(context);
        this.mActivity = getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = HerSetupWizardApplication.getApplication().getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREF_WIFI_SETTINGS, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(Constants.SHARED_PREF_KEY_WIFI_FOREGROUND, true).commit();
        SystemProperties.set("persist.sys.wifi.status", "1");
        if(isConnected()) {
            mNextView.setVisibility(View.VISIBLE);
        } else {
            mNextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Context context = HerSetupWizardApplication.getApplication().getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREF_WIFI_SETTINGS, Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(Constants.SHARED_PREF_KEY_WIFI_FOREGROUND, false).commit();
        SystemProperties.set("persist.sys.wifi.status", "0");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        scanTimer.stop();
        unbinder.unbind();
        DataEventBus.getEventBus().unregister(this);
    }

    private boolean isConnected() {
        return ((HerBootGuideXiaoWeiActivity)mActivity).isWiFiConnected();
    }

    @OnClick({R.id.wifi_next})
    public void onClick(View view) {
        if(view.getId() == R.id.wifi_next) {
            Activity parentActivity = getActivity();
            if((parentActivity != null) && (parentActivity instanceof HerBootGuideXiaoWeiActivity)) {
                ((HerBootGuideXiaoWeiActivity)parentActivity).toGuideBindFragment();
            }
        }
    }

    private void startScan() {
        scanTimer.stop();
        mListView.smoothScrollToPositionFromTop(0, 0);

        Animation mRotateAnimation = new RotateAnimation(0.0f, 720.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setFillAfter(true);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setDuration(2000);
        mRotateAnimation.setRepeatCount(Animation.INFINITE);
        mRotateAnimation.setRepeatMode(Animation.RESTART);

        mScanView.setVisibility(View.VISIBLE);
        mScanView.setAnimation(mRotateAnimation);

        scanTimer.start();

        mScanContentView.setEnabled(false);
        mScanContentView.setText(R.string.wifi_scanning);
    }


    @OnItemClick({R.id.wifi_listview})
    public void onItemClick(int postion) {
        isAuthDlgShown = false;
        postion = postion - mListView.getHeaderViewsCount();
        if (postion < 0 || postion >= mAdapter.getCount()) {
            return;
        }
        ScanResultEntity scanResultEntity = (ScanResultEntity) mAdapter.getItem(postion);
        ScanResult scanResult = scanResultEntity.getScanResult();
        //无密码
        if (!WifiHelper.isNeedAuth(scanResult.capabilities)) {
            WifiConfiguration configuration = WifiHelper.getInstance().createWifiConfiguration(scanResult.SSID, WifiHelper.AUTH_NONE, "", "");
            WifiHelper.getInstance().connectNetwork(configuration);
            return;
        }
        //已记录密码
        int networkId = WifiHelper.getInstance().getConfiguredNetworkID(scanResult.SSID);
        if (networkId != -1) {
            WifiHelper.getInstance().connectConfiguredNetwork(scanResult.SSID);
            return;
        }
        //有且没记录密码
        isAuthDlgShown = true;
        WifiAuthDialogFragment wifiAuthDialogFragment = WifiAuthDialogFragment.newInstance(mAdapter.getItem(postion).getScanResult());
        wifiAuthDialogFragment.show((getActivity()).getSupportFragmentManager(), "wifiAuthDialogFragment");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScanTimeout(WifiScanTimeoutEntity timeoutEntity) {
        scanTimer.stop();

        mScanView.setVisibility(View.INVISIBLE);
        mScanView.clearAnimation();

        mScanContentView.setText(R.string.wifi_not_scan);
        mScanContentView.setEnabled(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScanSuccess(WifiScanSuccessEntity successEntity) {
        DebugUtil.LogD("HerXiaoweiWifiFragment","scanSuccess,successEntity:"+successEntity.toString());
        scanTimer.stop();

        mScanView.setVisibility(View.INVISIBLE);
        mScanView.clearAnimation();

        mScanContentView.setText(R.string.wifi_not_scan);
        mScanContentView.setEnabled(true);

        mAdapter.refreshData(WifiHelper.getInstance().getScanResultEntityList());
        mHeaderView.bindStatus(new WifiConnectStatusChangeEntity(NetworkInfo.State.CONNECTED, NetworkInfo.DetailedState.CONNECTED));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiNetworkStatusChange(WifiConnectStatusChangeEntity statusEntity) {
        DebugUtil.LogD("HerXiaoWeiWifiFragment",
                "onWifiNetworkStatusChange: statusEntity.getState()"+statusEntity.getState());
        mHeaderView.bindStatus(statusEntity);
        boolean isConnected = false;
        switch (statusEntity.getState()) {
            case CONNECTED:
                DebugUtil.LogD("HerXiaoWeiWifiFragment","connected");
                mListView.smoothScrollToPositionFromTop(0, 0);
                mAdapter.refreshData(WifiHelper.getInstance().getScanResultEntityList());
                mConnecting = false;
                isConnected = true;
                break;
            case CONNECTING:
                DebugUtil.LogD("HerXiaoWeiWifiFragment","connecting");
                mAdapter.setLoadingStatus();
                mConnecting = true;
                break;
            case DISCONNECTED:
                if(mConnecting) {
                    DebugUtil.LogD("HerXiaoWeiWifiFragment","disconnected and mConnecting is true");
                    if(!isAuthDlgShown) {
                        DebugUtil.LogD("HerXiaoWeiWifiFragment","show toast");
                        ToastHelper.makeText(getContext(), getContext().getDrawable(R.drawable.toast_fail),
                                getContext().getString(R.string.connect_failed), Toast.LENGTH_LONG).show();
                        WifiHelper.getInstance().removeWifi(WifiHelper.getInstance().getLastNetworkId());
                    }
                    //重置所有loading状态
                    mAdapter.resetLoadingStatus();
                    mConnecting = false;
                }
        }
        DebugUtil.LogD("HerXiaoWeiWifiFragment","isConnected:"+isConnected);
        if(isConnected) {
            mNextView.setVisibility(View.VISIBLE);
        } else {
            mNextView.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiConnectSuccess(WifiConnectSuccessEntity successEntiry) {
        //do nothing
    }
}
