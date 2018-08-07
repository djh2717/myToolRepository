package my.network.listener;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import my.demo.two.R;
import my.utils.ActivityUtil;
import my.utils.NetWorkUtil;
import my.utils.PxUtil;

/**
 * This is a net work listener activity, has a inner broadcast receiver, when the net work
 * state change, will show a hint view at the top of activity, if you set a anchor view,
 * the hint view will location at the anchor view top.
 *
 * @author djh on 2018-8-1 13:06:23
 */
@SuppressLint("Registered")
public class NetWorkStateListenerActivity extends AppCompatActivity {

    /**
     * A interface call back use to obtain anchor view.
     */
    private AnchorViewInterface mAnchorViewInterface;

    /**
     * A call back when net work available.
     */
    private NetWorkAvailable mNetWorkAvailable;

    /**
     * A call back when net work unavailable.
     */
    private NetWorkUnavailable mNetWorkUnavailable;

    /**
     * The net work broadcast listener.
     */
    private NetWorkStateBroadcast mNetWorkStateBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the net work listener broadcast.
        mNetWorkStateBroadcast = register();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the broadcast.
        if (mNetWorkStateBroadcast != null) {
            unRegister(mNetWorkStateBroadcast);
        }
    }

    /**
     * When the activity is finish, must remove the hint view, otherwise will leak
     * a window, because all view or windows, is rely on activity.
     */
    @Override
    public void finish() {
        mNetWorkStateBroadcast.removeHintView();
        super.finish();
    }

    private NetWorkStateBroadcast register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        NetWorkStateBroadcast netWorkStateBroadcast = new NetWorkStateBroadcast();
        registerReceiver(netWorkStateBroadcast, intentFilter);

        return netWorkStateBroadcast;
    }

    private void unRegister(NetWorkStateBroadcast netWorkStateBroadcast) {
        unregisterReceiver(netWorkStateBroadcast);
    }

    /**
     * Set the hint view aim anchor view, the hint view top will anchor the aim
     * view top, then the aim anchor view will move down a hint view height(43dp),
     * this is only adapter when the anchor view top layout params is margin.
     */
    public final void setAnchorViewOfHintView(AnchorViewInterface anchorViewInterface) {
        mAnchorViewInterface = anchorViewInterface;
    }

    /**
     * Use to listener net work available.
     */
    public final void setNetWorkAvailableListener(NetWorkAvailable netWorkAvailable) {
        mNetWorkAvailable = netWorkAvailable;
    }

    /**
     * Use to listener net work  unAvailable.
     */
    public final void setNetWorkUnavailableListener(NetWorkUnavailable netWorkUnavailable) {
        mNetWorkUnavailable = netWorkUnavailable;
    }

    /**
     * notice: Can only be dynamically registered after 7.0
     * This action is use to static register.
     * <action android:name="android.NET.conn.CONNECTIVITY_CHANGE" />
     */
    public class NetWorkStateBroadcast extends BroadcastReceiver {
        private View mHintView;

        /**
         * The hint view aim anchor view, the hint view will location at the anchor
         * view top.
         */
        private View mAnchorView;
        private int mHintViewHeight;
        private boolean mHasAddHintView = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            // When the net work state change, this will be call.
            boolean isConnect = NetWorkUtil.isNetWorkConnected();
            if (isConnect) {
                removeHintView();
                // If client set the listener, call back it.
                if (mNetWorkAvailable != null) {
                    mNetWorkAvailable.isAvailable();
                }
            } else {
                if (!mHasAddHintView) {
                    addHintView();
                }
                // If client set the listener, call back it.
                if (mNetWorkUnavailable != null) {
                    mNetWorkUnavailable.unAvailable();
                }
            }
        }

        @SuppressLint("InflateParams")
        private void addHintView() {
            mHasAddHintView = true;

            final WindowManager windowManager = getWindowManager();
            WindowManager.LayoutParams layoutParams = getLayoutParam();
            mHintView = getLayoutInflater().inflate(R.layout.net_work_not_connect_hint, null);
            windowManager.addView(mHintView, layoutParams);

            // Set some listener of the hint view.
            setListener(windowManager);
        }

        private void setListener(final WindowManager windowManager) {
            // When use click the hint view, jump to internet setting interface.
            mHintView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtil.startHermitActivity(NetWorkStateListenerActivity.this,
                            Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                }
            });

            // This listener is use to anchor the hint view to aim view top, and
            // then move down the aim view a hint view height.
            mHintView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mAnchorViewInterface != null) {
                        // Get the anchor view.
                        mAnchorView = mAnchorViewInterface.getAnchorView();
                        // Get the anchor view top.
                        int anchorViewTop = mAnchorView.getTop();

                        // Let hint view top anchor the anchor view top.
                        WindowManager.LayoutParams hintViewLayoutParams = (WindowManager.LayoutParams) mHintView.getLayoutParams();
                        hintViewLayoutParams.y = anchorViewTop;
                        windowManager.updateViewLayout(mHintView, hintViewLayoutParams);

                        // Get the hint view height, it is fixed value 43dp.
                        mHintViewHeight = (int) PxUtil.dpToPx(43);

                        // Add the anchor view top margin of a hint view height.
                        ViewGroup.MarginLayoutParams anchorViewLayoutParams = (ViewGroup.MarginLayoutParams) mAnchorView.getLayoutParams();
                        anchorViewLayoutParams.topMargin = mHintViewHeight + anchorViewLayoutParams.topMargin;
                        mAnchorView.setLayoutParams(anchorViewLayoutParams);

                        // After set the anchor view, remove the listener.
                        mHintView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        /**
         * When net work connect, remove hint view.
         */
        private void removeHintView() {
            WindowManager windowManager = getWindowManager();
            if (mHintView != null && mHasAddHintView) {
                mHasAddHintView = false;
                windowManager.removeView(mHintView);
                mHintView = null;
                // Let the anchor view minus a hint view height.
                if (mAnchorView != null) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mAnchorView.getLayoutParams();
                    marginLayoutParams.topMargin = marginLayoutParams.topMargin - mHintViewHeight;
                    mAnchorView.setLayoutParams(marginLayoutParams);
                    mAnchorView = null;
                }
            }
        }

        private WindowManager.LayoutParams getLayoutParam() {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

            layoutParams.gravity = Gravity.TOP;
            layoutParams.format = PixelFormat.TRANSPARENT;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            return layoutParams;
        }
    }

    public interface AnchorViewInterface {
        /**
         * Get the hint view aim anchor view.
         *
         * @return The anchor view.
         */
        View getAnchorView();
    }

    public interface NetWorkAvailable {

        /**
         * This will call back when net work available.
         */
        void isAvailable();
    }

    public interface NetWorkUnavailable {
        /**
         * This will call back when net work unavailable.
         */
        void unAvailable();
    }
}
