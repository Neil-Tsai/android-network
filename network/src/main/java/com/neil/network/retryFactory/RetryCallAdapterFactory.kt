package com.neil.network.retryFactory

import android.accounts.NetworkErrorException
import android.util.Log
import com.neil.network.RequestTag
import kotlinx.coroutines.*
import okhttp3.Request
import okio.Timeout
import retrofit2.*
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicInteger

class RetryCallAdapterFactory : CallAdapter.Factory() {

    companion object {
        val TAG = RetryCallAdapterFactory::class.simpleName

        fun create(): RetryCallAdapterFactory {
            return RetryCallAdapterFactory()
        }
    }
    private val errorMessage: String = "Connection request error, please try again later."

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *> {
        val retry = getRetry(annotations)
        return RetryCallAdapter(
            retrofit.nextCallAdapter(this, returnType, annotations),
            RetryData(retry?.max ?: 0, retry?.delay ?: 0, errorMessage)
        )
    }

    private fun getRetry(annotations: Array<Annotation>): Retry? {
        for (annotation in annotations) {
            if (annotation is Retry) {
                return annotation
            }
        }
        return null
    }

    class RetryCallAdapter<R, T>(private val delegated: CallAdapter<R, T>, private val retryData: RetryData) :
        CallAdapter<R, T> {

        override fun responseType(): Type {
            return delegated.responseType()
        }

        override fun adapt(call: Call<R>): T {
            var newCall = call
            if (retryData.maxRetries > 0)
                newCall = RetryingCall(call, retryData)
            return delegated.adapt(newCall)
        }

    }

    class RetryingCall<R>(private val delegated: Call<R>, private val retryData: RetryData) : Call<R> {

        override fun clone(): Call<R> {
            return RetryingCall(delegated.clone(), retryData)
        }

        override fun execute(): Response<R> {
            return delegated.execute()
        }

        override fun enqueue(callback: Callback<R>) {
            delegated.enqueue(RetryCallback(delegated, callback, retryData))
        }

        override fun isExecuted(): Boolean {
            return delegated.isExecuted
        }

        override fun cancel() {
            delegated.cancel()
        }

        override fun isCanceled(): Boolean {
            return delegated.isCanceled
        }

        override fun request(): Request {
            return delegated.request()
        }

        override fun timeout(): Timeout {
            return delegated.timeout()
        }

    }

    class RetryCallback<T>(
        private val call: Call<T>,
        private val callback: Callback<T>,
        private val retryData: RetryData
    ) : Callback<T> {

        private val retryCount = AtomicInteger(0)
        private val maxRetries get() = retryData.maxRetries

        override fun onResponse(call: Call<T>, response: Response<T>) {
            val isCanceled = call.request().tag()?.let {
                it as RequestTag
                "Canceled" == it.url
            } ?: false

            if (!response.isSuccessful && retryCount.incrementAndGet() <= maxRetries && !isCanceled) {
                Log.d(TAG, "Call with no success result code: {} " + response.code())
                retryCall()
            } else {
                callback.onResponse(call, response)
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            Log.d(TAG, "Call failed with message: " + t.message)

            t.printStackTrace()
            /** 如果有取消，就不重試 */
            call.request().tag()?.let {
                it as RequestTag
                if ("Canceled" == it.url) {
                    callback.onFailure(call, t)
                    return
                }
            }
            // 如果取消，就不用重試
            if (t.message == "Canceled") {
                callback.onFailure(call, t)
                return
            }

            if (retryCount.incrementAndGet() <= maxRetries) {
                retryCall()
            } else {
                Log.d(TAG, "No retries left sending timeout up.")
                callback.onFailure(
                    call,
                    NetworkErrorException(retryData.errorMessage)
                )
            }
        }

        private fun retryCall() {
            GlobalScope.launch {
                delay(retryData.delay)
                Log.w(TAG, "" + retryCount.get() + "/" + maxRetries + " " + " Retrying...")
                call.clone().enqueue(this@RetryCallback)
            }
        }
    }
}

data class RetryData(val maxRetries: Int, val delay: Long, val errorMessage: String)