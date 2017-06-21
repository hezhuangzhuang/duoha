package com.zxwl.duoha;

import android.app.Application;

import com.huawei.esdk.cc.MobileCC;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;
import com.zxwl.duoha.utils.sharedpreferences.PreferencesHelper;

/**
 * Copyright 2015 蓝色互动. All rights reserved.
 * author：hw
 * data:2017/4/14 17:17
 * ClassName: ${Class_Name}
 */

public class App extends Application {
    private static App mInstance;

    public static App getInstance() {
        return mInstance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
//        NetWorkConfiguration configuration = new NetWorkConfiguration(this);
//        HttpUtils.setConFiguration(configuration);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        //腾讯bug线上搜集工具 TODO KEY_ID换成真实的数据
        CrashReport.initCrashReport(getApplicationContext(), "aca6f8ef97", true);

        //初始化SharedPreferences
        PreferencesHelper.init(mInstance);

        //华为初始化
        MobileCC.getInstance().initSDK(this);
        MobileCC.getInstance().setLog("CCTUPLOG", 3);
        MobileCC.getInstance().setAnonymousCard("AnonymousCard");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        MobileCC.getInstance().unInitSDK(); // 停止SDK服务
    }
}
