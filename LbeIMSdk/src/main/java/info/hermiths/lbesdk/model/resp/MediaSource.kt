package info.hermiths.lbesdk.model.resp

data class MediaSource(
    val width: Int,
    val height: Int,
    val thumbnail: Thumbnail,
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
