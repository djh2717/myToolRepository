package my.code.repository.custom.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import advanced.nioDemo.R;
import my.code.repository.utils.MatrixUtil;
import my.code.repository.utils.PxUtil;

/**
 * Imitate the cropped image of WeChat. Directly use in xml.
 * notice: Remember to add attributes for custom crop categories.
 *
 * @author 15445
 */
public class WeChatCrop extends AppCompatImageView implements
        ScaleGestureDetector.OnScaleGestureListener {

    private int clipType;
    private int drawableWidth;
    private int drawableHeight;

    /**
     * The width and height of the component.
     */
    private int width;
    private int height;

    /**
     * The coordinates of the last touch in the touch listen event.
     */
    private float lastX;
    private float lastY;

    /**
     * The initial scaling value after scaling the image.
     */
    private float initializeScaleX;
    private float initializeScaleY;

    /**
     * Crop category,round and square.
     */
    private static final int CLIP_TYPE_RECT = 0;
    private static final int CLIP_TYPE_CIRCLE = 1;

    private Paint paint;
    private Matrix matrix;
    private RectF transparentSquare;
    private ScaleGestureDetector scaleGestureDetector;

    WeChatCrop(Context context, AttributeSet attrs) {
        super(context, attrs);

        setScaleType(ScaleType.MATRIX);
        matrix = new Matrix();
        transparentSquare = new RectF();
        paint = new Paint();
        scaleGestureDetector = new ScaleGestureDetector(getContext(), this);
        //获取裁剪类别,默认裁剪方形
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WeChatCrop);
        clipType = typedArray.getInt(R.styleable.WeChatCrop_clipType, 0);
        typedArray.recycle();
    }

    /**
     * 动态设置裁剪图片的类型
     */
    public void setClipType(int type) {
        clipType = type;
    }

    /**
     * 无意中发现,ImageView的setImageBitmap会导致onLayout回调,因为重新设置了图片是可能导致
     * ImageView的大小发生改变,所以要重新布局,猜想.onSizeChange也可能发生改变,如果宽高模式
     * 为wrap_content的话,为match_parent不会触发(已经实践).
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            drawableWidth = drawable.getIntrinsicWidth();
            drawableHeight = drawable.getIntrinsicHeight();
        }
        //在测量和布局之后,绘制之前,用矩阵来实现对图片一系列的变化
        changeByMatrix();
    }

    private void changeByMatrix() {
        //用MatrixTool实现了封装
        matrix = MatrixUtil.getAdapterImageViewMatrix(this);
        //记录初始缩放比例
        initializeScaleX = MatrixUtil.getMatrixValues(MatrixUtil.SCALE_X, matrix);
        initializeScaleY = MatrixUtil.getMatrixValues(MatrixUtil.SCALE_Y, matrix);

        setImageMatrix(matrix);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制灰色的图层
        transparentSquare.top = 0;
        transparentSquare.left = 0;
        transparentSquare.right = width;
        transparentSquare.bottom = height;
        paint.setColor(Color.GRAY);
        paint.setAlpha(180);
        paint.setDither(true);
        paint.setAntiAlias(true);
        canvas.saveLayer(transparentSquare, paint);
        canvas.drawRect(transparentSquare, paint);

        //根据屏幕宽度计算透明正方形(也是圆形的外接矩形)的位置
        transparentSquare.left = PxUtil.dpToPx(20);
        transparentSquare.right = width - PxUtil.dpToPx(20);
        transparentSquare.top = (height - transparentSquare.width()) / 2;
        transparentSquare.bottom = transparentSquare.top + transparentSquare.width();
        //绘制边框线的画笔设置
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(PxUtil.dpToPx(1));
        paint.setAlpha(255);
        //根据裁剪类别绘制不同的透明图形
        drawTypeGraph(canvas);
    }

    private void drawTypeGraph(Canvas canvas) {
        switch (clipType) {
            case CLIP_TYPE_RECT:
                drawTransparentRect(canvas);
                break;
            case CLIP_TYPE_CIRCLE:
                drawTransparentCircle(canvas);
                break;
            default:

        }
    }

    private void drawTransparentRect(Canvas canvas) {
        //绘制边框线
        canvas.drawRect(transparentSquare, paint);
        //绘制透明正方形
        paint.reset();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        paint.setAlpha(0);
        canvas.drawRect(transparentSquare, paint);
        paint.setXfermode(null);
        canvas.restore();
    }

    private void drawTransparentCircle(Canvas canvas) {
        //绘制边框线(采用绘制圆弧的方式,用正方形指定为外界矩形,画出来的就是圆形)
        canvas.drawArc(transparentSquare, 0, 360, false, paint);
        //绘制透明圆形
        paint.reset();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        paint.setAlpha(0);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawArc(transparentSquare, 0, 360, true, paint);
        paint.setXfermode(null);
        canvas.restore();
    }

    /**
     * 根据裁剪类别,供外界获取裁剪后的图片,先对缩放后的图片进行判断,
     * 如果宽高小于透明正方形宽高,直接返回,如果大于,就裁剪图片.
     */
    public Bitmap getCropBitmap() {
        switch (clipType) {
            case CLIP_TYPE_RECT:
                return getRectBitmap();
            case CLIP_TYPE_CIRCLE:
                return getCircleBitmap();
            default:
        }
        return null;
    }

    private Bitmap getScaledBitmap() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return null;
        }
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        //根据初始图片生成被用户缩放后的当前显示在屏幕上的图片用于裁剪
        return Bitmap.createBitmap(bitmapDrawable.getBitmap(), 0, 0, drawableWidth, drawableHeight, matrix, true);
    }

    private Bitmap getRectBitmap() {
        //获取当前显示在屏幕上的图片(是用户缩放过后的)
        Bitmap bitmap = getScaledBitmap();
        if (bitmap == null) {
            return null;
        }
        //获取缩放后图片的RectF
        RectF rectF = MatrixUtil.getRectFByMatrix(this, matrix);
        //缩放之后图片的宽高
        float scaledDrawableWidth = bitmap.getWidth();
        float scaledDrawableHeight = bitmap.getHeight();
        //透明正方形的宽高
        float transparentSquareWidth = transparentSquare.width();
        float transparentSquareHeight = transparentSquare.height();

        //对四种情况分别判断,进行裁剪或拉伸(宽大于,高大于,宽高同时大于,同时小于)

        //宽大于,高小于(高全部裁剪,宽根据黑边来判断裁剪)
        if (scaledDrawableWidth >= transparentSquareWidth &&
                scaledDrawableHeight < transparentSquareHeight) {
            //计算图片所处位置和透明正方形左右边的差值,即左右边的黑边
            int distanceLeft = (int) (rectF.left - transparentSquare.left);
            int distanceRight = (int) (rectF.right - transparentSquare.right);

            //如果没有黑边
            if (distanceLeft <= 0 && distanceRight >= 0) {
                return Bitmap.createBitmap(bitmap, -distanceLeft, 0, (int) transparentSquare.width(), bitmap.getHeight());
            } else {
                //如果有黑边,居中裁剪
                return Bitmap.createBitmap(bitmap, (int) (bitmap.getWidth() - transparentSquare.width()) / 2, 0,
                        (int) transparentSquare.width(), bitmap.getHeight());
            }
        }

        //宽小于,高大于(宽全部裁剪,高根据黑边来判断裁剪)
        if (scaledDrawableHeight >= transparentSquareHeight &&
                scaledDrawableWidth < transparentSquareWidth) {
            //计算图片所处位置和透明正方形上下边的差值,即上下边的黑边
            int distanceTop = (int) (rectF.top - transparentSquare.top);
            int distanceBottom = (int) (rectF.bottom - transparentSquare.bottom);

            //如果没有黑边
            if (distanceTop <= 0 && distanceBottom >= 0) {
                return Bitmap.createBitmap(bitmap, 0, -distanceTop, bitmap.getWidth(), (int) transparentSquare.height());
            } else {
                //如果上下有黑边,居中裁剪
                return Bitmap.createBitmap(bitmap, 0, (int) (rectF.width() - transparentSquare.width()) / 2,
                        bitmap.getWidth(), (int) transparentSquare.height());
            }
        }


        //宽高同时大于
        if (scaledDrawableHeight >= transparentSquareHeight &&
                scaledDrawableWidth >= transparentSquareWidth) {
            //计算图片所处位置和透明正方形上下左右边的差值
            int distanceTop = (int) (rectF.top - transparentSquare.top);
            int distanceLeft = (int) (rectF.left - transparentSquare.left);
            int distanceRight = (int) (rectF.right - transparentSquare.right);
            int distanceBottom = (int) (rectF.bottom - transparentSquare.bottom);
            //如果top,left为负数,right,bottom为正数,说明透明正方形没有黑边,直接裁剪一个正方形大小
            if (distanceTop <= 0 && distanceLeft <= 0 &&
                    distanceRight >= 0 && distanceBottom >= 0) {
                return Bitmap.createBitmap(bitmap, -distanceLeft, -distanceTop,
                        (int) transparentSquareWidth, (int) transparentSquareHeight);
            } else {
                //只要正方形某一个方向有黑边,直接裁剪图片中心,一个正方形大小
                return Bitmap.createBitmap(bitmap, (int) (rectF.width() - transparentSquare.width()) / 2,
                        (int) (rectF.height() - transparentSquare.height()) / 2,
                        (int) transparentSquare.width(), (int) transparentSquare.height());
            }
        }

        //宽高同时小于,不处理,直接返回
        if (scaledDrawableHeight < transparentSquareHeight &&
                scaledDrawableWidth < transparentSquareWidth) {
            return bitmap;
        }
        return null;
    }

    /**
     * 获得圆形的裁剪图片的主要思路为:
     * 1,如果图片的宽高小于圆形的外接矩形的宽高,直接拉伸至外接矩形宽高,然后裁剪.
     * 如果都不小于,先根据透明正方形生成生成一张正方形的裁剪图片,
     * (因为裁剪一张照片只能进行方形裁剪,透明圆形的外接矩形就是那个透明正方形).
     * 2,获得了正方形的图片之后采用画笔的setXfermode来更改图像混合模式,根据正方形图片生成一张
     * 圆形图片然后返回.
     */
    private Bitmap getCircleBitmap() {
        //获取当前显示在屏幕上的图片(是用户缩放过后的)
        Bitmap bitmap = getScaledBitmap();
        if (bitmap == null) {
            return null;
        }
        //缩放之后图片的宽高
        float scaledDrawableWidth = bitmap.getWidth();
        float scaledDrawableHeight = bitmap.getHeight();
        //透明正方形的宽高
        float transparentSquareWidth = transparentSquare.width();
        float transparentSquareHeight = transparentSquare.height();

        //如果用户缩放后的图片宽或高小于外接矩形的宽高,直接把图片拉伸至透明正方形宽高
        Bitmap extrudeBitmap = null;
        if (scaledDrawableHeight < transparentSquareHeight ||
                scaledDrawableWidth < transparentSquareWidth) {
            extrudeBitmap = Bitmap.createScaledBitmap(bitmap, (int) transparentSquareWidth, (int) transparentSquareHeight, false);
        }
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        //如果拉伸了图片,在拉伸的图片的基础上裁剪成圆形图片
        if (extrudeBitmap != null) {
            Bitmap circleBitmap = Bitmap.createBitmap(extrudeBitmap.getWidth(), extrudeBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(circleBitmap);
            //此处绘制圆形不能再用那个透明正方形作为外接矩形,因为那个透明正方形包含一定的位置信息
            //此位置信息在生成圆形图片时是不需要的
            canvas.drawCircle(circleBitmap.getWidth() / 2, circleBitmap.getHeight() / 2, circleBitmap.getWidth() / 2, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(extrudeBitmap, 0, 0, paint);
            paint.setXfermode(null);
            return circleBitmap;
        } else {
            //如果没有拉伸图片,根据getRectBitmap方法返回的裁剪后的正方形图片生成圆形图片
            Bitmap rectBitmap = getRectBitmap();
            if (rectBitmap != null) {
                Bitmap circleBitmap = Bitmap.createBitmap(rectBitmap.getWidth(), rectBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(circleBitmap);
                //此处绘制圆形不能再用那个透明正方形作为外接矩形,因为那个透明正方形包含一定的位置信息
                //此位置信息在生成圆形图片时是不需要的
                canvas.drawCircle(circleBitmap.getWidth() / 2, circleBitmap.getHeight() / 2, circleBitmap.getWidth() / 2, paint);

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(rectBitmap, 0, 0, paint);
                paint.setXfermode(null);
                return circleBitmap;
            }
        }
        return null;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        float currentScaleX = MatrixUtil.getMatrixValues(MatrixUtil.SCALE_X, matrix);
        float currentScaleY = MatrixUtil.getMatrixValues(MatrixUtil.SCALE_Y, matrix);
        //最大最小值判断,最小缩小到比初始缩放值一半,最大放大到初始缩放值5倍
        if ((initializeScaleX / currentScaleX < 2 && initializeScaleY / currentScaleY < 2 && scaleFactor < 1)
                || (scaleFactor >= 1 && currentScaleY / initializeScaleY < 5 && currentScaleX / initializeScaleX < 5)) {
            //直接用currentScale乘以当前矩阵,因为当前矩阵的XY可能是因为图片太大已经缩放过了的矩阵
            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            setImageMatrix(matrix);
            //onScale返回true表示总是对此次缩放事件进行消费,不然会出现越放大,放大速度越快
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        Drawable drawable = getDrawable();
        int scaledDrawableWidth = 0;
        int scaledDrawableHeight = 0;
        if (drawable != null) {
            //只有一个触摸点,且触摸点的位置要在图片的范围内才能移动图片.
            scaledDrawableWidth = (int) (drawable.getIntrinsicWidth() * MatrixUtil.getMatrixValues(MatrixUtil.SCALE_X, matrix));
            scaledDrawableHeight = (int) (drawable.getIntrinsicHeight() * MatrixUtil.getMatrixValues(MatrixUtil.SCALE_Y, matrix));
        }
        float translationX = MatrixUtil.getMatrixValues(MatrixUtil.TRANS_X, matrix);
        float translationY = MatrixUtil.getMatrixValues(MatrixUtil.TRANS_Y, matrix);
        if (pointerCount == 1) {
            /*让图片跟随手指拖动,此处要注意,即使是使用相对于当前控件的相对坐标,
            也要计算绝对偏移量,不停的更新最后一次的坐标
            因为ImageView控件是不会移动的,移动的是drawable*/
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float downX = event.getX();
                    float downY = event.getY();
                    lastX = downX;
                    lastY = downY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    //判断触摸点是否在图片的范围内
                    if (lastX > translationX && lastX < translationX + scaledDrawableWidth
                            && lastY > translationY && lastY < translationY + scaledDrawableHeight) {
                        float currentX = event.getX();
                        float currentY = event.getY();
                        float offSetX = currentX - lastX;
                        float offSetY = currentY - lastY;
                        //变化矩阵
                        matrix.postTranslate(offSetX, offSetY);
                        setImageMatrix(matrix);
                        //更新坐标
                        lastX = currentX;
                        lastY = currentY;
                        break;
                    }
                default:
            }
        } else {
            //Gesture monitoring
            scaleGestureDetector.onTouchEvent(event);
        }
        return true;
    }
}
