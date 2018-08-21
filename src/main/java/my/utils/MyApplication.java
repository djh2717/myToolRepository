package my.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
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

        // LitePal init.
        LitePal.initialize(this);


        // Initialize Logger.
        FormatStrategy formatStrategy = PrettyFormatStrategy
                .newBuilder()
                .tag("Log")
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return true;
            }
        });

        // Initialize LeakCanary.
        refWatcher = installRefWatcher();
        //EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
    }

    public static Context getContext() {
        return context;
    }

    public static RefWatcher getRefWatcher() {
        MyApplication myApplication = (MyApplication) getContext();
        return myApplication.refWatcher;
        //在Activity或fragment的onDestroy中如下使用
        //RefWatcher refWatcher = MyApplication.getRefWatcher().watch(this);
    }

//--------------------------------------------------------------------------------------------------

    private RefWatcher installRefWatcher() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            //如果该进程用来给LeakCanary进行堆分析的,则RefWatcher不可用
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }

}
