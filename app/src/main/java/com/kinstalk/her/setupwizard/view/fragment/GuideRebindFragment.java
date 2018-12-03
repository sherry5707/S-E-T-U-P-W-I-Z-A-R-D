package com.kinstalk.her.setupwizard.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kinstalk.her.setupwizard.R;
import com.kinstalk.her.setupwizard.view.activity.HerBootGuideXiaoWeiActivity;
import com.kinstalk.qloveaicore.AIManager;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Zhigang Zhang on 2017/6/23.
 */

public class GuideRebindFragment extends Fragment {
    protected Activity mActivity;
    private Unbinder unbinder;

    public static GuideRebindFragment getInstance() {
        return new GuideRebindFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_guide_rebind, container, false);

        unbinder = ButterKnife.bind(this, rootView);
        playTTSWithContent(" ");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                playTTSWithContent(getString(R.string.unbind_tts_content));
            }
        },600);
        return rootView;
    }

    public void playTTSWithContent(String content) {
        Log.d("GuideRebindFragment","playTTSWithContentt");
        AIManager.getInstance(getContext().getApplicationContext()).playTextWithStr(content,null);
//        final String ACTION_TXSDK_PLAY_TTS = "kingstalk.action.wateranimal.playtts";
//        Intent intent = new Intent(ACTION_TXSDK_PLAY_TTS);
//        Bundle bundle = new Bundle();
//        bundle.putString("text", content);
//        intent.putExtras(bundle);
//
//        getContext().getApplicationContext().sendBroadcast(intent);
    }

    @OnClick({R.id.rebind})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.rebind:
                ((HerBootGuideXiaoWeiActivity)mActivity).doRebind();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }
}
