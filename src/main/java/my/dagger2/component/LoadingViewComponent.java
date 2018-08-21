package my.dagger2.component;

import dagger.Component;
import my.custom.view.LoadingView;
import my.dagger2.module.LoadingViewModule;

/**
 * @author djh on  2018/8/21 16:16
 * @E-Mail 1544579459@qq.com
 */
@Component(modules = LoadingViewModule.class)
public interface LoadingViewComponent {

    /**
     * Use to inject.
     *
     * @param loadingView the aim host inject view.
     */
    void inject(LoadingView loadingView);
}
