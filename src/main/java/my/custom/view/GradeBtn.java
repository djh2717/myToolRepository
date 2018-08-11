package my.custom.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;

import my.utils.PxUtil;

/**
 * A grade button, use lottie animator to implement.
 *
 * @author djh on  2018/8/11 22:36
 * @E-Mail 1544579459@qq.com
 */
public class GradeBtn extends LinearLayout {


    /**
     * The fraction array.
     */
    private boolean[] fraction;

    /**
     * The the grade button array.
     */
    private LottieAnimationView[] mGradeLottie;


    public GradeBtn(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        // Init the lottie view.
        initialize(context);
        // Init the fraction array.
        fraction = new boolean[5];
        // Set the listener.
        setListener();
    }

    /**
     * Use to get the grade result, range is 0-5.
     */
    public int getGradeResult() {
        int i = 0;
        for (boolean b : fraction) {
            if (b) {
                i++;
            }
        }
        return i;
    }

    /**
     * Use to set show fractions.
     */
    public void setFraction(int fractions) {
        for (int i = 0; i < fractions; i++) {
            fraction[i] = true;
            mGradeLottie[i].setProgress(1.0f);
        }
    }

    private void initialize(Context context) {
        mGradeLottie = new LottieAnimationView[5];
        for (int i = 0; i < 5; i++) {
            mGradeLottie[i] = new LottieAnimationView(context);
            // Set the json file.
            mGradeLottie[i].setAnimation("grade.json");
            // Set clickable and focusable.
            mGradeLottie[i].setClickable(true);
            mGradeLottie[i].setFocusable(true);
            // Add to the linearLayout.
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(((int) PxUtil.dpToPx(70)), ((int) PxUtil.dpToPx(70)));
            addView(mGradeLottie[i], layoutParams);
        }
    }

    private void setListener() {
        for (int i = 0; i < 5; i++) {
            final int j = i;
            mGradeLottie[j].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fraction[j]) {
                        // If the location is true, let after the location to false.
                        for (int k = j + 1; k < 5; k++) {
                            fraction[k] = false;
                            // Cancel the animator and checked state.
                            mGradeLottie[k].cancelAnimation();
                            mGradeLottie[k].setProgress(0f);
                        }
                        // Before the location resume the animator.
                        for (int k = 0; k <= j; k++) {
                            mGradeLottie[k].resumeAnimation();
                        }
                    } else {
                        for (int k = 0; k <= j; k++) {
                            fraction[k] = true;
                            // Start the checked animator.
                            mGradeLottie[k].playAnimation();
                        }
                    }
                }
            });
        }
    }
}
