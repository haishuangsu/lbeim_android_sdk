package com.lbe.imsdk.data.remote

import com.lbe.imsdk.model.req.FaqReqBody
import com.lbe.imsdk.model.req.HistoryBody
import com.lbe.imsdk.model.req.MarkReadReqBody
import com.lbe.imsdk.model.req.MsgBody
import com.lbe.imsdk.model.req.SessionBody
import com.lbe.imsdk.model.req.SessionListReq
import com.lbe.imsdk.model.req.TimeoutReqBody
import com.lbe.imsdk.model.resp.History
import com.lbe.imsdk.model.resp.SendMsg
import com.lbe.imsdk.model.resp.Session
import com.lbe.imsdk.model.resp.SessionListRep
import com.lbe.imsdk.model.resp.TimeoutRespBody
import com.lbe.imsdk.service.RetrofitInstance

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
        return lbeIMRepository.createSession(lbeSign = lbeSign, lbeIdentity = lbeIdentity, body)
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