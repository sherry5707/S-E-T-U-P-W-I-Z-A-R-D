package com.kinstalk.her.setupwizard.view.widget;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinstalk.her.setupwizard.R;
import com.kinstalk.her.setupwizard.data.eventbus.DataEventBus;
import com.kinstalk.her.setupwizard.data.eventbus.entity.WifiConnectStatusChangeEntity;
import com.kinstalk.her.setupwizard.data.eventbus.entity.WifiConnectSuccessEntity;
import com.kinstalk.her.setupwizard.data.wifi.WifiHelper;

/**
 * Created by zhigang zhang on 17/10/12.
 */

public class WifiListHeaderView extends LinearLayout implements View.OnClickListener {
    private Context mContext;

    private TextView mLabelView;
    private ImageView mSignalView;
    private ViewGroup mConnectLayout;

    private String deleteTitle = "";
    private OnConnectedWifiClickedListener clickListener;

    public WifiListHeaderView(Context context) {
        super(context);
        initView(context);
    }

    public WifiListHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public WifiListHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_wifilist_header, this);

        mLabelView = (TextView) findViewById(R.id.wifi_ssid);
        mSignalView = (ImageView) findViewById(R.id.wifi_signal_icon);
        mConnectLayout = (ViewGroup) findViewById(R.id.connected_wifi_info);
        mConnectLayout.setOnClickListener(this);
        refreshView();
    }

    public void bindStatus(WifiConnectStatusChangeEntity statusEntity) {
        switch (statusEntity.getState()) {
            case DISCONNECTED:
                mConnectLayout.setVisibility(View.GONE);
                break;
            case CONNECTED:
                refreshView();
                break;
        }
    }

    private void refreshView() {
        WifiInfo wifiInfo = WifiHelper.getInstance().getWifiInfo();
        if ((wifiInfo != null) && (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED)) {
            ScanResult scanResult = WifiHelper.getInstance().getScanResultBySSID(wifiInfo.getSSID());
            if (scanResult != null) {
                boolean isNeedAuth = WifiHelper.isNeedAuth(scanResult.capabilities);
                int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
                deleteTitle = String.format(getContext().getString(R.string.wifi_delete_title), scanResult.SSID);
                mLabelView.setText(scanResult.SSID);
                if (isNeedAuth) {
                    mSignalView.setImageResource(R.drawable.wifi_signal_lock_icon);
                } else {
                    mSignalView.setImageResource(R.drawable.wifi_signal_icon);
                }
                mSignalView.setImageLevel(level);
                mConnectLayout.setVisibility(View.VISIBLE);
                if (WifiHelper.getInstance().getLastNetworkId() != -1) {
                    WifiHelper.getInstance().setLastConnectNetworkId(-1);
                }
                //发出当前网络已可用的事件通知
                DataEventBus.getEventBus().post(new WifiConnectSuccessEntity());
            } else {
                mConnectLayout.setVisibility(View.GONE);
            }
        } else {
            mConnectLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connected_wifi_info:
                final WifiInfo wifiInfo = WifiHelper.getInstance().getWifiInfo();
                if (wifiInfo != null) {
                    WifiHelper.getInstance().removeWifi(wifiInfo.getNetworkId());
                    if (clickListener != null) {
                        clickListener.onClick();
                    }
                }
                break;
        }
    }

    public void setConnectedWifiClickedListener(OnConnectedWifiClickedListener listener) {
        this.clickListener = listener;
    }

    public interface OnConnectedWifiClickedListener {
        void onClick();
    }
}
