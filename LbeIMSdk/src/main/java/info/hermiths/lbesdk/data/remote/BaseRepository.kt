package info.hermiths.lbesdk.data.remote

import info.hermiths.lbesdk.service.RetrofitInstance
import okhttp3.ResponseBody

class BaseRepository {
    private val baseService = RetrofitInstance.downloadService

    suspend fun downloadImage(url: String): ResponseBody {
        return baseService.downloadImage(url)
    }
}

