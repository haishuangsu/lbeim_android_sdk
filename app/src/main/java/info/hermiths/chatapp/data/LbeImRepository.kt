package info.hermiths.chatapp.data

import info.hermiths.chatapp.model.req.ConfigBody
import info.hermiths.chatapp.model.req.HistoryBody
import info.hermiths.chatapp.model.req.MsgBody
import info.hermiths.chatapp.model.req.SessionBody
import info.hermiths.chatapp.model.resp.History
import info.hermiths.chatapp.model.resp.SendMsg
import info.hermiths.chatapp.model.resp.Session
import info.hermiths.chatapp.service.RetrofitInstance

object LbeImRepository {
    private val lbeIMRepository = RetrofitInstance.imApiService;

    suspend fun createSession(lbeSign: String, body: SessionBody): Session {
        return lbeIMRepository.createSession(lbeSign = lbeSign, body);
    }

    suspend fun fetchHistory(lbeSign: String, body: HistoryBody): History {
        return lbeIMRepository.fetchHistory(lbeSign = lbeSign, body);
    }

    suspend fun sendMsg(lbeToken: String, lbeSession: String, body: MsgBody): SendMsg {
        return lbeIMRepository.sendMsg(lbeToken = lbeToken, lbeSession = lbeSession, body);
    }
}