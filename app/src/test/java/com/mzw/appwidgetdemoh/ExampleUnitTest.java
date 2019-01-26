package com.mzw.appwidgetdemoh;

import android.util.Log;

import org.junit.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test_a() throws Exception {
        Map tree = new TreeMap();
        Map linked = new LinkedHashMap();
        Map hash = new HashMap();
        System.out.println("tree :"+buildMap(tree));
        System.out.println("link :"+buildMap(linked));
        System.out.println("hash :"+buildMap(hash));
    }
    private static Map buildMap(Map map){
        map.put("0110", "kfc");
        map.put("0209", "kfc");
        map.put("0309", "kfc");
        map.put("0809", "kfc");
        map.put("0508", "kfc");
        map.put("0109", "kfc");
        map.put("0610", "kfc");
        map.put("0419", "kfc");
        return map;
    }


//    class myCustomer implements Comparator<String> {
//
//        @Override
//        public int compare(String o1, String o2) {
//            System.out.println(o1 + " --  " + o2);
//            return 0;
//        }
//    }
    @Test
    public void addition_isCorrect() throws Exception {
        Map<String, String> map = new TreeMap<String, String>();

        map.put("0110", "kfc");
        map.put("0209", "kfc");
        map.put("0309", "kfc");
        map.put("0809", "kfc");
        map.put("0508", "kfc");
        map.put("0109", "kfc");
        map.put("0610", "kfc");
        map.put("0419", "kfc");


        Map<String, String> resultMap = sortMapByKey(map);    //按Key进行排序

        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }
    /**
     * 使用 Map按key进行排序
     * @param map
     * @return
     */
    public static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, String> sortMap = new TreeMap<String, String>(new MapKeyComparator());

        sortMap.putAll(map);

        return sortMap;
    }
    static class MapKeyComparator implements Comparator<String>{

        @Override
        public int compare(String o1, String o2) {
//            System.out.println(str1 + " --  " + str2);
            if(o1.replace("闰","").startsWith("一")){o1 = "01";}
            if(o1.replace("闰","").startsWith("二")){o1 = "02";}
            if(o1.replace("闰","").startsWith("三")){o1 = "03";}
            if(o1.replace("闰","").startsWith("四")){o1 = "04";}
            if(o1.replace("闰","").startsWith("五")){o1 = "05";}
            if(o1.replace("闰","").startsWith("六")){o1 = "06";}
            if(o1.replace("闰","").startsWith("七")){o1 = "07";}
            if(o1.replace("闰","").startsWith("八")){o1 = "08";}
            if(o1.replace("闰","").startsWith("九")){o1 = "09";}
            if(o1.replace("闰","").startsWith("十")){o1 = "10";}
            if(o1.replace("闰","").startsWith("冬")){o1 = "11";}
            if(o1.replace("闰","").startsWith("腊")){o1 = "12";}

            if(o2.replace("闰","").startsWith("一")){o2 = "01";}
            if(o2.replace("闰","").startsWith("二")){o2 = "02";}
            if(o2.replace("闰","").startsWith("三")){o2 = "03";}
            if(o2.replace("闰","").startsWith("四")){o2 = "04";}
            if(o2.replace("闰","").startsWith("五")){o2 = "05";}
            if(o2.replace("闰","").startsWith("六")){o2 = "06";}
            if(o2.replace("闰","").startsWith("七")){o2 = "07";}
            if(o2.replace("闰","").startsWith("八")){o2 = "08";}
            if(o2.replace("闰","").startsWith("九")){o2 = "09";}
            if(o2.replace("闰","").startsWith("十")){o2 = "10";}
            if(o2.replace("闰","").startsWith("冬")){o2 = "11";}
            if(o2.replace("闰","").startsWith("腊")){o2 = "12";}
            return o1.compareTo(o2);
        }
    }
}