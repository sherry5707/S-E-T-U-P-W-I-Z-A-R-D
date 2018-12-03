package com.kinstalk.her.setupwizard.view.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinstalk.her.setupwizard.R;
import com.kinstalk.her.setupwizard.util.PackageUtil;

import com.kinstalk.util.AppUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.kinstalk.her.owner.provider.TryToSayEnum;
//import com.kinstalk.her.ownerproviderlib.OwnerProviderLib;

import kinstalk.com.countly.CountlyUtils;
import ly.count.android.sdk.Countly;

public class BindConfirmActivity extends Activity {

	private String TAG = "BindConfirmActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//设置无标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);


		setContentView(R.layout.activity_bind_confirm);
		disableAutoHome(getWindow());
		CountlyUtils.initCountly(this, false, true);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "a22417 onDestroy");
	}

	public void bindButtonClicked(View view) {
		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setPackage("com.kinstalk.her.setupwizard");
		i.setClassName("com.kinstalk.her.setupwizard","com.kinstalk.her.setupwizard.view.activity.HerBootGuideXiaoWeiActivity");
		startActivity(i);
		finish();

	}

	public void cancelButtonClicked(View view) {
		//Need to do.
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addCategory(Intent.CATEGORY_HOME);
		//i.addCategory(Intent.CATEGORY_DEFAULT);
		startActivity(i);
		finish();
	}

	private void disableAutoHome(Window window) {
		AppUtils.setAutoActivityTimeout(window, false);
	}
}
