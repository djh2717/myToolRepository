package my.code.repository.dagger2.component;

import dagger.Component;
import my.code.repository.custom.view.LoadingView;
import my.code.repository.dagger2.module.LoadingViewModule;

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
