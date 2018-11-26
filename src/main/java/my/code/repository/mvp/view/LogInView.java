package my.code.repository.mvp.view;

import my.code.repository.mvp.presenter.LogInPresenter;

/**
 * @author djh on  2018/8/17 20:47
 * @E-Mail 1544579459@qq.com
 */
public interface LogInView extends BaseView<LogInPresenter> {

    /**
     * @param message Log message.
     */
    void logSuccess(String message);

    /**
     * @param message Log message.
     */
    void logFailure(String message);

}
