package com.neil.network.retryFactory


@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Retry(val max: Int = 0, val delay: Long = 1000)