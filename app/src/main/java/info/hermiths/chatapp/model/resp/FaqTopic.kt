package info.hermiths.chatapp.model.resp

data class FaqTopic(
    val knowledgeBaseTitle: String,
    val knowledgeBaseList: MutableList<KnowledgeBaseList>,
)

data class KnowledgeBaseList(
    val id: String,
    val url: String,
    val knowledgeBaseName: String,
)

data class FaqEntryUrl(
    val key: String,
    val url: String,
)
