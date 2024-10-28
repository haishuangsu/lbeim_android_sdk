package info.hermiths.chatapp.model.resp

import com.google.gson.annotations.SerializedName

data class SessionListRep(
    val code: Long,
    val msg: String,
    val dlt: String,
    @SerializedName("data") val data: SLData,
)

data class SLData(
    val total: Long,
    val sessionList: List<SessionList>,
)

data class SessionList(
    val sessionId: String,
    val nickName: String,
    val headIcon: String,
    val uid: String,
    val source: String,
    val language: String,
    val devNo: String,
    val extra: String,
    val latestMsg: LatestMsg,
    val createTime: Long,
)

data class LatestMsg(
    val senderUid: String,
    val receiverUid: String,
    @SerializedName("clientMsgID") val clientMsgId: String,
    val msgType: Long,
    val msgSeq: Long,
    val msgBody: String,
    val status: Long,
    val createTime: Long,
)
