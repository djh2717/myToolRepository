package my.code.repository.dagger2.module;

import android.graphics.Paint;
import android.graphics.RectF;

import dagger.Module;
import dagger.Provides;

/**
 * @author djh on  2018/8/21 16:17
 * @E-Mail 1544579459@qq.com
 */
@Module
public class LoadingViewModule {

    @Provides
    public Paint providesPaint() {
        return new Paint();
    }

    @Provides
    public RectF providesRectF() {
        return new RectF();
    }
}
