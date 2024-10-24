package info.hermiths.chatapp.model.req

data class SessionBody(
    val extraInfo: String,
    val headIcon: String,
    val nickId: String,
    val nickName: String,
    val uid: String,
)
