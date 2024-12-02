package info.hermiths.lbesdk.data.remote

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
import info.hermiths.lbesdk.service.RetrofitInstance

object LbeImRepository {
    private val lbeIMRepository = RetrofitInstance.imApiService

    suspend fun fetchSessionList(
        lbeToken: String, lbeIdentity: String, body: SessionListReq
    ): SessionListRep {
        return lbeIMRepository.fetchSessionList(
            lbeToken = lbeToken, lbeIdentity = lbeIdentity, body
        )
    }

    suspend fun createSession(lbeSign: String, lbeIdentity: String, body: SessionBody): Session {
        return lbeIMRepository.createSession(lbeSign = lbeSign, lbeIdentity = lbeIdentity, body);
    }

    suspend fun fetchHistory(
        lbeSign: String, lbeToken: String, lbeIdentity: String, body: HistoryBody
    ): History {
        return lbeIMRepository.fetchHistory(
            lbeSign = lbeSign, lbeToken = lbeToken, lbeIdentity = lbeIdentity, body = body
        )
    }

    suspend fun sendMsg(
        lbeToken: String, lbeSession: String, lbeIdentity: String, body: MsgBody
    ): SendMsg {
        return lbeIMRepository.sendMsg(
            lbeToken = lbeToken, lbeSession = lbeSession, lbeIdentity = lbeIdentity, body = body
        )
    }

    suspend fun fetchTimeoutConfig(
        lbeSign: String, lbeToken: String, lbeIdentity: String
    ): TimeoutRespBody {
        return lbeIMRepository.fetchTimeoutConfig(
            lbeSign = lbeSign,
            lbeToken = lbeToken,
            lbeIdentity = lbeIdentity,
            body = TimeoutReqBody(userType = 2)
        )
    }

    suspend fun markRead(
        lbeSign: String, lbeToken: String, lbeIdentity: String, body: MarkReadReqBody
    ) {
        lbeIMRepository.markRead(
            lbeSign = lbeSign, lbeToken = lbeToken, lbeIdentity = lbeIdentity, body = body
        )
    }

    suspend fun faq(
        lbeSession: String, lbeToken: String, lbeIdentity: String, body: FaqReqBody
    ) {
        lbeIMRepository.faq(
            lbeToken = lbeToken, lbeIdentity = lbeIdentity, lbeSession = lbeSession, body = body
        )
    }

    suspend fun turnCustomerService(
        lbeSign: String,
        lbeToken: String,
        lbeIdentity: String,
        lbeSession: String,
    ) {
        lbeIMRepository.turnCustomerService(
            lbeSign = lbeSign,
            lbeToken = lbeToken,
            lbeIdentity = lbeIdentity,
            lbeSession = lbeSession
        )
    }
}