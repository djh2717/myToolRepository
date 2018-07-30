package my.utils;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Use to viewPager transition animation.
 *
 * @author Djh on 2018/7/18 12:06
 * E-Mail ：1544579459@qq.com
 */
public class PageTransFormerUtil implements ViewPager.PageTransformer {

    /**
     * Transition mode
     */
    public static final int TYPE_ALPHA_SCALE_X_Y = 0;
    public static final int TYPE_ROTATE_PAN = 1;
    public static final int TYPE_PUT_IN = 2;
    public static final int TYPE_ROTATE_SCALE = 3;

    private int transFormerType;

    public PageTransFormerUtil(int transFormerType) {
        this.transFormerType = transFormerType;
    }

    @Override
    public void transformPage(@NonNull View page, float position) {
        switch (transFormerType) {
            case TYPE_ALPHA_SCALE_X_Y:
                typeAlphaScaleXY(page, position);
                break;
            case TYPE_ROTATE_PAN:
                typeRotatePan(page, position);
                break;
            case TYPE_ROTATE_SCALE:
                typeRotateScale(page, position);
                break;
            case TYPE_PUT_IN:
                typePutIn(page, position);
                break;
            default:
        }
    }

    private void typeAlphaScaleXY(View page, float position) {
        int height = page.getHeight();
        int width = page.getWidth();
        if (position >= -1 && position <= 0) {
            page.setScaleY((height + position * 200) / height);
            page.setScaleX((width + position * 200) / width);
            page.setAlpha(1 + position);
        } else if (position >= 0 && position <= 1) {
            page.setScaleY((height - position * 200) / height);
            page.setScaleX((width - position * 200) / width);
            page.setAlpha(1 - position);
        }
    }

    private void typeRotatePan(View page, float position) {
        int height = page.getHeight();
        int width = page.getWidth();
        if (position >= -1 && position <= 0) {
            page.setScaleY((height + position * 460) / height);
            page.setScaleX((width + position * 460) / width);
            page.setTranslationX(position * width / 2);
            page.setRotation(position * 45);
        } else if (position >= 0 && position <= 1) {
            page.setScaleY((height - position * 460) / height);
            page.setScaleX((width - position * 460) / width);
            page.setTranslationX(position * width / 2);
            page.setRotation(position * 45);
        }
    }

    private void typeRotateScale(View page, float position) {
        int height = page.getHeight();
        int width = page.getWidth();
        if (position <= -1) {
            page.setAlpha(0);
        } else if (position > -1 && position <= 0) {
            page.setScaleY((height + position * 1000) / height);
            page.setScaleX((width + position * 1000) / width);
            //Counteract default horizontal displacement
            page.setTranslationX(-position * width);

            page.setRotation(position * 360);
            page.setAlpha(1 + position);
        }
    }

    private void typePutIn(View page, float position) {
        int height = page.getHeight();
        int width = page.getWidth();
        if (position <= -1) {
            page.setAlpha(0);
        } else if (position > -1 && position <= 0) {
            page.setAlpha(1);
            page.setScaleY((height + position * 460) / height);
            page.setScaleX((width + position * 460) / width);
            //Counteract default horizontal displacement
            page.setTranslationX(-position * width);
        } else if (position > 0 && position < 1) {
            page.setAlpha(1);
            page.setScaleY((height + position * 1500) / height);
            page.setScaleX((width + position * 1500) / width);
            //Counteract default horizontal displacement
            page.setTranslationX(-position * width);
            page.setAlpha(1 - position);
        } else if (position >= 1) {
            page.setAlpha(0);
        }
    }
}
