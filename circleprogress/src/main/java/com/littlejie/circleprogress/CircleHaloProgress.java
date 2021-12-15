package com.littlejie.circleprogress;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.littlejie.circleprogress.utils.Constant;
import com.littlejie.circleprogress.utils.MiscUtil;

/**
 * 圆形进度条，类似 QQ 健康中运动步数的 UI 控件
 * Created by littlejie on 2017/2/21.
 */

public class CircleHaloProgress extends View {

    private Context mContext;

    //默认大小
    private int mDefaultSize;
    //是否开启抗锯齿
    private boolean antiAlias;

    //绘制圆弧
    private Paint mArcPaint;
    private float mArcWidth;
    private SweepGradient mSweepGradient;
    private int[] mGradientColors = {Color.GREEN, Color.YELLOW, Color.GREEN};
    private Matrix mMatrix = new Matrix();
    private int mStartAngle = 0;
    private int mAlpha = 255;
    private int mAlphaReverse = 1;

    //绘制光晕
    private Paint mBgArcPaint;
    private float mHaloRadius;

    //圆心坐标，半径
    private Point mCenterPoint;
    private float mRadius;

    //动画时间
    private long mAnimTime;
    //属性动画
    private ValueAnimator mAnimator;

    public CircleHaloProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mDefaultSize = MiscUtil.dipToPx(mContext, Constant.DEFAULT_SIZE);
        mAnimator = new ValueAnimator();
        mCenterPoint = new Point();
        initAttrs(attrs);
        initPaint();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.CircleHaloProgressBar);

        antiAlias = typedArray.getBoolean(R.styleable.CircleHaloProgressBar_antiAlias, Constant.ANTI_ALIAS);
        mArcWidth = typedArray.getDimension(R.styleable.CircleHaloProgressBar_arcWidth, Constant.DEFAULT_ARC_WIDTH);
        mHaloRadius = typedArray.getDimension(R.styleable.CircleHaloProgressBar_haloRadius, 10);
        mAnimTime = typedArray.getInt(R.styleable.CircleHaloProgressBar_animTime, Constant.DEFAULT_ANIM_TIME);

        int gradientArcColors = typedArray.getResourceId(R.styleable.CircleHaloProgressBar_arcColors, 0);
        if (gradientArcColors != 0) {
            try {
                int[] gradientColors = getResources().getIntArray(gradientArcColors);
                if (gradientColors.length == 0) {//如果渐变色为数组为0，则尝试以单色读取色值
                    int color = getResources().getColor(gradientArcColors);
                    mGradientColors = new int[2];
                    mGradientColors[0] = color;
                    mGradientColors[1] = color;
                } else if (gradientColors.length == 1) {//如果渐变数组只有一种颜色，默认设为两种相同颜色
                    mGradientColors = new int[2];
                    mGradientColors[0] = gradientColors[0];
                    mGradientColors[1] = gradientColors[0];
                } else {
                    mGradientColors = gradientColors;
                }
            } catch (Resources.NotFoundException e) {
                throw new Resources.NotFoundException("the give resource not found.");
            }
        }

        typedArray.recycle();
    }

    private void initPaint() {
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(antiAlias);
        // 设置画笔的样式，为FILL，FILL_OR_STROKE，或STROKE
        mArcPaint.setStyle(Paint.Style.STROKE);
        // 设置画笔粗细
        mArcPaint.setStrokeWidth(mArcWidth);
        // 当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式，如圆形样式
        // Cap.ROUND,或方形样式 Cap.SQUARE
        mArcPaint.setStrokeCap(Paint.Cap.BUTT);

        mBgArcPaint = new Paint();
        mBgArcPaint.setAntiAlias(antiAlias);
        // 设置画笔的样式，为FILL，FILL_OR_STROKE，或STROKE
        mBgArcPaint.setStyle(Paint.Style.FILL);
        // 设置画笔粗细
        mBgArcPaint.setStrokeWidth(0);
        // 当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式，如圆形样式
        // Cap.ROUND,或方形样式 Cap.SQUARE
        mBgArcPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MiscUtil.measure(widthMeasureSpec, mDefaultSize),
                MiscUtil.measure(heightMeasureSpec, mDefaultSize));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //求最小值作为实际值
        float minSize = Math.min(w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingTop() - getPaddingBottom());
        //减去圆弧的宽度，否则会造成部分圆弧绘制在外围
        mRadius = minSize / 2 - mHaloRadius - mArcWidth;
        //获取圆的相关参数
        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;
        updateArcPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawArc(canvas);

        mStartAngle = ++mStartAngle % 360;
        if(mSweepGradient != null) {
            mMatrix.setRotate(mStartAngle, mCenterPoint.x, mCenterPoint.y);
            mSweepGradient.setLocalMatrix(mMatrix);
        }

        mAlpha = mAlpha - 5 * mAlphaReverse;
        if(mAlpha == 0 || mAlpha == 255) {
            mAlphaReverse = mAlphaReverse * -1;
        }
        mArcPaint.setAlpha(mAlpha);
        postInvalidateDelayed(10);
    }

    private void drawArc(Canvas canvas) {
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius - mArcWidth, mBgArcPaint);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius, mArcPaint);
    }

    /**
     * 更新圆弧画笔
     */
    private void updateArcPaint() {
        // 设置渐变
        mSweepGradient = new SweepGradient(mCenterPoint.x, mCenterPoint.y, mGradientColors, null);
        mArcPaint.setShader(mSweepGradient);

        mBgArcPaint.setShader(mSweepGradient);
        mBgArcPaint.setMaskFilter(new BlurMaskFilter(mHaloRadius, BlurMaskFilter.Blur.OUTER));
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    private void startAnimator(float start, float end, long animTime) {
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(animTime);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                mPercent = (float) animation.getAnimatedValue();
//                mValue = mPercent * mMaxValue;
                invalidate();
            }
        });
        mAnimator.start();
    }

    /**
     * 设置渐变
     *
     * @param gradientColors
     */
    public void setGradientColors(int[] gradientColors) {
        mGradientColors = gradientColors;
        updateArcPaint();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //释放资源
    }
}
