package info.hermiths.chatapp.model.resp

import com.google.gson.annotations.SerializedName

data class History(
    val code: Long,
    val msg: String,
    val dlt: String,
    @SerializedName("data") val data: HistoryData,
)

data class HistoryData(
    val total: Long,
    val content: List<Content>,
)

data class Content(
    @SerializedName("sender_uid") val senderUid: String,
    @SerializedName("receiver_uid") val receiverUid: String,
    @SerializedName("msg_type") val msgType: Long,
    @SerializedName("msg_seq") val msgSeq: Long,
    @SerializedName("msg_body") val msgBody: String,
    val status: Long,
)
