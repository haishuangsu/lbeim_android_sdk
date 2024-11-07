package info.hermiths.chatapp.model.req

data class CompleteMultiPartUploadReq(
    val uploadId: String,
    val name: String,
    val part: MutableList<Part>,
)

data class Part(
    val partNumber: Int,
    val etag: String,
)
