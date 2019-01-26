package com.mzw.appwidgetdemoh.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mzw.appwidgetdemoh.R;
import com.mzw.appwidgetdemoh.tools.Lunar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by think on 2019/1/20.
 */

public class GridAdapter extends BaseAdapter {
    // 储存格式为  {"农历月日":"备注,农历年月日,阳历年月日"}  如{"腊月十六":"小虎,戊戌狗年腊月十六,2019年01月21日"}
    private Map<String,Object> birthdayMap;
    List<String> keyList;
    private Context mContext;
    private LayoutInflater layoutInflater;

    public GridAdapter(Map<String,Object> birthdayMap, Context mContext, LayoutInflater layoutInflater) {
        this.birthdayMap = birthdayMap;
        this.mContext = mContext;
        this.layoutInflater = layoutInflater;
        this.keyList = new ArrayList<String>();
        for (String key:birthdayMap.keySet()) {
            this.keyList.add(key);
        }
    }

    public void setBirthdayMap(Map<String, Object> birthdayMap) {
        this.birthdayMap = birthdayMap;
        this.keyList = new ArrayList<String>();
        for (String key:birthdayMap.keySet()) {
            this.keyList.add(key);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return birthdayMap.size();
    }

    @Override
    public Object getItem(int position) {
        return keyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String key = keyList.get(position);
        String val = (String)birthdayMap.get(key);

        ViewHolder mViewHolder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.grid_item,parent,false);
            mViewHolder = new ViewHolder();
            mViewHolder.date_view = convertView.findViewById(R.id.date_view);
            mViewHolder.nick_view = convertView.findViewById(R.id.nick_view);
            convertView.setTag(mViewHolder);
        }else{
            mViewHolder = (ViewHolder)convertView.getTag();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Lunar lunar = new Lunar(calendar);

            mViewHolder.date_view.setText("");
            mViewHolder.nick_view.setText("");
        if(!TextUtils.isEmpty(val)){
            String[] value = val.split(",");
            if(value != null && value.length >= 2){
                mViewHolder.nick_view.setText(value[0]);
                String str1 = value[1].substring(value[1].indexOf("年")+1,value[1].length());
                mViewHolder.date_view.setText(str1);

                if(lunar.toString().equals(str1)){
                    mViewHolder.date_view.setTextColor(mContext.getResources().getColor(R.color.today_text));
                    mViewHolder.nick_view.setTextColor(mContext.getResources().getColor(R.color.today_text));
                }else{
                    mViewHolder.date_view.setTextColor(mContext.getResources().getColor(R.color.week_text));
                    mViewHolder.nick_view.setTextColor(mContext.getResources().getColor(R.color.week_text));
                }
            }
        }

        return convertView;
    }

    class ViewHolder{
        TextView date_view;
        TextView nick_view;
    }
}
