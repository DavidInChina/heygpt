package com.common.basesdk.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ShimmerView extends View {
    private static final int ANIMATION_TIME = 2000;
    private Shader mGradient = null;
    private Matrix mGradientMatrix = null;
    private Paint mPaint;
    private float mCornerRadius = 0f;
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private float mTranslateX = 0f;
    private float mTranslateY = 0f;
    private RectF rectF;
    private ValueAnimator valueAnimator;
    private boolean autoRun = true; //是否自动运行动画

    public ShimmerView(Context context) {
        super(context);
        init(context, null);
    }

    public ShimmerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ShimmerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        rectF = new RectF();
        mPaint = new Paint();
        initGradientAnimator();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        rectF.set(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mViewWidth == 0) {
            mViewWidth = getWidth();
            mViewHeight = getHeight();
            if (mViewWidth > 0) {
                mGradient = new LinearGradient(0f, 0f, mViewWidth, mViewHeight,
                        new int[] {0x00ffffff, 0x73ffffff, 0x00ffffff},
                        new float[] {0.4f, 0.45f, 0.5f},
                        Shader.TileMode.CLAMP);
                mPaint.setShader(mGradient);
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
                mGradientMatrix = new Matrix();
                mGradientMatrix.setTranslate((-2 * mViewWidth), mViewHeight);
                mGradient.setLocalMatrix(mGradientMatrix);
                rectF.set(0, 0, w, h);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (valueAnimator != null && valueAnimator.isRunning() && mGradientMatrix != null) {
            canvas.drawRoundRect(rectF, mCornerRadius, mCornerRadius, mPaint);
        }
    }

    private void initGradientAnimator() {
        valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(ANIMATION_TIME);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                //❶ 改变每次动画的平移x、y值，范围是[-2mViewWidth, 2mViewWidth]
                mTranslateX = 4f * mViewWidth * v - mViewWidth * 2;
                mTranslateY = mViewHeight * v;
                //❷ 平移matrix, 设置平移量
                if (mGradientMatrix != null) {
                    mGradientMatrix.setTranslate(mTranslateX, mTranslateY);
                }
                //❸ 设置线性变化的matrix
                if (mGradient != null) {
                    mGradient.setLocalMatrix(mGradientMatrix);
                }
                //❹ 重绘
                invalidate();
            }
        });
        if (autoRun && valueAnimator != null) {
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                public void onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    startAnimation();
                }
            });
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    //停止动画
    private void stopAnimation() {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
            invalidate();
        }
    }

    //开始动画
    private void startAnimation() {
        if (valueAnimator != null && !valueAnimator.isRunning() && getVisibility() == View.VISIBLE) {
            valueAnimator.start();
        }
    }
}
