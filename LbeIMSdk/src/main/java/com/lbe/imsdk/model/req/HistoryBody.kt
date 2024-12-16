package com.lbe.imsdk.model.req

import com.google.gson.annotations.SerializedName


data class HistoryBody(
    @SerializedName("seqCondition")
    val seqCondition: SeqCondition,
    @SerializedName("sessionId")
    val sessionId: String,
)

data class SeqCondition(
    val endSeq: Int,
    val startSeq: Int,
)
