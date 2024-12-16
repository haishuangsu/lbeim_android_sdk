package com.lbe.imsdk.data.remote

import com.lbe.imsdk.service.RetrofitInstance
import okhttp3.ResponseBody

class BaseRepository {
    private val baseService = RetrofitInstance.downloadService

    suspend fun downloadImage(url: String): ResponseBody {
        return baseService.downloadImage(url)
    }
}

