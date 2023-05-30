package com.mucc.flownet


import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

var baseUrl:String =""

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

/**
 * 获取头信息
 */
private fun getHeaders(): Map<String, String> {
    val headersMap: HashMap<String, String> = HashMap()
    headersMap["app_id"] = "com.supertools.wallpaper"
    return headersMap
}