package com.example.studentsmanager.commons

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceFactory {

    internal const val TRUSTED_HOST = "192.168.43.17"
    internal const val BASE_URL = "http://$TRUSTED_HOST:23333/"

    private val loggingInterceptor = HttpLoggingInterceptor()
        .apply { level = HttpLoggingInterceptor.Level.BODY }

    val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .connectTimeout(40, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(40, TimeUnit.SECONDS)
        .addNetworkInterceptor(loggingInterceptor)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    inline operator fun <reified T> invoke(): T = retrofit.create(T::class.java)
}

/**
 * A common wrapper class of respond data from open.twtstudio.com/api.
 *
 * @see com.twt.wepeiyang.commons.experimental.service.AuthService
 */
data class CommonBody<out T>(
    val error_code: Int,
    val message: String,
    val data: T?
)