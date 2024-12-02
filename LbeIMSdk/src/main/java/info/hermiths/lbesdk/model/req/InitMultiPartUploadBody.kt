package info.hermiths.lbesdk.model.req


data class InitMultiPartUploadBody(
    val size: Long,
    val name: String,
    val contentType: String,
)
