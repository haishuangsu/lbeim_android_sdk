package com.lbe.imsdk.service

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // uat
    private const val BASE_URL = "https://mob.imsz.online/"

    // sit
//    private const val BASE_URL = "http://www.im-sit-dreaminglife.cn/"

    // dev
//     private const val BASE_URL = "http://www.im-dreaminglife.cn/"

    var IM_URL = ""
    var UPLOAD_BASE_URL = ""


    val baseApiService: ApiService by lazy {
        val retrofit =
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .build()
        retrofit.create(ApiService::class.java)
    }

    val imApiService: LbeIMAPiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

        val retrofit = Retrofit.Builder().baseUrl(IM_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        retrofit.create(LbeIMAPiService::class.java)
    }

    val uploadService: UploadService by lazy {
        val retrofit = Retrofit.Builder().baseUrl(UPLOAD_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        retrofit.create(UploadService::class.java)
    }

    val downloadService: BaseService by lazy {
        val retrofit = Retrofit.Builder().baseUrl(UPLOAD_BASE_URL).build()
        retrofit.create(BaseService::class.java)
    }
}