package com.example.myapplication.rxUtil

import android.accounts.NetworkErrorException
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import retrofit2.Response
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

abstract class ResultObserver<T> : Observer<Response<T>> {

    override fun onSubscribe(d: Disposable) {
        if (!d.isDisposed) {
            onRequestStart()
        }
    }

    override fun onNext(reposnse: Response<T>) {
        onRequestEnd()
        if (reposnse.isSuccessful) {
            try {
                onSuccess(reposnse.body())
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            try {
                onBusinessFail(reposnse.code(), reposnse.message())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onError(e: Throwable) {
        onRequestEnd()
        try {
            if (e is ConnectException
                || e is TimeoutException
                || e is NetworkErrorException
                || e is UnknownHostException
            ) {
                onFailure(e, true)
            } else {
                onFailure(e, false)
            }
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
    }

    override fun onComplete() {}

    /**
     * 請求開始
     */
    open fun onRequestStart() {

    }

    /**
     * 請求結束
     */
    open fun onRequestEnd() {

    }

    /**
     * 返回成功
     *
     * @param result
     * @throws Exception
     */
    @Throws(Exception::class)
    abstract fun onSuccess(result: T?)

    /**
     * 返回失败
     *
     * @param e
     * @param isNetWorkError 是否是網路錯誤
     * @throws Exception
     */
    @Throws(Exception::class)
    abstract fun onFailure(e: Throwable, isNetWorkError: Boolean)

    /**
     * 業務錯誤
     * 返回成功了,但是code错誤
     *
     * @param code
     * @param message
     * @throws Exception
     */
    @Throws(Exception::class)
    open fun onBusinessFail(code: Int, message: String) {
    }
}