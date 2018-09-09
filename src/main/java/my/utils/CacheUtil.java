package my.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A cache util, use the disk cache and memory cache to implement cache frame, the
 * cache frame is only cache the bitmap.
 *
 * @author djh on  2018/7/31 15:53
 * @E-Mail 1544579459@qq.com
 */
public class CacheUtil {


    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    /**
     * Disk cache.
     */
    private static DiskLruCache sDiskLruCache;

    /**
     * Memory cache.
     */
    private static LruCache<String, Bitmap> sLruCache;

    /**
     * Use to automatic clear cache, if open, automatically clear the
     * memory and disk cache every ten minutes.
     */
    private static ScheduledExecutorService sAutomaticTimingClearCache;

    private static final String OPEN_AUTO_CLEAR_CACHE = "autoClearCache";

    static {
        sContext = MyApplication.getContext();
        // If you do not overwrite the sizeOf method, the cache size is
        // value number, we main cache the bitmap, so need overwrite the sizeOf method.
        sLruCache = new LruCache<String, Bitmap>((int) ((Runtime.getRuntime().maxMemory()) / 8)) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        try {
            // Initialize disk lru cache, the cache size is 30M
            sDiskLruCache = DiskLruCache.open(getCacheDir(), getAppVersion(), 1, 30 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Start auto clear cache, if it is opened.
        startAutoClear();
    }

    /**
     * You should call this when the activity is destroy, avoid leak instance by
     * the ScheduledExecutorService.
     */
    public static void exitScheduledClear() {
        stopAutoClear();
    }

    /**
     * If you need auto clare cache, call this, then will ten minutes clear once.
     */
    public static void openAutoClearCache() {
        SharePrefUtil.put().putBoolean(OPEN_AUTO_CLEAR_CACHE, true).apply();
        startAutoClear();
    }

    /**
     * Close auto clear cache, and shutdownNow the ScheduledExecutorService.
     */
    public static void closeAutoClearCache() {
        SharePrefUtil.put().putBoolean(OPEN_AUTO_CLEAR_CACHE, false).apply();
        stopAutoClear();
    }

    /**
     * Get the object by key, may be return null.
     */
    public static Bitmap get(String key) {
        Bitmap bitmap = getFromMemory(key);
        if (bitmap == null) {
            bitmap = getFromDisk(key);
        }
        return bitmap;
    }

    /**
     * Cache the value, if success will return true.
     */
    public static boolean put(String key, Bitmap bitmap) {
        if (putToDisk(key, bitmap)) {
            putToMemory(key, bitmap);
            return true;
        }
        return false;
    }

    /**
     * This method will sync the operation to journal, only need call this at the
     * activity onPause. High frequency call this method will create frequency
     * io operation, it will affect performance.
     */
    public static void flush() {
        try {
            sDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method need call at the activity onDestroy, after call this method, you
     * can not call any cache method.
     */
    public static void close() {
        try {
            sDiskLruCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the all data size by byte that is cache in the cacheDir.
     */
    public static long getCacheSize() {
        return sDiskLruCache.size();
    }

    /**
     * This will delete the all memory and disk cache.
     */
    public static void clearAllCache() {
        try {
            sLruCache.evictAll();
            sDiskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove the specific cache by the key, this usually is not need to call, the
     * cache size never exceed max cache size.
     */
    public static void remove(String key) {
        try {
            sLruCache.remove(key);
            sDiskLruCache.remove(getKeyByMd5(key));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ------------------ Internal API ------------------


    private static void startAutoClear() {
        if (SharePrefUtil.get().getBoolean(OPEN_AUTO_CLEAR_CACHE, false)) {
            if (sAutomaticTimingClearCache == null) {
                sAutomaticTimingClearCache = Executors.newScheduledThreadPool(1);
            }
            // Start rate clear the cache, once every ten minutes.
            sAutomaticTimingClearCache.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    clearAllCache();
                }
            }, 10, 10, TimeUnit.MINUTES);
        }
    }

    private static void stopAutoClear() {
        if (sAutomaticTimingClearCache != null) {
            sAutomaticTimingClearCache.shutdownNow();
            sAutomaticTimingClearCache = null;
        }
    }


    private static Bitmap getFromMemory(String key) {
        return sLruCache.get(key);
    }

    private static void putToMemory(String key, Bitmap bitmap) {
        sLruCache.put(key, bitmap);
    }

    private static Bitmap getFromDisk(String key) {
        InputStream inputStream = null;
        try {
            // Every read operation, will add a read item in journal file.
            DiskLruCache.Snapshot snapshot = sDiskLruCache.get(getKeyByMd5(key));
            if (snapshot != null) {
                inputStream = snapshot.getInputStream(0);
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static boolean putToDisk(String key, Bitmap bitmap) {
        DiskLruCache.Editor editor = null;
        OutputStream outputStream = null;
        try {
            editor = sDiskLruCache.edit(getKeyByMd5(key));
            outputStream = editor.newOutputStream(0);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            // If write success, commit the editor, the will let the journal file add
            // a clean item.
            editor.commit();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            // If fail, abort the editor, the will let journal file add a remove item.
            if (editor != null) {
                editor.abort();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static File getCacheDir() {
        String cachePath;
        // Judge the external storage whether available.
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = Objects.requireNonNull(sContext.getExternalCacheDir()).getPath();
        } else {
            cachePath = sContext.getCacheDir().getPath();
        }
        File cacheDir = new File(cachePath + File.separator + "CacheUtil");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    private static int getAppVersion() {
        try {
            PackageInfo packageInfo = sContext.getPackageManager()
                    .getPackageInfo(sContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * According to url,use MD5 encryption algorithm to get unique Key.
     */
    private static String getKeyByMd5(String url) {
        String key;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            key = md5Encryption(messageDigest.digest());
            return key;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            key = String.valueOf(url.hashCode());
        }
        return key;
    }

    /**
     * MD5 encryption algorithm.
     */
    private static String md5Encryption(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
