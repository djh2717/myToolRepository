package my.code.repository.glide4;

import android.content.Context;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import advanced.nioDemo.R;

/**
 * This is use to setting the glide default config, and also can use to replace
 * the component.
 * If you use another request options this default config still valid.
 * <p>
 * Notice: A model can only have one custom module extends appGlideModule.
 *
 * @author djh on  2018/8/19 15:45
 * @E-Mail 1544579459@qq.com
 */
@GlideModule
public class MyGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // Set disk cache size is 50M.
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, "glide4_cache_dir", 1024 * 1024 * 50));

        // Set the memory cache size is max memory/8.
        long cacheSize = Runtime.getRuntime().maxMemory() / 8;
        builder.setMemoryCache(new LruResourceCache(cacheSize));

        // Set every request default requestOptions.
        builder.setDefaultRequestOptions(
                new RequestOptions()
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.place_holder_image)
                        .error(R.drawable.load_error_image));

        // Set every request default transitionOption, use cross fade animator will let image deform.
        // builder.setDefaultTransitionOptions(Drawable.class, new DrawableTransitionOptions().crossFade(10000));
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // Use to register and replace components.
    }
}
