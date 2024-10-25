package info.hermiths.chatapp.model.req

import com.google.gson.annotations.SerializedName

data class MsgBody(
    @SerializedName("msgBody")
    val msgBody: String,
    @SerializedName("msgSeq")
    val msgSeq: Long,
    @SerializedName("msgType")
    val msgType: Long,
    val source: Long,
)
