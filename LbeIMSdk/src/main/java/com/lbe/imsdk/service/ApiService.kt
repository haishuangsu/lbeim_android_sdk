package com.lbe.imsdk.service

import com.lbe.imsdk.model.resp.Config

import com.lbe.imsdk.model.req.ConfigBody

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

private const val FETCH_CONFIG_URL = "miner-api/trans/nodes"


interface ApiService {
    @POST(FETCH_CONFIG_URL)
    suspend fun fetchConfig(
        @Header("lbeSign") lbeSign: String,
        @Header("lbeIdentity") lbeIdentity: String,
        @Body body: ConfigBody
    ): Config
}