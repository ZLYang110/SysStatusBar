package com.zlyandroid.sysstatusbar.statusbar.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.zlyandroid.sysstatusbar.R;

import java.util.Timer;
import java.util.TimerTask;

public class BatteryView  extends View {
    private Context mContext;
    private int mPower = 100;       // 电池电量
    private int width;              // 电池总宽度
    private int height;             // 电池总高度
    private int margin = 2;         // 电池内芯与边框距离
    private int border = 2;         // 电池边框线条宽度
    private float radius = 4;       // 圆角角度
    private boolean isCharge = false;
    private int chargePower;
    private Timer mTimer;

    public BatteryView(Context context) {
        super(context);
        this.mContext=context;

    }

    public BatteryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

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

    /**
     * 设置电量
     *
     * @param mPower 电量百分比 1-100
     */
    public void setPower(@IntRange(from = 1, to = 100) int mPower) {
        this.mPower = mPower;
        if (this.mPower < 0) {
            this.mPower = 0;
        }
        if (this.mPower > 100) {
            this.mPower = 100;
        }
        postInvalidate();
    }

    /**
     * 获取电量
     *
     * @return 电量百分比 1-100
     */
    public int getPower() {
        return mPower;
    }

    /**
     * 设置充电模式，如果传入true自动开启线程，使电池无限循环，营造出一个正在充电的状态
     *
     * @param charge
     */
    public void setCharge(boolean charge) {
        isCharge = charge;
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
        if (isCharge) {
            chargePower = 0;
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    chargePower += 10;
                    if (chargePower > 100) {
                        chargePower = 0;
                    }
                    setPower(chargePower);
                }
            }, 500, 300);
        }
    }
}
