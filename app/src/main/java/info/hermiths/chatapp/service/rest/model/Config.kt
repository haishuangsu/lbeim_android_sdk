package info.hermiths.chatapp.service.rest.model

data class Config(
    val code: Long,
    val msg: String,
    val dlt: String,
    val data: Data,
)

data class Data(
    val oss: List<String>,
    val ws: List<String>,
    val rest: List<String>,
)
