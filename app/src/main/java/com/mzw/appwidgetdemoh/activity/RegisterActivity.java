package com.mzw.appwidgetdemoh.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mzw.appwidgetdemoh.MainActivity;
import com.mzw.appwidgetdemoh.NetworkRequest;
import com.mzw.appwidgetdemoh.R;
import com.mzw.appwidgetdemoh.tools.ConstantParameter;
import com.mzw.appwidgetdemoh.tools.FileUtils;
import com.mzw.appwidgetdemoh.tools.MD5;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

/**
 * 注册
 * Created by think on 2019/1/20.
 */

public class RegisterActivity  extends Activity implements View.OnClickListener {

    private LinearLayout linearLayout;
    private TextView username_view;
    private EditText password_view;
    private TextView submit_view;

    private Context mContext;

    private String username, password;
    private int sign = 1;// 1注册    2找回密码

    private String SDPath;//SD根目录
    private File srcFile,encFile,decFile;

    private Map<String,String> userMap = new HashMap<String,String>();//登陆 用户与密码  （电话号为key，密码为value）

    private NetworkRequest mNetworkRequest;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    //注册成功
                    ConstantParameter.saveUserName(mContext, username, password);
                    startActivity(new Intent(mContext, MainActivity.class));
                    RegisterActivity.this.finish();
                    break;
                case 1:
                    int sign = msg.arg1;
                    try {
                        //文件解密
                        FileUtils.DecFile(encFile,decFile);
                        //读取文件json
                        String _response = FileUtils.readFile(decFile).trim();
                        if(!TextUtils.isEmpty(_response) && _response.startsWith("{")){
                            Gson gson = new Gson();
                            userMap = gson.fromJson(_response,new TypeToken<Map<String,Object> >(){}.getType());
                        }else{
                            userMap.clear();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(sign == 1){
                        rigisterNetwork(username,password);
                    }
                    break;
                case -1:
                    Toast.makeText(mContext, "操作失败请重试...", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        mContext = this;
        mNetworkRequest = new NetworkRequest(mContext);
        //获取 sd卡路径
        SDPath = FileUtils.getSDPath(mContext);

        srcFile = new File(SDPath+"/"+ConstantParameter.USER_INFO_KEY+".mzw"); //初始文件
        encFile = new File(SDPath+"/"+ConstantParameter.USER_INFO_KEY+".by"); //加密文件
        decFile = new File(SDPath+"/"+ConstantParameter.USER_INFO_KEY+".mzw"); //解密文件

        linearLayout = findViewById(R.id.id_layout);
        username_view = findViewById(R.id.id_username_view);
        password_view = findViewById(R.id.id_password_view);
        submit_view = findViewById(R.id.id_submit_view);

        submit_view.setOnClickListener(this);

        sign = getIntent().getIntExtra("sign",1);
        username = getIntent().getStringExtra("phone");
        username_view.setText(username);
        if(sign == 1){
            submit_view.setText("注  册");
        }else{
            submit_view.setText("提  交");
        }
        //下载 用户信息文件
        getUserInfo(0);
    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
        boolean b = true;
        password = MD5.encrypt(password_view.getText().toString().trim());
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            return;
        }

        if(sign == 1){
            if(!TextUtils.isEmpty(userMap.get(username))){
                b = false;
                new AlertDialog.Builder(mContext).setTitle("注册失败").setMessage("用户已存在！！")
                        .setPositiveButton("去登陆",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.putExtra("phone",username);
                                Pair<View,String> pairLayout = new Pair<View, String>(linearLayout,"layout");

                                if (Build.VERSION.SDK_INT >= 22) {
                                    ActivityOptionsCompat options = ActivityOptionsCompat
                                            .makeSceneTransitionAnimation(RegisterActivity.this,pairLayout);
                                    startActivity(intent, options.toBundle());
                                } else {
                                    startActivity(intent);
                                }
                            }
                        })
                        .create().show();
            }
        }

        /**
         * 为了避免 并发出现错误， 在上传文件前重新下载文件
         * 解析后立刻上传
         */
        if(b){
            getUserInfo(1);
        }
    }

    //上传 用户信息
    private void rigisterNetwork(String username, String password) {

        try {
            if(userMap == null){
                userMap = new HashMap<String,String>();
            }
            userMap.put(username,password);
            //转 json
            Gson gson = new Gson();
            String strJson  = gson.toJson(userMap);
            //将json写入文档文件
            FileUtils.writeFile(srcFile,strJson);
            //文件加密
            FileUtils.EncFile(srcFile,encFile);

            //将加密文件上传到 云
            mNetworkRequest.uploadingFile(ConstantParameter.USER_INFO_KEY+".by",encFile.getPath(),new CosXmlResultListener() {
                @Override
                public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                    Log.d("TEST",  "Success: " + result.printResult());
                    mHandler.sendEmptyMessage(0);
                }

                @Override
                public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                    Log.d("TEST",  "Failed: " + (exception == null ? serviceException.getMessage() : exception.toString()));
                    Message msg = new Message();
                    msg.what = -1;
                    msg.obj = exception == null ? serviceException.getMessage() : exception.toString();
                    mHandler.sendMessage(msg);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取 用户信息   1 下载后上传文件，  0不上传
    private void getUserInfo(final int sign) {
        String fileName = ConstantParameter.USER_INFO_KEY+".by";
        //到云下载加密文件
        mNetworkRequest.downloadFile(fileName,SDPath,new CosXmlResultListener(){
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                Log.d("TEST",  "下载结果Success: " + result.printResult());
                if(encFile.exists()){
                    Log.d("TEST",  "文件存在：" + encFile.getPath());
                    Log.d("TEST",  "文件大小：" + encFile.length());
                    Message msg = new Message();
                    msg.arg1 = sign;
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                }else{
                    Log.d("TEST",  "文件不存在");
                }
            }
            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                String error = exception == null ? serviceException.getMessage() : exception.toString();
                Log.d("TEST",  "下载结果Failed: " + error);
//                Not Found
                if(!TextUtils.isEmpty(error) && error.indexOf("404") > 0 && error.indexOf("Not") > 0 && error.indexOf("Found") > 0){
                    rigisterNetwork(username,password);
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        //推出时 删除缓存文件
        FileUtils.deleteFile(SDPath);
        super.onDestroy();
    }
}
