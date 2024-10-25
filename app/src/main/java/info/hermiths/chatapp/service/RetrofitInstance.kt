package info.hermiths.chatapp.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://42nz10y3hhah.dreaminglife.cn:10002/"
    var IM_URL = ""


    private val baseRetrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val baseApiService: ApiService by lazy {
        baseRetrofit.create(ApiService::class.java)
    }

    private val imRetrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl(IM_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val imApiService: LbeIMAPiService by lazy {
        imRetrofit.create(LbeIMAPiService::class.java)
    }
}