package my.custom.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import android.view.MotionEvent;

import advanced.nioDemo.R;

/**
 * A score component,implement by AppCompatCheckBox,direct use in xml.
 * Usually use the five same this.
 * notice: Must set the mLocation,mScoreBoolean and other estimateCheckBox.
 *
 * @author djh
 */
public class EstimateCheckBox extends AppCompatCheckBox {

    /**
     * This location in the estimateCheckBoxes.
     */
    public int location;
    public boolean[] scoreBoolean;

    /**
     * Other estimateCheckBox,usually is five.
     */
    public EstimateCheckBox[] estimateCheckBoxes;

    private RectF mRectF;
    private Paint mPaint;
    private Bitmap mCheckBitmap;
    private Bitmap mNotCheckBitmap;


    public EstimateCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCheckBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grade2);
        mNotCheckBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grade);
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mRectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mRectF.top = 0;
        mRectF.left = 0;
        mRectF.right = getMeasuredWidth();
        mRectF.bottom = getMeasuredHeight();
        if (isChecked()) {
            canvas.drawBitmap(mCheckBitmap, null, mRectF, mPaint);
        } else {
            canvas.drawBitmap(mNotCheckBitmap, null, mRectF, mPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (estimateCheckBoxes != null && scoreBoolean != null
                && estimateCheckBoxes.length == scoreBoolean.length) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    //Can not set your mLocation as true,otherwise after
                    //trigger onCheck it again become false.
                    for (int j = 0; j < location; j++) {
                        scoreBoolean[j] = true;
                    }

                    for (int j = location; j < scoreBoolean.length; j++) {
                        scoreBoolean[j] = false;
                    }
                    //According to the scoreBoolean values,set the estimateCheckBox state.
                    for (int j = 0; j < scoreBoolean.length; j++) {
                        estimateCheckBoxes[j].setChecked(scoreBoolean[j]);
                    }
                    //After set checked,set you location boolean as true.
                    scoreBoolean[location] = true;
                    break;
                default:
                    break;
            }
        } else {
            throw new RuntimeException("EstimateCheckBox is not work correct");
        }
        return super.onTouchEvent(event);
    }
}
