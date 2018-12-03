package com.kinstalk.her.setupwizard.data.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcast extends BroadcastReceiver {
    private static String TAG = "ScheduleApp";

    @Override
    public void onReceive(Context context, Intent mintent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(mintent.getAction())) {
            //开启Wifi扫描，如附近有已连接过自动连接
            WifiHelper.getInstance().startScan();
        }
    }

}  
