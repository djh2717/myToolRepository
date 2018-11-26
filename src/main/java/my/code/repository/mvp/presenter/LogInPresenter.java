package my.code.repository.mvp.presenter;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import my.code.repository.mvp.model.LoginModel;
import my.code.repository.mvp.view.LogInView;

/**
 * @author djh on  2018/8/17 20:52
 * @E-Mail 1544579459@qq.com
 */
public class LogInPresenter extends BasePresenter<LogInView, LoginModel> implements ILogInPresenter {

    private LogInView mLogInView;

    /**
     * Register lifecycle observer and init the mvp view.
     */
    public LogInPresenter(Lifecycle lifecycle, LogInView mvpView) {
        super(lifecycle, mvpView);
        // Init the model.
        mModel = new LoginModel();
    }


    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        // Init logIn view.
        mLogInView = getView();

    }

    public void login(String userName, String passWord) {
        if (userName.length() > 2 && passWord.length() > 2) {
            mModel.startLogin(userName, passWord, this);
        } else {
            mLogInView.logFailure("账号或密码格式错误!");
        }
    }

    @Override
    public void logSuccess(String message) {
//        if (isAtLeastResume()) {
            mLogInView.logSuccess(message);
//        }
    }

    @Override
    public void logFailure(String errorMessage) {
        mLogInView.logFailure(errorMessage);
    }
}
