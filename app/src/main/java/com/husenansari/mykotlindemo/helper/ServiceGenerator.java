package com.husenansari.mykotlindemo.helper;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

class ServiceGenerator {

    public static MultipartBody.Part prepareFilePart(String partName, String fileUri) {
        File file = new File(fileUri);
        String mimeType= URLConnection.guessContentTypeFromName(file.getName());
        Log.e("mimeType",mimeType);

        //create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
        //MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private static OkHttpClient getRequestHeader(Context context) {

        return new OkHttpClient().newBuilder()
                //.addInterceptor(new ChuckInterceptor(context))
                .connectTimeout(60, TimeUnit.SECONDS).readTimeout(2, TimeUnit.MINUTES).build();
    }

    private static OkHttpClient getRequestHeader() {

        return new OkHttpClient().newBuilder()
                //.addInterceptor(interceptor())
                .connectTimeout(60, TimeUnit.SECONDS).readTimeout(2, TimeUnit.MINUTES).build();
    }

    private static Interceptor interceptor(){
        return new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                Log.e("Request Data","Data : "+chain.request());
                Log.e("Response Data","Data : "+ Objects.requireNonNull(response.body()).string());
                return response;
            }
        };
    }




}
