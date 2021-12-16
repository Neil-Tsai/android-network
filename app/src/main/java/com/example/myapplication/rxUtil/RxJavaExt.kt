package com.example.myapplication.rxUtil

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Response

/**
 * Observable 擴展
 */
fun <T> Observable<Response<T>>.executeResult(subscriber: ResultObserver<T>){
    this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(subscriber)
}