package info.hermiths.lbesdk.model.req

import com.google.gson.annotations.SerializedName

data class ConfigBody(
    @SerializedName("roleType")
    val roleType: Long,
    val source: Long,
)
