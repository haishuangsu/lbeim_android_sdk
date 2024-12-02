package info.hermiths.lbesdk.service

import okhttp3.ResponseBody

import retrofit2.http.GET

import retrofit2.http.Url

interface BaseService {

    @GET
    suspend fun downloadImage(
        @Url url: String
    ): ResponseBody
}