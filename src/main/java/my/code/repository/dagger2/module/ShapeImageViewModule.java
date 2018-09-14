package my.code.repository.dagger2.module;

import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import dagger.Module;
import dagger.Provides;

/**
 * @author djh on  2018/8/21 11:49
 * @E-Mail 1544579459@qq.com
 */
@Module
public class ShapeImageViewModule {

    @Provides
    public Rect providesRect() {
        return new Rect();
    }

    @Provides
    public RectF providesRectF() {
        return new RectF();
    }

    @Provides
    public Paint providesPaint() {
        return new Paint();
    }

    @Provides
    public PorterDuffXfermode providesXfermode() {
        return new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }
}
