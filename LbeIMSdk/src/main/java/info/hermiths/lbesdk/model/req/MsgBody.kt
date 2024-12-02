package info.hermiths.lbesdk.model.req

import com.google.gson.annotations.SerializedName

data class MsgBody(
    @SerializedName("msgBody")
    var msgBody: String,
    @SerializedName("msgSeq")
    val msgSeq: Int,
    @SerializedName("msgType")
    val msgType: Int,
    val source: Int,
    val clientMsgId: String,
    val sendTime: String,
)
