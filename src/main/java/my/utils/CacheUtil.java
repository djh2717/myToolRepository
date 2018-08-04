package my.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * A cache util, use the disk cache and memory cache to implement cache frame.
 * notice: The cache value of object need implement Serializable interface.
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
    private static LruCache<String, Object> sLruCache;

    static {
        sContext = MyApplication.getContext();
        // If you do not overwrite the sizeOf method, the cache size is
        // value number, at there is 20.
        sLruCache = new LruCache<>(20);
        try {
            // Initialize disk lru cache, the cache size is 20M.
            sDiskLruCache = DiskLruCache.open(getCacheDir(), getAppVersion(), 1, 20 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the object by key, may be return null.
     */
    public static Object get(String key) {
        Object object = getFromMemory(key);
        if (object == null) {
            object = getFromDisk(key);
        }
        return object;
    }

    /**
     * Cache the value, if success will return true.
     */
    public static boolean put(String key, Object value) {
        if (putToDisk(key, value)) {
            putToMemory(key, value);
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
     * This will delete the all cache.
     */
    public static void deleteAllCache() {
        try {
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
            sDiskLruCache.remove(getKeyByMd5(key));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static Object getFromMemory(String key) {
        return sLruCache.get(key);
    }

    private static void putToMemory(String key, Object object) {
        sLruCache.put(key, object);
    }

    private static Object getFromDisk(String key) {
        ObjectInputStream objectInputStream = null;
        try {
            // Every read operation, will add a read item in journal file.
            DiskLruCache.Snapshot snapshot = sDiskLruCache.get(getKeyByMd5(key));
            if (snapshot != null) {
                objectInputStream = new ObjectInputStream(snapshot.getInputStream(0));
                return objectInputStream.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static boolean putToDisk(String key, Object object) {
        DiskLruCache.Editor editor = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            editor = sDiskLruCache.edit(getKeyByMd5(key));
            objectOutputStream = new ObjectOutputStream(editor.newOutputStream(0));
            objectOutputStream.writeObject(object);
            // If write success, commit the editor, the will let the journal file add
            // a clean item.
            editor.commit();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
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
