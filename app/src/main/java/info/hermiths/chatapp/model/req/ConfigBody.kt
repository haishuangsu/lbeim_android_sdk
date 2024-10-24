package info.hermiths.chatapp.model.req

import com.google.gson.annotations.SerializedName

data class ConfigBody(
    @SerializedName("role_type")
    val roleType: Long,
    val source: Long,
)
