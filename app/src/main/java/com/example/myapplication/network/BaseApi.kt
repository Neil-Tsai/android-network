package com.example.myapplication.network

import com.example.myapplication.App
import com.example.myapplication.BuildConfig
import com.neil.network.NetworkClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory

object BaseApi {
    val networkClient: NetworkClient by lazy {
        NetworkClient
            .getInstance()
            .also {
                it.setLoggingInterceptor(BuildConfig.DEBUG)
                it.setCookie(App.instance)
            }
    }

    /**
     *  創建新的連線配置
     */
    private val newClient: OkHttpClient by lazy {
        NetworkClient
            .getInstance()
            .clientNewBuilder()
            .addInterceptor(headerInterceptor())
            .build()
    }
    val newRetrofit: Retrofit by lazy {
        NetworkClient
            .getInstance()
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

    fun cancel(url: String) {
        networkClient.clientCancel(url, newClient)
    }
}