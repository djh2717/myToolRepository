package my.utils;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
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
    private static Handler sHandler;

    static {
        sOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .cache(new Cache(MyApplication.getContext().getCacheDir(), 1024 * 1024 * 30))
                .build();
        // Use to store all call of request, when activity destroy, cancel all request.
        sCallMap = new HashMap<>();
        // Use to listener call back to ui thread.
        sHandler = new Handler();
    }

    public static void get(String url, @Nullable CacheControl cacheControl, @NonNull ResponseCallBack responseCallBack) {
        getSmallData(url, cacheControl, responseCallBack);
    }

    /**
     * Form post, use handler send response data to main thread.
     */
    public static void formPost(String url, Map<String, String> valueMap, ResponseCallBack responseCallBack) {
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
        smallDataResponseCall(call, responseCallBack);
    }

    /**
     * MultipartBody post, use to post file or values.
     */
    public static void multipartPost(String url, @Nullable Map<String, File> fileMap,
                                     @Nullable Map<String, String> valueMap, ResponseCallBack responseCallBack) {

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
                MediaType mediaType = MediaType.parse(Types.APPLICATION_OCTET_STREAM);
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
        smallDataResponseCall(call, responseCallBack);
    }

    /**
     * Json post, use to post json data.
     */
    public static void jsonPost(String url, String json, ResponseCallBack responseCallBack) {

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
        smallDataResponseCall(call, responseCallBack);
    }

    /**
     * This post use to post other custom requestBody.
     */
    public static void otherPost(String url, RequestBody requestBody, ResponseCallBack responseCallBack) {
        // Create the request.
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // Enqueue the call.
        Call call = sOkHttpClient.newCall(request);
        sCallMap.put(url, call);
        smallDataResponseCall(call, responseCallBack);
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

    // ------------------ Internal API ------------------

    private static void getSmallData(String url, CacheControl cacheControl, final ResponseCallBack responseCallBack) {
        Request.Builder builder = new Request.Builder();
        if (cacheControl != null) {
            builder.cacheControl(cacheControl);
        }
        builder.url(url);
        Request request = builder.build();

        Call call = sOkHttpClient.newCall(request);

        sCallMap.put(url, call);
        smallDataResponseCall(call, responseCallBack);
    }

    /**
     * Async execute call, when the response data is small,
     * use handler send to main thread.
     */
    private static void smallDataResponseCall(Call call, final ResponseCallBack responseCallBack) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
                sCallMap.remove(call.request().url().toString());
                // Post the response to the UI thread.
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        responseCallBack.onFailure(call, e);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        final byte[] responseContent = responseBody.bytes();
                        // Send this response content to UI thread.
                        sHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                responseCallBack.onResponse(responseContent);
                            }
                        });
                    } else {
                        showToast(sHandler, "数据获取失败" + response.code());
                    }
                } else {
                    showToast(sHandler, "请求失败" + response.code());
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

    /**
     * Use to response call back on UI thread.
     */
    public interface ResponseCallBack {
        /**
         * Failure call back.
         *
         * @param call the failure call.
         * @param e    the exception.
         */
        void onFailure(@NonNull Call call, @NonNull IOException e);

        /**
         * Success call back, this guarantee the request must success when this is
         * call.
         *
         * @param responseContent the response content.
         */
        void onResponse(@NonNull byte[] responseContent);
    }
}
