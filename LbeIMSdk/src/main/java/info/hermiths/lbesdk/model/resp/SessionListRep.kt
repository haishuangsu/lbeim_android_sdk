package info.hermiths.lbesdk.model.resp

import com.google.gson.annotations.SerializedName

data class SessionListRep(
    val code: Long,
    val msg: String,
    val dlt: String,
    @SerializedName("data") val data: SLData,
)

data class SLData(
    val total: Long,
    val sessionList: List<SessionEntry>,
)

data class SessionEntry(
    val sessionId: String = "",
    val nickName: String = "",
    val headIcon: String = "",
    val uid: String = "",
    val source: String = "",
    val language: String = "",
    val devNo: String = "",
    val extra: String = "",
    val latestMsg: LatestMsg?,
    val createTime: Long = 0,
    var sync: Boolean = false,
)

data class LatestMsg(
    val senderUid: String,
    val receiverUid: String,
    @SerializedName("clientMsgID") val clientMsgId: String,
    val msgType: Int,
    val msgSeq: Int,
    val msgBody: String,
    val status: Long,
    val createTime: Long,
)
