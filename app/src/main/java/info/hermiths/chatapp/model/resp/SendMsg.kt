package info.hermiths.chatapp.model.resp

import com.google.gson.annotations.SerializedName


data class SendMsg(
    val code: Long,
    val msg: String,
    val dlt: String,
    @SerializedName("data") val data: SendData,
)

data class SendData(
    @SerializedName("msg_req") val msgReq: Long,
)