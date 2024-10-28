package info.hermiths.chatapp.model.req

data class HistoryListReq(
    val pagination: Pagination,
    val sessionType: Long,
)

data class Pagination(
    val pageNumber: Long,
    val showNumber: Long,
)