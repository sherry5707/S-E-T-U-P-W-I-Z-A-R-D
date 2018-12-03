package com.kinstalk.her.setupwizard.view.fragment;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.kinstalk.her.httpsdk.rx.RxUtil;
import com.kinstalk.her.setupwizard.HerSetupWizardApplication;
import com.kinstalk.her.setupwizard.R;
import com.kinstalk.her.setupwizard.data.retrofit.HerApiService;
import com.kinstalk.her.setupwizard.data.retrofit.entity.AccountResponse;
import com.kinstalk.her.setupwizard.data.retrofit.entity.AccountSubscriber;
import com.kinstalk.her.setupwizard.util.Constants;
import com.kinstalk.her.setupwizard.util.DebugUtils;
import com.kinstalk.her.setupwizard.util.DeviceUtils;
import com.kinstalk.her.setupwizard.util.PackageUtil;
import com.kinstalk.her.setupwizard.util.QRCodeUtil;
import com.kinstalk.her.setupwizard.view.activity.HerBootGuideActivity;
import com.kinstalk.her.setupwizard.view.activity.HerBootGuideXiaoWeiActivity;
import com.kinstalk.m4.publicownerlib.OwnerColumn;
import com.kinstalk.m4.publicownerlib.OwnerUri;
import com.kinstalk.qloveaicore.AIManager;

import org.json.JSONException;
import org.json.JSONObject;

import com.kinstalk.util.AppUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kinstalk.com.qloveaicore.AICoreDef;

public class GuideBindFragment extends Fragment {

    @BindView(R.id.code)
    ImageView qrCodeImgView;

    private Unbinder unbinder;
    private Context mContext;
    private boolean mCreateUser = true;

    private String tinyID;
    private String remark;
    private String din;

    private final Handler mHandler = new Handler();

    public static GuideBindFragment getInstance() {
        return new GuideBindFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_guide_bind, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        registerPIDSN();
        registerBind();

        disableAutoHome(getActivity().getWindow());
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mHandler.removeCallbacks(mCreateUserRunnable);
        unbinder.unbind();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @OnClick({R.id.back_button})
    public void onClick(View view) {
        if (view.getId() == R.id.back_button) {
            ((HerBootGuideXiaoWeiActivity) getActivity()).toGuideWifiFragment();
            //((HerBootGuideXiaoWeiActivity)getActivity()).onBackPressed();
        }
    }

    private void registerPIDSN() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("kinstalk.com.aicore.action.txsdk");
        getActivity().registerReceiver(receiver, intentFilter);
    }

    private void registerBind() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("kinstalk.com.aicore.action.txsdk.bind_status");
        getActivity().registerReceiver(receiver, intentFilter);
    }

    private void createUser() {
        DebugUtils.LogE("serial no : " + DeviceUtils.getHerSerialNumber());

        if ((TextUtils.isEmpty(tinyID)) ||
                (!TextUtils.isEmpty(tinyID) && (Long.parseLong(tinyID) == 0))) {
            mHandler.postDelayed(mCreateUserRunnable, 3500);
            return;
        }
        getAccountInfo();
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
                                PackageUtil.setAccountBindMobile(mContext,response.user.getMobile());

                                Uri insertUri = mContext.getContentResolver().insert(OwnerUri.OWNER_URI, contentValues);
                                Log.i("a22418", "a22418 the insertUri = " + insertUri);
                                if (insertUri != null) {
                                    mHandler.postDelayed(mCloseRunnable, 1000);
                                    //getActivity().finish();
                                }
                            } else {
                                Toast.makeText(mContext, "出错了 ：" +response.getMsg(), Toast.LENGTH_LONG).show();

                                //LENGTH_LONG is 3.5 seconds, retry to create user after toast disappear
                                mHandler.postDelayed(mCreateUserRunnable, 3500);
                            }
                        }
                    }

                    @Override
                    public void resultError(Throwable e) {
                        DebugUtils.LogE("" + e.getMessage());

                        Toast.makeText(mContext, "创建异常 ：" + e.getMessage(), Toast.LENGTH_LONG).show();

                        //LENGTH_LONG is 3.5 seconds, retry to create user after toast disappear
                        mHandler.postDelayed(mCreateUserRunnable, 3500);
                    }
                });
    }

    private String decData(String data) {
        if (data == null) {
            return null;
        }
        String d = data.substring(0, 2) + data.substring(4);
        return new String(Base64.decode(d, Base64.DEFAULT));
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String acion = intent.getAction();

            DebugUtils.LogE("receiver : " + acion);

            if ("kinstalk.com.aicore.action.txsdk".equals(acion)) {
                String pid;
                String sn;

                pid = decData(intent.getStringExtra("kinstalk.com.aicore.action.txsdk.pid"));
                sn = decData(intent.getStringExtra("kinstalk.com.aicore.action.txsdk.sn"));
                String url = decData(intent.getStringExtra(AICoreDef.ACTION_TXSDK_EXTRA_QRURL));
                DebugUtils.LogE("txsdk.pid : " + pid);
                DebugUtils.LogE("txsdk.sn : " + sn);
                DebugUtils.LogE(AICoreDef.ACTION_TXSDK_EXTRA_QRURL + " - txsdk.url:" + url);

                mCreateUser = true;
                if (!generateQrCodeImage(url)) {
                    generateQrCodeImage(pid, sn);
                }
            } else if (Constants.KINSTALK_TXSDK_BIND_STATUS.equals(acion)) {
                boolean bind_status = intent.getBooleanExtra(Constants.EXTRA_BIND_STATUS, false);

                if (bind_status && mCreateUser) {
                    //mBack.setVisibility(View.GONE);
                    getAccountInfo();
                    if (PackageUtil.getWechatBindStatus(mContext)) {
                        PackageUtil.setDeviceProvisioned(mContext, true);
                    } else {
                        PackageUtil.enableBootGuideActivity(mContext);
                        Intent newIntent = new Intent(mContext, HerBootGuideActivity.class);
                        newIntent.putExtra(Constants.EXTRA_QCHAT_BIND_STATUS, true);
                        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(newIntent);
                    }
                    PackageUtil.setXiaoWeiBindStatus(mContext, true);
                    PackageUtil.disableBootGuideXiaoWeiActivity(mContext);

                    mCreateUser = false;
                    mHandler.post(mCreateUserRunnable);
                }
            }
        }
    };

    /**
     * 生成/显示二维码
     */
    private boolean generateQrCodeImage(String pid, String sn) {
        if (TextUtils.isEmpty(pid) || TextUtils.isEmpty(sn)) {
            return false;
        }
        StringBuilder content = new StringBuilder("http://iot.qq.com/add?pid=")
                .append(pid)
                .append("&sn=")
                .append(sn);
        DebugUtils.LogE("txsdk.url : " + content.toString());
        Bitmap xiaoWeiIconBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.qrcode_xiaowei);
        Bitmap bitmap = QRCodeUtil.createQRImage(content.toString(), 320, 320, xiaoWeiIconBitmap);
        qrCodeImgView.setImageBitmap(bitmap);
        return true;
    }

    private boolean generateQrCodeImage(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        Bitmap xiaoWeiIconBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.qrcode_xiaowei);
        Bitmap bitmap = QRCodeUtil.createQRImage(url, 320, 320, xiaoWeiIconBitmap);
        qrCodeImgView.setImageBitmap(bitmap);
        return true;
    }

    private final Runnable mCloseRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    };

    private final Runnable mCreateUserRunnable = new Runnable() {

        @Override
        public void run() {
            createUser();
        }
    };

    private void back2Home() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(i);
    }

    private void disableAutoHome(Window window) {
        try {
            AppUtils.setAutoActivityTimeout(window, false);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void getAccountInfo() {
        String jasonStr = AIManager.getInstance(mContext).getAccountInfo(null);
        Log.i("a22418", "a22418 begin to ");
        Log.e("GuideBindFragment", ": jasonStr:" + jasonStr);
        try {
            JSONObject jsonObject = new JSONObject(jasonStr);
            tinyID = jsonObject.optString("tinyID");
            din = jsonObject.optString("din");
            remark = jsonObject.optString("remark");
            Log.i("a22418", "a22418 begin to  the tingID = " + tinyID);
            Log.i("a22418", "a22418 begin to  the din = " + din);
            Log.i("a22418", "a22418 begin to  the remark = " + remark);
            PackageUtil.setXiaoWeiTinyId(mContext,tinyID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
