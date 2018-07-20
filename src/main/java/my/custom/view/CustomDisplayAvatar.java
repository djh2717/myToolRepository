package my.custom.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.DecelerateInterpolator;

import my.util.MatrixTool;

/**
 * Imitate the avatar of WeChat. Directly use in xml.
 *
 * @author 15445
 */
public class CustomDisplayAvatar extends AppCompatImageView
        implements ScaleGestureDetector.OnScaleGestureListener {

    private Matrix matrix;

    private float lastX;
    private float lastY;

    private float width;
    private float height;
    private float drawableWidth;
    private float drawableHeight;
    private float initializeScaleX;
    private float initializeScaleY;

    /**
     * Used to record the initial state matrix value
     */
    private float[] initializeMatrixValues = new float[9];

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    CustomDisplayAvatar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
        matrix = new Matrix();
        scaleGestureDetector = new ScaleGestureDetector(getContext(), this);
        gestureDetector = new GestureDetector(getContext(), new MyGestureListener());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //Get picture width and height and component width.
        Drawable drawable = getDrawable();
        if (drawable != null) {
            drawableWidth = drawable.getIntrinsicWidth();
            drawableHeight = drawable.getIntrinsicHeight();
        }
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        changeByMatrix();
    }

    private void changeByMatrix() {
        //Use MatrixTool encapsulation.
        matrix = MatrixTool.getAdapterImageViewMatrix(this);
        //Record initial scaling
        initializeScaleX = MatrixTool.getMatrixValues(MatrixTool.SCALE_X, matrix);
        initializeScaleY = MatrixTool.getMatrixValues(MatrixTool.SCALE_Y, matrix);
        //Record initial matrix values
        matrix.getValues(initializeMatrixValues);
        setImageMatrix(matrix);
    }

    /**
     * 如果是没有放大图片,只进行水平平移
     * 放大后可以随意移动
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*最后的UP事件,触控点会一直是1,
        所以要判断是否缩放过(改进后的Up事件已经无需判断是否缩放过了)
        Up事件中,只是对图片位置判断是否出现黑边,如果出现就回弹*/
        if (event.getPointerCount() == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getX();
                    lastY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    move(event);
                    break;
                case MotionEvent.ACTION_UP:
                    up();
                    break;
                default:
            }
            //Gesture monitoring,mainly monitor double taps and throws(For inertial sliding).
            gestureDetector.onTouchEvent(event);
        } else {
            //Zoom gesture monitoring
            scaleGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    private void move(MotionEvent event) {
        float currentX = event.getX();
        float offSetX = currentX - lastX;
        //如果是放大
        if (MatrixTool.getMatrixValues(MatrixTool.SCALE_X, matrix) > initializeScaleX
                && MatrixTool.getMatrixValues(MatrixTool.SCALE_Y, matrix) > initializeScaleY) {
            float currentY = event.getY();
            float offSetY = currentY - lastY;
            //判断放大之后的图片的宽高是否大于控件宽高,如果都大于,就正常拖动
            //如果高小于,垂直慢速拖动(不存在宽小于)
            float scaledWidth = MatrixTool.getMatrixValues(MatrixTool.SCALE_X, matrix) * drawableWidth;
            float scaledHeight = MatrixTool.getMatrixValues(MatrixTool.SCALE_Y, matrix) * drawableHeight;
            //宽高都大于控件宽高,判断出现黑边的方向,有黑边方向的慢速拖动
            if (scaledWidth > width && scaledHeight > height) {
                //根据矩阵获取图片的矩形,来判断上下左右边是否出现黑边
                RectF rectF = MatrixTool.getRectFByMatrix(this, matrix);
                if (rectF.left < 0 && rectF.right > width && rectF.top < 0 && rectF.bottom > height) {
                    //没有黑边
                    matrix.postTranslate(offSetX, offSetY);
                } else if ((rectF.left > 0 || rectF.right < width) && (rectF.top < 0 && rectF.bottom > height)) {
                    //只有有水平黑边
                    matrix.postTranslate(offSetX * 0.3f, offSetY);
                } else if ((rectF.top > 0 || rectF.bottom < height) && (rectF.left < 0 && rectF.right > width)) {
                    //只有有垂直黑边
                    matrix.postTranslate(offSetX, offSetY * 0.3f);
                } else {
                    //水平垂直黑边都有
                    matrix.postTranslate(offSetX * 0.3f, offSetY * 0.3f);
                }
            } else {
                //根据矩阵获取图片的矩形,来判断左右边是否出现黑边
                RectF rectF = MatrixTool.getRectFByMatrix(this, matrix);
                if (rectF.left > 0 || rectF.right < width) {
                    //有水平黑边,水平慢速移动
                    matrix.postTranslate(offSetX * 0.3f, offSetY * 0.3f);
                } else {
                    matrix.postTranslate(offSetX, offSetY * 0.3f);
                }
            }
            //相对坐标也要更新坐标,因为控件的左上角不会动
            lastY = currentY;
        } else {
            //没有放大,只能水平慢速移动
            matrix.postTranslate(offSetX * 0.4f, 0);
        }
        setImageMatrix(matrix);
        //相对坐标也要更新坐标,因为控件的左上角不会动
        lastX = currentX;
    }

    private void up() {
        //Use property animation,Transition back to the original position
        ValueAnimator valueAnimatorX = null;
        ValueAnimator valueAnimatorY = null;
        AnimatorSet animatorSet = new AnimatorSet();
        RectF rectF = MatrixTool.getRectFByMatrix(this, matrix);

        //缩放后图片的宽高与控件宽高的差值,用于右边和下边有黑边时计算偏移量
        float widthDiffValue = rectF.width() - width;
        float heightDiffValue = rectF.height() - height;

        //如果放大后的高小于控件的高,水平方向根据黑边来判断回弹,垂直方向直接居中
        if (rectF.height() <= height) {
            float targetY = (height - rectF.height()) / 2;
            float startY = MatrixTool.getMatrixValues(MatrixTool.TRANS_Y, matrix);
            //垂直居中的动画
            final float[] lastY = {startY};
            valueAnimatorY = ValueAnimator.ofFloat(startY, targetY);
            valueAnimatorYListener(lastY, valueAnimatorY);

            //判断是否有水平黑边,如果没有无需回弹
            if (rectF.left > 0) {
                //如果是左边有水平黑边
                float startX = MatrixTool.getMatrixValues(MatrixTool.TRANS_X, matrix);
                final float[] lastX = {startX};
                valueAnimatorX = ValueAnimator.ofFloat(startX, 0f);
                valueAnimatorXListener(lastX, valueAnimatorX);
            } else if (rectF.right < width) {
                //如果是右边有水平黑边
                float targetX = -widthDiffValue;
                float startX = MatrixTool.getMatrixValues(MatrixTool.TRANS_X, matrix);
                final float[] lastX = {startX};
                valueAnimatorX = ValueAnimator.ofFloat(startX, targetX);
                valueAnimatorXListener(lastX, valueAnimatorX);
            }
        } else {
            //如果高大于控件的高,对八种可能出现黑边的情况一一判断
            //四边为四种,还有四个角落的组合为4中(即出现两条黑边)

            //顶部黑边
            if (rectF.top > 0 && rectF.left < 0 && rectF.right > width) {
                valueAnimatorY = topBlackBorder();
            }

            //底部黑边
            if (rectF.bottom < height && rectF.left < 0 && rectF.right > width) {
                valueAnimatorY = bottomBlackBorder(heightDiffValue);
            }

            //左部黑边
            if (rectF.left > 0 && rectF.top < 0 && rectF.bottom > height) {
                valueAnimatorX = leftBlackBorder();
            }

            //右部黑边
            if (rectF.right < width && rectF.top < 0 && rectF.bottom > height) {
                valueAnimatorX = rightBlackBorder(widthDiffValue);
            }

            //左上角黑边
            if (rectF.left > 0 && rectF.top > 0 && rectF.bottom > height && rectF.right > width) {
                valueAnimatorY = topBlackBorder();

                valueAnimatorX = leftBlackBorder();
            }

            //右上角黑边
            if (rectF.top > 0 && rectF.bottom > height && rectF.left < 0 && rectF.right < width) {
                valueAnimatorY = topBlackBorder();

                valueAnimatorX = rightBlackBorder(widthDiffValue);
            }

            //左下角黑边
            if (rectF.left > 0 && rectF.bottom < height && rectF.top < 0 && rectF.right > width) {
                valueAnimatorX = leftBlackBorder();

                valueAnimatorY = bottomBlackBorder(heightDiffValue);
            }

            //右下角黑边
            if (rectF.left < 0 && rectF.top < 0 && rectF.bottom < height && rectF.right < width) {
                valueAnimatorX = rightBlackBorder(widthDiffValue);

                valueAnimatorY = bottomBlackBorder(heightDiffValue);
            }
        }

        startAnimator(animatorSet, valueAnimatorX, valueAnimatorY);
    }

    private ValueAnimator leftBlackBorder() {
        float startX = MatrixTool.getMatrixValues(MatrixTool.TRANS_X, matrix);
        final float[] lastX = {startX};
        ValueAnimator valueAnimatorX = ValueAnimator.ofFloat(startX, 0f);
        valueAnimatorXListener(lastX, valueAnimatorX);
        return valueAnimatorX;
    }

    private ValueAnimator rightBlackBorder(float widthDiffValue) {
        float targetX = -widthDiffValue;
        float startX = MatrixTool.getMatrixValues(MatrixTool.TRANS_X, matrix);
        final float[] lastX = {startX};
        ValueAnimator valueAnimatorX = ValueAnimator.ofFloat(startX, targetX);
        valueAnimatorXListener(lastX, valueAnimatorX);
        return valueAnimatorX;
    }

    private ValueAnimator topBlackBorder() {
        float startY = MatrixTool.getMatrixValues(MatrixTool.TRANS_Y, matrix);
        final float[] lastY = {startY};
        ValueAnimator valueAnimatorY = ValueAnimator.ofFloat(startY, 0f);
        valueAnimatorYListener(lastY, valueAnimatorY);
        return valueAnimatorY;
    }

    private ValueAnimator bottomBlackBorder(float heightDiffValue) {
        float targetY = -heightDiffValue;
        float startY = MatrixTool.getMatrixValues(MatrixTool.TRANS_Y, matrix);
        final float[] lastY = {startY};
        ValueAnimator valueAnimatorY = ValueAnimator.ofFloat(startY, targetY);
        valueAnimatorYListener(lastY, valueAnimatorY);
        return valueAnimatorY;
    }

    private void valueAnimatorYListener(final float[] lastY, ValueAnimator valueAnimatorY) {
        valueAnimatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentY = (float) animation.getAnimatedValue();
                float offSetY = currentY - lastY[0];
                matrix.postTranslate(0f, offSetY);
                setImageMatrix(matrix);
                lastY[0] = currentY;
            }
        });
    }

    private void valueAnimatorXListener(final float[] lastX, ValueAnimator valueAnimatorX) {
        valueAnimatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentX = (float) animation.getAnimatedValue();
                float offSetX = currentX - lastX[0];
                matrix.postTranslate(offSetX, 0f);
                setImageMatrix(matrix);
                lastX[0] = currentX;
            }
        });
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        float currentScaleX = MatrixTool.getMatrixValues(MatrixTool.SCALE_X, matrix);
        float currentScaleY = MatrixTool.getMatrixValues(MatrixTool.SCALE_Y, matrix);
        //最大最小值判断,最小缩小到比初始缩放值一半,最大放大到初始缩放值5倍
        if ((initializeScaleX / currentScaleX < 2 && initializeScaleY / currentScaleY < 2 && scaleFactor < 1)
                || (scaleFactor >= 1 && currentScaleY / initializeScaleY < 5 && currentScaleX / initializeScaleX < 5)) {
            //直接用currentScale乘以当前矩阵,因为当前矩阵的XY可能是因为图片太大已经缩放过了的矩阵
            if (scaleFactor < 1) {
                //缩小以控件中心为缩放中心
                matrix.postScale(scaleFactor, scaleFactor, width / 2, height / 2);
            } else {
                //放大以触控点为放大中心
                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            }
            setImageMatrix(matrix);
        }
        //onScale返回true表示总是对此次缩放事件进行消费,不然会出现越放大,放大速度越快
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    /**
     * 在结束缩放时,如果是缩小了,就采用属性动画把当前矩阵变化到初始状态的矩阵,
     * 注意是直接把一个矩阵变化到另外一个矩阵,不用矩阵的乘法运算,
     * 直接使用ObjectAnimator更改矩阵元素的值实现变化.
     */
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        float currentScaleX = MatrixTool.getMatrixValues(MatrixTool.SCALE_X, matrix);
        //如果缩小了,在缩放结束时恢复到初始缩放状态
        if (currentScaleX < initializeScaleX) {
            float[] valuesFrom = new float[9];
            matrix.getValues(valuesFrom);
            ValueAnimator valueAnimator = MatrixTool.matrixToMatrix(new MatrixTool.MatrixEvaluator(),
                    valuesFrom, initializeMatrixValues, 300);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    MatrixTool.MatrixValues matrixValues = (MatrixTool.MatrixValues) animation.getAnimatedValue();
                    matrix.setValues(matrixValues.getValues());
                    setImageMatrix(matrix);
                }
            });
            valueAnimator.start();
        }
    }

    /**
     * 手势监听类,实现双击放大和惯性滑动,用valueAnimator.ofObject来实现
     */
    class MyGestureListener extends SimpleOnGestureListener {
        private float[] fromValues;
        private float[] targetValues;

        /**
         * 会在第二次down之后,up之前触发,中间会触发move事件
         */
        @Override
        public boolean onDoubleTap(final MotionEvent e) {
            //获取双击放大前的矩阵值
            fromValues = new float[9];
            matrix.getValues(fromValues);

            //判断是否已经放大过,如果没有缓慢放大到1.5倍,否则还原
            //只要是放大过的状态双击就直接还原成初始状态
            float currentScaleX = fromValues[Matrix.MSCALE_X];
            float initializeScaleX = initializeMatrixValues[Matrix.MSCALE_X];
            if (currentScaleX == initializeScaleX) {
                //获取双击放大后的矩阵值
                matrix.postScale(1.5f, 1.5f, e.getX(), e.getY());
                targetValues = new float[9];
                matrix.getValues(targetValues);

                //属性动画,实现从矩阵变换到矩阵
                ValueAnimator valueAnimator = MatrixTool.matrixToMatrix(new MatrixTool.MatrixEvaluator(),
                        fromValues, targetValues, 200);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        MatrixTool.MatrixValues currentValues = (MatrixTool.MatrixValues) animation.getAnimatedValue();
                        matrix.setValues(currentValues.getValues());
                        setImageMatrix(matrix);
                    }
                });
                //动画结束时,手动调用Up,使图片根据黑边情况自动居中
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        up();
                    }
                });

                valueAnimator.start();
            } else {
                //还原到初始矩阵,逻辑和放大一样,只需将valuesFrom和targetValues互换
                ValueAnimator valueAnimator = MatrixTool.matrixToMatrix(new MatrixTool.MatrixEvaluator(), fromValues, initializeMatrixValues, 200);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        MatrixTool.MatrixValues currentValues = (MatrixTool.MatrixValues) animation.getAnimatedValue();
                        matrix.setValues(currentValues.getValues());
                        setImageMatrix(matrix);
                    }
                });
                valueAnimator.start();
            }
            return super.onDoubleTap(e);
        }

        /**
         * 用来实现惯性滑动,惯性距离为down事件的坐标到e2(触发onFling的最后一次move事件)的距离
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            RectF rectF = MatrixTool.getRectFByMatrix(CustomDisplayAvatar.this, matrix);
            //缩放后图片的宽高与控件宽高的差值,用于右边和下边有黑边时计算偏移量
            final float widthDiffValue = rectF.width() - width;
            final float heightDiffValue = rectF.height() - height;

            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator valueAnimatorX = null;
            ValueAnimator valueAnimatorY = null;
            //判断是否有水平黑边,如果没有才可以水平惯性滑动
            if (rectF.left < 0 && rectF.right > width) {
                float distanceX = e2.getX() - e1.getX();
                valueAnimatorX = ValueAnimator.ofFloat(0f, distanceX * 2.3f);
                valueAnimatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    private float lastX;

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float currentX = (float) animation.getAnimatedValue();
                        float offSetX = currentX - lastX;
                        matrix.postTranslate(offSetX, 0f);

                        //矩阵平移后是否出现了水平黑边,如果出现了,把矩阵平移回没有黑边
                        float matrixTranslateX = MatrixTool.getMatrixValues(MatrixTool.TRANS_X, matrix);
                        if (matrixTranslateX > 0) {
                            //左边黑边
                            matrix.postTranslate(-matrixTranslateX, 0f);
                        } else if (matrixTranslateX < -widthDiffValue) {
                            //右边黑边
                            matrix.postTranslate(-matrixTranslateX - widthDiffValue, 0f);
                        }

                        setImageMatrix(matrix);
                        lastX = currentX;
                    }
                });

            }
            //是否有垂直黑边,没有才能垂直惯性滑动
            if (rectF.top < 0 && rectF.bottom > height) {
                float distanceY = e2.getY() - e1.getY();
                valueAnimatorY = ValueAnimator.ofFloat(0f, distanceY * 2.3f);
                valueAnimatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    private float lastY;

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float currentY = (float) animation.getAnimatedValue();
                        float offSetY = currentY - lastY;
                        matrix.postTranslate(0f, offSetY);

                        //矩阵平移后是否出现了垂直黑边,如果出现了,把矩阵平移回没有黑边
                        float matrixTranslateY = MatrixTool.getMatrixValues(MatrixTool.TRANS_Y, matrix);
                        if (matrixTranslateY > 0) {
                            //顶部黑边
                            matrix.postTranslate(0f, -matrixTranslateY);
                        } else if (matrixTranslateY < -heightDiffValue) {
                            //底部黑边
                            matrix.postTranslate(0f, -matrixTranslateY - heightDiffValue);
                        }

                        setImageMatrix(matrix);
                        lastY = currentY;
                    }
                });
            }
            animatorSet.setInterpolator(new DecelerateInterpolator());
            startAnimator(animatorSet, valueAnimatorX, valueAnimatorY);
            return super.onFling(e1, e2, velocityX, velocityY);
        }

    }

    private void startAnimator(AnimatorSet animatorSet, ValueAnimator valueAnimatorX, ValueAnimator valueAnimatorY) {
        //开始动画
        animatorSet.setDuration(300);
        if (valueAnimatorY == null) {
            //animatorSet的play方法传入null不会报错,只是会返回null,
            animatorSet.play(valueAnimatorX);
            animatorSet.start();
        } else if (valueAnimatorX == null) {
            animatorSet.play(valueAnimatorY);
            animatorSet.start();
        } else {
            //playTogether传入null会报错
            animatorSet.playTogether(valueAnimatorX, valueAnimatorY);
            animatorSet.start();
        }
    }
}


