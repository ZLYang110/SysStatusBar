package com.zlyandroid.sysstatusbar;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


/**
 * 应用, 主要用来做一下初始化的操作
 *
 * @author zhangliyang
 * @since 1.0
 */
public class ProApplication extends Application {
    private static final String TAG = "ProApplication";
    private static Context mContext =null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        if (mContext == null) {
            throw new RuntimeException("mContext未在Application中初始化");
        }
        return mContext;
    }

}
