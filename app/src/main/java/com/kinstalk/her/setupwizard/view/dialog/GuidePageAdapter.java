package com.kinstalk.her.setupwizard.view.dialog;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.kinstalk.her.setupwizard.R;

import java.util.List;

public class GuidePageAdapter extends PagerAdapter {

    private int[] imageIdArray;
    private Context mContext;
    private View.OnClickListener mListener;

    public GuidePageAdapter(Context context, int[] imageIdArray, View.OnClickListener listener) {
        mContext = context;
        this.imageIdArray = imageIdArray;
        mListener = listener;
    }

    /**
     * @return 返回页面的个数
     */
    @Override
    public int getCount() {
        if (imageIdArray != null){
            return imageIdArray.length;
        }
        return 0;
    }

    /**
     * 判断对象是否生成界面
     * @param view
     * @param object
     * @return
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * 初始化position位置的界面
     * @param container
     * @param position
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.activity_guide_button, null);
        ImageView button = (ImageView) v.findViewById(R.id.guide_image);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClick(v);
            }
        });

        ImageView imageView = (ImageView) v.findViewById(R.id.guide_image);
        imageView.setBackgroundResource(imageIdArray[position]);

        if (imageIdArray.length > 0 && position == imageIdArray.length - 1) {
            button.setVisibility(View.VISIBLE);
            Log.d("GuidePageAdapter", "instantiateItem: " + position);
        }

        container.addView(v);

        return v;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
