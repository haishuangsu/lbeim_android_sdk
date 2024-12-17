package com.lbe.imsdk.data.remote

import com.lbe.imsdk.service.RetrofitInstance
import com.lbe.imsdk.model.resp.Config
import com.lbe.imsdk.model.req.ConfigBody

object LbeConfigRepository {
    private val lbeIMRepository = RetrofitInstance.baseApiService

    suspend fun fetchConfig(lbeSign: String, lbeIdentity: String, body: ConfigBody): Config {
        return lbeIMRepository.fetchConfig(
            lbeSign = lbeSign, lbeIdentity = lbeIdentity, body = body
        )
    }
}