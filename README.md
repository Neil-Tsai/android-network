# Android-Network-Module
retrofit2 + okhttp3 單例基礎封裝，可取消單一或全部的client

# 基本配置可直接使用
object TestApi {

    private val service: ApiService by lazy {
        NetworkClient.getInstance(App.instance, BuildConfig.DEBUG)
            .create(ApiService::class.java)
    }
    
    interface ApiService {
        @GET
        @Retry(2)
        suspend fun getServiceConfig(@Url url: String): Response<JsonObject>
    }
}

# 自定義配置RxJava或更改配置
object BaseApi {

    private val newClient: OkHttpClient by lazy {
        NetworkClient.getInstance(App.instance, BuildConfig.DEBUG)
            .clientNewBuilder()
            .build()
    }
    
    val newRetrofit: Retrofit by lazy {
        NetworkClient.getInstance(App.instance, BuildConfig.DEBUG)
            .retrofitNewBuilder()
            .client(newClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }
    
}

object TestApi {

    private val service2: ApiService by lazy {
        newRetrofit.create(ApiService::class.java)
    }

    interface ApiService {
        @GET
        @Retry(2)
        fun getConfig(@Url url: String) : Observable<Response<JsonObject>>
    }
    
    fun getConfig(): Observable<Response<JsonObject>> {
        return service2.getConfig(yourUrl)
    }
    
}
