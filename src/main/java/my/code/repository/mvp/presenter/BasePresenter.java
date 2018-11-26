package my.code.repository.mvp.presenter;


import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import my.code.repository.mvp.model.BaseModel;
import my.code.repository.mvp.view.BaseView;


/**
 * @author djh on  2018/8/17 21:00
 * @E-Mail 1544579459@qq.com
 */
@SuppressWarnings("ALL")
public abstract class BasePresenter<V extends BaseView, M extends BaseModel> implements LifecycleObserver {

    /**
     * Activity or fragment lifecycle, use weak reference package.
     */
    private Lifecycle mLifecycle;

    /**
     * Mvp view weak reference.
     */
    private WeakReference<V> mViewWeakReference;


    protected M mModel;

    /**
     * Register lifecycle observer and init the mvp view.
     */
    BasePresenter(Lifecycle lifecycle, V mvpView) {
        // Register lifecycle observer.
        lifecycle.addObserver(this);

        // Init lifecycle.
        WeakReference<Lifecycle> lifecycleWeakReference = new WeakReference<>(lifecycle);
        mLifecycle = lifecycleWeakReference.get();

        // Init mvp view.
        mViewWeakReference = new WeakReference<>(mvpView);
    }

    /**
     * Use to get mvp view.
     */
    V getView() {
        return mViewWeakReference == null ? null : mViewWeakReference.get();
    }

    /**
     * Use to judge the mvp view is attach.
     */
    boolean isAttachView() {
        return mViewWeakReference != null && mViewWeakReference.get() != null;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate(@NonNull LifecycleOwner owner) {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart(@NonNull LifecycleOwner owner) {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(@NonNull LifecycleOwner owner) {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause(@NonNull LifecycleOwner owner) {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop(@NonNull LifecycleOwner owner) {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(@NonNull LifecycleOwner owner) {
        //Use to remove mvp view, to avoid leak memory.
        mViewWeakReference.clear();
        // Clean the lifecycle.
        mLifecycle.removeObserver(this);
        mLifecycle = null;
    }

    boolean isAtLeastCreate() {
        return mLifecycle.getCurrentState().isAtLeast(Lifecycle.State.CREATED);
    }

    boolean isAtLeastResume() {
        return mLifecycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
    }

    boolean isAtLeastDestroy() {
        return mLifecycle.getCurrentState().isAtLeast(Lifecycle.State.DESTROYED);
    }

}
