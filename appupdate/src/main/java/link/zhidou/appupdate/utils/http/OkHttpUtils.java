package link.zhidou.appupdate.utils.http;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by ganyu on 2017/8/9.
 */

public class OkHttpUtils implements IHttpUtils {

    private OkHttpClient okHttpClient;
    private Gson gson;

    public OkHttpUtils() {
        okHttpClient = new OkHttpClient();
        gson = new Gson();
    }

    @Override
    public Cancelable postDownloadAsync(final String url, long startSize, final OnRequestListener listener) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Range", "bytes=" + startSize + "-")
                .post(formBodyBuilder.build())
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                try {
                    listener.onRequestError(OnRequestListener.ERROR_CODE_UNKENOW, url, e);
                } catch (Exception e1) {
                    listener.onError(OnRequestListener.ERROR_CODE_METHOD_ONREQUESTERROR, url, e1);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (ResponseBody responseBody = response.body()) {
                    //反回码在200到300以外视为不成功
                    if (!response.isSuccessful()) {
                        listener.onRequestError(response.code(), url,
                                new IOException("Response UnExcepted Code : " + response.code()));
                        return;
                    }

                    if (responseBody == null) {
                        listener.onRequestError(response.code(), url,
                                new IOException("Response Body is NULL : " + response.code()));
                        return;
                    }
                    DownloadResponse downloadResponse = new DownloadResponse(responseBody.byteStream(),
                            responseBody.contentLength());
                    listener.onResponse(url, downloadResponse);
                } catch (Exception e) {
                    listener.onError(OnRequestListener.ERROR_CODE_METHOD_ONRESPONSE, url, e);
                }
            }
        });
        return null;
    }

    @Override
    public String postStringSync(String url, Map<String, String> formParams) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : formParams.entrySet()) {
            formBodyBuilder.add(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder().url(url).post(formBodyBuilder.build()).build();
        Call call = okHttpClient.newCall(request);
        try (Response response = call.execute()) {
            //反回码在200到300以外视为不成功
            if (!response.isSuccessful()) {
                return null;
            }

            ResponseBody responseBody = response.body();

            if (responseBody == null) {
                return null;
            }

            return responseBody.string();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Object jsonStr2Object(Class clazz, String jsonStr) {
        return gson.fromJson(jsonStr, clazz);
    }

    @Override
    public String object2JsonStr(Object object) {
        return gson.toJson(object);
    }
}
