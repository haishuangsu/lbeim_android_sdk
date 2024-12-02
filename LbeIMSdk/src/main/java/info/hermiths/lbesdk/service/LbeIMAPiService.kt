package info.hermiths.lbesdk.service

import info.hermiths.lbesdk.model.req.FaqReqBody
import info.hermiths.lbesdk.model.req.HistoryBody
import info.hermiths.lbesdk.model.req.MarkReadReqBody
import info.hermiths.lbesdk.model.req.MsgBody
import info.hermiths.lbesdk.model.req.SessionBody
import info.hermiths.lbesdk.model.req.SessionListReq
import info.hermiths.lbesdk.model.req.TimeoutReqBody
import info.hermiths.lbesdk.model.resp.History
import info.hermiths.lbesdk.model.resp.SendMsg
import info.hermiths.lbesdk.model.resp.Session
import info.hermiths.lbesdk.model.resp.SessionListRep
import info.hermiths.lbesdk.model.resp.TimeoutRespBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

private const val LBE_SIGN = "lbeSign"
private const val LBE_TOKEN = "lbeToken"
private const val LBE_IDENTITY = "lbeIdentity"
private const val LBE_SESSION = "lbeSession"


private const val FETCH_HISTORY_URL = "miner-api/trans/history"
private const val CREATE_ChAT_URL = "miner-api/trans/session"
private const val SEND_MSG_URL = "miner-api/trans/msg-send"
private const val FETCH_HISTORY_LIST_URL = "miner-api/trans/session-list"
private const val FETCH_TIMEOUT_CONFIG = "miner-api/trans/timeout-config"
private const val MARK_READ = "miner-api/trans/mark-msg-as-read"
private const val FAQ = "miner-api/trans/faq"
private const val TURN = "miner-api/trans/service-support"


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
        @Header(LBE_SESSION) lbeSession: String,
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

    @POST(FAQ)
    suspend fun faq(
        @Header(LBE_TOKEN) lbeToken: String,
        @Header(LBE_IDENTITY) lbeIdentity: String,
        @Header(LBE_SESSION) lbeSession: String,
        @Body body: FaqReqBody
    )

    @POST(TURN)
    suspend fun turnCustomerService(
        @Header(LBE_SIGN) lbeSign: String,
        @Header(LBE_TOKEN) lbeToken: String,
        @Header(LBE_IDENTITY) lbeIdentity: String,
        @Header(LBE_SESSION) lbeSession: String,
    )
}