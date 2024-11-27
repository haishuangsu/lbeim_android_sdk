package info.hermiths.chatapp.model.resp

data class FaqAnswer(
    val type: Int,
    val content: String,
    val contents: MutableList<LinkText>,
)

data class LinkText(
    val content: String,
    val url: String,
)