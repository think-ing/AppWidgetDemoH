package com.mzw.appwidgetdemoh;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.gson.reflect.TypeToken;
import com.mzw.appwidgetdemoh.activity.SampleActivity;
import com.mzw.appwidgetdemoh.bean.DateBean;
import com.mzw.appwidgetdemoh.tools.ConstantParameter;
import com.mzw.appwidgetdemoh.tools.DateUtil;
import com.mzw.appwidgetdemoh.tools.GanZhiJIRi;
import com.mzw.appwidgetdemoh.tools.Lunar;
import com.mzw.appwidgetdemoh.tools.SolarTerms24;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.app.Notification.PRIORITY_DEFAULT;
import static android.app.Notification.VISIBILITY_SECRET;

/**
 * 日历挂件
 * Implementation of App Widget functionality.
 */
public class CalendarWidget extends AppWidgetProvider {

    //显示所有天  集合
    private List<DateBean> mDateList;
    //记录接收的时间   为当天时间（虚拟，如点击下一月时   当天时间为下一月今天）
    private Date receive_date;
    //24节气
    private Map<String ,Object> map24 = null;
    //三伏三九
    private Map<String ,Object> map3 = null;
    // 储存格式为  {"农历月日":"备注,农历年月日,阳历年月日"}  如{"腊月十六":"小虎,戊戌狗年腊月十六,2019年01月21日"}
    private Map<String,Object> birthdayMap = new TreeMap<String,Object>();//生日，纪念日等 特殊节日

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("---mzw---","============onUpdate===========");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Lunar lunar = new Lunar(calendar);
        String str = (String)birthdayMap.get(lunar.getMonthStr()+""+lunar.getDayStr());
        if(!TextUtils.isEmpty(str)){
            String content = str.split(",")[0];
            //今天是生日
            Log.i("---mzw---","今天是" + content + "的生日");
//            sendNotification(context,"今天是重要的日子：" + str);
            if(ConstantParameter.getBirthdayRemind(context) == 0){//如果人为关闭提醒 则不在提醒
                sendNotification(context,lunar.toString(),content);
            }else{
                //今天不是特殊节日则打开提醒
                ConstantParameter.saveBirthdayRemind(context,0);
            }
        }
        //获取数据
        getData(new Date());
        for (int appWidgetId : appWidgetIds) {
            //填充布局
            drawWidget(context, appWidgetId);
        }
    }
    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        getData(new Date());
        drawWidget(context, appWidgetId);
    }
    @Override
    public void onEnabled(Context context) {
        Log.i("---mzw---","============onEnabled===========");
        // 设置 背景参数
        Intent mIntent = new Intent(context,SampleActivity.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mIntent);
    }

    @Override
    public void onDisabled(Context context) {
        // 重置背景参数
        Map<String,Object> map = new HashMap<String,Object>();
        map.put(ConstantParameter.WIDGET_BACKGROUND_COLOR,"#000000");
        map.put(ConstantParameter.WIDGET_BACKGROUND_A,"66");
        ConstantParameter.saveMap(context,ConstantParameter.WIDGET_BACKGROUND,map);
    }

    private void redrawWidgets(Context context) {
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(context, CalendarWidget.class));
        for (int appWidgetId : appWidgetIds) {
            drawWidget(context, appWidgetId);
        }
    }

    //生成数据
    private void getData(Date d) {
        Log.i("---mzw---","getData: "+ ConstantParameter.sdf.format(d));

        mDateList = new ArrayList<DateBean>();
        receive_date = d;
        if(receive_date == null){
            receive_date = new Date();
        }
        //传入时间  获取 月的第一天
        Date date = DateUtil.getMonthStart(receive_date);
        //传入时间  获取 月的最后一天
        Date monthEnd = DateUtil.getMonthEnd(receive_date);
        while (!date.after(monthEnd)) {
            Calendar calendar= Calendar.getInstance();
            calendar.setTime(date);
            Lunar lunar = new Lunar(calendar);
            mDateList.add(new DateBean(date,ConstantParameter.sdf_year.format(date),ConstantParameter.sdf_month.format(date),ConstantParameter.sdf_day.format(date),lunar.toString(),lunar.getMonthStr()+""+lunar.getDayStr()));
            date = DateUtil.getNext(date);
        }

        //如果一号不是周一，取前几天到周一
        while(!"星期一".equals(DateUtil.dateToWeek(mDateList.get(0).date))){
            Date date1 = DateUtil.getPrevious(mDateList.get(0).date);
            Calendar calendar= Calendar.getInstance();
            calendar.setTime(date1);
            Lunar lunar = new Lunar(calendar);
            mDateList.add(0,new DateBean(date1, ConstantParameter.sdf_year.format(date1),ConstantParameter.sdf_month.format(date1),ConstantParameter.sdf_day.format(date1),lunar.toString(),lunar.getMonthStr()+""+lunar.getDayStr()));
        }
        //如果月末不是周日，取后几天到周日
        while(!"星期日".equals(DateUtil.dateToWeek(mDateList.get(mDateList.size()-1).date))){
            Date date2 = DateUtil.getNext(mDateList.get(mDateList.size()-1).date);
            Calendar calendar= Calendar.getInstance();
            calendar.setTime(date2);
            Lunar lunar = new Lunar(calendar);
            mDateList.add(new DateBean(date2,ConstantParameter.sdf_year.format(date2),ConstantParameter.sdf_month.format(date2),ConstantParameter.sdf_day.format(date2),lunar.toString(),lunar.getMonthStr()+""+lunar.getDayStr()));
        }

        while(mDateList != null && mDateList.size() < 42){
            Date date2 = DateUtil.getNext(mDateList.get(mDateList.size()-1).date);
            Calendar calendar= Calendar.getInstance();
            calendar.setTime(date2);
            Lunar lunar = new Lunar(calendar);
            mDateList.add(new DateBean(date2,ConstantParameter.sdf_year.format(date2),ConstantParameter.sdf_month.format(date2),ConstantParameter.sdf_day.format(date2),lunar.toString(),lunar.getMonthStr()+""+lunar.getDayStr()));
        }
    }
    //填充布局
    private void drawWidget(Context context, int appWidgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews mRemoteView = new RemoteViews(context.getPackageName(), R.layout.calendar_widget);

        // 清空
        mRemoteView.removeAllViews(R.id.id_calendar);
        RemoteViews mRowView = null;
        RemoteViews mDayView = null;
        String _temp = "";

        //背景
        Map<String,Object> map = ConstantParameter.getMap(context,ConstantParameter.WIDGET_BACKGROUND);
        String mBackgroundColor = "#000000";
        int mProgress = 66;
        if(map != null && map.size() > 0){
            mBackgroundColor = (String)map.get(ConstantParameter.WIDGET_BACKGROUND_COLOR);
            mProgress = Integer.parseInt(map.get(ConstantParameter.WIDGET_BACKGROUND_A).toString());
        }

        int colorInt = Color.parseColor(mBackgroundColor);
        int red = (colorInt & 0xff0000) >> 16;
        int green = (colorInt & 0x00ff00) >> 8;
        int blue = (colorInt & 0x0000ff);
        int _mBackgroundColor = Color.argb(mProgress,red,green,blue);
        mRemoteView.setInt(R.id.id_relativeLayout,"setBackgroundColor",_mBackgroundColor);

        //加载数据 显示
        for(int i =0; i < mDateList.size(); i++){
            DateBean mDateBean = mDateList.get(i);

            // 周
            if (mRowView == null || i % 7 == 0){
                mRowView = new RemoteViews(context.getPackageName(), R.layout.calendar_row);
            }
            mDayView = new RemoteViews(context.getPackageName(), R.layout.calendar_day);
            //获取当前时间 用来区分当天，当月，其他月
            //当月
            if(ConstantParameter.sdf_year.format(receive_date).equals(mDateBean.yangli_year) &&
                    ConstantParameter.sdf_month.format(receive_date).equals(mDateBean.yangli_month)){
                mDayView.setTextColor(R.id.id_yangli_text, context.getResources().getColor(R.color.this_month_text));
                mDayView.setTextColor(R.id.id_yinli_text, context.getResources().getColor(R.color.this_month_text));
            }else{
                //其他
                mDayView.setTextColor(R.id.id_yangli_text, context.getResources().getColor(R.color.other_month_text));
                mDayView.setTextColor(R.id.id_yinli_text, context.getResources().getColor(R.color.other_month_text));
            }

            //将农历拆分为 月 和 日
            String yinli_month = mDateBean.yinlia.substring(0,2);
            String yinli_day = mDateBean.yinlia.substring(2,4);
            if(mDateBean.yinlia.startsWith("闰")){
                yinli_month = mDateBean.yinlia.substring(0,3);
                yinli_day = mDateBean.yinlia.substring(3,5);
            }

            String _key = ConstantParameter.sdf.format(mDateBean.date);//使用阳历为 key
            //如果是初一 则显示月分
            if("初一".equals(yinli_day)){
                mDayView.setTextViewText(R.id.id_yinli_text, yinli_month);
                mDayView.setTextColor(R.id.id_yinli_text, context.getResources().getColor(R.color.yinli_month_text));
            }else{
                mDayView.setTextViewText(R.id.id_yinli_text, yinli_day);
            }

            mDayView.setTextViewText(R.id.id_yangli_text,mDateBean.yangli_day);

            //特殊节日  > 农历节日 > 阳历节日 > 24节气 > 三伏三九
            if("除夕".equals(yinli_day)){
                mDayView.setTextColor(R.id.id_yinli_text, context.getResources().getColor(R.color.yinli_month_text));
            }else{
                // 三伏  三九
                if(map3 != null ){
                    if(!TextUtils.isEmpty((String)map3.get(_key)) && ((String)map3.get(_key)).indexOf("天") == -1){
                        mDayView.setTextViewText(R.id.id_yinli_text, (String)map3.get(_key));
                        mDayView.setTextColor(R.id.id_yinli_text, context.getResources().getColor(R.color.yinli_month_text));
                    }
                }

                //24节气
                if(map24 != null ){
                    if(!TextUtils.isEmpty((String)map24.get(_key))){
                        mDayView.setTextViewText(R.id.id_yinli_text, (String)map24.get(_key));
                        mDayView.setTextColor(R.id.id_yinli_text, context.getResources().getColor(R.color.yinli_month_text));
                    }
                }

                //阳历节日
                String festivalSolar = ConstantParameter.getFestivalSolarMap().get(ConstantParameter.sdf_a.format(mDateBean.date));
                if(!TextUtils.isEmpty(festivalSolar)){
                    mDayView.setTextViewText(R.id.id_yinli_text, festivalSolar);
                    mDayView.setTextColor(R.id.id_yinli_text, context.getResources().getColor(R.color.yinli_month_text));
                }

                //农历节日
                String festivalLunar = ConstantParameter.getFestivalLunarMap().get(mDateBean.yinlia);
                if(!TextUtils.isEmpty(festivalLunar)){
                    mDayView.setTextViewText(R.id.id_yinli_text, festivalLunar);
                    mDayView.setTextColor(R.id.id_yinli_text, context.getResources().getColor(R.color.yinli_month_text));
                }
            }

            //特殊节日
            String birthday = (String)birthdayMap.get(mDateBean.yinlib);
            if(!TextUtils.isEmpty(birthday) && birthday.indexOf(",") > 0){
                mDayView.setTextViewText(R.id.id_yinli_text, birthday.split(",")[0]);
                mDayView.setTextColor(R.id.id_yinli_text, context.getResources().getColor(R.color.yinli_month_text));
            }

            // 突出 当天
            if(ConstantParameter.sdf_year.format(new Date()).equals(mDateBean.yangli_year) &&
                    ConstantParameter.sdf_month.format(new Date()).equals(mDateBean.yangli_month) &&
                    ConstantParameter.sdf_day.format(new Date()).equals(mDateBean.yangli_day)){
                mRemoteView.setViewVisibility(R.id.id_today_background, View.VISIBLE);
                mDayView.setImageViewResource(R.id.id_today_background,R.drawable.btn_b);
            }else{
                mRemoteView.setViewVisibility(R.id.id_today_background,View.GONE);
                mDayView.setInt(R.id.id_itemLayout, "setBackgroundResource", R.drawable.btn_a);
            }

            //监听每天点击事件
            mDayView.setOnClickPendingIntent(R.id.id_itemLayout,
                    PendingIntent.getBroadcast(context, 0,
                            new Intent(context, CalendarWidget.class)
                                    .setAction(ConstantParameter.ACTION_ITEM_LAYOUT+"_"+i)
                                    .putExtra("receive_date",mDateBean.date.getTime()),
                            PendingIntent.FLAG_UPDATE_CURRENT));

            mRowView.addView(R.id.id_calendar_row,mDayView);
            if (mRowView != null && i % 7 == 6){
                mRemoteView.addView(R.id.id_calendar,mRowView);
            }
        }

        //监听点击事件
        //上月
        mRemoteView.setOnClickPendingIntent(R.id.id_month_previous,
                PendingIntent.getBroadcast(context, 0,
                        new Intent(context, CalendarWidget.class)
                                .setAction(ConstantParameter.ACTION_MONTH_PREVIOUS)
                                .putExtra("receive_date",receive_date.getTime()),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        //下月
        mRemoteView.setOnClickPendingIntent(R.id.id_month_next,
                PendingIntent.getBroadcast(context, 0,
                        new Intent(context, CalendarWidget.class)
                                .setAction(ConstantParameter.ACTION_MONTH_NEXT)
                                .putExtra("receive_date",receive_date.getTime()),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        //返回今天
        mRemoteView.setOnClickPendingIntent(R.id.id_today_view_a,
                PendingIntent.getBroadcast(context, 0,
                        new Intent(context, CalendarWidget.class)
                                .setAction(ConstantParameter.ACTION_BACK_TODAY)
                                .putExtra("receive_date",new Date().getTime()),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        // 当前时间 显示
        Calendar calendar= Calendar.getInstance();
        calendar.setTime(receive_date);
        Lunar lunar = new Lunar(calendar);

        mRemoteView.setTextViewText(R.id.id_today_view_a,ConstantParameter.sdf.format(receive_date));
        mRemoteView.setTextViewText(R.id.id_today_view_b,lunar.cyclical() + " " + lunar.animalsYear() + "年 " + lunar.toString());

        // 当前时间 详情显示
        //农历节日
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTime(receive_date);
        Lunar lunar1 = new Lunar(mCalendar);
        String lunar1Str = lunar1.toString();
        if(lunar1Str.startsWith("闰")){
            lunar1Str = lunar1Str.substring(1,lunar1Str.length());
        }
        String festivalLunar = ConstantParameter.getFestivalLunarMap().get(lunar1Str);
        if(lunar1Str.indexOf("除夕") > 0){
            _temp = "除夕";
        }else if(!TextUtils.isEmpty(festivalLunar)){
            _temp = festivalLunar;
        }
        //特殊节日
        String birthday = (String)birthdayMap.get(lunar1.getMonthStr()+""+lunar1.getDayStr());
        if(!TextUtils.isEmpty(birthday) && birthday.indexOf(",") > 0){
            _temp = birthday.split(",")[0];
        }
        //阳历节日
        String festivalSolar = ConstantParameter.getFestivalSolarMap().get(ConstantParameter.sdf_a.format(receive_date));
        if(!TextUtils.isEmpty(festivalSolar)){
            if(TextUtils.isEmpty(_temp)){
                _temp = festivalSolar;
            }else{
                _temp += "●"+festivalSolar;
            }
        }

        //24节气
        if(map24 != null ){
            if(!TextUtils.isEmpty((String)map24.get(ConstantParameter.sdf.format(receive_date)))){
                if(TextUtils.isEmpty(_temp)){
                    _temp = (String)map24.get(ConstantParameter.sdf.format(receive_date));
                }else{
                    _temp += "●"+(String)map24.get(ConstantParameter.sdf.format(receive_date));
                }
            }
        }

//        -------------------------------------
        if(!TextUtils.isEmpty(_temp)){
            mRemoteView.setViewVisibility(R.id.id_textView_24,View.VISIBLE);
            mRemoteView.setTextViewText(R.id.id_textView_24,_temp);
        }else{
            mRemoteView.setViewVisibility(R.id.id_textView_24,View.GONE);
            mRemoteView.setTextViewText(R.id.id_textView_24,"");
        }

        _temp = (String)map3.get(ConstantParameter.sdf.format(receive_date));
        if(map3 != null && !TextUtils.isEmpty(_temp)){
            mRemoteView.setViewVisibility(R.id.id_textView_3,View.VISIBLE);
            if(_temp.indexOf("天") > 0){
                mRemoteView.setTextViewText(R.id.id_textView_3,_temp);
            }else{
                mRemoteView.setTextViewText(R.id.id_textView_3,_temp+" 第1天");
            }
        }else{
            mRemoteView.setViewVisibility(R.id.id_textView_3,View.GONE);
            mRemoteView.setTextViewText(R.id.id_textView_3,"");
        }


        //刷新页面
        appWidgetManager.updateAppWidget(appWidgetId, mRemoteView);
    }




    @Override
    public void onReceive(Context context, Intent intent) {
        //接收广播处理
        String action = intent.getAction();
        Log.i("---mzw---", "action1 : " + action);
        Date _receive_date = new Date(intent.getLongExtra("receive_date", 0));
        Log.i("---mzw---", "_receive_date : " + ConstantParameter.sdf.format(_receive_date));
        Date mDate = new Date();

        if(ConstantParameter.ACTION_BIRTHDAY_REMIND.equals(action)){
            //关闭通知
            NotificationManager notiManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
            int notifyId = intent.getIntExtra("notifyId",0);
            if(notifyId != 0){
                notiManager.cancel(notifyId);
            }
            //不再提醒
            ConstantParameter.saveBirthdayRemind(context,-1);
        }else if(ConstantParameter.ACTION_BACK_TODAY.equals(action)){
            //回到今天
            mDate = new Date();
        }else if(ConstantParameter.ACTION_MONTH_PREVIOUS.equals(action)){
            //上一月
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(_receive_date);
            calendar.add(Calendar.MONTH, -1);
            mDate = calendar.getTime();
        }else if(ConstantParameter.ACTION_MONTH_NEXT.equals(action)){
            //下一月
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(_receive_date);
            calendar.add(Calendar.MONTH, 1);
            mDate = calendar.getTime();
        }else{
            //点击 天
            mDate = _receive_date;
        }

        Type typeOfT = new TypeToken<TreeMap<String,Object>>(){}.getType();
        birthdayMap = ConstantParameter.getMap(context,"birthday",new TreeMap<String,Object>(),typeOfT);
        map24 = ConstantParameter.getMap(context,"map24",new TreeMap<String,Object>(),typeOfT);
        map3 = ConstantParameter.getMap(context,"map3",new TreeMap<String,Object>(),typeOfT);
        if(map3 == null || map3.size() <= 0 || map24 == null || map24.size() <= 0 || !map24.get("A").equals(ConstantParameter.sdf.format(mDate))){
            try {
                //  获取24节气 和 3伏 3九
                solarTerms(context,mDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        getData(mDate);
        redrawWidgets(context);
        super.onReceive(context, intent);
    }

    private void solarTerms(Context mContext,Date date) throws ParseException {
        //没有彻底加载成功时会调用此方法， 默认时间为1970年，我这设置 为的是没有加载完不执行
        if("1970".equals(ConstantParameter.sdf_day.format(date))){
            date = new Date();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if(map24 == null){
            map24 = new LinkedHashMap<String,Object>();
        }else{
            map24.clear();
        }
        //第一步  获取24节气 集合   今年和去年   避免在年末时 计算不出 三九
        map24.putAll(SolarTerms24.solarTermToMap(calendar.get(Calendar.YEAR) - 1));//去年
        map24.putAll(SolarTerms24.solarTermToMap(calendar.get(Calendar.YEAR)));//今年


        int i = 0;
        boolean b = true;

        for (String key:map24.keySet()) {
            /*
            第二步  获取三伏 集合
                初伏 从夏至那天 开始 循环找到第三个庚日
                中伏 从夏至那天 开始 循环找到第四个庚日
                末伏 从立秋那天 开始 循环找到第一个庚日
            */
            if("夏至".equals(map24.get(key))){
                String xiaZhi = key;
                Date xiaZhiDate = ConstantParameter.sdf.parse(xiaZhi);
                calendar.setTime(xiaZhiDate);
                b = true;
                i = 0;
                do{
                    GanZhiJIRi ganZhi = new GanZhiJIRi(calendar);
                    if(ganZhi.toString().startsWith("庚")){
                        i += 1;
                        if(i == 3){
                            map3.put(ConstantParameter.sdf.format(calendar.getTime()),"初伏");
                        }
                        if(i == 4){
                            map3.put(ConstantParameter.sdf.format(calendar.getTime()),"中伏");
                            b = false;
                        }
                    }
                    calendar.add(Calendar.DATE, 1);
                }while (b);

            }
            if("立秋".equals(map24.get(key))) {
                String liQiu = key;
                Date liQiuDate  = ConstantParameter.sdf.parse(liQiu);
                calendar.setTime(liQiuDate);
                b = true;
                do{
                    GanZhiJIRi ganZhi = new GanZhiJIRi(calendar);
                    if(ganZhi.toString().startsWith("庚")){
                        map3.put(ConstantParameter.sdf.format(calendar.getTime()),"末伏");
                        b = false;
                    }
                    calendar.add(Calendar.DATE, 1);
                }while (b);
            }

            /*
            第三步  获取三九 集合
                从冬至那天 开始 循环  冬至为 一九   每9天加一九 （如 冬至后第十天是二九，第十八天是三九 ... 一直到九九）
                一九二九不出手，三九四九冰上走，五九六九沿河看柳，七九河开，八九雁来，九九归一九，犁牛遍地走。
             */
            if("冬至".equals(map24.get(key))) {
                String dongZhi = key;
                Date dongZhiDate = ConstantParameter.sdf.parse(dongZhi);
                calendar.setTime(dongZhiDate);

                String sanJiu[] = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
                i = 0;
                String str = "";
                while (i <= 80){
                    if(i % 9 == 0){
                        str = sanJiu[i / 9]+"九";
                        map3.put(ConstantParameter.sdf.format(calendar.getTime()),str);
                    }else{
                        map3.put(ConstantParameter.sdf.format(calendar.getTime()),str + " 第"+(i%9 + 1)+"天");
                    }
                    calendar.add(Calendar.DATE, 1);
                    i += 1;
                }
            }
        }

        ConstantParameter.saveMap(mContext,"map24",map24);
        ConstantParameter.saveMap(mContext,"map3",map3);
    }


    //重要日子 发送通知
    private void sendNotification(Context mContext,String title,String content) {
        Log.i("---mzw---","title: " + title + " , content: " + content);
//        int notifyId = Integer.parseInt(ConstantParameter.sdf_hhmm.format(new Date()));
        int notifyId = 123456;

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_layout);
        remoteViews.setTextViewText(R.id.id_title,"【"+title+"】");
        content = "今天是你备注【"+content+"】的特殊日子，不要忘记哦！！";
        remoteViews.setTextViewText(R.id.id_content,content);
        //返回今天
        remoteViews.setOnClickPendingIntent(R.id.id_btn,
                PendingIntent.getBroadcast(mContext, 0,
                        new Intent(mContext, CalendarWidget.class)
                                .setAction(ConstantParameter.ACTION_BIRTHDAY_REMIND)
                                .putExtra("notifyId",notifyId)
                                .putExtra("receive_date",new Date().getTime()),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        // 获取通知服务对象NotificationManager
        NotificationManager notiManager = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /**
             * 紧急级别（发出通知声音并显示为提示通知）
             IMPORTANCE_HIGH
             PRIORITY_HIGH或者PRIORITY_MAX

             高级别（发出通知声音并且通知栏有通知）
             IMPORTANCE_DEFAULT
             PRIORITY_DEFAULT

             中等级别（没有通知声音但通知栏有通知）
             IMPORTANCE_LOW
             PRIORITY_LOW

             低级别（没有通知声音也不会出现在状态栏上）
             IMPORTANCE_MIN
             PRIORITY_MIN
             */
            NotificationChannel channel = new NotificationChannel("channel_id", "权限设置",
                    NotificationManager.IMPORTANCE_DEFAULT);//高级别（发出通知声音并且通知栏有通知）
            channel.canBypassDnd();//是否绕过请勿打扰模式
            channel.enableLights(true);//闪光灯
            channel.setLockscreenVisibility(VISIBILITY_SECRET);//锁屏显示通知
            channel.setLightColor(Color.RED);//闪关灯的灯光颜色
            channel.canShowBadge();//桌面launcher的消息角标
            channel.enableVibration(true);//是否允许震动
//            channel.getAudioAttributes();//获取系统通知响铃声音的配置
            channel.setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" +R.raw.aa), Notification.AUDIO_ATTRIBUTES_DEFAULT);
            channel.getGroup();//获取通知取到组
            channel.setBypassDnd(true);//设置可绕过  请勿打扰模式
            channel.setVibrationPattern(new long[]{100, 100, 200});//设置震动模式
            channel.shouldShowLights();//是否会有灯光

            notiManager.createNotificationChannel(channel);
        }

        // 创建Notification对象
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(mContext, "channel_id");
        } else {
            builder = new NotificationCompat.Builder(mContext);
            builder.setPriority(PRIORITY_DEFAULT);
            builder.setDefaults(Notification.DEFAULT_SOUND);    // 设置声音/震动等
        }

        builder.setContent(remoteViews);
        builder.setTicker("birthday");            // 通知弹出时状态栏的提示文本
        builder.setContentInfo("别忘记哦！！！");  //
        builder.setContentTitle("重大通知")    // 通知标题
                .setContentText(content)  // 通知内容
                .setSmallIcon(R.mipmap.f000);    // 通知小图标
        builder.setAutoCancel(true);
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        notiManager.notify(notifyId, notification);
    }


}

