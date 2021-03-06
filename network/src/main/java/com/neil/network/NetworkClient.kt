package com.neil.network

import android.content.Context
import android.util.Log
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.GsonBuilder
import com.neil.network.retryFactory.RetryCallAdapterFactory
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

import java.util.concurrent.TimeUnit

/**
 *  okHttp + retrofit 基礎配置module
 *  單例使用，可取消單一或全部 client
 *  2021/11/24 by neil
 */

data class RequestTag(var url: String = "")

class NetworkClient {

    companion object {
        val TAG = NetworkClient::class.simpleName
        @Volatile
        private var instance: NetworkClient? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: NetworkClient().also { instance = it }
            }
    }

    private val defaultTimeOut = 10L

    private val clientBuilder: OkHttpClient.Builder by lazy {
        initOkHttpClientBuilder()
    }

    private val okHttpClient: OkHttpClient by lazy {
        clientBuilder.build()
    }

    private val retrofit: Retrofit by lazy {
        initRetrofitBuilder()
    }

    private fun initRetrofitBuilder(): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(
                GsonConverterFactory.create(
                    /** 如果回傳值不是json，會報錯，忽略不標準的JSON數據格式 */
                    GsonBuilder().setLenient().create()
                )
            )
            .addCallAdapterFactory(RetryCallAdapterFactory.create())
            /**
             * 添加retrofit tag ，以url作為tag,
             * 取消client時依據url tag來取消連線
             * 取消連線後tag變更為Canceled，作為判斷retry不重試
             */
            .callFactory(object : Call.Factory {
                override fun newCall(request: Request): Call {
                    val newRequest = request.newBuilder()
                        .tag(RequestTag(request.url.toString()))
                        .build()
                    return okHttpClient.newCall(newRequest)
                }
            })
            .baseUrl("http://localhost")
            .build()
    }

    private fun initOkHttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .connectTimeout(defaultTimeOut, TimeUnit.SECONDS)
            .writeTimeout(defaultTimeOut, TimeUnit.SECONDS)
            .readTimeout(defaultTimeOut, TimeUnit.SECONDS)
            /** 統一由retrofit 發起重連 */
            .retryOnConnectionFailure(false)
            /** 預設為不同host name 5個，超過等待連接 */
            .dispatcher(Dispatcher().also {
                it.maxRequestsPerHost = 10
            })
    }

    /**
     * timeOut setting 預設15s，此處預設10s
     * @param timeOut
     */
    fun setTimeOut(timeOut: Long) {
        clientBuilder
            .connectTimeout(timeOut, TimeUnit.SECONDS)
            .writeTimeout(timeOut, TimeUnit.SECONDS)
            .readTimeout(timeOut, TimeUnit.SECONDS)
    }
    
    /**
     * HttpLoggingInterceptor setting
     * @param isDebugModel
     */
    fun setLoggingInterceptor(isDebugModel: Boolean = false) {
        val loggingInterceptor = HttpLoggingInterceptor().also {
                it.setLevel(
                    if (isDebugModel)
                        HttpLoggingInterceptor.Level.BODY
                    else
                        HttpLoggingInterceptor.Level.NONE
                )
            }
        clientBuilder.addInterceptor(loggingInterceptor)
    }

    /**
     * setCookie
     * @param context
     */
    fun setCookie(context: Context) {
        val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
        clientBuilder.cookieJar(cookieJar)
    }

    /**
     * addInterceptor
     * @param interceptor
     */
    fun addInterceptor(interceptor: Interceptor) {
        clientBuilder.addInterceptor(interceptor)
    }

    /**
     * newBuilder會複制原有的builder
     * 添加或重新配置更多設置，配置後 build()創建另一個okHttpClient來使用
     */
    fun clientNewBuilder(): OkHttpClient.Builder = okHttpClient.newBuilder()

    /**
     * newBuilder會複制原有的builder
     * 添加或重新配置更多設置，配置後 build()創建另一個okHttpClient來使用
     */
    fun retrofitNewBuilder(newClient: OkHttpClient? = null): Retrofit.Builder {
        val newBuilder =  retrofit.newBuilder()
        if (newClient != null) {
            newBuilder
                .client(newClient)
                .callFactory(object : Call.Factory {
                    override fun newCall(request: Request): Call {
                        val newRequest = request.newBuilder()
                            .tag(RequestTag(request.url.toString()))
                            .build()
                        return newClient.newCall(newRequest)
                    }
                })
        }
        return newBuilder
    }


    /**
     * 取消全部client請求
     * @param newClient 新的請求, 不帶參數為原client
     */
    fun cancelAll(newClient: OkHttpClient = okHttpClient) {
        newClient.dispatcher.cancelAll()
    }

    /**
     * 取消單個client請求
     * @param url 比對tag url是否存在
     * @param newClient 新的請求, 不帶參數為原client
     */
    fun clientCancel(url: String?, newClient: OkHttpClient = okHttpClient) {
        if (url.isNullOrEmpty()) {
            Log.w(TAG, "clientCancel param url is null or empty.")
            return
        }
        for (call: Call in newClient.dispatcher.queuedCalls()) {
            cancel(call, url)
        }
        for (call: Call in newClient.dispatcher.runningCalls()) {
            cancel(call, url)
        }
    }

    /**
     * 比對tag url 判斷要取消client，並將要取消的client tag設置為Canceled
     */
    private fun cancel(call: Call, url: String) {
        call.request().tag()?.also {
            it as RequestTag
            if (url == it.url) {
                it.url = "Canceled"
                call.cancel()
            }
        }
    }

    fun <T> create(clz: Class<T>): T = retrofit.create(clz)
}