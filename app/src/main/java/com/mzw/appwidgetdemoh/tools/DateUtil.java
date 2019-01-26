package com.mzw.appwidgetdemoh.tools;

import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具
 * Created by think on 2018/11/4.
 */

public class DateUtil {
    public static String[] WEEK = {"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};
//    public static String[] WEEK = {"日","一","二","三","四","五","六"};
    public static final int WEEKDAYS = 7;

    /**
     * 月的第一天
     */
    public static Date getMonthStart(Date date) {
        //获取实例
        Calendar calendar = Calendar.getInstance();
        //设置为当前时间
        calendar.setTime(date);
        //当前时间在本月的下标
        int index = calendar.get(Calendar.DAY_OF_MONTH);
        //设置时间为（天，加减）
        calendar.add(Calendar.DATE, (1 - index));
        //返回设置好的时间
        return calendar.getTime();
    }
    // 月的最后一天
    public static Date getMonthEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //设置时间为（月，加减）
        calendar.add(Calendar.MONTH, 1);
        int index = calendar.get(Calendar.DAY_OF_MONTH);
        //设置时间为（天，加减）
        calendar.add(Calendar.DATE, (-index));
        return calendar.getTime();
    }
    //昨天
    public static Date getPrevious(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }
    //明天
    public static Date getNext(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }



    public static String dateToWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayIndex < 1 || dayIndex > WEEKDAYS) {
            return null;
        }

        return WEEK[dayIndex - 1];
    }
}
