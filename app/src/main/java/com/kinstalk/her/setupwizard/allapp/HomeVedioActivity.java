package com.kinstalk.her.setupwizard.allapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.kinstalk.her.setupwizard.R;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * 屏保视频播放
 */
public class HomeVedioActivity extends Activity implements View.OnClickListener {
    private VideoView videoView;
    private ImageView hometext;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_homevedio);
        videoView = (VideoView)findViewById(R.id.vedioview);
        hometext = (ImageView)findViewById(R.id.home);
        hometext.setOnClickListener(this);
        initData();
    }

    private void initData() {

        videoView.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/raw/girl"));
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
               mp.start();
               mp.setLooping(true);
            }
        });
        /*videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/raw/girl"));
                videoView.start();
            }
        });*/
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.home){
            finish();
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setFlags(FLAG_ACTIVITY_NEW_TASK);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView!=null) {
            videoView.stopPlayback();
            videoView=null;
        }

    }
}
