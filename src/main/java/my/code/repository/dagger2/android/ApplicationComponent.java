package my.code.repository.dagger2.android;

import dagger.Component;
import my.code.repository.utils.MyApplication;

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
