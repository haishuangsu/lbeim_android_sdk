package info.hermiths.chatapp.service.rest.service

import info.hermiths.chatapp.BuildConfig
import info.hermiths.chatapp.service.rest.model.Config
import info.hermiths.chatapp.service.rest.model.History
import info.hermiths.chatapp.service.rest.model.SendMsg
import info.hermiths.chatapp.service.rest.model.Session
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

private const val FETCH_CONFIG_URL = "api/trans/nodes"
private const val FETCH_HISTORY_URL = "api/trans/history"
private const val CREATE_ChAT_URL = "api/trans/session"
private const val SEND_MSG_URL = "api/trans/msg-send"

interface ApiService {
    @Headers("lbe_sign: ${BuildConfig.lbeSign}")
    @POST(FETCH_CONFIG_URL)
    suspend fun fetchConfig(): Config

    @Headers("lbe_sign: ${BuildConfig.lbeSign}")
    @POST(FETCH_HISTORY_URL)
    suspend fun fetchHistory(@Body payLod: String): History

    @Headers("lbe_sign: ${BuildConfig.lbeSign}")
    @POST(CREATE_ChAT_URL)
    suspend fun createChat(@Body payLod: String): Session

    @Headers("lbe_sign: ${BuildConfig.lbeSign}")
    @POST(SEND_MSG_URL)
    suspend fun sendMsg(@Body payLod: String): SendMsg
}