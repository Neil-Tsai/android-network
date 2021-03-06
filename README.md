# Android-Network-Module
retrofit2 + okhttp3 單例基礎封裝，可取消單一或全部的client

## Gradle
[![](https://jitpack.io/v/Neil-Tsai/android-network.svg)](https://jitpack.io/#Neil-Tsai/android-network)

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

```
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.Neil-Tsai:android-network:1.0.1'
	}

```
## 基本配置使用

```kotlin
object TestApi {
    private val service: ApiService by lazy {
        NetworkClient.getInstance().also { 
                it.setTimeOut(10L)
                it.setLoggingInterceptor(BuildConfig.DEBUG)
                it.setCookie(App.instance)
                it.addInterceptor(headerInterceptor())
        }.create(ApiService::class.java)
    }
    
    interface ApiService {
        @GET
        @Retry(2)
        suspend fun getServiceConfig(@Url url: String): Response<JsonObject>
    }
}
```
## 自定義配置RxJava或更改配置
```kotlin
object BaseApi {

    private val newClient: OkHttpClient by lazy {
        NetworkClient.getInstance(mContext)
            .clientNewBuilder()
            .build()
    }
    
    val newRetrofit: Retrofit by lazy {
        NetworkClient.getInstance(mContext)
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
```
## 取消連線
```
NetworkClient.getInstance(mContext).clientCancel("yourUrl", newClient)
```

| 屬性          | 說明 						       |
| ---      	| ---       						    |
| url 		| 不需額外設定tag，預設以url為tag，使用上直接帶上url即可        |
| newClient     | 若有自定義client則帶入自定義client，不帶值則使用原本的client  |
