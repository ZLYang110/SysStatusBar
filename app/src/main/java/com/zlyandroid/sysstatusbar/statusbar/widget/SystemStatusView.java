package com.zlyandroid.sysstatusbar.statusbar.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.zlyandroid.sysstatusbar.R;
import com.zlyandroid.sysstatusbar.statusbar.SystemStatusConstant;
import com.zlyandroid.sysstatusbar.statusbar.SystemStatusHelp;
import com.zlyandroid.sysstatusbar.statusbar.widget.BatteryView;


/**
 * Created by zhangliyang
 */
public class SystemStatusView extends RelativeLayout {



    private TextView tv_system_time;                                     //时间标示
    private RelativeLayout ly_system_battery;                            //电量
    private BatteryView bv_system_battery;                               //电量标示
    private TextView tv_system_battery;                                  //电量标示

    private TextView tv_system_net;                                      //网络标示

    private ImageView iv_system_location;                                //定位标示

    private ImageView iv_system_vol;                                     //音量标示



    private SystemStatusHelp mSystemStatusManager;                //系统状态管理
    private Context mContext;

    private boolean isShowBattery = true;//是否显示电量
    private boolean isShowBatteryNum = true;//是否显示电量数字
    private boolean isShowNet = true;//是否显示网络
    private boolean isShowLocation = true;//是否显示定位
    private boolean isShowVol = true;//是否显示音量

    private int themeMode= 1;// 1主题为白色 2主题为黑色
    private int themeColor= Color.WHITE;


    public SystemStatusView(Context context) {
        this(context, null);
    }

    public SystemStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SystemStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
        setAttributeSet(context,attrs);
        initView(context);
    }
    private void setAttributeSet(Context context,@Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SystemStatusBar);
        isShowBattery = typedArray.getBoolean(R.styleable.SystemStatusBar_isShowBattery, isShowBattery);
        isShowBatteryNum = typedArray.getBoolean(R.styleable.SystemStatusBar_isShowBatteryNum, isShowBatteryNum);
        isShowNet = typedArray.getBoolean(R.styleable.SystemStatusBar_isShowNet, isShowNet);
        isShowLocation = typedArray.getBoolean(R.styleable.SystemStatusBar_isShowLocation, isShowLocation);
        isShowVol = typedArray.getBoolean(R.styleable.SystemStatusBar_isShowVol, isShowVol);
        themeMode = typedArray.getInt(R.styleable.SystemStatusBar_themeMode, themeMode);
        typedArray.recycle();
    }


    /**
     * 注册广播监听
     */
    public void registerStatusBarReceiver() {
        mSystemStatusManager.registerStatusBarReceiver();
    }

    /**
     * 取消广播监听
     */
    public void unregisterStatusBarReceiver() {
        mSystemStatusManager.unregisterStatusBarReceiver();
    }


    private void initView(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.system_status_view, this);

        tv_system_time =  findViewById(R.id.tv_system_time);
        ly_system_battery =  findViewById(R.id.ly_system_battery);
        bv_system_battery =  findViewById(R.id.bv_system_battery);
        tv_system_battery =  findViewById(R.id.tv_system_battery);
        tv_system_net =  findViewById(R.id.tv_system_net);
        iv_system_location =  findViewById(R.id.iv_system_location);
        iv_system_vol = findViewById(R.id.iv_system_vol);
        mSystemStatusManager = new SystemStatusHelp(context, this);

        if(!isShowBattery){
            ly_system_battery.setVisibility(GONE);
        }else{
            ly_system_battery.setVisibility(VISIBLE);
        }
        if(!isShowBatteryNum){
            tv_system_battery.setVisibility(GONE);
        }else{
            tv_system_battery.setVisibility(VISIBLE);
        }
        if(!isShowNet){
            tv_system_net.setVisibility(GONE);
        }else{
            tv_system_net.setVisibility(VISIBLE);
        }
        if(!isShowLocation){
            iv_system_location.setVisibility(GONE);
        }else{
            iv_system_location.setVisibility(VISIBLE);
        }
        if(!isShowVol){
            iv_system_vol.setVisibility(GONE);
        }else{
            iv_system_vol.setVisibility(VISIBLE);
        }
        if(themeMode==1){
            themeColor= Color.WHITE;
        }else if(themeMode==2){
            themeColor= Color.BLACK;
        }else{
            themeColor= Color.WHITE;
        }

    }






    /**
     * 刷新音量布局
     */
   public void refreshVolumeView(int curVolume, double maxVolume) {

        if(!isShowVol)return;

        int status;

        double percentage = curVolume / maxVolume;

        //计算音量显示状态
        if (percentage > 0.8) {
            status = SystemStatusConstant.STREAM_MUSIC_STATUS_OK;
        } else if (percentage <= 0.8 && percentage > 0.5) {
            status = SystemStatusConstant.STREAM_MUSIC_STATUS_WEAK;
        } else {
            status = SystemStatusConstant.STREAM_MUSIC_STATUS_LOST;
        }


        Object tag = iv_system_vol.getTag();
        int preStatus = Integer.parseInt(tag == null ? "1" : tag.toString());

        //状态不变,直接返回
        if (status == preStatus) {
            return;
        }

        int srcDrawableId;

        switch (status) {
            case SystemStatusConstant.STREAM_MUSIC_STATUS_OK:
                srcDrawableId = R.drawable.statusbar_vol_ok;
                break;
            case SystemStatusConstant.STREAM_MUSIC_STATUS_WEAK:
                srcDrawableId = R.drawable.statusbar_vol_weak;
                break;
            case SystemStatusConstant.STREAM_MUSIC_STATUS_LOST:
                srcDrawableId = R.drawable.statusbar_vol_lost;
                break;
            default:
                srcDrawableId = R.drawable.statusbar_vol_lost;
                break;
        }
        iv_system_vol.setImageResource(srcDrawableId);
        iv_system_vol.setTag(status);
    }


    /**
     * 刷新电量布局
     * @param percentage
     */
    public void refreshBatteryView(int percentage) {
        if(!isShowBattery)return;
        String batteryInfo = percentage + "%";

        String currentBatteryInfo = tv_system_battery.getText().toString();
        if(batteryInfo.equals(currentBatteryInfo)) {
            return;
        }
        int currentPercentage = Integer.parseInt(currentBatteryInfo.substring(0,
                currentBatteryInfo.length() - 1));

        int status;

        //高电量
        if (percentage > 50) {
            if (currentPercentage > 50) {
                status = SystemStatusConstant.BATTERY_STATUS_CONTINUE;
            } else {
                status = SystemStatusConstant.BATTERY_STATUS_OK;
            }
        } else if (percentage > 20) {
            //半电量
            if (currentPercentage > 20) {
                status = SystemStatusConstant.BATTERY_STATUS_CONTINUE;
            } else {
                status = SystemStatusConstant.BATTERY_STATUS_WEAK;
            }
        } else {
            //弱电量
            if (currentPercentage <= 20) {
                status = SystemStatusConstant.BATTERY_STATUS_CONTINUE;
            } else {
                status = SystemStatusConstant.BATTERY_STATUS_LOST;
            }
        }


        int textColorId;

      /*  //只刷新电量,颜色不变
        if (status == SystemStatusConstant.BATTERY_STATUS_CONTINUE) {
            bv_system_battery.setPower(percentage);
            tv_system_battery.setText(batteryInfo);
            return;
        }
*/
        switch (status) {
            //正常
            case SystemStatusConstant.BATTERY_STATUS_OK:
                textColorId = themeColor;
                break;
            //警告
            case SystemStatusConstant.BATTERY_STATUS_WEAK:
                textColorId = themeColor;
                break;
            //低电量
            case SystemStatusConstant.BATTERY_STATUS_LOST:
                textColorId = R.color.statusbar_text_red;
                break;
            default:
                textColorId = R.color.statusbar_text_red;
                break;
        }
        Log.d("refreshBatteryView","================"+percentage);
        bv_system_battery.setPower(percentage);
        tv_system_battery.setText(batteryInfo);
        tv_system_battery.setTextColor(textColorId);

    }


    /**
     * 刷新时间布局
     */
    public void refreshTimeView(String time, int status) {

        //只刷新时间数值,颜色不变
        if (status == SystemStatusConstant.TASK_STATUS_CONTINUE) {
            tv_system_time.setText(time);
            return;
        }
       /* int textColorId;
        switch (status) {
            //正常
            case SystemStatusConstant.TASK_STATUS_OK:
                textColorId = themeColor;
                break;
            //警告
            case SystemStatusConstant.TASK_STATUS_EDGE:
                textColorId = themeColor;
                break;
            //超时
            case SystemStatusConstant.TASK_STATUS_OVER:
                textColorId = R.color.statusbar_text_red;
                break;
            default:
                textColorId = R.color.statusbar_text_red;
                break;
        }
*/
        tv_system_time.setText(time);

    }


    /**
     * 根据Gps信号状态，刷新Gps布局
     */
    public void refreshGpsView(int status) {
        if(!isShowLocation)return;

        int srcDrawableId;

        switch (status) {
            case SystemStatusConstant.GPS_STATUS_OK:
                srcDrawableId = R.drawable.statusbar_gps_ok;
                break;
            case SystemStatusConstant.GPS_STATUS_WEAK:
                srcDrawableId = R.drawable.statusbar_gps_weak;
                break;
            case SystemStatusConstant.GPS_STATUS_LOST:
            case SystemStatusConstant.GPS_STATUS_CLOSED:
                srcDrawableId = R.drawable.statusbar_gps_lost;
                break;
            default:
                srcDrawableId = R.drawable.statusbar_gps_lost;
                break;
        }

        iv_system_location.setImageResource(srcDrawableId);

    }


    /**
     * 根据网络类型和网络状态，刷新网络布局
     */
    public void refreshSignalView(String networkType, int status) {
        if(!isShowNet)return;
        int textColorId;

        //网络状态不改变时,不做任何界面刷新处理
        if (tv_system_net.getTag() != null
                && tv_system_net.getTag().toString().equals(networkType + status)) {
            return;
        }

        switch (status) {
            //正常绿色
            case SystemStatusConstant.NET_STATUS_OK:
                textColorId = themeColor;
                break;
            //微弱黄色
            case SystemStatusConstant.NET_STATUS_WEAK:
                textColorId = R.color.statusbar_text_yellow;
                break;
            //丢失红色
            case SystemStatusConstant.NET_STATUS_LOST:
            case SystemStatusConstant.NET_STATUS_CLOSED:
                textColorId = R.color.statusbar_text_red;
                break;
            //默认红色
            default:
                textColorId = R.color.statusbar_text_red;
                break;
        }

        tv_system_net.setText(networkType);
        tv_system_net.setTextColor(textColorId);
        tv_system_net.setTag(networkType + status);

    }


}
