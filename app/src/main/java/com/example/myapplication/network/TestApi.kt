package com.example.myapplication.network

import com.example.myapplication.App
import com.example.myapplication.network.BaseApi.networkClient
import com.example.myapplication.network.BaseApi.newRetrofit
import com.google.gson.JsonObject
import com.neil.network.NetworkClient
import com.neil.network.retryFactory.Retry
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.*

object TestApi{
    // Test open data api
    private const val apiKey = "?limit=1&api_key=9be7b239-557b-4c10-9775-78cadfc555e9&sort=ImportDate%20desc&format=json"

    private const val baseUrl = "https://data.epa.gov.tw/api/v1"
    private const val test1 = "/acidr_p_01$apiKey"
    private const val test2 = "/acidr_p_02$apiKey"
    private const val test3 = "/acidr_p_04$apiKey"
    private const val test4 = "/aqf_p_01$apiKey"

    private val service: ApiService by lazy {
        networkClient.create(ApiService::class.java)
    }

    private val service2: ApiService by lazy {
        newRetrofit.create(ApiService::class.java)
    }

    interface ApiService {
        @GET
        @Retry(2)
        suspend fun getServiceConfig(@Url url: String): Response<JsonObject>

        @GET
        @Retry(2)
        fun getConfig(@Url url: String) : Observable<Response<JsonObject>>
    }

    suspend fun getServerConfig(): Any = withContext(Dispatchers.IO) {
        return@withContext service.getServiceConfig(baseUrl+ test1).body() ?: ""
    }

    suspend fun getServerConfig2(): Any = withContext(Dispatchers.IO) {
        return@withContext service.getServiceConfig(baseUrl+test2).body() ?: ""
    }

    //RxJava
    fun getConfig(): Observable<Response<JsonObject>> {
        return service2.getConfig(baseUrl+test3)
    }

    fun getConfig2():  Observable<Response<JsonObject>>{
        return service2.getConfig(baseUrl+ test4)
    }
}