package com.kinstalk.her.setupwizard.allapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.kinstalk.her.setupwizard.R;
import com.kinstalk.her.setupwizard.util.Constants;
import com.kinstalk.her.setupwizard.util.PackageUtil;
import com.kinstalk.her.setupwizard.view.dialog.GuidePageDialog;
import com.kinstalk.qloveaicore.AIManager;
import com.kinstalk.util.SystemProperty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import ly.count.android.sdk.Countly;

public class AllAPPList extends Activity {

    private String TAG = "AllAPPList";
    private TextView mLauncherTime, mTipsTime, mAllStar;
    private final String ACTION_NEW_HOMEWORK = "com.kinstalk.her.qchat.homework";
    private final String ACTION_NEW_MESSAGE = "com.kinstalk.her.qchat.message";
    private final String ACTION_ALL_STARTS_UPDATE = "com.kinstalk.her.qchat.star";
    private final String ACTION_TASK_UPDATE = "com.kinstalk.her.qchat.reminder";
    private ImageView leftDownImage, rightDownImage;
    private AnimationDrawable mHomeWorkAnimationDrawable, mMessageAnimationDrawable;
    private static boolean newMessage = false;
    private static boolean newHomework = false;
    private FrameLayout mMediaFrameLayout = null;
    private FrameLayout mTimerFrameLayout = null;
    private String mContent;//语音播放内容;
    private List<String> yuyinList = new ArrayList<String>(27);
    private int position = 0;//当前语音位于第position个
    private TextView mYuyinText;
    private ImageView remindImage;//根据时间移动的图片
    private RelativeLayout.LayoutParams remindParams;
    private List<Integer> remindList = new ArrayList<>();
    private int all_stars = 3;//星星总数,默认3
    private ImageView pkImg;//PK图标
    private boolean mReceiverTag = false;
    public static final int REMIND_TYPE_NONE = -1;
    public static final int REMIND_TYPE_SERVER = 0;
    public static final int REMIND_TYPE_XIAOWEI = 2;
    public static final int REMIND_TYPE_GETUP = 3;
    public static final int REMIND_TYPE_SLEEP = 4;
    public static final int REMIND_TYPE_WORK = 5;
    public static final int REMIND_TYPE_SELF = 6;

    //add by mengzhaoxue for 和家固话
    private ImageView dialerImage;

    private int time = 600;//10分钟换一个语音提示
    /**
     * 屏保
     */
    private int updataVedio = 5 *60*1000 /*5*60*1000*/;
    private boolean isScreenSaver;
    //首页如果退出了，被遮挡了，便不触发onTouch事件
    private boolean isOnpause;

    private Handler handler = new Handler();
    private int nowTime;//当前时间;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            nowTime++;
            Log.i(TAG, "nowTime=" + nowTime);
            if (nowTime >= time) {
                nowTime = 0;
                if (position == (yuyinList.size() - 1)) {
                    position = 0;
                } else {
                    position++;
                }
                mContent = yuyinList.get(position);
                mYuyinText.setText("你好小微，" + mContent);
                updateYuyinTextWidth();
            }
            handler.postDelayed(runnable, 1000);
        }
    };

    private final BroadcastReceiver mUpdateLauncherInfoReceiver = new UpdateLauncherInfoIntentReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.all_app_list);
        leftDownImage = (ImageView) findViewById(R.id.left_down_image);
        rightDownImage = (ImageView) findViewById(R.id.right_down_image);
        mLauncherTime = (TextView) findViewById(R.id.time_text_view);
        mTipsTime = (TextView) findViewById(R.id.am_pm_text_view);
        mAllStar = (TextView) findViewById(R.id.all_star);
        mYuyinText = (TextView) findViewById(R.id.yuyin_text);
        mMediaFrameLayout = (FrameLayout) findViewById(R.id.media_frame_layout);
        mTimerFrameLayout = (FrameLayout) findViewById(R.id.timer_frame_layout);
        remindImage = (ImageView) findViewById(R.id.remind_image);
        pkImg = (ImageView) findViewById(R.id.img_pk);
        dialerImage = (ImageView) findViewById(R.id.img_dialer);
        dialerImage.setVisibility(isCmccVersion() ? View.VISIBLE : View.GONE);
        remindParams = (RelativeLayout.LayoutParams) remindImage.getLayoutParams();
        remindList.add(R.drawable.remind_red);
        remindList.add(R.drawable.remind_sleep);
        remindList.add(R.drawable.remind_work);
        remindList.add(R.drawable.remind_getup);
        remindList.add(R.drawable.remind_blue);
        remindList.add(R.drawable.remind_new_hobby);

        //
        isScreenSaver = SystemProperty.getBoolean("persist.sys.zhuxiao", false);
        Log.d(TAG,"isScreenSaver="+isScreenSaver);

        //测试图片显示位置,不用屏蔽即可
        //testLayout();

        disableAutoHome(getWindow());
        PackageUtil.setScreenOffTimeout(this, 1800000);
        addYuyinList();
        mContent = yuyinList.get(position);//显示第一条消息
        mYuyinText.setText(mContent);
        mYuyinText.setText("你好小微，" + mContent);
        updateYuyinTextWidth();
        handler.postDelayed(runnable, 1000);
        initAudioRecord();
        registerLauncherReceiver();


        newMessage = PackageUtil.getMessageStatus(this);
        newHomework = PackageUtil.getHomeworkStatus(this);
    }

    public void registerLauncherReceiver() {
        if (!mReceiverTag) {
            mReceiverTag = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_DATE_CHANGED);
            filter.addAction(ACTION_NEW_HOMEWORK);
            filter.addAction(ACTION_NEW_MESSAGE);
            filter.addAction(ACTION_ALL_STARTS_UPDATE);
            filter.addAction(ACTION_TASK_UPDATE);
            filter.addAction(Constants.ACTION_MEDIA_INFO);
            filter.addAction("com.kinstalk.her.qchat.clear.data");
            filter.addAction("com.kinstalk.her.qchat.enter.homework");
            filter.addAction("com.kinstalk.her.qchat.enter.message");
            registerReceiver(mUpdateLauncherInfoReceiver, filter);
        }
    }

    private void updateYuyinTextWidth() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mYuyinText.getLayoutParams();
        layoutParams.width = (int) (Layout.getDesiredWidth(mYuyinText.getText(), mYuyinText.getPaint()) + 50);
        mYuyinText.setLayoutParams(layoutParams);
    }

    private boolean isCmccVersion(){
        String flag = SystemProperty.get("persist.sys.elink");
        if("ELINK_CMCC".equals(flag)){
            return true;
        }else {
            return false;
        }
    }


    int testHour = 0;

    Handler testHandler = new Handler();
    Runnable testRunnable = new Runnable() {
        @Override
        public void run() {
            setRemindLayout(testHour, 3);
            remindImage.setVisibility(View.VISIBLE);
            if (testHour == 12) {
                testHour = 0;
            } else {
                testHour++;
            }
            testHandler.postDelayed(testRunnable, 2000);
        }
    };

    private void testLayout() {
        testHandler.postDelayed(testRunnable, 100);
    }

    private void addYuyinList() {
        try {
            InputStream is = this.getAssets().open("yuyin.json");
            BufferedReader bufr = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = bufr.readLine()) != null) {
                builder.append(line);
            }
            is.close();
            bufr.close();
            try {
                JSONObject root = new JSONObject(builder.toString());
                JSONArray array = root.getJSONArray("yuyin_list");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    yuyinList.add(json.getString("name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnpause = false;
        if (newMessage) {
            //leftDownImage.setImageResource(R.drawable.left_down_pressed);
            startAnimation(true, false);
        } else {
            //leftDownImage.setImageResource(R.drawable.left_down);
            leftDownImage.setBackgroundResource(R.drawable.left_down_selector);
        }
        if (newHomework) {
            startAnimation(false, true);
            //rightDownImage.setImageResource(R.drawable.right_down_pressed);
        } else {
            //rightDownImage.setImageResource(R.drawable.right_down);
            rightDownImage.setBackgroundResource(R.drawable.right_down_selector);
        }
        all_stars = PackageUtil.getStars(this);
        mAllStar.setText(String.valueOf(all_stars));
        updateTime();
        Log.d(TAG,"onResume ");
        if (isScreenSaver)
            handler.postDelayed(homeVedioRunnable, updataVedio);
//        if (PackageUtil.getShowGuideStatus(this)) {
//            showGuide();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            unregisterReceiver(mUpdateLauncherInfoReceiver);
        }
        releaseRecord();
        if (null != handler) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        isOnpause =true;
        stopAnimation(true, true);
        if (isScreenSaver)
            handler.removeCallbacks(homeVedioRunnable);
    }


    /**
     * Launcher receiver.
     */
    private class UpdateLauncherInfoIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_TICK) ||
                    TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_CHANGED)) {
                updateTime();
            } else if (TextUtils.equals(intent.getAction(), "com.kinstalk.her.qchat.clear.data") ||
                    TextUtils.equals(intent.getAction(), "com.kinstalk.her.qchat.enter.message") ||
                    TextUtils.equals(intent.getAction(), "com.kinstalk.her.qchat.enter.homework")) {
                if (TextUtils.equals(intent.getAction(), "com.kinstalk.her.qchat.enter.message")) {
                    newMessage = false;
                    PackageUtil.setMessageStatus(context,false);
                    stopAnimation(false, true);
                }
                if (TextUtils.equals(intent.getAction(), "com.kinstalk.her.qchat.enter.homework")) {
                    newHomework = false;
                    PackageUtil.setHomeworkStatus(context,false);
                    stopAnimation(true, false);
                }
                if (TextUtils.equals(intent.getAction(), "com.kinstalk.her.qchat.clear.data")) {
                    newHomework = false;
                    newMessage = false;
                    PackageUtil.setHomeworkStatus(context,false);
                    PackageUtil.setMessageStatus(context,false);
                    stopAnimation(true, true);
                }
//                rightDownImage.setImageResource(R.drawable.right_down);
//                leftDownImage.setImageResource(R.drawable.left_down);
            } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_DATE_CHANGED)) {
                updateTime();
            } else if (TextUtils.equals(intent.getAction(), ACTION_NEW_HOMEWORK)) {
                newHomework = true;
                PackageUtil.setHomeworkStatus(context,true);
//                rightDownImage.setImageResource(R.drawable.right_down_pressed);
                startAnimation(false, true);
            } else if (TextUtils.equals(intent.getAction(), ACTION_NEW_MESSAGE)) {
                newMessage = true;
                PackageUtil.setMessageStatus(context,true);
//                leftDownImage.setImageResource(R.drawable.left_down_pressed);
                startAnimation(true, false);
            } else if (TextUtils.equals(intent.getAction(), ACTION_ALL_STARTS_UPDATE)) {
                all_stars = intent.getIntExtra("all_star", 0);
                Log.i(TAG, "a22417 UpdateLauncherInfoIntentReceiver all_stars = " + all_stars);
                mAllStar.setText(String.valueOf(all_stars));
                PackageUtil.setStars(context, all_stars);
            } else if (TextUtils.equals(intent.getAction(), Constants.ACTION_MEDIA_INFO)) {
                updateMediaInfo(context, intent);
            } else if (TextUtils.equals(intent.getAction(), ACTION_TASK_UPDATE)) {
                int type = intent.getIntExtra("type", -1);
                long time = intent.getLongExtra("time", 0);
                Calendar mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(time);
                int hour = mCalendar.get(Calendar.HOUR);//12小时制
                if (type == REMIND_TYPE_NONE) {
                    remindImage.setVisibility(View.GONE);
                } else {
                    remindImage.setVisibility(View.VISIBLE);
                    setRemindLayout(hour, type2ImageId(type));
                }
            }
        }
    }

    private void updateMediaInfo(Context context, Intent intent) {
        String update = intent.getStringExtra(Constants.REMOTE_VIEW_OPERATION_KEY);
        String type = intent.getStringExtra(Constants.REMOTE_VIEW_TYPE_KEY);

        if (type != null && type.isEmpty()) {
            return;
        }

        if (type.equals("media")) {
            FrameLayout layout = mMediaFrameLayout;
            if (update.equals(Constants.REMOTE_VIEW_OPERATION_ADD)) {
                RemoteViews remoteViews = intent.getParcelableExtra(Constants.REMOTE_VIEW_OBJECT_KEY);
                if (remoteViews != null) {
                    layout.removeAllViews();
                    View view = remoteViews.apply(context, layout);
                    layout.addView(view);
                }
            } else {
                layout.removeAllViews();
            }
        } else if (type.equals("timer")) {
            FrameLayout layout = mTimerFrameLayout;
            if (update.equals(Constants.REMOTE_VIEW_OPERATION_ADD)) {
                RemoteViews remoteViews = intent.getParcelableExtra(Constants.REMOTE_VIEW_OBJECT_KEY);
                if (remoteViews != null) {
                    layout.removeAllViews();
                    View view = remoteViews.apply(context, layout);
                    layout.addView(view);
                }
            } else {
                layout.removeAllViews();
            }
        }
    }

    private void updateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        mLauncherTime.setText(dateFormat.format(new Date()));
        //mTipsTime.setText(TimeCompare());
    }

    public void rightUpButtonClicked(View v) {
        launcherStartActivity("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.skillscenter.SkillsCenterActivity");
        Countly.sharedInstance().recordEvent("Launcher", "t_voice_assistant");
    }


    //语音播报
    public void yuyinClicked(View v) {
        if (TextUtils.isEmpty(mContent)) {
            return;
        }
        newMessage = false;
        PackageUtil.setMessageStatus(this,false);
        Log.i(TAG, "语音播放数据 : " + mContent);
        playContent("想知道这是什么技能，可以对我说，你好小微," + mContent);
        Countly.sharedInstance().recordEvent("Launcher", "t_voice_assistantpop");
    }

    public void leftDownButtonClicked(View v) {
        newMessage = false;
        PackageUtil.setMessageStatus(this,false);
        launcherStartActivity("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.ChatActivity");
        Countly.sharedInstance().recordEvent("Launcher", "t_chatroom");
    }

    public void rightDownButtonClicked(View v) {
        newHomework = false;
        PackageUtil.setHomeworkStatus(this,false);
        launcherStartActivity("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.WorkActivity");
        Countly.sharedInstance().recordEvent("Launcher", "t_homework");
    }

    public void PKButtonClicked(View v) {
        newHomework = false;
        PackageUtil.setHomeworkStatus(this,false);
        launcherStartActivity("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.PKStartActivity");
    }

    public void DialerButtonClicked(View v) {
        newHomework = false;
//        launcherStartActivity("com.kinstalk.her.dialer", "com.kinstalk.her.dialer.DialtactsActivity");

        PackageManager packageManager = getPackageManager();
        String packageName = "com.kinstalk.her.dialer";
        Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(packageName);
        if (launchIntentForPackage != null) {
            startActivity(launchIntentForPackage);
        } else {
            Toast.makeText(this, "手机未安装该应用", Toast.LENGTH_SHORT).show();
        }
    }

    public void leftUpButtonClicked(View view) {
        //launcherStartActivity("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.GiftActivity");
        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setPackage("com.kinstalk.her.qchat");
        i.setClassName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.GiftActivity");
        i.putExtra("all_stars", all_stars);
        Log.i(TAG, "all_stars=" + all_stars);
        try {
            startActivity(i);
            overridePendingTransition(R.anim.fullscreen_enter, R.anim.fullscreen_exit);
        } catch (ActivityNotFoundException e) {
            //catch exception
        }
        Countly.sharedInstance().recordEvent("Launcher", "t_gift_store");
    }

    public void middleButtonClicked(View v) {
        launcherStartActivity("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.RemindActivity");
        Countly.sharedInstance().recordEvent("Launcher", "t_todolist");
    }

    public void launcherStartActivity(String packageName, String className) {
        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setPackage(packageName);
        i.setClassName(packageName, className);
        try {
            startActivity(i);
            overridePendingTransition(R.anim.fullscreen_enter, R.anim.fullscreen_exit);
        } catch (ActivityNotFoundException e) {
            //catch exception
        }
    }

    private void disableAutoHome(Window window) {
        com.kinstalk.util.AppUtils.setAutoActivityTimeout(window, false);
    }

    private int TimeCompare() {
        //格式化时间
        SimpleDateFormat CurrentTime = new SimpleDateFormat("HH");
        Date date = new Date();
        String tipsTime = CurrentTime.format(date);
        if (Integer.parseInt(tipsTime) >= 0 && Integer.parseInt(tipsTime) < 6) {
            //凌晨
            return R.string.time_lingchen;
        } else if (Integer.parseInt(tipsTime) >= 6 && Integer.parseInt(tipsTime) < 12) {
            //上午
            return R.string.time_shangwu;
        } else if (Integer.parseInt(tipsTime) >= 12 && Integer.parseInt(tipsTime) < 18) {
            //下午
            return R.string.time_xiawu;
        } else if (Integer.parseInt(tipsTime) >= 18 && Integer.parseInt(tipsTime) < 24) {
            //晚上
            return R.string.time_wanshang;
        } else {
            return R.string.time_lingchen;
        }
    }

    public void playWorkContent(String content) {
        AIManager.getInstance(this).playTextWithStr(content, null);
//        final String ACTION_TXSDK_PLAY_TTS = "kingstalk.action.wateranimal.playtts";
//        Intent intent = new Intent(ACTION_TXSDK_PLAY_TTS);
//        intent.setPackage("kinstalk.com.wateranimapp");
//        Bundle bundle = new Bundle();
//        bundle.putString("text", content);
//        intent.putExtras(bundle);
//        sendBroadcast(intent);
    }

    public void showGuide() {
        GuidePageDialog.actionStart(this);
    }

    private void playContent(final String content) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                playWorkContent(content);
                try {
                    //Thread.sleep(2000);

                    startRecord();
                    Thread.sleep(5000);
                    stopRecord();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }).start();
    }

    private void initAudioRecord() {
        int frequency = 16000;
        //格式
        int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
    }

    private AudioRecord audioRecord = null;


    public void startRecord() {
        if (audioRecord != null) {
            audioRecord.startRecording();
        }
    }

    private void releaseRecord() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    public void stopRecord() {
        if (audioRecord != null) {
            audioRecord.stop();
        }
    }

    public void setRemindLayout(int hour, int imageId) {
        int imageHeight = 60 / 2;//图片中心点到图片顶部距离
        int imageWidth = 60 / 2;//图片中心到图片左边的距离

        //默认在0点位置
        int left = 400;
        int top = 114;
        remindImage.setImageResource(remindList.get(imageId));
        switch (hour) {
            case 0:
            case 12:
            case 24:
                left = 400;
                top = 131;
                break;
            case 1:
            case 13:
                left = 476;
                top = 151;
                break;
            case 2:
            case 14:
                left = 531;
                top = 207;
                break;
            case 3:
            case 15:
                left = 551;
                top = 282;
                break;
            case 4:
            case 16:
                left = 531;
                top = 358;
                break;
            case 5:
            case 17:
                left = 476;
                top = 413;
                break;
            case 6:
            case 18:
                left = 400;
                top = 433;
                break;
            case 7:
            case 19:
                left = 324;
                top = 413;
                break;
            case 8:
            case 20:
                left = 269;
                top = 358;
                break;
            case 9:
            case 21:
                left = 249;
                top = 282;
                break;
            case 10:
            case 22:
                left = 269;
                top = 207;
                break;
            case 11:
            case 23:
                left = 324;
                top = 151;
                break;
        }
        remindParams.leftMargin = left - imageWidth;
        remindParams.topMargin = top - imageHeight;
        remindImage.setLayoutParams(remindParams);

    }

    public int type2ImageId(int type) {

        if (type == REMIND_TYPE_SERVER) {
            return 0;
        } else if (type == REMIND_TYPE_SLEEP) {
            return 1;
        } else if (type == REMIND_TYPE_WORK) {
            return 2;
        } else if (type == REMIND_TYPE_GETUP) {
            return 3;
        } else if (type == REMIND_TYPE_XIAOWEI) {
            return 4;
        } else if (type == REMIND_TYPE_SELF) {
            return 5;
        } else {
            return 5;
        }
    }

    public void startAnimation(boolean message, boolean homework) {
        if (message) {
            leftDownImage.setBackgroundResource(R.drawable.new_message_animation);
            mMessageAnimationDrawable = (AnimationDrawable) leftDownImage.getBackground();
            mMessageAnimationDrawable.start();
        }

        if (homework) {
            rightDownImage.setBackgroundResource(R.drawable.new_homework_animation);
            mHomeWorkAnimationDrawable = (AnimationDrawable) rightDownImage.getBackground();
            mHomeWorkAnimationDrawable.start();
        }
    }

    public void stopAnimation(boolean stopHomework, boolean stopMessage) {
        if ((mHomeWorkAnimationDrawable != null) && mHomeWorkAnimationDrawable.isRunning() && stopHomework) {
            mHomeWorkAnimationDrawable.selectDrawable(0);
            mHomeWorkAnimationDrawable.stop();
            rightDownImage.setBackgroundResource(R.drawable.right_down_selector);
        }

        if ((mMessageAnimationDrawable != null) && mMessageAnimationDrawable.isRunning() && stopMessage) {
            mMessageAnimationDrawable.selectDrawable(0);
            mMessageAnimationDrawable.stop();
            leftDownImage.setBackgroundResource(R.drawable.left_down_selector);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"touch");
        if (isScreenSaver&&!isOnpause) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG,"touch down");
                    handler.removeCallbacks(homeVedioRunnable);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG,"touch up");
                    handler.postDelayed(homeVedioRunnable, updataVedio);
                    break;
            }
        }

        return super.onTouchEvent(event);

    }

    Runnable homeVedioRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"homeVedioRunnable");
            Intent i = new Intent();
            i.setClass(AllAPPList.this, HomeVedioActivity.class);
            startActivity(i);
        }
    };
}
