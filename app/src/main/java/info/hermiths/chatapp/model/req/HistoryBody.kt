package info.hermiths.chatapp.model.req
import com.google.gson.annotations.SerializedName


data class HistoryBody(
    @SerializedName("seq_condition")
    val seqCondition: SeqCondition,
    @SerializedName("session_id")
    val sessionId: String,
)

data class SeqCondition(
    val endSeq: Long,
    val startSeq: Long,
)
