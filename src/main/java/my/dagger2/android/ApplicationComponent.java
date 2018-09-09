package my.dagger2.android;

import dagger.Component;
import my.utils.MyApplication;

/**
 * @author djh on  2018/8/23 15:53
 * @E-Mail 1544579459@qq.com
 */
@Component(modules = BaseAllActivityModule.class)
public interface ApplicationComponent {

    /**
     * Use to inject.
     *
     * @param myApplication aim inject application.
     */
    void inject(MyApplication myApplication);
}
