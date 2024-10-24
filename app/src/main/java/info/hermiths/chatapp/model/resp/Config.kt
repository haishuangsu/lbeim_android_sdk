package info.hermiths.chatapp.model.resp

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
