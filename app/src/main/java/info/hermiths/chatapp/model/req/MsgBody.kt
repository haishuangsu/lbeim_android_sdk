package info.hermiths.chatapp.model.req

import com.google.gson.annotations.SerializedName

data class MsgBody(
    @SerializedName("msg_body")
    val msgBody: String,
    @SerializedName("msg_seq")
    val msgSeq: Long,
    @SerializedName("msg_type")
    val msgType: Long,
    val source: Long,
)
