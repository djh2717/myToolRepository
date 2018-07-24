package my.util;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.litepal.LitePal;

/**
 * @author 15445
 */
public class MyApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    /**
     * Plugin for memory leaks.
     */
    RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(context);
        Logger.init("Log");
        refWatcher = installRefWatcher();
        //EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
    }

    public static Context getContext() {
        return context;
    }

    private RefWatcher installRefWatcher() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            //如果该进程用来给LeakCanary进行堆分析的,则RefWatcher不可用
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }

    public static RefWatcher getRefWatcher() {
        MyApplication myApplication = (MyApplication) getContext();
        return myApplication.refWatcher;
        //在Activity或fragment的onDestroy中如下使用
        //RefWatcher refWatcher = MyApplication.getRefWatcher();
        //refWatcher.watch(this);
    }
}
