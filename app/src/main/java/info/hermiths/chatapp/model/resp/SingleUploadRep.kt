package info.hermiths.chatapp.model.resp

import com.google.gson.annotations.SerializedName


data class SingleUploadRep(
    val code: Long,
    val msg: String,
    val dlt: String,
    @SerializedName("data") val data: SData,
)

data class SData(
    val paths: List<Path>,
)

data class Path(
    val key: String,
    val url: String,
)
