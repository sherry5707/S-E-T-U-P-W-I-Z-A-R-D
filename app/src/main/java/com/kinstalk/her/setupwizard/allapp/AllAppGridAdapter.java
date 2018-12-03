package com.kinstalk.her.setupwizard.allapp;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.setupwizard.R;

import java.util.List;

public class AllAppGridAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<PakageMod> datas;
    private OnListClickListener mOnListClickListener;

    public AllAppGridAdapter(Context context, List<PakageMod> datas) {
        super();

        inflater = LayoutInflater.from(context);
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            // 使用View的对象itemView与R.layout.item关联
            convertView = inflater.inflate(R.layout.all_app_list_item, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.apps_image);
//            holder.label = (TextView) convertView
//                    .findViewById(R.id.apps_textview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.icon.setImageDrawable(datas.get(position).icon);
//        holder.label.setText(datas.get(position).appName);

        convertView.findViewById(R.id.select_layout).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.findViewById(R.id.apps_select_image).setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.findViewById(R.id.apps_select_image).setVisibility(View.GONE);
                        if (null != mOnListClickListener) {
                            mOnListClickListener.onItemClick(datas.get(position), position);
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                        v.findViewById(R.id.apps_select_image).setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });

        return convertView;
    }

    public void setOnListClickListener(OnListClickListener mOnListClickListener) {
        this.mOnListClickListener = mOnListClickListener;
    }

    public interface OnListClickListener {
        boolean onItemClick(PakageMod pakageMod, int position);
    }

    class ViewHolder {
        private ImageView icon;
        private TextView label;
    }
}
