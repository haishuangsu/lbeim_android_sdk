package info.hermiths.chatapp.service

import info.hermiths.chatapp.model.resp.Config

import info.hermiths.chatapp.model.req.ConfigBody

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

private const val FETCH_CONFIG_URL = "miners-api/trans/nodes"


interface ApiService {
    @POST(FETCH_CONFIG_URL)
    suspend fun fetchConfig(@Header("lbe_sign") lbeSign: String, @Body body: ConfigBody): Config
}