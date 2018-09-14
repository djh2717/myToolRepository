package my.code.repository.custom.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import javax.inject.Inject;

import advanced.nioDemo.R;
import my.code.repository.dagger2.component.DaggerLoadingViewComponent;
import my.code.repository.utils.PxUtil;

/**
 * A loading view, use to show loading.
 *
 * @author djh on  2018/8/21 15:00
 * @E-Mail 1544579459@qq.com
 */
public class LoadingView extends View {

    /**
     * Custom styleable.
     */
    private int mLoadingColor;
    private int mBackgroundColor;
    private int mBackgroundAlpha;

    /**
     * Circle radius offset.
     */
    private float mOffset;

    private int mMeasureWidth;
    private int mMeasureHeight;

    private float mProgress;
    private float mRoundRadius;
    private float mCircleRadius;

    @Inject
    Paint mPaint;
    @Inject
    RectF mBorderRectF;
    @Inject
    RectF mLoadingRectF;

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Use dagger2 init.
        DaggerLoadingViewComponent.create().inject(this);

        // Get custom styleable.
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        // Default is white.
        mLoadingColor = typedArray.getColor(R.styleable.LoadingView_loadingColor, Color.WHITE);
        // Default is gray.
        mBackgroundColor = typedArray.getColor(R.styleable.LoadingView_backgroundColor, Color.GRAY);
        // Default is 108.
        mBackgroundAlpha = typedArray.getInt(R.styleable.LoadingView_backgroundAlpha, 108);
        typedArray.recycle();

        // Config paint.
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);

        // Round radius is 10dp.
        mRoundRadius = PxUtil.dpToPx(10);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureWidth = getMeasureSize(widthMeasureSpec);
        mMeasureHeight = getMeasureSize(heightMeasureSpec);
        setMeasuredDimension(mMeasureWidth, mMeasureHeight);
    }

    private int getMeasureSize(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.UNSPECIFIED:
                return MeasureSpec.getSize(measureSpec);
            case MeasureSpec.AT_MOST:
                return (int) PxUtil.dpToPx(80);
            default:
        }
        return 0;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Set the border rectF.
        mBorderRectF.left = 0;
        mBorderRectF.right = mMeasureWidth;
        mBorderRectF.top = 0;
        mBorderRectF.bottom = mMeasureHeight;

        // Set the loading rectF, satisfying the golden ratio.
        float loadingWidth = mMeasureWidth * 0.618f;
        float loadingHeight = mMeasureHeight * 0.618f;

        mLoadingRectF.left = mMeasureWidth * 1.0f / 2 - loadingWidth / 2;
        mLoadingRectF.right = mLoadingRectF.left + loadingWidth;

        mLoadingRectF.top = mMeasureHeight * 1.0f / 2 - loadingHeight / 2;
        mLoadingRectF.bottom = mLoadingRectF.top + loadingHeight;

        // Set the circle radius according the loading rectF.
        mCircleRadius = Math.min(mLoadingRectF.width() / 2, mLoadingRectF.height() / 2);
        // The circle radius offset.
        mOffset = PxUtil.dpToPx(3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mBackgroundColor);
        mPaint.setAlpha(mBackgroundAlpha);

        // Draw the border background, color is gray.
        canvas.drawRoundRect(mBorderRectF, mRoundRadius, mRoundRadius, mPaint);

        mPaint.setAlpha(255);
        mPaint.setColor(mLoadingColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);

        // Draw a circle.
        canvas.drawCircle(mMeasureWidth / 2, mMeasureHeight / 2, mCircleRadius + mOffset, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        // Draw loading arc.
        canvas.drawArc(mLoadingRectF, -90, mProgress * 3.6f, true, mPaint);
    }

    /**
     * Use to set loading color, default is white.
     */
    public void setLoadingColor(int color) {
        mLoadingColor = color;
        postInvalidate();
    }

    /**
     * Use to set progress.
     */
    public void setProgress(float progress) {
        if (progress > 100.0) {
            throw new RuntimeException("Progress must less than 100!");
        }
        mProgress = progress;
        postInvalidate();
    }
}
