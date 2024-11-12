package info.hermiths.chatapp.data.remote

import info.hermiths.chatapp.model.req.HistoryBody
import info.hermiths.chatapp.model.req.MsgBody
import info.hermiths.chatapp.model.req.SessionBody
import info.hermiths.chatapp.model.req.SessionListReq
import info.hermiths.chatapp.model.resp.History
import info.hermiths.chatapp.model.resp.SendMsg
import info.hermiths.chatapp.model.resp.Session
import info.hermiths.chatapp.model.resp.SessionListRep
import info.hermiths.chatapp.service.RetrofitInstance

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
}