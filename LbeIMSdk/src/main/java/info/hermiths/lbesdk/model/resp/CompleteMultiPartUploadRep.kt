package info.hermiths.lbesdk.model.resp

import com.google.gson.annotations.SerializedName

data class CompleteMultiPartUploadRep(
    val code: Long,
    val msg: String,
    val dlt: String,
    @SerializedName("data") val data: MergeData,
)

data class MergeData(
    val location: String,
    val etag: String,
)