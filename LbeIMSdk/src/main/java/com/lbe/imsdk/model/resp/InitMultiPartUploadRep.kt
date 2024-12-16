package com.lbe.imsdk.model.resp

import com.google.gson.annotations.SerializedName


data class InitMultiPartUploadRep(
    val code: Long,
    val msg: String,
    val dlt: String,
    @SerializedName("data") val data: InitData,
)

data class InitData(
    val uploadId: String,
    val node: List<Node>,
)

data class Node(
    val url: String,
    val header: Header,
    val size: Long,
)

data class Header(
    @SerializedName("Authorization") val authorization: List<String>,
    @SerializedName("X-Amz-Content-Sha256") val xAmzContentSha256: List<String>,
    @SerializedName("X-Amz-Date") val xAmzDate: List<String>,
)