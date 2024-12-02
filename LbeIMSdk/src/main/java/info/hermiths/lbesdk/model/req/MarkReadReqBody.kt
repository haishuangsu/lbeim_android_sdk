package info.hermiths.lbesdk.model.req

import com.google.gson.annotations.SerializedName

data class MarkReadReqBody(
    val seq: Int,
    @SerializedName("sessionID")
    val sessionId: String,
)
