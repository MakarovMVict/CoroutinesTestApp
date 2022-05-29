package com.example.testapp

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class RxTestClass {
    val single: Observable<String> = Observable.just("")


    var counter = 0

    fun testSingle(): Observable<String> {
        return single
            .map { return@map "** single test ${counter++}" }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())


    }


    // .doOnSuccess { Log.d("RxTestClass", it)   }


}