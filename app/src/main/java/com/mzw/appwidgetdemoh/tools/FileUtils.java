package com.mzw.appwidgetdemoh.tools;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * 文件创建、写入、读取、加密、解密、删除
 * Created by think on 2018/11/28.
 *
 * android SD卡主要有两种存储方式 Internal 、 External Storage
 *
 * Internal
 *  它总是可用的。
 *  这里保存的文件只有你的应用程序可以访问。
 *  当用户卸载应用程序时，系统将从内部存储中删除应用程序的所有文件。
 *  内部存储不需要申请任何权限
 *
 * External Storage
 *  这个文件也分两种 Private files 、Public files
 *
 * 详细解释  https://blog.csdn.net/huaiyiheyuan/article/details/52473984
 *
 */

public class FileUtils {

    private static final int numOfEncAndDec = 0x09; //加密解密秘钥
    private static int dataOfFile = 0; //文件字节内容
    private static String charsetName = "utf-8";

    public static String getSDPath(Context mContext){

        //使用内部存储
        File fileCache = new File(mContext.getCacheDir(), "vital");
        if (!fileCache.exists()) {
            boolean isInner = fileCache.mkdirs();
            Log.i("---mzw---",  "创建文件夹: " + isInner);
        }
        Log.i("---mzw---",  "缓存路径: " + fileCache.getAbsolutePath());


        return fileCache.getAbsolutePath();
    }
    //文件加密
    public static void EncFile(File srcFile, File encFile) throws Exception {
        if(!srcFile.exists()){
            return;
        }
        if(!encFile.exists()){
            encFile.createNewFile();
        }
        InputStream fis  = new FileInputStream(srcFile);
        OutputStream fos = new FileOutputStream(encFile);
        while ((dataOfFile = fis.read()) > -1) {
            //异或（^） 两边的位不同时，结果为1，否则为0.如1100^1010=0110
            fos.write(dataOfFile^numOfEncAndDec);
        }
        fis.close();
        fos.flush();
        fos.close();
    }

    //文件解密
    public static void DecFile(File encFile, File decFile) throws Exception {
        if(!encFile.exists()){
            return;
        }
        if(!decFile.exists()){
            decFile.createNewFile();
        }

        InputStream fis  = new FileInputStream(encFile);
        OutputStream fos = new FileOutputStream(decFile);
        while ((dataOfFile = fis.read()) > -1) {
            //异或（^） 两边的位不同时，结果为1，否则为0.如1100^1010=0110
            fos.write(dataOfFile^numOfEncAndDec);
        }

        fis.close();
        fos.flush();
        fos.close();
    }

    //文件写入
    public static void writeFile(File file,String content) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(fos,charsetName);
        writer.write(content);
        writer.close();
        fos.close();
    }

    //文件读取
    public static String readFile(File file) throws IOException {
        if(!file.exists()){
            return null;
        }
        char[] buffer = null;

        FileInputStream bis = new FileInputStream(file);
        InputStreamReader reader=new InputStreamReader(bis,"utf-8");
        int len;
        buffer = new char[bis.available()];
        do {
            reader.read(buffer);
        }while ((len = reader.read()) != -1);
        //br.read() != -1  执行这个条件的时候其实它已经读取了一个字符了   所以 以下代码每一行都会少一个字
//        while ((len = reader.read()) != -1) {
//            reader.read(buffer);
//        }
        reader.close();
        bis.close();

        return new String(buffer);
    }

    //删除目录下所有文件
    public static void deleteFile(String path) {
        deleteFile(new File(path));
    }
    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
//            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }
}
