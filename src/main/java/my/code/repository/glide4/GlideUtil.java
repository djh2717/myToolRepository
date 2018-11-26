package my.code.repository.glide4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.Nullable;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import my.code.repository.utils.MyApplication;
import my.code.repository.utils.PxUtil;

/**
 * Glide util.
 *
 * @author djh on  2018/8/19 18:02
 * @E-Mail 1544579459@qq.com
 */
@SuppressWarnings("ALL")
public class GlideUtil {
    @SuppressLint("StaticFieldLeak")
    private static final Context CONTEXT = MyApplication.getContext();

    /**
     * Use to load imageView src.
     */
    @SuppressLint("CheckResult")
    public static void load(String url, ImageView imageView, @Nullable RequestOptions requestOptions) {
        getRequestBuilder(url, requestOptions).into(imageView);
    }

    /**
     * Use to load imageView background.
     */
    @SuppressLint("CheckResult")
    public static void loadBackground(String url, final ImageView imageView, @Nullable RequestOptions requestOptions) {
//        getRequestBuilder(url, requestOptions).into(new SimpleTarget<Drawable>() {
//
//            @Override
//            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                imageView.setBackground(resource);
//            }
//        });
    }

    /**
     * Use to load round image.
     */
    @SuppressLint("CheckResult")
    public static void roundLoad(String url, ImageView imageView, @Nullable RequestOptions requestOptions) {
        if (requestOptions != null) {
            requestOptions.transform(new RoundedCornersTransformation((int) PxUtil.dpToPx(10), 0));
            getRequestBuilder(url, requestOptions).into(imageView);
        } else {
            RequestOptions requestOption = new RequestOptions();
            requestOption.transform(new RoundedCornersTransformation((int) PxUtil.dpToPx(10), 0));
            getRequestBuilder(url, requestOption).into(imageView);
        }
    }

    /**
     * Use to load circle image.
     */
    @SuppressLint("CheckResult")
    public static void circleLoad(String url, ImageView imageView, @Nullable RequestOptions requestOptions) {
        if (requestOptions != null) {
            requestOptions.circleCrop();
            getRequestBuilder(url, requestOptions).into(imageView);
        } else {
            RequestOptions requestOption = new RequestOptions();
            requestOption.circleCrop();
            getRequestBuilder(url, requestOption).into(imageView);
        }
    }

    /**
     * Use to pre load.
     */
    public static void preLoad(String url) {
        getRequestBuilder(url, null).preload();
    }

    @SuppressLint("CheckResult")
    private static RequestBuilder getRequestBuilder(String url, RequestOptions requestOptions) {
        RequestManager requestManager = Glide.with(CONTEXT);
        RequestBuilder requestBuilder = requestManager.load(url);
        if (requestOptions != null) {
            requestBuilder.apply(requestOptions);
        }
        return requestBuilder;
    }
}
