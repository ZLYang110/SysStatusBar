package com.zlyandroid.sysstatusbar;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.zlyandroid.sysstatusbar.statusbar.widget.BatteryView;
import com.zlyandroid.sysstatusbar.statusbar.widget.SystemStatusView;

public class MainActivity extends AppCompatActivity {

    private SystemStatusView statusView;

    private BatteryView batteryview,batteryview2,batteryview3,batteryview4;
    private TextView tv_system_battery,tv_system_battery2,tv_system_battery3,tv_system_battery4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusView = (SystemStatusView) findViewById(R.id.status_bar);
        statusView.registerStatusBarReceiver();
        batteryview =  findViewById(R.id.batteryview);
        batteryview2=  findViewById(R.id.batteryview2);
        batteryview3=  findViewById(R.id.batteryview3);
        batteryview4=  findViewById(R.id.batteryview4);
        tv_system_battery =  findViewById(R.id.tv_system_battery);
        tv_system_battery2 =  findViewById(R.id.tv_system_battery2);
        tv_system_battery3 =  findViewById(R.id.tv_system_battery3);
        tv_system_battery4 =  findViewById(R.id.tv_system_battery4);

        batteryview2.setPower(100);
        tv_system_battery2.setText(batteryview2.getPower()+"%");

        batteryview3.setPower(60);
        tv_system_battery3.setText(batteryview3.getPower()+"%");

        batteryview4.setPower(15);
        tv_system_battery4.setText(batteryview4.getPower()+"%");
        tv_system_battery4.setTextColor(Color.RED);
    }

    @Override
    protected void onDestroy() {
        statusView.unregisterStatusBarReceiver();
        super.onDestroy();
    }
}
