package my.custom.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import javax.inject.Inject;

import advanced.nioDemo.R;
import my.dagger2.component.DaggerShapeImageViewComponent;
import my.utils.BitmapUtil;
import my.utils.PxUtil;

/**
 * Custom image view, round, circle, heart, shape image view, direct use in xml.
 *
 * @author djh on  2018/8/19 22:43
 * @E-Mail 1544579459@qq.com
 */
public class ShapeImageView extends View {

    /**
     * Custom styleable.
     */
    private int mShapeType;
    private int mScaleType;
    private int mRoundRadius;

    /**
     * Custom styleable.
     */
    private boolean mStroke;
    private int mStrokeColor;
    private float mStrokeWidth;

    /**
     * Bitmap is get from styleable or dynamic set, so do not use dagger2.
     */
    private Bitmap mSrcBitmap;
    private Bitmap mLoveBitmap;
    private Bitmap mFiveStarBitmap;

    /**
     * Use to draw shape bitmap.
     */
    @Inject
    RectF mShapeRectF;

    @Inject
    Paint mPaint;
    @Inject
    Paint mStrokePaint;

    @Inject
    Rect mSrcRect;
    @Inject
    RectF mDstRectF;

    @Inject
    PorterDuffXfermode mDuffXfermode;

    /**
     * Image shape type.
     */
    private static final int SHAPE_LOVE = 2;
    private static final int SHAPE_ROUND = 1;
    private static final int SHAPE_CIRCLE = 0;
    private static final int SHAPE_FIVE_STAR = 3;

    /**
     * Scale type.
     */
    private static final int FIT_XY = 0;
    private static final int CENTER = 3;
    private static final int FIT_CENTER = 1;
    private static final int CENTER_CROP = 2;
    private static final int CENTER_INSIDE = 4;

    /**
     * Use to mark measure border.
     */
    private static final int BORDER_WIDTH = 0;
    private static final int BORDER_HEIGHT = 1;

    public ShapeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Use dagger2 to init.
        DaggerShapeImageViewComponent.create().inject(this);
        // Get the custom styleable.
        getStyleable(attrs);
    }

    /**
     * Get the custom styleable.
     */
    private void getStyleable(AttributeSet attrs) {

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ShapeImageView);
        // Get the type.
        mShapeType = typedArray.getInt(R.styleable.ShapeImageView_shapeType, -1);
        // If not specify the type of the imageView, throw exception.
        if (mShapeType == -1) {
            throw new RuntimeException("Must specify the circle or round mShapeType!");
        }
        // Init the shape bitmap, if need.
        switch (mShapeType) {
            case SHAPE_LOVE:
                // Init the love bitmap.
                mLoveBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.love);
                break;
            case SHAPE_FIVE_STAR:
                // Init the five star bitmap.
                mFiveStarBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.five_point_star);
                break;
            default:
        }

        // Get the round radius, default round radius is 10dp.
        mRoundRadius = typedArray.getDimensionPixelSize(R.styleable.ShapeImageView_round_radius, (int) PxUtil.dpToPx(10));

        // Get the drawable.
        int drawableId = typedArray.getResourceId(R.styleable.ShapeImageView_src, -1);
        if (drawableId != -1) {
            mSrcBitmap = BitmapUtil.drawableToBitmap(getResources().getDrawable(drawableId, null));
        }

        // Get the scale type.
        mScaleType = typedArray.getInt(R.styleable.ShapeImageView_scaleType, -1);
        if (mScaleType == -1) {
            throw new RuntimeException("The scale type is not specify!");
        }

        // Get the whether need stroke, default is false.
        mStroke = typedArray.getBoolean(R.styleable.ShapeImageView_stroke, false);

        // Get stroke width and color, if need stroke, else garbage the stroke paint.
        if (mStroke) {
            mStrokeColor = typedArray.getColor(R.styleable.ShapeImageView_strokeColor, Color.WHITE);
            mStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.ShapeImageView_strokeWidth, (int) PxUtil.dpToPx(1));
        } else {
            mStrokePaint = null;
        }
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getMeasureSize(widthMeasureSpec, BORDER_WIDTH);
        int height = getMeasureSize(heightMeasureSpec, BORDER_HEIGHT);
        setMeasuredDimension(width, height);
    }

    /**
     * If mode is wrap_content, the size is bitmap size and padding.
     */
    private int getMeasureSize(int measureSpec, int border) {
        int measureMode = MeasureSpec.getMode(measureSpec);
        switch (measureMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.UNSPECIFIED:
                return MeasureSpec.getSize(measureSpec);
            case MeasureSpec.AT_MOST:
                if (mSrcBitmap != null) {
                    if (border == BORDER_WIDTH) {
                        return mSrcBitmap.getWidth() + getPaddingLeft() + getPaddingRight();
                    } else if (border == BORDER_HEIGHT) {
                        return mSrcBitmap.getHeight() + getPaddingTop() + getPaddingBottom();
                    }
                }
                break;
            default:
        }
        return 0;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // If bitmap is not null, set the scale type and other configs.
        if (mSrcBitmap != null) {
            // Set the scale type.
            setScaleType();

            // Close hardware acceleration, if set the soft layer, will always redraw.
            setLayerType(View.LAYER_TYPE_HARDWARE, null);

            // Set paint.
            mPaint.setDither(true);
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.WHITE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // If the bitmap is null, nothing to draw.
        if (mSrcBitmap == null) {
            return;
        }

        // Draw the type shape.
        switch (mShapeType) {
            case SHAPE_CIRCLE:
                float radius = Math.min(mDstRectF.width() / 2 * 1.0f, mDstRectF.height() / 2 * 1.0f);
                canvas.drawCircle(getMeasuredWidth() * 1.0f / 2, getMeasuredHeight() * 1.0f / 2, radius, mPaint);
                break;
            case SHAPE_ROUND:
                canvas.drawRoundRect(mDstRectF, mRoundRadius, mRoundRadius, mPaint);
                break;
            case SHAPE_LOVE:
                drawShapeBitmap(canvas, mLoveBitmap);
                break;
            case SHAPE_FIVE_STAR:
                drawShapeBitmap(canvas, mFiveStarBitmap);
                break;
            default:
        }

        // Set the xfermode then draw the bitmap.
        mPaint.setXfermode(mDuffXfermode);
        // Draw the bitmap.
        if (mSrcRect.width() == 0) {
            canvas.drawBitmap(mSrcBitmap, null, mDstRectF, mPaint);
        } else {
            canvas.drawBitmap(mSrcBitmap, mSrcRect, mDstRectF, mPaint);
        }
        mPaint.setXfermode(null);

        // If need stroke, draw it, the stroke is not implement love stroke.
        if (mStroke) {
            // Set paint.
            mStrokePaint.setColor(mStrokeColor);
            mStrokePaint.setStyle(Paint.Style.STROKE);
            mStrokePaint.setStrokeWidth(mStrokeWidth);
            // Draw.
            switch (mShapeType) {
                case SHAPE_CIRCLE:
                    float radius = Math.min(mDstRectF.width() / 2 * 1.0f, mDstRectF.height() / 2 * 1.0f);
                    canvas.drawCircle(getMeasuredWidth() * 1.0f / 2, getMeasuredHeight() * 1.0f / 2, radius - mStrokeWidth / 2, mStrokePaint);
                    break;
                case SHAPE_ROUND:
                    canvas.drawRoundRect(mDstRectF, mRoundRadius, mRoundRadius, mStrokePaint);
                    break;
                default:
            }
        }
    }

    private void drawShapeBitmap(Canvas canvas, Bitmap shapeBitmap) {
        float border = Math.min(mDstRectF.width(), mDstRectF.height());
        // Set the shape rectF, it is a square, the border is min of dstRectF.
        mShapeRectF.top = getMeasuredHeight() * 1.0f / 2 - border / 2;
        mShapeRectF.bottom = mShapeRectF.top + border;
        mShapeRectF.left = getMeasuredWidth() * 1.0f / 2 - border / 2;
        mShapeRectF.right = mShapeRectF.left + border;
        // Draw shape bitmap.
        canvas.drawBitmap(shapeBitmap, null, mShapeRectF, mPaint);
    }

    /**
     * This is use to set the src rect and dst rectF according the scale type.
     */
    private void setScaleType() {
        switch (mScaleType) {
            case FIT_XY:
                scaleFitXY();
                break;
            case CENTER_INSIDE:
            case FIT_CENTER:
                scaleFitCenter();
                break;
            case CENTER_CROP:
                scaleCenterCrop();
                break;
            case CENTER:
                scaleCenter();
                break;
            default:
        }
    }

    private void scaleFitXY() {
        mDstRectF.top = 0;
        mDstRectF.left = 0;
        mDstRectF.right = getMeasuredWidth();
        mDstRectF.bottom = getMeasuredHeight();
    }

    private void scaleFitCenter() {
        int bitmapWidth = mSrcBitmap.getWidth();
        int bitmapHeight = mSrcBitmap.getHeight();
        float aspectRatio = bitmapWidth * 1.0f / bitmapHeight * 1.0f;
        // Let the bitmap width is component width, then equal ratio scaled,
        // if the component height is large than equal ratio scaled bitmap height,
        // so, the scale is ok, and the bitmap is not deform, otherwise let the bitmap
        // height is component height, equal ratio the width.
        int scaledWidth = getMeasuredWidth();
        int scaledHeight = Math.round(scaledWidth / aspectRatio);
        if (getMeasuredHeight() >= scaledHeight) {
            mDstRectF.left = 0;
            mDstRectF.right = getMeasuredWidth();
            mDstRectF.top = (getMeasuredHeight() - scaledHeight) / 2;
            mDstRectF.bottom = mDstRectF.top + scaledHeight;
        } else {
            scaledHeight = getMeasuredHeight();
            scaledWidth = Math.round(scaledHeight * aspectRatio);
            mDstRectF.top = 0;
            mDstRectF.bottom = getMeasuredHeight();
            mDstRectF.left = (getMeasuredWidth() - scaledWidth) / 2;
            mDstRectF.right = mDstRectF.left + scaledWidth;
        }
    }

    private void scaleCenterCrop() {
        mDstRectF.top = 0;
        mDstRectF.left = 0;
        mDstRectF.right = getMeasuredWidth();
        mDstRectF.bottom = getMeasuredHeight();
        int bitmapWidth = mSrcBitmap.getWidth();
        int bitmapHeight = mSrcBitmap.getHeight();
        float aspectRatio = bitmapWidth * 1.0f / bitmapHeight * 1.0f;
        // This scale type is opposite with the fit center, we need let the
        // scaled width is component width, then if the equal ratio height is
        // large than component height, the scale is ok, otherwise let the
        // scaled height is component height.
        int scaledWidth = getMeasuredWidth();
        int scaledHeight = Math.round(scaledWidth / aspectRatio);
        if (getMeasuredHeight() <= scaledHeight) {
            float scaleFactor = bitmapHeight * 1.0f / scaledHeight * 1.0f;
            mSrcRect.left = 0;
            mSrcRect.right = bitmapWidth;
            mSrcRect.top = (int) (((scaledHeight - getMeasuredHeight()) / 2) * scaleFactor);
            mSrcRect.bottom = bitmapHeight - mSrcRect.top;
        } else {
            scaledHeight = getMeasuredHeight();
            scaledWidth = Math.round(scaledHeight * aspectRatio);
            float scaleFactor = bitmapWidth * 1.0f / scaledWidth * 1.0f;
            mSrcRect.top = 0;
            mSrcRect.bottom = bitmapHeight;
            mSrcRect.left = (int) ((scaledWidth - getMeasuredWidth()) / 2 * scaleFactor);
            mSrcRect.right = bitmapWidth - mSrcRect.left;
        }
    }

    private void scaleCenter() {
        // Do not any scale, direct center display.
        int bitmapWidth = mSrcBitmap.getWidth();
        int bitmapHeight = mSrcBitmap.getHeight();
        if (bitmapWidth >= getMeasuredWidth()) {
            mDstRectF.left = 0;
            mDstRectF.right = getMeasuredWidth();
            mSrcRect.left = (bitmapWidth - getMeasuredWidth()) / 2;
            mSrcRect.right = mSrcRect.left + getMeasuredWidth();
        } else {
            mDstRectF.left = (getMeasuredWidth() - bitmapWidth) / 2;
            mDstRectF.right = mDstRectF.left + bitmapWidth;
            mSrcRect.left = 0;
            mSrcRect.right = bitmapWidth;
        }
        if (bitmapHeight >= getMeasuredHeight()) {
            mDstRectF.top = 0;
            mDstRectF.bottom = getMeasuredHeight();
            mSrcRect.top = (bitmapHeight - getMeasuredHeight()) / 2;
            mSrcRect.bottom = mSrcRect.top + getMeasuredHeight();
        } else {
            mDstRectF.top = (getMeasuredHeight() - bitmapHeight) / 2;
            mDstRectF.bottom = mDstRectF.top + bitmapHeight;
            mSrcRect.top = 0;
            mSrcRect.bottom = bitmapHeight;
        }
    }

    /**
     * Use to dynamic set the bitmap.
     */
    public void setImageBitmap(Bitmap bitmap) {
        mSrcBitmap = bitmap;
        if (!isInLayout()) {
            requestLayout();
        }
        postInvalidate();
    }

    /**
     * Use to dynamic set stroke color.
     */
    public void setStrokeColor(int color) {
        mStrokeColor = color;
        postInvalidate();
    }
}
