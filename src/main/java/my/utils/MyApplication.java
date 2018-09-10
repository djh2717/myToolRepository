package my.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import event.bus.MyEventBusIndex;
import my.dagger2.android.DaggerApplicationComponent;

/**
 * @author 15445
 */
public class MyApplication extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> mDispatchingAndroidInjector;

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    /**
     * Plugin for memory leaks.
     */
    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        // Init the dagger android.
        DaggerApplicationComponent.create().inject(this);

        context = getApplicationContext();

        // LitePal init.
        LitePal.initialize(this);

        // Logger init.
        initLogger();

        // Initialize LeakCanary.
        refWatcher = installRefWatcher();

        // EventBus index.
        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
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

    /**
     * Use to inject rely at activity.
     */
    @Override
    public AndroidInjector<Activity> activityInjector() {
        return mDispatchingAndroidInjector;
    }

    // ------------------ Internal API ------------------

    private void initLogger() {
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
    }

    private RefWatcher installRefWatcher() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            //如果该进程用来给LeakCanary进行堆分析的,则RefWatcher不可用
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }

}
