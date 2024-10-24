package info.hermiths.chatapp.service

import info.hermiths.chatapp.model.resp.Config
import info.hermiths.chatapp.model.resp.History
import info.hermiths.chatapp.model.resp.SendMsg
import info.hermiths.chatapp.model.resp.Session
import info.hermiths.chatapp.model.req.ConfigBody
import info.hermiths.chatapp.model.req.HistoryBody
import info.hermiths.chatapp.model.req.MsgBody
import info.hermiths.chatapp.model.req.SessionBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

private const val FETCH_CONFIG_URL = "api/trans/nodes"
private const val FETCH_HISTORY_URL = "api/trans/history"
private const val CREATE_ChAT_URL = "api/trans/session"
private const val SEND_MSG_URL = "api/trans/msg-send"

interface ApiService {
    @POST(FETCH_CONFIG_URL)
    suspend fun fetchConfig(@Header("lbe_sign") lbeSign: String, @Body body: ConfigBody): Config

    @POST(FETCH_HISTORY_URL)
    suspend fun fetchHistory(@Header("lbe_sign") lbeSign: String, @Body body: HistoryBody): History

    @POST(CREATE_ChAT_URL)
    suspend fun createSession(@Header("lbe_sign") lbeSign: String, @Body body: SessionBody): Session

    @POST(SEND_MSG_URL)
    suspend fun sendMsg(
        @Header("lbe_token") lbeToken: String,
        @Header("lbe_session") lbeSession: String,
        @Body body: MsgBody
    ): SendMsg
}