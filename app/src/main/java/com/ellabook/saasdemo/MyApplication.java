package com.ellabook.saasdemo;

import static com.ellabook.saassdk.annotation.LinkModeConstants.LinkMode_Api;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.ellabook.saassdk.EllaReaderApi;
import com.ellabook.saassdk.annotation.DownloadZipModeConstants;
import com.ellabook.saassdk.annotation.LinkModeConstants;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;

/**
 * Created by tiandehua on 2021/1/5
 *
 * @description:
 */
public class MyApplication extends Application {
    public static Context CONTEXT;

    @Override
    public void onCreate() {
        super.onCreate();
        //全局捕获异常
        CrashUtils.getInstance().init(this);
        CONTEXT = this;

        //初始化阅读器(必须)
        initEllaReader();

        CrashReport.initCrashReport(getApplicationContext(), "8aafddc609", true);
    }

    private void initEllaReader() {
        /**
         * 初始化，这是第一步，也是必须的
         *
         * @param appContext context
         * @param setRootPath   存放图书的目录，建议是一个单独目录，如果为null,则使用默认目录，getFilesDir()/ellabook
         * @param setDownloadZipMode 下载模式:[SUB_ZIP],分包下载;[ALL_ZIP],全部下载
         * @param setLinkMode 连接地址:[LinkMode_Api],国内环境;[LinkMode_Out],海外环境
         */
//        String logo = getFilesDir() + File.separator + "logo.png";
//        ResourceUtils.copyFileFromAssets("logo.png", logo);
        EllaReaderApi.getInstance()
                .setLinkMode(LinkMode_Api)
                .setRootPath(getExternalFilesDir(null).getAbsolutePath() + File.separator + "ellabook")
                .setDownloadZipMode(DownloadZipModeConstants.ALL_ZIP)
//                .setCustomDomainUrl("https://")
                //设置阅读器加载logo和背景颜色
//                .setLaunchLogoPath(/*logoPath*/logo, /*A*/255, /*R*/111, /*G*/111, /*B*/111)
                .init(this);

        //设置第三方域名:仅用于自己存储分包资源时的下载.
//        EllaReaderApi.getInstance().setCustomDomainUrl("www.xxx.com");

        //是否debug模式(默认false),可输出更多日志.注意线上环境关闭.
        EllaReaderApi.isDebug(true);
    }

}
