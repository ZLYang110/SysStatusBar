package com.zlyandroid.sysstatusbar.statusbar.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.zlyandroid.sysstatusbar.R;

public class SignalView  extends View {
    private String TAG ="SignalView";

    private int signalMaximum= 4; //信号柱数量
    private int signalLevel= 0; //信号等级
    private int primaryColor= Color.BLACK;//无信号颜色
    private int levelColor= Color.WHITE; //有信号颜色
    private int spacing= 0; //间隙
    private float unitWidth= 6; //信号柱宽度
    private Boolean connected= true; //链接状态
    private int shadowColor= Color.TRANSPARENT; //阴影颜色
    private Boolean shadowOpen= false; //是否开启阴影效果

    private Paint mPaint;

    private int mRectHeight= 0;
    private int mRectWidth= 0;

    public SignalView(Context context) {
        super(context);
    }
    public SignalView(Context context,@Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SignalView(Context context,@Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttributeSet(context,attrs);
        init();
        initSize();
    }


    private void setAttributeSet(Context context,@Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SignalView);
        signalMaximum = typedArray.getInt(R.styleable.SignalView_signal_maximum, signalMaximum);
        signalLevel = typedArray.getInt(R.styleable.SignalView_signal_level, signalLevel);
        primaryColor = typedArray.getColor(R.styleable.SignalView_primary_color,primaryColor);
        levelColor = typedArray.getColor(R.styleable.SignalView_level_color, levelColor);
        spacing = typedArray.getInt(R.styleable.SignalView_spacing, spacing);
        unitWidth = typedArray.getFloat(R.styleable.SignalView_unit_width, unitWidth);
        connected = typedArray.getBoolean(R.styleable.SignalView_connected, connected);
        shadowColor = typedArray.getColor(R.styleable.SignalView_shadow_color, shadowColor);
        shadowOpen = typedArray.getBoolean(R.styleable.SignalView_shadow_open, shadowOpen);
        typedArray.recycle();
        }


    private void init() {

        Log.i(TAG,"init");
        mPaint = new Paint();
        mPaint.setAntiAlias(true);// 去锯齿
        if (shadowOpen) {
            float shadowWith = (float) (unitWidth * 0.2);
            mPaint.setShadowLayer(5F, shadowWith, shadowWith, shadowColor);
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec ){
        int width;
        // 测量模式，从xml可知
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        // 测量大小,从xml中获取
        int specSize = MeasureSpec.getSize(widthMeasureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            width = specSize;
        } else {
            width = 80;
            // wrap_content模式，选择最小值
            if (specMode == MeasureSpec.AT_MOST) {
                width = width>specSize?specSize :width;
            }
        }
        return width;
    }
    private int measureHeight( int heightMeasureSpec )  {
        int height;
        // 测量模式，从xml可知
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        // 测量大小,从xml中获取
        int specSize = MeasureSpec.getSize(heightMeasureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            height = specSize;
        } else {
            height = 50;
            // wrap_content模式，选择最小值
            if (specMode == MeasureSpec.AT_MOST) {
                height = height>specSize?specSize :height;
            }
        }
        mRectHeight = height;
        return height;
    }


    private void initSize(){
        mRectHeight = getHeight();
        mRectWidth = getWidth() / signalMaximum;
    }

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

    public void setSignalLevel(int level ) throws Exception {
        if (level > signalMaximum){
            throw new Exception("setSignalLevel method value error,can not exceed settings signalMaximum!");
        }
        if (signalLevel != level){
            signalLevel = level;
            initSize();
            this.invalidate();
        }
    }

    public void  setConnected(boolean connected){
        if (this.connected != connected){
            this.connected = connected;
            initSize();
            this.invalidate();
        }
    }
}
