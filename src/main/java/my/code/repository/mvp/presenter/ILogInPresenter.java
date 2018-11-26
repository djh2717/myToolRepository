package my.code.repository.mvp.presenter;

/**
 * @author djh on  2018/10/16 11:11
 * @E-Mail 1544579459@qq.com
 */
public interface ILogInPresenter {
    void logSuccess(String message);

    void logFailure(String errorMessage);
}
