package my.utils;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A okHttp util, package the get and formPost.
 *
 * @author djh on  2018/8/2 16:31
 * @E-Mail 1544579459@qq.com
 */
public class OkHttpUtil {
    private static Map<String, Call> sCallMap;
    private static OkHttpClient sOkHttpClient;

    static {
        sOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        // Use to store all call of request, when activity destroy, cancel all request.
        sCallMap = new HashMap<>();
    }

    public static void get(String url, @NonNull Handler handler) {
        getSmallData(url, handler);
    }

    /**
     * Form post, use handler send response content to main thread.
     */
    public static void formPost(String url, Map<String, String> valueMap, Handler handler) {
        String[] key = (String[]) valueMap.keySet().toArray();
        String[] values = (String[]) valueMap.values().toArray();

        FormBody.Builder builder = new FormBody.Builder();
        for (int i = 0; i < key.length; i++) {
            builder.add(key[i], values[i]);
        }

        FormBody formBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call call = sOkHttpClient.newCall(request);
        sCallMap.put(url, call);
        smallDataResponseCall(call, handler);
    }

    /**
     * MultipartBody post, use to post file or values.
     */
    public static void multipartPost(String url, Handler handler, @Nullable Map<String, File> fileMap, @Nullable Map<String, String> valueMap) {//                text/plain ：纯文本格式 .txt
//                text/xml ： XML格式 .xml
//                image/gif ：gif图片格式 .gif
//                image/jpeg ：jpg图片格式 .jpg
//                image/png：png图片格式 .png
//                audio/mp3 : 音频mp3格式 .mp3
//                audio/rn-mpeg :音频mpga格式 .mpga
//                video/mpeg4 : 视频mp4格式 .mp4
//                video/x-mpg : 视频mpa格式 .mpg
//                video/x-mpeg :视频mpeg格式 .mpeg
//                video/mpg : 视频mpg格式 .mpg
//                以application开头的媒体格式类型：
//                application/xhtml+xml ：XHTML格式
//                application/xml ： XML数据格式
//                application/atom+xml ：Atom XML聚合格式
//                application/json ： JSON数据格式
//                application/pdf ：pdf格式
//                application/msword ： Word文档格式
//                application/octet-stream ： 二进制流数据
        if (fileMap == null && valueMap == null) {
            throw new RuntimeException("Do you really want to post data?");
        }
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // This is the set the all multipart MIME data of content-disposition to form-data,
        // because this content-disposition is usually use, so set it.
        builder.setType(MultipartBody.FORM);
        // If file map is not null, add file part to multiPartBody.
        if (fileMap != null) {
            String[] keys = fileMap.keySet().toArray(new String[0]);
            File[] files = fileMap.values().toArray(new File[0]);
            // Splice all file to data body.
            for (int i = 0; i < files.length; i++) {
                if (!files[i].exists()) {
                    throw new RuntimeException("File is not exists!");
                }
                // MediaType is the content-type of the data head.
                MediaType mediaType = MediaType.parse("application/octet-stream");
                RequestBody filePartBody = RequestBody.create(mediaType, files[i]);
                builder.addFormDataPart(keys[i], files[i].getName(), filePartBody);
                // You also can use this to add a file part.
                // builder.addPart(Headers.of(
                //        "Content-Disposition",
                //        "form-data; name=\"mFile\";
                //        filename=\"wjd.mp4\""), fileBody)
            }
        }
        // If valueMap is not null, add it to multiPartBody.
        if (valueMap != null) {
            String[] keys = valueMap.keySet().toArray(new String[0]);
            String[] values = valueMap.values().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                builder.addFormDataPart(keys[i], values[i]);
            }
        }
        MultipartBody multipartBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();
        Call call = sOkHttpClient.newCall(request);
        sCallMap.put(url, call);
        smallDataResponseCall(call, handler);
    }

    /**
     * Json post, use to post json data.
     */
    public static void jsonPost(String url, String json, Handler handler) {
        // If you not specify the charset, default is UTF-8.
        MediaType mediaType = MediaType.parse("application/json; charset=UTF-8");
        // Create json request body.
        RequestBody requestBody = RequestBody.create(mediaType, json);
        // Create request.
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = sOkHttpClient.newCall(request);
        sCallMap.put(url, call);
        smallDataResponseCall(call, handler);
    }

    /**
     * When the activity destroy, you can use the to cancel all request, this may
     * cancel the request that is not request by you, so if you want request a specific
     * request, you should use {@link #cancelRequestByUrl}
     */
    public static void cancelAllRequest() {
        if (!sCallMap.isEmpty()) {
            for (Call call : sCallMap.values()) {
                call.cancel();
            }
        }
    }

    /**
     * Cancel request by url.
     */
    public static void cancelRequestByUrl(String... urls) {
        for (String url : urls) {
            sCallMap.get(url).cancel();
            sCallMap.remove(url);
        }
    }

//--------------------------------------------------------------------------------------------------

    private static void getSmallData(String url, final Handler handler) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = sOkHttpClient.newCall(request);
        sCallMap.put(url, call);
        smallDataResponseCall(call, handler);
    }

    /**
     * Async execute call, when the response data is small,
     * use handler send to main thread.
     */
    private static void smallDataResponseCall(Call call, final Handler handler) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sCallMap.remove(call.request().url().toString());
                showToast(handler, "连接失败");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        Message message = handler.obtainMessage();
                        message.obj = responseBody.bytes();
                        handler.sendMessage(message);
                    }
                } else {
                    showToast(handler, "请求失败" + response.code());
                }
                sCallMap.remove(call.request().url().toString());
            }
        });
    }

    private static void showToast(Handler handler, final String content) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showToast(content);
            }
        });
    }
}
