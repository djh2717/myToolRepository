package my.code.repository.dagger2.android;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.two.dagger.android.activity.AndroidActivity;
import dagger.two.dagger.android.module.AndroidActivityModule;

/**
 * @author djh on  2018/8/23 15:53
 * @E-Mail 1544579459@qq.com
 */
@Module
abstract class BaseAllActivityModule {

    /**
     * This is example for use dagger2 android, when you create a new activity, you
     * only need install the module at here.
     */
    @ContributesAndroidInjector(modules = AndroidActivityModule.class)
    abstract AndroidActivity contributeAndroidActivityInjector();
}
