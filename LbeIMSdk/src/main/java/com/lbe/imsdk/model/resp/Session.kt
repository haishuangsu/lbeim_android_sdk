package com.lbe.imsdk.model.resp

import com.google.gson.annotations.SerializedName

data class Session(
    val code: Long,
    val msg: String,
    val dlt: String,
    @SerializedName("data") val data: SessionData,
)

data class SessionData(
    val uid: String,
    val token: String,
    val sessionId: String,
)
