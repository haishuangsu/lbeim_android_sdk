package com.lbe.imsdk.model.resp

import com.google.gson.annotations.SerializedName


data class TimeoutRespBody(
    val code: Long,
    val msg: String,
    val dlt: String,
    @SerializedName("data") val data: TimeoutData,
)

data class TimeoutData(
    val isOpen: Boolean,
    val timeout: Long,
)
