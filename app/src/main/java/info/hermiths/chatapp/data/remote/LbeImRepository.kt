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

    suspend fun fetchSessionList(lbeToken: String, body: SessionListReq): SessionListRep {
        return lbeIMRepository.fetchSessionList(lbeToken = lbeToken, body)
    }

    suspend fun createSession(lbeSign: String, body: SessionBody): Session {
        return lbeIMRepository.createSession(lbeSign = lbeSign, body);
    }

    suspend fun fetchHistory(lbeSign: String, lbeToken: String, body: HistoryBody): History {
        return lbeIMRepository.fetchHistory(lbeSign = lbeSign, lbeToken = lbeToken, body);
    }

    suspend fun sendMsg(lbeToken: String, lbeSession: String, body: MsgBody): SendMsg {
        return lbeIMRepository.sendMsg(lbeToken = lbeToken, lbeSession = lbeSession, body);
    }
}