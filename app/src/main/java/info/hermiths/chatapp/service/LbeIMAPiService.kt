package info.hermiths.chatapp.service

import info.hermiths.chatapp.model.req.HistoryBody
import info.hermiths.chatapp.model.req.MarkReadReqBody
import info.hermiths.chatapp.model.req.MsgBody
import info.hermiths.chatapp.model.req.SessionBody
import info.hermiths.chatapp.model.req.SessionListReq
import info.hermiths.chatapp.model.req.TimeoutReqBody
import info.hermiths.chatapp.model.resp.History
import info.hermiths.chatapp.model.resp.SendMsg
import info.hermiths.chatapp.model.resp.Session
import info.hermiths.chatapp.model.resp.SessionListRep
import info.hermiths.chatapp.model.resp.TimeoutRespBody
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

private const val LBE_SIGN = "lbeSign"
private const val LBE_TOKEN = "lbeToken"
private const val LBE_IDENTITY = "lbeIdentity"
private const val FETCH_HISTORY_URL = "miner-api/trans/history"
private const val CREATE_ChAT_URL = "miner-api/trans/session"
private const val SEND_MSG_URL = "miner-api/trans/msg-send"
private const val FETCH_HISTORY_LIST_URL = "miner-api/trans/session-list"
private const val FETCH_TIMEOUT_CONFIG = "miner-api/trans/timeout-config"
private const val MARK_READ = "miner-api/trans/mark-msg-as-read"


interface LbeIMAPiService {

    @POST(FETCH_HISTORY_LIST_URL)
    suspend fun fetchSessionList(
        @Header(LBE_TOKEN) lbeToken: String,
        @Header(LBE_IDENTITY) lbeIdentity: String,
        @Body body: SessionListReq
    ): SessionListRep

    @POST(FETCH_HISTORY_URL)
    suspend fun fetchHistory(
        @Header(LBE_SIGN) lbeSign: String,
        @Header(LBE_TOKEN) lbeToken: String,
        @Header(LBE_IDENTITY) lbeIdentity: String,
        @Body body: HistoryBody
    ): History

    @POST(CREATE_ChAT_URL)
    suspend fun createSession(
        @Header(LBE_SIGN) lbeSign: String,
        @Header(LBE_IDENTITY) lbeIdentity: String,
        @Body body: SessionBody
    ): Session

    @POST(SEND_MSG_URL)
    suspend fun sendMsg(
        @Header(LBE_TOKEN) lbeToken: String,
        @Header(LBE_IDENTITY) lbeIdentity: String,
        @Header("lbeSession") lbeSession: String,
        @Body body: MsgBody
    ): SendMsg

    @POST(FETCH_TIMEOUT_CONFIG)
    suspend fun fetchTimeoutConfig(
        @Header(LBE_SIGN) lbeSign: String,
        @Header(LBE_TOKEN) lbeToken: String,
        @Header(LBE_IDENTITY) lbeIdentity: String,
        @Body body: TimeoutReqBody
    ): TimeoutRespBody

    @POST(MARK_READ)
    suspend fun markRead(
        @Header(LBE_SIGN) lbeSign: String,
        @Header(LBE_TOKEN) lbeToken: String,
        @Header(LBE_IDENTITY) lbeIdentity: String,
        @Body body: MarkReadReqBody
    )
}