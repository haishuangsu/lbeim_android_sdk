package info.hermiths.chatapp.ui.presentation.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinder.scarlet.Message
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.WebSocket.Event.OnConnectionClosed
import com.tinder.scarlet.WebSocket.Event.OnConnectionClosing
import com.tinder.scarlet.WebSocket.Event.OnConnectionFailed
import com.tinder.scarlet.WebSocket.Event.OnConnectionOpened
import com.tinder.scarlet.WebSocket.Event.OnMessageReceived
import com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import info.hermiths.chatapp.BuildConfig
import info.hermiths.chatapp.data.IMLocalRepository
import info.hermiths.chatapp.data.LbeConfigRepository
import info.hermiths.chatapp.data.LbeImRepository
import info.hermiths.chatapp.model.MessageEntity
import info.hermiths.chatapp.model.proto.IMMsg
import info.hermiths.chatapp.model.req.ConfigBody
import info.hermiths.chatapp.model.req.HistoryBody
import info.hermiths.chatapp.model.req.MsgBody
import info.hermiths.chatapp.model.req.Pagination
import info.hermiths.chatapp.model.req.SeqCondition
import info.hermiths.chatapp.model.req.SessionBody
import info.hermiths.chatapp.model.req.SessionListReq
import info.hermiths.chatapp.model.resp.History
import info.hermiths.chatapp.model.resp.SessionEntry
import info.hermiths.chatapp.service.ChatService
import info.hermiths.chatapp.service.DynamicHeaderUrlRequestFactory
import info.hermiths.chatapp.service.RetrofitInstance
import info.hermiths.chatapp.ui.presentation.screen.ChatScreenUiState
import info.hermiths.chatapp.utils.Converts.entityToSendBody
import info.hermiths.chatapp.utils.Converts.protoToEntity
import info.hermiths.chatapp.utils.Converts.sendBodyToEntity
import info.hermiths.chatapp.utils.TimeUtils.timeStampGen
import info.hermiths.chatapp.utils.UUIDUtils.uuidGen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

enum class ConnectionStatus {
    NOT_STARTED, OPENED, CLOSED, CONNECTING, CLOSING, FAILED, RECEIVED
}

class ChatScreenViewModel : ViewModel() {

    companion object {
        private const val TAG = "ChatScreenViewModel"
        private const val REALMTAG = "RealmTAG"
        var uid = "c-4385obtijcnd"
        var wssHost = ""
        var oss = ""
        var lbeToken = ""
        var lbeSession = ""
        var seq: Int = 0
        var sessionList: MutableList<SessionEntry> = mutableListOf()
        var currentSession: SessionEntry? = null
        var currentSessionIndex = 0
        var currentSessionTotalPages = 0
        var showPageNums = 10
        var currentPage = 14
    }

    private val _uiState = MutableLiveData(ChatScreenUiState())
    val uiState: LiveData<ChatScreenUiState> = _uiState

    private val _inputMsg = MutableLiveData("")
    val inputMsg: LiveData<String> = _inputMsg

    private var chatService: ChatService? = null

    init {
        prepare()
    }

    private fun prepare() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                fetchConfig()
                createSession()
                viewModelScope.launch(Dispatchers.IO) {
                    fetchSessionList()
                    observerConnection()
                }
            } catch (e: Exception) {
                println("Prepare error: $e")
            }
        }
    }

    private suspend fun fetchConfig() {
        val config = LbeConfigRepository.fetchConfig(BuildConfig.lbeSign, ConfigBody(0, 1))
        wssHost = config.data.ws[0]
        oss = config.data.oss[0]
        RetrofitInstance.IM_URL = config.data.rest[0]
    }

    private suspend fun createSession() {
        val session = LbeImRepository.createSession(
            BuildConfig.lbeSign, SessionBody(
                extraInfo = "",
                headIcon = "",
                nickId = "HermitK15",
                nickName = "HermitK15",
                uid = ""
            )
        )
        lbeToken = session.data.token
        lbeSession = session.data.sessionId
        uid = session.data.uid
    }

    private suspend fun fetchSessionList() {
        val sessionListRep = LbeImRepository.fetchSessionList(
            lbeToken = lbeToken, body = SessionListReq(
                pagination = Pagination(
                    pageNumber = 1, showNumber = 1000
                ), sessionType = 0
            )
        )
        sessionList.addAll(sessionListRep.data.sessionList)
        currentSession = sessionList[currentSessionIndex]
        filterLocalMessages()
    }

    // TODO 分页只改变 currentPage; 跨会话改变 currentSession, currentPage > currentSessionTotalPages
    private suspend fun filterLocalMessages(sid: String = currentSession?.sessionId ?: "") {
        var cacheMessages = IMLocalRepository.filterMessages(sid)
        val remoteSeq = currentSession?.latestMsg?.msgSeq ?: 0
        Log.d(
            REALMTAG,
            "find all msg filter ---->>> ${cacheMessages.map { m -> "(${m.msgBody},${m.msgSeq})" }}"
        )
        Log.d(
            REALMTAG,
            "缓存 size: ${cacheMessages.size}, 缓存 lastSeq: ${cacheMessages.last().msgSeq} ; remote size: ${currentSession?.latestMsg?.msgSeq}, remote lastSeq: $remoteSeq "
        )

        // sync
        if (cacheMessages.size < remoteSeq || cacheMessages.last().msgSeq < remoteSeq) {
            fetchHistoryAndSync()
        }

        // query newest cache
        cacheMessages = IMLocalRepository.filterMessages(sid)
        currentSessionTotalPages = cacheMessages.size / showPageNums
        val yu = cacheMessages.size % showPageNums
        Log.d(REALMTAG, "分页总页数: $currentSessionTotalPages, 取余: $yu")

        val subList = if (currentPage == currentSessionTotalPages && yu != 0) {
            val start = Math.max((currentPage - 1) * showPageNums, 0)
            val end = Math.min(currentPage * showPageNums + yu, cacheMessages.size)
            Log.d(REALMTAG, "最后一页 --->>> start: $start, end: $end")
            cacheMessages.subList(start, end)
        } else {
            val start = Math.max((currentPage - 1) * showPageNums, 0)
            val end = Math.min(currentPage * showPageNums, cacheMessages.size)
            Log.d(REALMTAG, "非最后一页 --->>> start: $start, end: $end")
            cacheMessages.subList(start, end)
        }
        viewModelScope.launch(Dispatchers.Main) {
            val messages = uiState.value?.messages?.toMutableList()
            // TODO 不清空，取增量
            messages?.clear()
            messages?.addAll(subList)
            _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
        }
    }

    private suspend fun fetchHistoryAndSync(): History {
        val history = LbeImRepository.fetchHistory(
            BuildConfig.lbeSign, lbeToken, HistoryBody(
                sessionId = currentSession?.sessionId ?: "",
                seqCondition = SeqCondition(startSeq = 0, endSeq = 1000)
            )
        )
        Log.d(REALMTAG, "History sync")
        if (history.data.content.isNotEmpty()) {
            seq = history.data.content.last().msgSeq
            for (content in history.data.content) {
                if (content.msgType == 1 || content.msgType == 2 || content.msgType == 3) {
                    val entity = MessageEntity().apply {
                        sessionId = content.sessionId
                        senderUid = content.senderUid
                        msgBody = content.msgBody
                        clientMsgID = content.clientMsgID
                        msgType = content.msgType
                        sendStamp = content.clientMsgID.split("-").last().toLong()
                        msgSeq = content.msgSeq
                    }
                    IMLocalRepository.insertMessage(entity)
                }
            }
        }
        return history
    }

    @SuppressLint("CheckResult")
    private fun observerConnection() {
        chatService = Scarlet.Builder().webSocketFactory(
            OkHttpClient.Builder().build().newWebSocketFactory(
                DynamicHeaderUrlRequestFactory(
                    url = wssHost, lbeToken = lbeToken, lbeSession = lbeSession,
                )
            )
        ).addMessageAdapterFactory(ProtobufMessageAdapter.Factory())
            .addStreamAdapterFactory(RxJava2StreamAdapterFactory()).build().create<ChatService>()

        Log.d(TAG, "Observing Connection")
        updateConnectionStatus(ConnectionStatus.CONNECTING)
        chatService?.observeConnection()?.subscribe({ response ->
            Log.d(TAG, response.toString())
            onResponseReceived(response)
        }, { error ->
            error.localizedMessage?.let { Log.e(TAG, it) }
        })
    }

    private fun onResponseReceived(response: WebSocket.Event) {
        when (response) {
            is OnConnectionOpened<*> -> updateConnectionStatus(ConnectionStatus.OPENED)

            is OnConnectionClosed -> updateConnectionStatus(ConnectionStatus.CLOSED)

            is OnConnectionClosing -> updateConnectionStatus(ConnectionStatus.CLOSING)

            is OnConnectionFailed -> updateConnectionStatus(ConnectionStatus.FAILED)

            is OnMessageReceived -> handleOnMessageReceived(response.message)
        }
    }

    private fun handleOnMessageReceived(message: Message) {
        Log.d(TAG, "handleOnMessageReceived: $message")
        try {
            val value = (message as Message.Bytes).value
            viewModelScope.launch {
                // val chatMsg= Gson().fromJson(value, ChatMessage::class.java)
                val msgEntity = IMMsg.MsgEntityToFrontEnd.parseFrom(value)
                Log.d(TAG, "handleOnMessageReceived protobuf bytes: $msgEntity")
                if (msgEntity.msgBody.sessionId.isEmpty() || msgEntity.msgBody.clientMsgID.isEmpty()) {
                    return@launch
                }
                val receivedReq = msgEntity.msgBody.msgSeq
                if (receivedReq - seq > 2) {
                    fetchHistoryAndSync()
                } else {
                    seq = receivedReq
                }
                viewModelScope.launch {
                    val entity = protoToEntity(msgEntity.msgBody)
                    IMLocalRepository.insertMessage(entity)
                    if (entity.senderUid != uid) {
                        attachMsgToUI(entity)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleOnMessageReceived: ", e)
        }
    }

    private fun updateConnectionStatus(connectionStatus: ConnectionStatus) {
        viewModelScope.launch(Dispatchers.Main) {
            _uiState.postValue(_uiState.value?.copy(connectionStatus = connectionStatus))
        }
    }

    fun onMessageChange(message: String) {
        _inputMsg.postValue(message)
    }

    fun sendMessage(messageSent: () -> Unit) {
        if ((_inputMsg.value ?: "").isEmpty()) return

        val uuid = uuidGen()
        val timeStamp = timeStampGen()
        val clientMsgId = "${uuid}-${timeStamp}"
        val body = MsgBody(
            msgBody = _inputMsg.value ?: "",
            msgSeq = seq,
            msgType = 1,
            clientMsgId = clientMsgId,
            source = 100
        )
        val entity = sendBodyToEntity(body)
        viewModelScope.launch(Dispatchers.IO) {
            IMLocalRepository.insertMessage(entity)
            try {
                val senMsg = LbeImRepository.sendMsg(
                    lbeToken = lbeToken, lbeSession = lbeSession, body
                )
                seq = senMsg.data.msgReq
                IMLocalRepository.findMsgAndSetSeq(clientMsgId, seq)
            } catch (e: Exception) {
                println("send error -->> $e")
                IMLocalRepository.findMsgAndSetStatus(clientMsgId, false)
            } finally {
                filterLocalMessages()
                viewModelScope.launch(Dispatchers.Main) {
                    messageSent()
                    clearInput()
                }
            }
        }
    }

    fun reSendMessage(clientMsgId: String) {
        val entity = IMLocalRepository.findMsgByClientMsgId(clientMsgId)
        var newClientMsgId = ""
        if (entity != null) {
            val list = entity.clientMsgID.split("-").toMutableList()
            list.removeLast()
            list.add(timeStampGen().toString())
            newClientMsgId = list.joinToString(separator = "-")
            val body = entityToSendBody(entity, newClientMsgId)
            println("reSend ====>>> old: ${entity.clientMsgID}, new: $newClientMsgId")
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val senMsg = LbeImRepository.sendMsg(
                        lbeToken = lbeToken, lbeSession = lbeSession, body
                    )
                    seq = senMsg.data.msgReq
                    IMLocalRepository.updateResendMessage(clientMsgId, newClientMsgId, seq)
                } catch (e: Exception) {
                    println("send error -->> $e")
                } finally {
                    // TODO
                    filterLocalMessages()
                }
            }
        }
    }

    private fun attachMsgToUI(message: MessageEntity) {
        Log.d(TAG, "attachMsgToUI: $message")
        val messages = uiState.value?.messages?.toMutableList()
        messages?.add(message)
        _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
    }

    private fun clearInput() {
        viewModelScope.launch {
            delay(50)
            _inputMsg.postValue("")
        }
    }
}