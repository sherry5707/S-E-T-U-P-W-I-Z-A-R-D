package com.kinstalk.her.setupwizard.view.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kinstalk.her.setupwizard.R;
import com.kinstalk.her.setupwizard.view.activity.HerBootGuideActivity;

import com.kinstalk.util.SystemProperty;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by zhigang zhang on 2017/10/11.
 */

public class GuideActivateFailFragment extends Fragment{
    protected Activity mActivity;
    private Unbinder unbinder;

    @BindView(R.id.activate_fail_tip)
    TextView mFailTipView;
    @BindView(R.id.try_again)
    Button mTryAgainView;

    public static GuideActivateFailFragment getInstance() {
        return new GuideActivateFailFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_guide_activate_fail, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String content = String.format(getContext().getString(R.string.guide_activate_fail_tip), getSerialNumber());
        mFailTipView.setText(content);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }

    @OnClick({R.id.try_again, R.id.back_button})
    public void onClick(View view) {
        if (view.getId() == R.id.try_again) {
            Activity parentActivity = getActivity();
            if(parentActivity != null) {
                ((HerBootGuideActivity)parentActivity).tryBindAgain();
            }
        } else if (view.getId() == R.id.back_button) {
            ((HerBootGuideActivity)getActivity()).toGuideWifiFragment();
        }
    }

    private String getSerialNumber() {
        String serial = null;
        try {
            serial = SystemProperty.get("ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }
}
