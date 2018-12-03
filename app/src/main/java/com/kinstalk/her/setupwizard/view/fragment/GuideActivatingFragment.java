package com.kinstalk.her.setupwizard.view.fragment;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.setupwizard.R;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by zhigang zhang on 2017/10/11.
 */

public class GuideActivatingFragment extends Fragment{
    protected Activity mActivity;
    private Unbinder unbinder;
    private String launchMode;
    private TextView indicateText,indicateText3;

    public static GuideActivatingFragment getInstance() {
        return new GuideActivatingFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = getActivity();
        launchMode = (String)getArguments().get("MODE");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_guide_activating, container, false);
        indicateText = (TextView) rootView.findViewById(R.id.activating_text);
        indicateText3 = (TextView) rootView.findViewById(R.id.activating_text3);
        if (!TextUtils.isEmpty(launchMode) && TextUtils.equals(launchMode,"wechat")) {
            indicateText.setText(R.string.wechat_activing);
            indicateText3.setText(R.string.wechat_activing_prompt3);
        } else {
            indicateText.setText(R.string.xiaowei_activing);
            indicateText3.setText(R.string.xiaowei_activing_prompt3);
        }

        ImageView iv = (ImageView)rootView.findViewById(R.id.activating_animation);
        //把帧动画的资源文件指定为iv的背景
        iv.setBackgroundResource(R.drawable.activating);
        //获取iv的背景
        AnimationDrawable ad = (AnimationDrawable) iv.getBackground();
        ad.start();

        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }
}
