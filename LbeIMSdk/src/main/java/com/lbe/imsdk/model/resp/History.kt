package com.lbe.imsdk.model.resp

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
    @SerializedName("msgType") val msgType: Int,
    @SerializedName("msgSeq") val msgSeq: Int,
    @SerializedName("msgBody") val msgBody: String,
    val clientMsgID: String,
    val sendTime: String,
    val status: Int,
    val createTime: Long,
    val sessionId: String,
    val senderNickname: String,
    val senderFaceURL: String,
)
