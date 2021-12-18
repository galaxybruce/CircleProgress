package com.littlejie.circleprogress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.littlejie.circleprogress.utils.Constant;
import com.littlejie.circleprogress.utils.MiscUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 吹气泡
 */

public class CircleBubble extends View {

    private Context mContext;

    //默认大小
    private int mDefaultWidth, mDefaultHeight;
    //是否开启抗锯齿
    private boolean antiAlias;
    private int mDuration;

    //绘制气泡
    private Paint mBubblePaint;
    private float mStartBubbleRadius;
    private float mEndBubbleRadius;
    private int mStartBubbleAlpha = 0;
    private int mEndBubbleAlpha = 255;
    private int mBubbleColor = Color.BLUE;

    //气泡终点
    private Point mStartPoint;
    //气泡开始时间
    private List<Long> mBubbleStartTimeList = new CopyOnWriteArrayList<>();
    private int mLastValue;
    private long mLastTime;

    public CircleBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mDefaultWidth = MiscUtil.dipToPx(mContext, 50);
        mDefaultHeight = MiscUtil.dipToPx(mContext, 200);
        mStartPoint = new Point();
        initAttrs(attrs);
        initPaint();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.CircleBubble);

        antiAlias = typedArray.getBoolean(R.styleable.CircleBubble_antiAlias, Constant.ANTI_ALIAS);
        mStartBubbleRadius = typedArray.getDimension(R.styleable.CircleBubble_startBubbleRadius, 0);
        mEndBubbleRadius = typedArray.getDimension(R.styleable.CircleBubble_endBubbleRadius, 50);
        mStartBubbleAlpha = typedArray.getInt(R.styleable.CircleBubble_startBubbleAlpha, 0);
        mEndBubbleAlpha = typedArray.getInt(R.styleable.CircleBubble_endBubbleAlpha, 0);
        mBubbleColor = typedArray.getColor(R.styleable.CircleBubble_bubbleColor, Color.BLUE);
        mDuration = typedArray.getInt(R.styleable.CircleBubble_duration, 1000);

        if(mStartBubbleRadius > mEndBubbleRadius) {
            throw new IllegalArgumentException("startBubbleAlpha must < endBubbleAlpha");
        }

        typedArray.recycle();
    }

    private void initPaint() {
        mBubblePaint = new Paint();
        mBubblePaint.setAntiAlias(antiAlias);
        // 设置画笔的样式，为FILL，FILL_OR_STROKE，或STROKE
        mBubblePaint.setStyle(Paint.Style.FILL);
        // 当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式，如圆形样式
        // Cap.ROUND,或方形样式 Cap.SQUARE
        mBubblePaint.setStrokeCap(Paint.Cap.BUTT);
        mBubblePaint.setColor(mBubbleColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MiscUtil.measure(widthMeasureSpec, mDefaultWidth),
                MiscUtil.measure(heightMeasureSpec, mDefaultHeight));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //获取圆的相关参数
        mStartPoint.x = w / 2;
        mStartPoint.y = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBubble(canvas);

        postInvalidateDelayed(10);
    }

    private void drawBubble(Canvas canvas) {
        float maxY = mStartPoint.y + mStartBubbleRadius / 2 + mEndBubbleRadius / 2;
        float x = mStartPoint.x * 1.0f;
        float y = 0.0f;
        float radius = 0.0f;
        float ratio = 0.0f;
        int alpha = mStartBubbleAlpha;
        long now = System.currentTimeMillis();
        List<Long> list = new ArrayList<>(mBubbleStartTimeList);
        for (Long startTime : list) {
            ratio = ((now - startTime) * 1.0f / mDuration);
            if(ratio > 1.0f) {
                continue;
            }
            y = maxY - (maxY + mEndBubbleRadius / 2) * ratio;
            radius = mStartBubbleRadius + (mEndBubbleRadius - mStartBubbleRadius) * ratio;
            alpha = mStartBubbleAlpha + (int)((mEndBubbleAlpha - mStartBubbleAlpha) * ratio);
            mBubblePaint.setAlpha(alpha);
            canvas.drawCircle(x, y, radius, mBubblePaint);
        }

    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //释放资源
    }

    /**
     * 已1秒内吹出3000毫升为标准，绘制50个气泡；也就是每毫秒3毫升，绘制(50/1000)=0.05个气泡
     * @param value
     */
    public void setValue(int value) {
        long now = System.currentTimeMillis();
//        if(mLastValue > 0) {
//            int count = (int) ((((value - mLastValue) * 1.0f / (now - mLastTime)) / 3) * 0.05f);
//            for (int i = 0; i < count; i++) {
//                mBubbleStartTimeList.add(now + (now - mLastTime) * i / count);
//            }
//        }
//        mLastValue = value;
//        mLastTime = now;

        for (int i = 0; i < 1; i++) {
            mBubbleStartTimeList.add(now + 20 * i);
        }

        Log.i("aaaaaaaaaaaa", "value: " + value + "-- " + mBubbleStartTimeList.size());
    }

    public void reset() {
        mLastValue = 0;
        mLastTime = 0;
        mBubbleStartTimeList.clear();
    }
}
