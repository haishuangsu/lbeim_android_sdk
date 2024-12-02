package info.hermiths.lbesdk.model.req

data class SessionBody(
    val extraInfo: String,
    val headIcon: String,
    val nickId: String,
    val nickName: String,
    val uid: String,
    val language: String,
)
