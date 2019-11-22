package com.mzw.appwidgetdemoh.activity;

import android.app.Activity;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mzw.appwidgetdemoh.CalendarWidget;
import com.mzw.appwidgetdemoh.R;
import com.mzw.appwidgetdemoh.tools.ConstantParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * 样式调整  主要是背景颜色和透明度
 * Created by think on 2019/1/19.
 */
public class SampleActivity  extends Activity {

    private SeekBar seekBar;
    private LinearLayout linearLayout;
    private LinearLayout layout;
    private Context mContext;
    private int mProgress = 66;
    private int sign = 0;// 0桌面挂件打开，1 Main 打开
    private String mBackgroundColor = "#000000";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.sample_activity);
        getWindow().setLayout(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);

        sign = getIntent().getIntExtra("sign",0);

        seekBar = findViewById(R.id.id_seekBar);
        linearLayout = findViewById(R.id.id_linearLayout);
        layout = findViewById(R.id.id_layout);

        if(sign == 1){
            _setBackground();
            layout.setVisibility(View.VISIBLE);
        }else{
            layout.setVisibility(View.INVISIBLE);
        }

        layout.setBackgroundColor(Color.parseColor(mBackgroundColor));
        layout.getBackground().setAlpha(mProgress);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("---mzw---","onProgressChanged=" +progress);
                layout.getBackground().setAlpha(progress);//0~255透明度值
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i("---mzw---","onStartTrackingTouch=");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i("---mzw---","onStopTrackingTouch=" + seekBar.getProgress());
                mProgress = seekBar.getProgress();
                layout.getBackground().setAlpha(mProgress);//0~255透明度值
                notificationCalendarWidget();
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.id_error:
                Log.i("---mzw---","id_error");
                SampleActivity.this.finish();
                break;
            case R.id.id_success:
                Log.i("---mzw---","id_success");
                SampleActivity.this.finish();
                break;
            default:
                mBackgroundColor = ((TextView)view).getText().toString();
                layout.setBackgroundColor(Color.parseColor(mBackgroundColor));

                layout.getBackground().setAlpha(mProgress);
                notificationCalendarWidget();
                break;
        }
    }

    private void notificationCalendarWidget() {
        //保存 背景参数
        Map<String,Object> map = new HashMap<String,Object>();
        map.put(ConstantParameter.WIDGET_BACKGROUND_COLOR,mBackgroundColor);
        map.put(ConstantParameter.WIDGET_BACKGROUND_A,mProgress+"");
        ConstantParameter.saveMap(mContext,ConstantParameter.WIDGET_BACKGROUND,map);

        Log.i("---mzw---","挂件背景调整..." + mBackgroundColor + " , " + mProgress);

        ComponentName componentName = new ComponentName(mContext, CalendarWidget.class);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.calendar_widget);

        int colorInt = Color.parseColor(mBackgroundColor);
        int red = (colorInt & 0xff0000) >> 16;
        int green = (colorInt & 0x00ff00) >> 8;
        int blue = (colorInt & 0x0000ff);
        int _mBackgroundColor = Color.argb(mProgress,red,green,blue);
        remoteViews.setInt(R.id.id_relativeLayout,"setBackgroundColor",_mBackgroundColor);

        //由AppWidgetManager处理Wiget。
        AppWidgetManager awm = AppWidgetManager.getInstance(getApplicationContext());
        awm.updateAppWidget(componentName, remoteViews);

    }

    public void _setBackground(){
        // 获取壁纸管理器
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
        // 获取当前壁纸
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        // 将Drawable,转成Bitmap
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();

        //截取相应屏幕的Bitmap
        Bitmap pbm = Bitmap.createBitmap(bm, 0, 0, ConstantParameter.getScreenWidth(mContext), ConstantParameter.getScreenHeight(mContext));
        //设置 背景
        linearLayout.setBackground(new BitmapDrawable(mContext.getResources(),pbm));
    }
}






























