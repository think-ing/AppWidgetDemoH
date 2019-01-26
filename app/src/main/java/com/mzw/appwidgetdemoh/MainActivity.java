package com.mzw.appwidgetdemoh;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mzw.appwidgetdemoh.activity.LoginActivity;
import com.mzw.appwidgetdemoh.activity.SampleActivity;
import com.mzw.appwidgetdemoh.adapter.GridAdapter;
import com.mzw.appwidgetdemoh.tools.ConstantParameter;
import com.mzw.appwidgetdemoh.tools.FileUtils;
import com.mzw.appwidgetdemoh.tools.MyDatePickerDialog;
import com.mzw.appwidgetdemoh.tools.NotificationsUtils;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;


import java.io.File;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


/**
 * 主要技术
 * 使用腾讯云 对象存储  https://console.cloud.tencent.com
 *
 * mob短信验证
 * 文件创建、写入、读取、加密、解密、删除
 * 密码MD5加盐 加密
 * 阳历转阴历
 * 自定义时间选择器
 * 通知栏权限
 *
 * 主要功能
 * 桌面日历挂件（可查看上下月）
 * 在挂件中  农历 阳历 24节气 三伏三九天 特殊节日 高亮提醒。
 * 特殊节日可自定义添加（如生日，纪念日等）
 * 挂件每30分钟刷新一次
 * 挂件刷新时检测今天是否为特殊节日 是：发送通知提醒
 *
 *
 * 内部流程
 * 注册  mob手机验证 -- 腾讯云下载用户信息文件 -- 解密 -- 将注册信息添加到文件 -- 加密 -- 上传 -- 注册成功
 * 登陆  腾讯云下载用户信息文件 -- 解密 -- 数据校验 -- 登陆成功
 * 特殊节日添加  腾讯云下载用户信息文件 -- 解密 -- 选择时间（默认忽略年） -- 添加备注（限两个字） -- 数据添加到文件 -- 加密 -- 上传到云  完成
 * 特殊节日删除  腾讯云下载用户信息文件 -- 解密 -- 长按删除 -- 文件加密 -- 上传到云 完成
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private Context mContext;
    private Calendar calendar = Calendar.getInstance();

    private TextView detailsView;//特殊节日 详情
    private TextView textView;//特殊节日
    private ImageView settingView;//设置
    private TextView buttonAddView;//添加
    private TextView dateTextView;//选择时间
    private EditText nicknameView;//添加备注
    private LinearLayout progressBar_layout;// loading
    private GridView gridView;//
    private GridAdapter mGridAdapter;

    private String userName = "mzw";//记录登陆账号（电话号，生成文件名字）
    private String SDPath;//SD根目录

    private File srcFile,encFile,decFile;

    private String lunarMD = "";//农历月日  数字表示   便于排序
    private String lunarYMD = "";//农历年月日
    private String solarYMD = "";//阳历年月日
    // 储存格式为  {"农历月日":"备注,农历年月日,阳历年月日"}  如{"腊月十六":"小虎,戊戌狗年腊月十六,2019年01月21日"}
    private Map<String,Object> birthdayMap;//生日，纪念日等 特殊节日
    private Type typeOfT;
    /**
     *输入法管理器
     */
    private InputMethodManager mInputMethodManager;
    private NetworkRequest mNetworkRequest;


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://网络数据改变  直接到这里
                    int arg1 = msg.arg1;
                    getBirthday(1);//网络数据改变 重新提取网络数据

                    String text = "";
                    if(arg1 == 1){
                        text = "添加成功";
                        dateTextView.setText("");
                        nicknameView.setText("");
                    }else{
                        text = "删除成功";
                    }
                    Toast.makeText(mContext,text,Toast.LENGTH_SHORT).show();
                    break;
                case 1://网络数据 直接到这里
                    try {
                        //文件解密
                        FileUtils.DecFile(encFile,decFile);
                        //读取文件json
                        String _response = FileUtils.readFile(decFile).trim();
                        if(!TextUtils.isEmpty(_response) && _response.startsWith("{")){
                            Gson gson = new Gson();
                            birthdayMap = gson.fromJson(_response,typeOfT);

                            //保存到本地
                            ConstantParameter.saveMap(mContext,"birthday",birthdayMap);
                        }else{
                            birthdayMap.clear();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(2);
                    break;
                case 2://本地数据 直接到这里
                    //显示数据
                    if(mGridAdapter != null){
                        int i = 0;
                        while (birthdayMap != null && birthdayMap.size() % 3 != 0 && i < 10){
                            birthdayMap.put("999"+i,"");
                            i++;
                        }
                        mGridAdapter.setBirthdayMap(birthdayMap);
                        progressBar_layout.setVisibility(View.GONE);
                    }
                    break;
                case -1:
                    String error = (String)msg.obj;
                    Toast.makeText(mContext,TextUtils.isEmpty(error)?"操作失败请重试":error,Toast.LENGTH_SHORT).show();
                    break;

                case 10:
                    final String str = (String)msg.obj;
                    detailsView.setText(str);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mNetworkRequest = new NetworkRequest(mContext);
        birthdayMap = new TreeMap<String, Object>();
        typeOfT = new TypeToken<TreeMap<String,Object>>(){}.getType();


        detailsView = findViewById(R.id.id_detailsView);
        textView = findViewById(R.id.id_textView);
        settingView = findViewById(R.id.id_settingView);
        buttonAddView = findViewById(R.id.id_buttonAddView);
        dateTextView = findViewById(R.id.id_dateTextView);
        nicknameView = findViewById(R.id.id_nicknameView);
        gridView = findViewById(R.id.id_gridView);
        progressBar_layout = findViewById(R.id.id_progressBar_layout);

        textView.setOnClickListener(this);
        settingView.setOnClickListener(this);
        buttonAddView.setOnClickListener(this);
        dateTextView.setOnClickListener(this);

        init();
    }

    private void init() {
        //获取本地用户信息
        userName = ConstantParameter.getUserName(mContext);

        //获取 sd卡路径
        SDPath = FileUtils.getSDPath(mContext);
        //清空目录中文件
        FileUtils.deleteFile(SDPath);
        if(TextUtils.isEmpty(userName)){
            // 登陆
            startActivity(new Intent(mContext,LoginActivity.class));
            this.finish();
        }

        srcFile = new File(SDPath+"/mzw"+userName+".mzw"); //初始文件
        encFile = new File(SDPath+"/mzw"+userName+".by"); //加密文件
        decFile = new File(SDPath+"/mzw"+userName+".mzw"); //解密文件

        mGridAdapter = new GridAdapter(birthdayMap, mContext, getLayoutInflater());
        gridView.setAdapter(mGridAdapter);
        gridView.setOnItemLongClickListener(mOnItemLongClickListener);
        gridView.setOnItemClickListener(mOnItemClickListener);

        mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        nicknameView.addTextChangedListener(textWatcher);


        getBirthday(0);
        checkSettings();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.id_settingView:
                //设置
                Intent mIntent = new Intent(mContext,SampleActivity.class);
                mIntent.putExtra("sign",1);
                startActivity(mIntent);
                break;
            case R.id.id_dateTextView:
                //主题 样式  0 -- 5
                //选择时间
                showDatePickerDialog(MainActivity.this,3,dateTextView,nicknameView,calendar);
                break;

            case R.id.id_textView:
                //查询 特殊节日
                getBirthday(1);
                break;
            case R.id.id_buttonAddView:
                //添加 特殊节日  每个账号最多50个
                String nickname = nicknameView.getText().toString().trim();
                if(TextUtils.isEmpty(lunarMD)){
                    Toast.makeText(mContext,"请添加时间！！！",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(nickname)){
                    Toast.makeText(mContext,"请添加备注！！！",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(birthdayMap.size() > 50){
                    Toast.makeText(mContext,"添加数量已达上线！！！",Toast.LENGTH_LONG).show();
                    return;
                }else{
                    birthdayMap.remove("9990");
                    birthdayMap.remove("9991");
                    birthdayMap.remove("9992");

                    birthdayMap.put(lunarMD,nickname+","+lunarYMD+","+solarYMD);
                    putBirthday(1);//添加
                }
                break;
        }
    }

    /**
     * 获取数据
     * 本地查找  null  网络下载
     * 到 云下载加密文件
     * 文件解密
     * 读取文件json
     * 保存到本地
     * 转为list
     *
     * sign 是否去网络 取数据（本地有就不去，新增或删除要去）
     */
    private void getBirthday(int sign) {
        progressBar_layout.setVisibility(View.VISIBLE);
        birthdayMap.clear();
        if(sign == 0){
            //查找本地
            birthdayMap = ConstantParameter.getMap(mContext,"birthday",new TreeMap<String, Object>(),typeOfT);
Log.i("---mzw---","查找本地 : " + birthdayMap);
            if(birthdayMap != null && birthdayMap.size() > 0){
                mHandler.sendEmptyMessage(2);
                return;
            }
        }

        //到云下载加密文件
        mNetworkRequest.downloadFile("mzw"+userName+".by",SDPath,new CosXmlResultListener(){
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                Log.d("TEST",  "下载结果Success: " + result.printResult());
                if(encFile.exists()){
                    Log.d("TEST",  "文件存在：" + encFile.getPath());
                    Log.d("TEST",  "文件大小：" + encFile.length());
                    mHandler.sendEmptyMessage(1);
                }else{
                    Log.d("TEST",  "文件不存在");
                }
            }
            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                String error = exception == null ? serviceException.getMessage() : exception.toString();
                Log.d("TEST",  "下载结果Failed: " + error);
                mHandler.sendEmptyMessage(2);
            }
        });
    }
    /**
     * 上传文件
     * 转为json
     * 写入文档文件
     * 文件加密
     * 上传到 云
     * 删除原文件和加密文件
     * @param arg1
     */
    private void putBirthday(final int arg1){
        try {
            //转 json
            Gson gson = new Gson();
            String strJson  = gson.toJson(birthdayMap);
            //将json写入文档文件
            FileUtils.writeFile(srcFile,strJson);
            //文件加密
            FileUtils.EncFile(srcFile,encFile);

            //将加密文件上传到云
            mNetworkRequest.uploadingFile("mzw"+userName+".by",encFile.getPath(),new CosXmlResultListener() {
                @Override
                public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                    Log.d("TEST",  "Success: " + result.printResult());
                    Message msg = new Message();
                    msg.what = 0;
                    msg.arg1 = arg1;
                    mHandler.sendMessage(msg);
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


    //选择时间
    public void showDatePickerDialog(Activity activity, int themeResId, final TextView tv, final EditText ev, Calendar calendar) {
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        new MyDatePickerDialog(activity
                ,  themeResId
                // 绑定监听器(How the parent is notified that the date is set.)
                ,new MyDatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day, String lunarYMD, String lunarMD) {
                // 此处得到选择的时间，可以进行你想要的操作
                MainActivity.this.lunarYMD = lunarYMD.replace("闰","");
                MainActivity.this.lunarMD = lunarMD.replace("闰","");
                MainActivity.this.solarYMD = year+ "年" + month+ "月" + day + "日";
                tv.setText( solarYMD + "【" + lunarYMD + "】");

//                ev.setText("");
//                ev.setFocusable(true);//设置输入框可聚集
//                ev.setFocusableInTouchMode(true);//设置触摸聚焦
//                ev.requestFocus();//请求焦点
//                ev.findFocus();//获取焦点
//                //强制键盘 隐藏 弹出 （）
//                mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                ev.setText("");
                ev.setFocusable(true);
                ev.setFocusableInTouchMode(true);
                ev.findFocus();
                ev.requestFocus();//edittext是一个EditText控件
                //强制显示软键盘,必须先让EditText重新获取焦点,等待UI绘制完成,才能弹出软键盘,加一个0.3s的定时器
                Timer timer =new Timer();//设置定时器
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {//弹出软键盘的代码
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(ev, InputMethodManager.RESULT_SHOWN);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                },300);//设置300毫秒的时长
            }
        }
                ,calendar
                ,true
                ,true
                ,true).show();
    }


    // 检查 设置  （是否开启通知栏权限）
    private void checkSettings() {
        if (!NotificationsUtils.isNotificationEnabled(this)) {
            final AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.show();

            View view = View.inflate(this, R.layout.dialog, null);
            dialog.setContentView(view);

            TextView context = (TextView) view.findViewById(R.id.tv_dialog_context);
            context.setText("检测到您没有打开通知权限！！！\n是否去打开？？");

            TextView confirm = (TextView) view.findViewById(R.id.btn_confirm);
            confirm.setText("是");
            confirm.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.cancel();
                    Intent localIntent = new Intent();
                    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= 9) {
                        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        localIntent.setData(Uri.fromParts("package", MainActivity.this.getPackageName(), null));
                    } else if (Build.VERSION.SDK_INT <= 8) {
                        localIntent.setAction(Intent.ACTION_VIEW);

                        localIntent.setClassName("com.android.settings",
                                "com.android.settings.InstalledAppDetails");

                        localIntent.putExtra("com.android.settings.ApplicationPkgName",
                                MainActivity.this.getPackageName());
                    }
                    startActivity(localIntent);
                }
            });

            TextView cancel = (TextView) view.findViewById(R.id.btn_off);
            cancel.setText("否");
            cancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
        }
    }

    private AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener(){
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            final String key = (String)mGridAdapter.getItem(position);
            final String value = (String)birthdayMap.get(key);

            if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)){
                new AlertDialog.Builder(MainActivity.this).setTitle("删除").setMessage("你确定？？")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        birthdayMap.remove("9990");
                                        birthdayMap.remove("9991");
                                        birthdayMap.remove("9992");
                                        birthdayMap.remove(key);
                                        putBirthday(0);//删除
                                    }
                                })
                        .setNegativeButton("取消",null)
                        .create().show();
                return true;
            }else{
                return false;
            }

        }
    };

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final String key = (String)mGridAdapter.getItem(position);
            final String value = (String)birthdayMap.get(key);

            Log.i("---mzw---","点击： " + key + " , " + value);
            if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)){
                String str = "";
                try {
                    str =  ConstantParameter.getConstellation(ConstantParameter.sdf.parse(value.split(",")[2]),true);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Message msg = new Message();
                msg.what = 10;
                msg.obj = value.replace(",","  ") + "\n          " +str;
                mHandler.sendMessage(msg);
            }
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
        @Override
        public void afterTextChanged(Editable s) {
            if(!TextUtils.isEmpty(s.toString()) && s.toString().length() >= 2){
                if(mInputMethodManager.isActive()){
                    mInputMethodManager.hideSoftInputFromWindow(nicknameView.getWindowToken(),0);//隐藏输入法
                }
            }
        }
    };

//    public TreeMap<String, Object> getMap() {
//        TreeMap<String, Object> treeMap = new TreeMap<String, Object>(new Comparator<String>(){
//            /*
//             * int compare(Object o1, Object o2) 返回一个基本类型的整型，
//             * 返回负数表示：o1 小于o2，
//             * 返回0 表示：o1和o2相等，
//             * 返回正数表示：o1大于o2。
//             */
//            public int compare(String o1, String o2) {
//                Log.i("---mzw---","=-=-=-=-=-=-=-=-="+o1 + " , " + o2);
////                "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"
//                if(o1.replace("闰","").startsWith("一")){o1 = "01";}
//                if(o1.replace("闰","").startsWith("二")){o1 = "02";}
//                if(o1.replace("闰","").startsWith("三")){o1 = "03";}
//                if(o1.replace("闰","").startsWith("四")){o1 = "04";}
//                if(o1.replace("闰","").startsWith("五")){o1 = "05";}
//                if(o1.replace("闰","").startsWith("六")){o1 = "06";}
//                if(o1.replace("闰","").startsWith("七")){o1 = "07";}
//                if(o1.replace("闰","").startsWith("八")){o1 = "08";}
//                if(o1.replace("闰","").startsWith("九")){o1 = "09";}
//                if(o1.replace("闰","").startsWith("十")){o1 = "10";}
//                if(o1.replace("闰","").startsWith("冬")){o1 = "11";}
//                if(o1.replace("闰","").startsWith("腊")){o1 = "12";}
//
//                if(o2.replace("闰","").startsWith("一")){o2 = "01";}
//                if(o2.replace("闰","").startsWith("二")){o2 = "02";}
//                if(o2.replace("闰","").startsWith("三")){o2 = "03";}
//                if(o2.replace("闰","").startsWith("四")){o2 = "04";}
//                if(o2.replace("闰","").startsWith("五")){o2 = "05";}
//                if(o2.replace("闰","").startsWith("六")){o2 = "06";}
//                if(o2.replace("闰","").startsWith("七")){o2 = "07";}
//                if(o2.replace("闰","").startsWith("八")){o2 = "08";}
//                if(o2.replace("闰","").startsWith("九")){o2 = "09";}
//                if(o2.replace("闰","").startsWith("十")){o2 = "10";}
//                if(o2.replace("闰","").startsWith("冬")){o2 = "11";}
//                if(o2.replace("闰","").startsWith("腊")){o2 = "12";}
//                //指定排序器按照降序排列
//                return o2.compareTo(o1);
//            }
//        });
//        return treeMap;
//    }


    @Override
    protected void onDestroy() {
        //退出时 删除缓存文件
        FileUtils.deleteFile(SDPath);
        super.onDestroy();
    }


}
