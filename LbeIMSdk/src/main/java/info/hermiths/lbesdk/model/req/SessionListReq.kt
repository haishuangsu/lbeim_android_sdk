package info.hermiths.lbesdk.model.req

data class SessionListReq(
    val pagination: Pagination,
    val sessionType: Long,
)

data class Pagination(
    val pageNumber: Long,
    val showNumber: Long,
)