package com.mzw.appwidgetdemoh.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by think on 2018/12/1.
 */

public class MD5 {

    public static String encrypt(String password){
        StringBuffer sb = new StringBuffer();
        // 得到一个信息摘要器
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(password.getBytes());
            // 把每一个byte做一个与运算 0xff
            for (byte b : result) {
                // 与运算
                int number = b & ConstantParameter.MD5_KEY;
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    //加盐
                    sb.append(ConstantParameter.MD5_KEY_FLAVOUR);
                }
                sb.append(str);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

//    public static String decode(String password){
//        return "MD5不可逆，解不了。。。";
//    }
}
