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

import jigsaw.puzzle.R;

/**
 * A score component,implement by AppCompatCheckBox,direct use in xml.
 * Usually use the five same this.
 * fixme: Must set the location,mScoreBoolean and other estimateCheckBox.
 *
 * @author djh
 */
public class EstimateCheckBox extends AppCompatCheckBox {

    /**
     * This location in the estimateCheckBoxes.
     */
    public int location;

    private boolean[] mScoreBoolean;

    private RectF mRectF;
    private Paint mPaint;
    private Bitmap mCheckBitmap;
    private Bitmap mNotCheckBitmap;

    /**
     * Other estimateCheckBox,usually is five.
     */
    private EstimateCheckBox[] mEstimateCheckBox;

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
        if (mEstimateCheckBox != null && mScoreBoolean != null
                && mEstimateCheckBox.length == mScoreBoolean.length) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    //Can not set your location as true,otherwise after
                    //trigger onCheck it again become false.
                    for (int j = 0; j < location; j++) {
                        mScoreBoolean[j] = true;
                    }

                    for (int j = location; j < mScoreBoolean.length; j++) {
                        mScoreBoolean[j] = false;
                    }
                    //According to the scoreBoolean values,set the estimateCheckBox state.
                    for (int j = 0; j < mScoreBoolean.length; j++) {
                        mEstimateCheckBox[j].setChecked(mScoreBoolean[j]);
                    }
                    //After set checked,set you location boolean as true.
                    mScoreBoolean[location] = true;
                    break;
                default:
                    break;
            }
        } else {
            throw new RuntimeException("EstimateCheckBox is not work correct");
        }
        return super.onTouchEvent(event);
    }

    public void setEstimateCheckBox(EstimateCheckBox[] estimateCheckBox) {
        mEstimateCheckBox = estimateCheckBox;
    }

    public void setScoreBoolean(boolean[] scoreBoolean) {
        mScoreBoolean = scoreBoolean;
    }
}
