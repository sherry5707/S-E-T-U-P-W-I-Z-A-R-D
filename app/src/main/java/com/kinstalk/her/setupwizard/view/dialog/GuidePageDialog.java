package com.kinstalk.her.setupwizard.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.kinstalk.her.setupwizard.R;
import com.kinstalk.her.setupwizard.util.PackageUtil;
import com.kinstalk.qloveaicore.AIManager;

import com.kinstalk.util.AppUtils;

import java.util.HashMap;

public class GuidePageDialog extends Activity implements ViewPager.OnPageChangeListener {

    private String TAG = "GuidePageDialog";
    private ViewPager vp;
    private int[] imageIdArray;//图片资源的数组
    HashMap<Integer, String> guideResource = new HashMap<>();
    private int mPosition;
    private static final Long SPACE_TIME = 800L;
    private static Long lastClickTime = 0L;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, GuidePageDialog.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);
    }

    static String ACTION_KEY_FINISHED = "ACTION_KEY_FINISHED";
    public static void  actionFinish(Context context) {
        Intent intent = new Intent(ACTION_KEY_FINISHED);
        context.sendBroadcast(intent);
    }

    private final LocalInfoIntentReceiver pLocalkeyInfoReceiver = new LocalInfoIntentReceiver();

    private void registerLocalReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_KEY_FINISHED);
        registerReceiver(pLocalkeyInfoReceiver, filter);
    }

    private class LocalInfoIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (TextUtils.equals(action, ACTION_KEY_FINISHED)) {
                finishActivity();
            }
        }
    }

    protected void finishActivity(){
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Settings.System.putInt(getContentResolver(),"show_guidepagedlg",1);
        PackageUtil.setDeviceProvisioned(getApplicationContext(), true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);


        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        getWindow().setGravity(Gravity.TOP);

        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                        // bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        disableAutoHome(getWindow());

        initView();
        initResource();
        registerLocalReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("GuidePageDialog","a22417 debug the begin to onStart");
        playWorkContent(guideResource.get(0));
    }

    public void initView() {

        //加载ViewPager
        vp = (ViewPager) findViewById(R.id.guide_vp);
        //实例化图片资源
        imageIdArray = new int[]{R.mipmap.guide_1, R.mipmap.guide_2,R.mipmap.guide_3, R.mipmap.guide_4,R.mipmap.guide_5, R.mipmap.guide_6};
        //获取一个Layout参数，设置为全屏
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        //View集合初始化好后，设置Adapter
        vp.setAdapter(new GuidePageAdapter(this, imageIdArray, listener));
        vp.addOnPageChangeListener(this);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPosition == 5) {
                PackageUtil.setShowGuideStatus(getApplicationContext(), false);
                //PackageUtil.setDeviceProvisioned(getApplicationContext(), true);
                finish();
                pauseTts();
                abandonAudioFocus();
                PackageUtil.switchPrivacy(getApplicationContext(), false);
            } else {
                vp.setCurrentItem((mPosition + 1),true);
            }
        }
    };

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mPosition = position;
        if (isFastSwitch()) {
            return;
        }
        pauseTts();
        abandonAudioFocus();
        playWorkContent(guideResource.get(position));
    }

    //有三个值：0（END）,1(PRESS) , 2(UP)
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void playWorkContent(String content) {
        Log.i("GuidePageDialog","a22417 debug the begin to playWorkContent");
        AIManager.getInstance(this).playTextWithStr(content,null);
//        final String ACTION_TXSDK_PLAY_TTS = "kingstalk.action.wateranimal.playtts";
//        Intent intent = new Intent(ACTION_TXSDK_PLAY_TTS);
//        Bundle bundle = new Bundle();
//        bundle.putString("text", content);
//        intent.putExtras(bundle);
//        this.sendBroadcast(intent);
    }

    private void disableAutoHome(Window window) {
        AppUtils.setAutoActivityTimeout(window, false);
    }

    public void initResource() {
        guideResource.put(0, "嗨，我是无所不能的小微，我会告诉你很多好玩的事情哟");
        guideResource.put(1, "你的小星星和礼物都放在这里啦");
        guideResource.put(2, "时钟里隐藏着获取小星星的神秘任务，一定要经常来看看哦");
        guideResource.put(3, "每天的作业我都帮你整理到这里啦");
        guideResource.put(4, "这里可以聊天哦，猜猜跟你聊天的是谁");
        guideResource.put(5, "由于你出色的表现，获得了3颗小星星，要继续努力，按时完成每天的任务哦");
    }

    private void pauseTts() {
        Log.d(TAG, "gain audioFocus.");
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(afChangeListener, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
    }

    /*
     * when complete the recording we should abandon the audio focus
     */
    private void abandonAudioFocus() {
        Log.d(TAG, "release audioFocus.");
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(afChangeListener);
    }

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                Log.d(TAG, "AudioFocus.changed:AUDIOFOCUS_LOSS_TRANSIENT");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Stop playback
                Log.d(TAG, "AudioFocus.changed:AUDIOFOCUS_LOSS");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // Lower the volume
                Log.d(TAG, "AudioFocus.changed:AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback or Raise it back to normal
                Log.d(TAG, "AudioFocus.changed:AUDIOFOCUS_GAIN");
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Settings.System.putInt(getContentResolver(),"show_guidepagedlg",0);
        unregisterReceiver(pLocalkeyInfoReceiver);
    }

    private static boolean isFastSwitch() {
        long currentTime = System.currentTimeMillis();//当前系统时间
        boolean isFastSwitch;//是否快速滑动
        if (currentTime - lastClickTime < SPACE_TIME) {
            isFastSwitch = true;
        } else {
            isFastSwitch = false;
            lastClickTime = currentTime;
        }
        return isFastSwitch;
    }
}
