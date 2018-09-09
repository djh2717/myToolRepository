package my.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * A bitmap util, use to compress bitmap and other operation.
 *
 * @author djh
 */
public class BitmapUtil {

    public static Bitmap decodeResources(int resId, int desireWidth, int desireHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(MyApplication.getContext().getResources(), resId, options);
        options.inSampleSize = getInSampleSize(options, desireWidth, desireHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(MyApplication.getContext().getResources(), resId, options);
    }


    public static Bitmap decodeFile(String pathName, int desireWidth, int desireHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = getInSampleSize(options, desireWidth, desireHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    /**
     * 对于从流中加载的Bitmap,一般都是直接从网络上获取,所以考虑使用临时文件的方式更合理
     * 如果不采用临时文件,在真正加载到内存之前,会解析一次,然后压缩优化后又会解析一次,
     * 使用了两次inputStream,需要进行mark和reset处理,而且第二次加载又是从网络获取
     * 会浪费流量而且费时.
     */
    public static Bitmap decodeStream(InputStream inputStream, int desireWidth, int desireHeight) {
        //创建临时文件,把图片加载到临时文件中保存
        File tempFile = null;
        BufferedInputStream bufferedInputStream;
        BufferedOutputStream bufferedOutputStream;
        try {
            tempFile = File.createTempFile("temp", null);
            tempFile.deleteOnExit();
            bufferedInputStream = new BufferedInputStream(inputStream);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
            int hasRed;
            byte[] bytes = new byte[1024];
            while ((hasRed = bufferedInputStream.read(bytes)) > -1) {
                bufferedOutputStream.write(bytes, 0, hasRed);
            }
            bufferedOutputStream.flush();

            bufferedInputStream.close();
            bufferedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //从临时文件中加载图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (tempFile != null) {
            BitmapFactory.decodeFile(tempFile.getPath(), options);
        }
        options.inSampleSize = getInSampleSize(options, desireWidth, desireHeight);
        options.inJustDecodeBounds = false;
        //前面已经调用过一次BitmapFactory.decodeStream,此时stream流已经发生了改变,
        //要调用inputStream.reset方法才能再次解析,否则会返回null
        //inputStream没有实现mark和reset,要通过其子类来实现,

        return BitmapFactory.decodeFile(Objects.requireNonNull(tempFile).getPath(), options);
    }

    public static Bitmap decodeByteArray(byte[] bytes, int desireWidth, int desireHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inSampleSize = getInSampleSize(options, desireWidth, desireHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(MyApplication.getContext().getResources(), bitmap);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        return bitmapDrawable.getBitmap();
    }

    /**
     * This method is use to transform the vector drawable to bitmap, the bitmap width
     * and height is the vector drawable width and height.
     */
    public static Bitmap vectorDrawableToBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight()
                , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    /**
     * Use to change bitmap to  bytes.
     */
    public static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getAllocationByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        return byteBuffer.array();
    }

    /**
     * If you use BitmapFactory.decodeByteArray may can not get the bitmap, because
     * the bytes may not included the image information, so you need provide the info.
     */
    public static Bitmap bytesToBitmap(byte[] bytes, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));
        return bitmap;
    }


    // ------------------ Internal API ------------------


    /**
     * 此处的判断中,用且和或是有很大区别的,且的话只有当图片的宽高同时大于控件宽高的两倍才会进行压缩
     * 此方式可以保留图片精度,不会造成精度丢失,因此消耗内存更多
     * <p>
     * 如果用或,只要图片的宽高大于照片的宽高的两倍(不需要同时)就进行压缩,
     * 此方式会导致可能压缩后图片某一边小于控件大小,从而会拉伸图片导致精度丢失,但是可以节省更多内存
     * <p>
     * 当今智能手机内存很大,推荐第一种,保留精度.
     */
    private static int getInSampleSize(BitmapFactory.Options options, int desireWidth, int desireHeight) {
        int inSampleSize = 1;
        float halfWidth = options.outWidth * 1.0f / 2;
        float halfHeight = options.outHeight * 1.0f / 2;
        if (halfWidth >= desireWidth && halfHeight >= desireHeight) {
            while ((halfWidth / inSampleSize) >= desireWidth && (halfHeight / inSampleSize) >= desireHeight) {
                inSampleSize = inSampleSize * 2;
            }
        }
        return inSampleSize;
    }
}
