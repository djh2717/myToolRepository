package my.utils;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

import java.util.Arrays;

/**
 * Use to operation matrix.
 *
 * @author 15445
 */
public class MatrixUtil {

    public static final int SCALE_X = 0;
    public static final int SCALE_Y = 4;

    public static final int TRANS_X = 2;
    public static final int TRANS_Y = 5;

    public static void showMatrixValues(Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(0);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                stringBuilder.append(String.valueOf(values[i * 3 + j])).append("   ");
            }
            stringBuilder.append("\n");
        }
        Logger.d(stringBuilder);
    }

    public static float getMatrixValues(int valuesType, Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        switch (valuesType) {
            case SCALE_X:
                return values[Matrix.MSCALE_X];
            case SCALE_Y:
                return values[Matrix.MSCALE_Y];
            case TRANS_X:
                return values[Matrix.MTRANS_X];
            case TRANS_Y:
                return values[Matrix.MTRANS_Y];
            default:
        }
        return 0;
    }

    /**
     * 根据变化后的矩阵,获得一个RectF,来获取图片所处的位置
     */
    public static RectF getRectFByMatrix(AppCompatImageView view, Matrix matrix) {
        RectF rectF = new RectF();
        Drawable drawable = view.getDrawable();
        if (drawable != null) {
            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    /**
     * 采用objectAnimator实现一个矩阵变化到另外一个矩阵
     */
    public static ValueAnimator matrixToMatrix(TypeEvaluator typeEvaluator, float[] matrixValuesFrom,
                                               float[] matrixValuesTo, int duration) {
        MatrixValues matrixFrom = new MatrixValues(matrixValuesFrom);
        MatrixValues matrixTo = new MatrixValues(matrixValuesTo);
        ValueAnimator valueAnimator = ValueAnimator.ofObject(typeEvaluator, matrixFrom, matrixTo);
        valueAnimator.setDuration(duration);
        return valueAnimator;
    }

    /**
     * 用来实现对图片不失真来适应ImageView控件,只有自绘控件才会用到,
     * 官方ImageView有直接的实现,自绘控件要实现对图片的处理必须设置ScaleType为Matrix
     * 所以要手动处理Matrix.
     */
    public static Matrix getAdapterImageViewMatrix(ImageView imageView) {
        float scaleX = 1f;
        float scaleY = 1f;
        float width = imageView.getMeasuredWidth();
        float height = imageView.getMeasuredHeight();
        Drawable drawable = imageView.getDrawable();
        //如果drawable为空,说明此时未设置图片,返回一个初始矩阵即可
        if (drawable == null) {
            return new Matrix();
        }
        float drawableWidth = drawable.getIntrinsicWidth();
        float drawableHeight = drawable.getIntrinsicHeight();
        Matrix matrix = new Matrix();

        //思考后发现,此方式缩放图片无需考虑图片大小,可以让任何图片实现保持图片宽高比来适应屏幕
        //实现思路是在控件的宽高范围内寻找两个最大的数,让其相除约等于图片的宽高比例
        boolean ok = false;
        for (float i = height; i >= 0; i--) {
            for (float j = width; j >= 0; j--) {
                if (Math.abs((i / j) - (drawableHeight * 1.0f / drawableWidth * 1.0f)) < 0.01) {
                    scaleX = j / drawableWidth * 1.0f;
                    scaleY = i / drawableHeight * 1.0f;
                    ok = true;
                    break;
                }
            }
            if (ok) {
                break;
            }
        }
        //适应控件并且保持图片比例缩放
        matrix.preScale(scaleX, scaleY, width / 2, height / 2);
        //平移到中央,要以drawableXXX来计算偏移量
        matrix.preTranslate((width - drawableWidth) / 2, (height - drawableHeight) / 2);

        return matrix;
    }

    /**
     * The entity used to implement the matrix change to the matrix.
     */
    public static class MatrixValues {
        private float[] values;

        MatrixValues(float[] values) {
            this.values = values;
        }

        public float[] getValues() {
            return values;
        }
    }

    /**
     * TypeEvaluator implement matrix transformation to matrix.
     * Only pan and zoom changes are implemented.
     *
     * @author 15445
     */
    public static class MatrixEvaluator implements TypeEvaluator<MatrixValues> {

        @Override
        public MatrixValues evaluate(float fraction, MatrixValues startValue, MatrixValues endValue) {
            float[] startValues = startValue.getValues();
            float[] endValues = endValue.getValues();
            float[] currentValues = Arrays.copyOf(startValues, 9);

            currentValues[Matrix.MSCALE_X] = startValues[Matrix.MSCALE_X] +
                    (endValues[Matrix.MSCALE_X] - startValues[Matrix.MSCALE_X]) * fraction;

            currentValues[Matrix.MSCALE_Y] = startValues[Matrix.MSCALE_Y] +
                    (endValues[Matrix.MSCALE_Y] - startValues[Matrix.MSCALE_Y]) * fraction;

            currentValues[Matrix.MTRANS_X] = startValues[Matrix.MTRANS_X] +
                    (endValues[Matrix.MTRANS_X] - startValues[Matrix.MTRANS_X]) * fraction;

            currentValues[Matrix.MTRANS_Y] = startValues[Matrix.MTRANS_Y] +
                    (endValues[Matrix.MTRANS_Y] - startValues[Matrix.MTRANS_Y]) * fraction;

            return new MatrixValues(currentValues);
        }
    }
}
