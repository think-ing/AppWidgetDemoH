package com.mzw.appwidgetdemoh.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by think on 2019/1/19.
 */

public class DateBean implements Serializable {
    /**
     * 阳历
     */
    public Date date;
    public String yangli_year;
    public String yangli_month;
    public String yangli_day;
    /**
     * 阴历 农历
     */
    public String yinlia;//五月初一
    public String yinlib;//0501  // key使用

    public DateBean(Date date, String yangli_year, String yangli_month, String yangli_day, String yinlia,String yinlib) {
        this.yangli_year = yangli_year;
        this.yangli_month = yangli_month;
        this.yangli_day = yangli_day;
        this.yinlib = yinlib;
        this.yinlia = yinlia;
        this.date = date;
    }

}
