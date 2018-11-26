package my.code.repository.mvp.model;

import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import my.code.repository.mvp.presenter.ILogInPresenter;

/**
 * @author djh on  2018/10/16 11:01
 * @E-Mail 1544579459@qq.com
 */
public class LoginModel extends BaseModel {

    private Handler mHandler;
    private ExecutorService cachePool;

    private int i = 0;


    public LoginModel() {
        mHandler = new Handler();
    }

    public void startLogin(String account, String passWord, ILogInPresenter iLogInPresenter) {
        i++;
        cachePool = Executors.newCachedThreadPool();
        if ("djh".equals(account) && "1234".equals(passWord)) {
            // Simulate net work.
            cachePool.execute(() -> {
                try {
                    Thread.sleep(3000);
                    mHandler.post(() -> iLogInPresenter.logSuccess("登录成功!" + i));
                    cachePool.shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    mHandler.post(() -> iLogInPresenter.logFailure("登录失败,未知错误"));
                }
            });
        } else {
            iLogInPresenter.logFailure("账号或密码错误!");
        }
    }
}
