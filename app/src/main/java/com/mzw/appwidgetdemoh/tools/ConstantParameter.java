package com.mzw.appwidgetdemoh.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 公共类
 * Created by think on 2019/1/19.
 */

public class ConstantParameter {

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
    public static SimpleDateFormat sdf_a = new SimpleDateFormat("MM月dd日");
    public static SimpleDateFormat sdf_year = new SimpleDateFormat("yyyy");
    public static SimpleDateFormat sdf_month = new SimpleDateFormat("MM");
    public static SimpleDateFormat sdf_day = new SimpleDateFormat("dd");

// ---------------- 广播  -------------------------------------------------------------------------
    //桌面挂件点击事件  日
    public static final String ACTION_ITEM_LAYOUT = "com.mzw.appwidgetdemob.itemLayout";
    //桌面挂件点击事件  上月
    public static final String ACTION_MONTH_PREVIOUS = "com.mzw.appwidgetdemob.month_previous";
    //桌面挂件点击事件  下月
    public static final String ACTION_MONTH_NEXT = "com.mzw.appwidgetdemob.month_next";
    //返回今天
    public static final String ACTION_BACK_TODAY = "com.mzw.appwidgetdemob.back_today";
    //不再发送通知提示
    public static final String ACTION_BIRTHDAY_REMIND = "com.mzw.appwidgetdemob.birthday_remind";
    //挂件 样式
    public static final String ACTION_WIDGET_BACKGROUND = "com.mzw.appwidgetdemob.widget_background";


// ---------------- map key ----------------------------------------------------------------------
    //挂件 样式 背景颜色 透明度  保存的名字
    public static final String WIDGET_BACKGROUND = "widget_background";
    //背景 key
    public static final String WIDGET_BACKGROUND_COLOR = "mBackgroundColor";
    //背景 透明度 key
    public static final String WIDGET_BACKGROUND_A = "widget_background_a";


    //腾讯云 个人用户文件名称
    public static final String USER_INFO_KEY = "mzwUserInfo";


// ---------------- 节日 ----------------------------------------------------------------------
    // 公历部分节假日
    public static HashMap<String, String> festivalSolarMap;
    // 农历部分假日
    public static HashMap<String, String> festivalLunarMap;

    //星座
    public static List<String[]> constellationList;//[1月20，2月18，水瓶座，古怪革新者的星座]
    public static String constellation;

    public static HashMap<String, String> getFestivalSolarMap() {
        festivalSolarMap = new HashMap<String, String>();
        festivalSolarMap.put("01月01日", "元旦");festivalSolarMap.put("05月12日", "护士");
        festivalSolarMap.put("02月14日", "情人");festivalSolarMap.put("06月01日", "儿童");
        festivalSolarMap.put("03月08日", "妇女");festivalSolarMap.put("07月01日", "建党");
        festivalSolarMap.put("03月12日", "植树");festivalSolarMap.put("08月01日", "建军");
        festivalSolarMap.put("03月15日", "打假");festivalSolarMap.put("08月08日", "父亲");
        festivalSolarMap.put("04月01日", "愚人");festivalSolarMap.put("09月01日", "教师");
        festivalSolarMap.put("05月01日", "劳动");festivalSolarMap.put("10月01日", "国庆");
        festivalSolarMap.put("05月04日", "青年");festivalSolarMap.put("12月25日", "圣诞");
        return festivalSolarMap;
    }

    public static HashMap<String, String> getFestivalLunarMap() {
        festivalLunarMap = new HashMap<String, String>();
        festivalLunarMap.put("一月初一", "春节");festivalLunarMap.put("八月十五", "中秋");
        festivalLunarMap.put("一月十五", "元宵");festivalLunarMap.put("九月初九", "重阳");
        festivalLunarMap.put("五月初五", "端午");festivalLunarMap.put("腊月初八", "腊八");
        festivalLunarMap.put("七月初七", "七夕");festivalLunarMap.put("腊月廿四", "小年");
        festivalLunarMap.put("七月十五", "中元");
        //腊月最后一天 为除夕 需要特殊处理  我直接写入 Lunar 类中了
        //festivalLunarMap.put("一月零零", "除夕");
        return festivalLunarMap;
    }
    public static List<String[]> getConstellationList() {
        constellationList = new ArrayList<String[]>();
        constellationList.add(new String[]{"1月20日","2月18日","宝瓶座","创意十足的鬼灵精"});
        constellationList.add(new String[]{"2月19日","3月20日","双鱼座","多愁善感的幻想家"});
        constellationList.add(new String[]{"3月21日","4月19日","白羊座","爱恨分明的冒险家"});
        constellationList.add(new String[]{"4月20日","5月20日","金牛座","固执己见的享受派"});
        constellationList.add(new String[]{"5月21日","6月21日","双子座","机智敏捷的双面人"});
        constellationList.add(new String[]{"6月22日","7月22日","巨蟹座","爱心四溢的收藏家"});
        constellationList.add(new String[]{"7月23日","8月22日","狮子座","霸气外漏的领导者"});
        constellationList.add(new String[]{"8月23日","9月22日","处女座","洞察一切的完美者"});
        constellationList.add(new String[]{"9月23日","10月23日","天枰座","优雅浪漫的正义使"});
        constellationList.add(new String[]{"10月24日","11月22日","天蝎座","精力旺盛的腹黑者"});//四个天蝎可包揽甄嬛传
        constellationList.add(new String[]{"11月23日","12月21日","射手座","天生乐观的自游侠"});//独处林妹妹群居纯爷们
        constellationList.add(new String[]{"12月22日","1月19日","摩羯座","谨慎保守的工作狂"});


        return constellationList;
    }

    //获取登陆名称
    public static String getUserName(Context mContext) {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(USER_INFO_KEY, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("username","");
    }

    //保存登陆信息
    public static void saveUserName(Context mContext,String userName,String password) {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(USER_INFO_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString("username",userName);
        edit.putString("password",password);
        edit.commit();
    }

    //关闭特殊节日提醒
    public static void saveBirthdayRemind(Context mContext,int i) {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(ConstantParameter.ACTION_BIRTHDAY_REMIND, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("birthdayRemind",i);
        edit.commit();
    }
    //特殊节日提醒标识  默认提醒   -1 不提醒  0 提醒
    public static int getBirthdayRemind(Context mContext) {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(ConstantParameter.ACTION_BIRTHDAY_REMIND, Context.MODE_PRIVATE);
        return mSharedPreferences.getInt("birthdayRemind",0);
    }
    /*
    保存 MAP
        map24 --> 24节气
        map3 --> 3伏 3九
        birthday --> 特殊节日（自定义的）
     */
    public static void saveMap(Context mContext,String name, Map<String,Object> map) {
        Gson gson = new Gson();
        String strJson = gson.toJson(map);
//        Log.i("---mzw---","saveMap:"+name+" --> " + strJson);
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(name,strJson);
        edit.commit();
    }
    public static Map getMap(Context mContext,String name) {
        return getMap(mContext,name,new HashMap<String, Object>(),new TypeToken<HashMap<String,Object> >(){}.getType());
    }
    public static Map getMap(Context mContext,String name,Map<String,Object> map,Type typeOfT) {
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);

        String strJson = mSharedPreferences.getString(name,null);
//        Log.i("---mzw---","getMap:"+name+" --> " + strJson);
        if (strJson == null){
            return map;
        }
        Gson gson = new Gson();
        map = gson.fromJson(strJson,typeOfT);
//        map = gson.fromJson(strJson,new TypeToken<Map<String,Object> >(){}.getType());
        return map;
    }

    //传入时间  返回 星座和简介  阳历时间
    public static String getConstellation(Date date,boolean isInfo) {

        constellationList = getConstellationList();
        try {
            for (String[] str:constellationList) {
                //[1月20日，2月18日，水瓶座，古怪革新者的星座]
                Date s_date = sdf.parse(sdf_year.format(date)+"年"+str[0]);
                Date e_date = sdf.parse(sdf_year.format(date)+"年"+str[1]);
                if("摩羯座".equals(str[2])){// 摩羯座区间 跨年 要特别处理
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(e_date);
                    calendar.add(Calendar.YEAR, 1);
                    e_date = calendar.getTime();
                    if(date.getTime() >= sdf.parse(sdf_year.format(date)+"年01月01日").getTime() &&
                        date.getTime() <= sdf.parse(sdf_year.format(date)+"年01月19日").getTime()){
                        calendar.setTime(date);
                        calendar.add(Calendar.YEAR, 1);
                        date = calendar.getTime();
                    }
                }

//                Log.i("---mzw---",sdf.format(date)+" , " + sdf.format(s_date)+" , " + sdf.format(e_date));
                if(date.getTime() >= s_date.getTime() && date.getTime() <= e_date.getTime()){
                    if(isInfo){
                        constellation = str[2] + "    " + str[3];
                    }else{
                        constellation = str[2];
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return constellation;
    }
}
