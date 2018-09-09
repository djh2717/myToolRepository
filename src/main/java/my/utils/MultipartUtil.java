package my.utils;

import android.support.annotation.NonNull;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * A multipart body util, use to construct a multipart body.
 *
 * @author djh on  2018/9/8 19:29
 * @E-Mail 1544579459@qq.com
 */
public class MultipartUtil {

    public static MultipartBody.Part filePart(@NonNull String name, @NonNull File file) {
        if (!file.exists()) {
            throw new RuntimeException("File is not exist!");
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse(Types.APPLICATION_OCTET_STREAM), file);
        return MultipartBody.Part.createFormData(name, file.getName(), requestBody);
    }

    public static MultipartBody.Part valuePart(String name, String value) {
        RequestBody requestBody = RequestBody.create(null, value);
        return MultipartBody.Part.createFormData(name, value);
    }

}
