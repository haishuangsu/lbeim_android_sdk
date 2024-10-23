package info.hermiths.chatapp.service.rest

import info.hermiths.chatapp.service.rest.model.Config
import info.hermiths.chatapp.service.rest.service.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://42nz10y3hhah.dreaminglife.cn:10002/";

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}