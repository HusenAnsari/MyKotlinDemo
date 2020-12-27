package com.thepitch.helper

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.FacebookSdk.sdkInitialize
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.thepitch.api.ApiConstant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

class MyApplication : Application() {

    companion object {
        private var gson: Gson? = null
        private var retrofit: Retrofit? = null
        private const val a = 6.1f
        internal var i = a.roundToLong()


        fun getRetrofit(): Retrofit {
            return this.retrofit!!
        }

        fun getGson(): Gson {
            return this.gson!!
        }

        private fun initRetrofit() {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val okHttpClient = OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
                    .build()

            retrofit = Retrofit.Builder()
                    .baseUrl(ApiConstant.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(StringConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

        }

        private fun initGson() {
            gson = GsonBuilder()
                    .setLenient()
                    .create()
        }
    }

    override fun onCreate() {
        super.onCreate()
        initGson()
        initRetrofit()
        sdkInitialize(this);
    }
}