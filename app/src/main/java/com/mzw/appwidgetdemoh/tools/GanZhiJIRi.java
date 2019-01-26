package com.mzw.appwidgetdemoh.tools;

import java.util.Calendar;

/**
 *
 * 干支纪日
 *
 *

 从已知日期计算干支纪日的公式为：

 G=4C+[C/4]+5y+[y/4]+[3*(M+1)/5]+d-3

 Z=8C+[C/4]+5y+[y/4]+[3*(M+1)/5]+d+7+i

 其中
 C是世纪数减一，y是年份后两位，M是月份，d是日数。
 1月和2月按上一年的13月和14月来算。
 奇数月i=0，偶数月i=6。

 G除以10的余数是天干，Z除以12的余数是地支。

 计算时带[]的数表示取整。

 例如：查2006年4月1日的干支日。将数值代入计算公式。

 G=4*20+[20/4]+5*06+[06/4]+[3*(4+1)/5]+1-3=117
 Z=8*20+[20/4]+5*06+[06/4]+[3*(4+1)/5]+1+7+6=213

 Gan[G%10] + " , " + Zhi[Z%12] 便是干支 日

 * Created by think on 2019/1/17.
 */

public class GanZhiJIRi {

    final static String[] Gan = new String[]{"癸", "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬"};
    final static String[] Zhi = new String[]{"亥", "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌"};

    int year,month,day;
    int C,y,M,d,i;

    public GanZhiJIRi(Calendar cal) {
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) +1;
        day = cal.get(Calendar.DATE);
//        Log.i("---mzw---",year+" "+month+" "+day);
        C = year / 100;
        y = year % 100;
        M = month;
        d = day;
        i = 0;

        if(month == 1){
            month = 13;
            year = year - 1;
        }
        if(month == 2){
            month = 14;
            year = year - 1;
        }
        if(M % 2 == 0){
            i = 6;
        }
    }

    //返回 日 的干支
    public String toString(){
        int G = 4*C + ((int)(C/4)) + 5*y + ((int)(y/4)) + ((int)(3*(M+1)/5)) + d-3;
        int Z = 8*C + ((int)(C/4)) + 5*y + ((int)(y/4)) + ((int)(3*(M+1)/5)) + d+7+i;
        return Gan[G%10] + "" + Zhi[Z%12];
    }

}
