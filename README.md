### 介绍

---
1. 自定义系统状态栏，时间，电量，信号强度实时更新 自定义主题
2. 自定义电量图标（低电量变红）
2. 自定义信号强度图标（自定义信号大小颜色等）

### Example

---
<img src="https://github.com/ZLYang110/SysStatusBar/blob/master/screenshot/com.zlyandroid.sysstatusbar.jpg" width = "180" height ="320" alt="图片名称"/>

---

### 可下载APK直接体验
[Demo.apk](https://github.com/ZLYang110/SysStatusBar/tree/master/app/release/app-release.apk)

---



##### 一、自定义系统状态栏，时间，电量，信号强度实时更新
#####  注册广播
```


/**
     * 注册广播监听
     */
    public void registerStatusBarReceiver() {
        mContext.registerReceiver(mReceiver, mFilter);
        mMobileSignalStrengthListener = new MobileSignalStrengthListener();
        mTelephonyManager.listen(mMobileSignalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    /**
     * 取消广播监听
     */
    public void unregisterStatusBarReceiver() {
        mContext.unregisterReceiver(mReceiver);
        mTelephonyManager.listen(mMobileSignalStrengthListener, PhoneStateListener.LISTEN_NONE);
    }

```
#####  广播内容
```


    private void initData(Context context) {

        mContext = context;

        mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_TIME_TICK);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mFilter.addAction("android.media.VOLUME_CHANGED_ACTION");



        mHandler = new Handler();
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);


        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                switch (action) {
                    //时间广播
                    case Intent.ACTION_TIME_TICK:
                        updateTaskStatus(SystemStatusConstant.TASK_STATUS_CONTINUE);
                        break;
                    //网络连接状态(网络切换,网络开关)
                    case ConnectivityManager.CONNECTIVITY_ACTION:
                        updateNetWorkStatus();
                        break;
                    //WiFi信号强度变化
                    case WifiManager.RSSI_CHANGED_ACTION:
                        mHandler.removeCallbacks(mSignalStrengthChangeRunnable);
                        mHandler.postDelayed(mSignalStrengthChangeRunnable, UPDATE_MIN_INTERVAL);
                        break;
                    //GPS连接状态(Gps开关)
                    case LocationManager.MODE_CHANGED_ACTION:
                    case LocationManager.PROVIDERS_CHANGED_ACTION:
                        updateGpsStatus();
                        break;
                    //电量变化
                    case Intent.ACTION_BATTERY_CHANGED:
                        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                        int percentage = (level * 100) / scale;
                        updateBatteryStatus(percentage);
                        break;
                    //音量变化
                    case "android.media.VOLUME_CHANGED_ACTION":
                        updateVolumeStatus();
                        break;
                    default:
                        break;
                }
            }
        };

        //初始化各个状态, 电量不需要刻意初始化
        updateTaskStatus(SystemStatusConstant.TASK_STATUS_OK);
        updateNetWorkStatus();
        updateGpsStatus();
        updateVolumeStatus();

    }

```
#####  网络信号
```
 /**
     * 刷新网络信号状态
     */
    private void updateNetWorkStatus() {

        String networkType = SystemStatusUtils.getNetworkType(mContext);

        int status;

        switch (networkType) {
            case "WIFI":
                status = getWifiLevel();
                break;
            case "2G":
            case "3G":
            case "4G":
                status = getMobileLevel();
                break;
            default:
                status = SystemStatusConstant.NET_STATUS_LOST;
                networkType = "无信号";
                break;
        }


        //没信号时，发送广播警告
        if (networkType.equals("无信号")) {
            SystemStatusUtils.sendBroadcast(mContext, SystemStatusConstant.Action.NET_STATUS,
                    SystemStatusConstant.EXTRA.NET_STATUS_EXTRA, status);
        }

       if(mOnNetWorkStatusListener != null){
                   mOnNetWorkStatusListener.onNetWorkStatus(networkType,status);
               }

    }

    //获取网络信号监听
               SystemStatusHelp.getInstance().setOnNetWorkStatusListener(new SystemStatusHelp.OnNetWorkStatusListener() {
                          @Override
                          public void onNetWorkStatus(String networkType, int status) {
                              refreshSignalView(networkType,status);
                          }
                      });

/**
     * 获取wifi连接的强度状态
     *
     * @return
     */
    private int getWifiLevel() {

        int rssi = mWifiManager.getConnectionInfo().getRssi();
        //WIFI信号最强
        if (rssi > -70) {
            return SystemStatusConstant.NET_STATUS_OK;
        } else if (rssi < -90 && rssi > -70) {
            //WIFI信号较弱
            return SystemStatusConstant.NET_STATUS_WEAK;
        } else {
            //WIFI信号微弱
            return SystemStatusConstant.NET_STATUS_LOST;
        }

    }

    /**
     * 获取蜂窝连接的强度状态
     *
     * @return
     */
    private int getMobileLevel() {

        int level = SystemStatusConstant.NET_STATUS_OK;

        if (mMobileSignalStrengthListener == null
                || mMobileSignalStrengthListener.getSignalStrength() == null) {
            return level;
        }

        String signalStrength = mMobileSignalStrengthListener.getSignalStrength().toString();
        String[] parts = signalStrength.split(" ");

        switch (mTelephonyManager.getNetworkType()) {
            //移动联通2G
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
                level = SystemStatusUtils.getGsmLevel(parts);
                break;
            //电信2G
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                break;
            //4G网络
            case TelephonyManager.NETWORK_TYPE_LTE:
                level = SystemStatusUtils.getLteLevel(parts);
                break;
            //移动3G网络
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                level = SystemStatusUtils.getSdcdmaLevel(parts);
                break;
            default:
                level = SystemStatusConstant.NET_STATUS_OK;
                break;
        }

        return level;

    }
```
#####  GPS状态
```
 /**
     * 刷新Gps状态
     */
    private void updateGpsStatus() {

        int status;

        if (SystemStatusUtils.isGPSOn(mContext)) {
            status = SystemStatusConstant.GPS_STATUS_OK;
        } else {
            status = SystemStatusConstant.GPS_STATUS_CLOSED;
        }


        //发送无GPS警告广播
        if (status == SystemStatusConstant.GPS_STATUS_CLOSED) {
            SystemStatusUtils.sendBroadcast(mContext, SystemStatusConstant.Action.GPS_STATUS,
                    SystemStatusConstant.EXTRA.GPS_STATUS_EXTRA, SystemStatusConstant.GPS_STATUS_CLOSED);
        }


        if(mOnGpsStatusListener!=null){
                   mOnGpsStatusListener.onGpsStatus(status);
               }
    }


    //获取GPS监听
            SystemStatusHelp.getInstance().setOnGpsStatusListener(new SystemStatusHelp.OnGpsStatusListener() {
                      @Override
                      public void onGpsStatus(int status) {
                          refreshGpsView(status);
                      }
                  });
```
#####  音量
```
 /**
     * 刷新媒体音量
     */
    private void updateVolumeStatus() {

        int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        double maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        double percentage = curVolume / maxVolume;

        //低音量警告通知
        if (percentage < 0.5) {
            SystemStatusUtils.sendBroadcast(mContext, SystemStatusConstant.Action.VOLUME_STATUS,
                    SystemStatusConstant.EXTRA.VOLUME_STATUS_EXTRA, curVolume);
        }


        if(mOnVolumeStatusListener!=null){
            mOnVolumeStatusListener.onVolumeStatus(curVolume, maxVolume);
        }


    }


      //获取声音监听
         SystemStatusHelp.getInstance().setOnVolumeStatusListener(new SystemStatusHelp.OnVolumeStatusListener() {
                    @Override
                    public void onVolumeStatus(int curVolume, double maxVolume) {
                        refreshVolumeView(curVolume,maxVolume);
                    }
                });

```
具体请查看源码

##### 二、自定义电池 （低电量变红）

```
 @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setAntiAlias(true);// 去锯齿
        paint.setColor(ContextCompat.getColor(mContext, R.color.zly_color_Grey100));// 设置画笔颜色
        float headWidth = width / 20.0f;// 电池头宽度

        // 画边框
        paint.setStyle(Paint.Style.STROKE);// 设置空心矩形
        paint.setStrokeWidth(border);
        RectF rect_1 = new RectF(border / 2, border / 2, width - headWidth - border / 2, height - border / 2);
        canvas.drawRoundRect(rect_1, radius, radius, paint);

        // 画电池头
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0);
        RectF rect_2 = new RectF(width - headWidth - border / 2, height * 0.25f, width, height * 0.75f);
        canvas.drawRect(rect_2, paint);

        // 画电量
        if (isCharge) {
            paint.setColor(Color.WHITE);
        } else {
            if (mPower < 20) {
                paint.setColor(Color.RED);
            } else {
                paint.setColor(Color.WHITE);
            }
        }
        float offset = (width - headWidth - border - margin) * mPower / 100.f;
        RectF rect_3 = new RectF(border + margin, border + margin, offset, height - border - margin);
        canvas.drawRoundRect(rect_3, radius, radius, paint);

    }
```

##### 三、自定义信号强度

```
   @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initSize();
        Log.i(TAG,"onDraw -- mRectWidth ="+mRectWidth+ "--mRectHeight="+mRectHeight);
        mPaint.setStrokeWidth(unitWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        for (int i=0;i< signalMaximum;i++){
            if (i < signalLevel) {
                mPaint.setColor(levelColor);
                mPaint.setStyle(Paint.Style.FILL);
            } else {
                mPaint.setColor(primaryColor);
                mPaint.setStyle(Paint.Style.FILL);
            }
            float x = (float) (mRectWidth * (i + 0.5) + spacing);
            float y = (float) (mRectHeight * (signalMaximum - i)  * 0.1) ;
            Log.i(TAG,"onDraw -- x ="+x+ "--y="+y+ "--y="+(getHeight() * 0.5));
            canvas.drawLine(x,y,x,(float)(getHeight() * 0.5),mPaint);
        }
        if (!connected) {
            mPaint.setColor(primaryColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawLine(
                    (float)(getWidth() * 0.2) ,
                    (float)(getHeight() * 0.1) ,
                    (float)(getWidth() * 0.8) ,
                    (float)(getHeight() * 0.6) ,
                    mPaint
            );
        }
    }

```
## 联系方式

QQ： 1833309873
E-mail: 1833309873@QQ.com

## 最后

### star 请赏赐 !!!

## LICENSE

MIT License

Copyright (c) 2018

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
