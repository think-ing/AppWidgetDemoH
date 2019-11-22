package com.mzw.appwidgetdemoh;

import android.content.Context;
import android.util.Log;

import com.mzw.appwidgetdemoh.tools.ConstantParameter;
import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLDownloadTask;
import com.tencent.cos.xml.transfer.COSXMLTask;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cos.xml.transfer.TransferStateListener;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;
import com.tencent.qcloud.core.http.HttpTaskMetrics;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 *
 * 网络方法
 * Created by think on 2019/1/20.
 */
public class NetworkRequest {
    private Context mContext;

    //创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
    private CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
            .setAppidAndRegion(ConstantParameter.appid, ConstantParameter.region)
            .setDebuggable(true)
            .builder();

    /**
     * 初始化 {@link QCloudCredentialProvider} 对象，来给 SDK 提供临时密钥。
     */
    private QCloudCredentialProvider qCloudCredentialProvider;
    //初始化 COS 服务类
    private CosXmlService cosXmlService;
    // 初始化 TransferConfig
    private TransferConfig transferConfig;
    //初始化 TransferManager
    private TransferManager transferManager;


    public NetworkRequest(Context mContext) {
        this.mContext = mContext;
        this.qCloudCredentialProvider = new ShortTimeCredentialProvider(ConstantParameter.secretId,ConstantParameter.secretKey, 300);
        this.cosXmlService = new CosXmlService(mContext, serviceConfig, qCloudCredentialProvider);
        this.transferConfig = new TransferConfig.Builder().build();
        this.transferManager = new TransferManager(cosXmlService, transferConfig);
    }

    /**
     * 下载文件
     * transferManager.download 中参数说明
     *
     applicationContext = "application 上下文"； // getApplicationContext()
     bucket = "存储桶名称"; //文件所在的存储桶
     cosPath = "对象键"; //即文件存储到 COS 上的绝对路径,格式如 cosPath = "test.txt";
     savedDirPath = "文件下载到本地的文件夹路径"；
     savedFileName = "文件下载本地的文件名"；//若不填（null）,则与 cos 上的文件名一样
     */
    public void downloadFile(String fileName,String savedDirPath,CosXmlResultListener mCosXmlResultListener) {
        //下载文件
        COSXMLDownloadTask cosxmlDownloadTask = transferManager.download(
                mContext, ConstantParameter.bucket, fileName, savedDirPath);

        //设置任务状态回调, 可以查看任务过程
        cosxmlDownloadTask.setTransferStateListener(new TransferStateListener(){
            @Override
            public void onStateChanged(TransferState state) {
                Log.d("TEST", "下载状态:" + state.name());
            }
        });

        //设置返回结果回调
        cosxmlDownloadTask.setCosXmlResultListener(mCosXmlResultListener);

        //设置下载进度回调
        cosxmlDownloadTask.setCosXmlProgressListener(new CosXmlProgressListener(){
            @Override
            public void onProgress(long complete, long target) {
                float progress = 1.0f * complete / target * 100;
                Log.d("TEST","下载进度 --> " + String.format("progress = %d%%", (int)progress));
            }
        });
    }

    public void uploadingFile(String fileName,String srcPath,CosXmlResultListener mCosXmlResultListener) {
        //上传文件
        COSXMLUploadTask cosxmlUploadTask = transferManager.upload(ConstantParameter.bucket, fileName, srcPath, null);
        //设置返回结果回调
        cosxmlUploadTask.setCosXmlResultListener(mCosXmlResultListener);
    }

}
