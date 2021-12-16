package com.example.myapplication.network

import com.example.myapplication.App
import com.example.myapplication.BuildConfig
import com.neil.network.NetworkClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory

object BaseApi {

    /**
     * 透過newBuilder增加或變更配置，client有重新配置的話，retrofit需要重新配置client對像
     */
    private val newClient: OkHttpClient by lazy {
        NetworkClient
            .getInstance(App.instance, BuildConfig.DEBUG)
            .clientNewBuilder()
            .addInterceptor(headerInterceptor())
            .build()
    }
    val newRetrofit: Retrofit by lazy {
        NetworkClient
            .getInstance(App.instance, BuildConfig.DEBUG)
            .retrofitNewBuilder()
            .client(newClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }

    /**
     * headerInterceptor
     */
    private fun headerInterceptor(): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            //TODO 根据项目适配调整
            request = request.newBuilder()
                .addHeader("content-type", "application/json")
                .build()

            chain.proceed(request)
        }
    }
}