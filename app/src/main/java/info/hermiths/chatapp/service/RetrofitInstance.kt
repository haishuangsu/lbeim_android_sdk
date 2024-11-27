package info.hermiths.chatapp.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // sit环境： www.im-sit-dreaminglife.cn
//    private const val BASE_URL = "http://www.im-sit-dreaminglife.cn/"

    // dev环境： www.im-dreaminglife.cn
     private const val BASE_URL = "http://www.im-dreaminglife.cn/"
    var IM_URL = ""
    var UPLOAD_BASE_URL = ""


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

    val uploadService: UploadService by lazy {
        val retrofit = Retrofit.Builder().baseUrl(UPLOAD_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        retrofit.create(UploadService::class.java)
    }

    val baseService: BaseService by lazy {
        val retrofit = Retrofit.Builder().baseUrl(UPLOAD_BASE_URL).build()
        retrofit.create(BaseService::class.java)
    }
}