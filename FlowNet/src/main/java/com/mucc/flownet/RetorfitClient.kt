package com.mucc.flownet


import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

const val HOST_TEST = "http://test-api.freeproxy.network"
const val HOST_PRE = "https://pre-api.freeproxy.network"
const val HOST_PRO = "https://api.freeproxy.network"

var baseUrl: String = HOST_PRO

val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(getOkHttpClient())
        .build();
}

private fun getOkHttpClient(): OkHttpClient {
    val builder: OkHttpClient.Builder = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS) //设置读取超时时间
        .writeTimeout(30, TimeUnit.SECONDS) //设置写的超时时间
        .connectTimeout(30, TimeUnit.SECONDS)
//    if (BuildConfig.DEBUG) {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    builder.addInterceptor(httpLoggingInterceptor.apply {
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    })
//    }
    builder.addInterceptor(RequestInterceptor())
    return builder.build()
}

private class RequestInterceptor : Interceptor {
    @Throws(IOException::class)

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest: Request = chain.request()
        val request: Request = oldRequest.newBuilder()
            .headers(getHeaders().toHeaders())
            .build()
        return chain.proceed(request)
    }
}

var headerMap: HashMap<String, String>? = null

/**
 * 获取头信息
 */
private fun getHeaders(): Map<String, String> {
    return headerMap ?: HashMap()
}