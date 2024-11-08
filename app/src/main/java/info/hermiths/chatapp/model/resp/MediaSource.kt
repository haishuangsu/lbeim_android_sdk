package info.hermiths.chatapp.model.resp

data class MediaSource(
    val thumbnail: Thumbnail? = null,
    val resource: Resource,
)

data class Thumbnail(
    val url: String,
    val key: String,
)

data class Resource(
    val url: String,
    val key: String,
)
