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
    @SerializedName("senderUid") val senderUid: String,
    @SerializedName("receiverUid") val receiverUid: String,
    @SerializedName("msgType") val msgType: Long,
    @SerializedName("msgSeq") val msgSeq: Long,
    @SerializedName("msgBody") val msgBody: String,
    val status: Long,
)
