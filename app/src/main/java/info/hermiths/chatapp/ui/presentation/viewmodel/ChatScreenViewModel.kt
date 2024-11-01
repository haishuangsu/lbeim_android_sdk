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
import info.hermiths.chatapp.utils.TimeUtils.timeStampGen
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
import info.hermiths.chatapp.service.ChatService
import info.hermiths.chatapp.service.DynamicHeaderUrlRequestFactory
import info.hermiths.chatapp.service.RetrofitInstance
import info.hermiths.chatapp.ui.presentation.screen.ChatScreenUiState
import info.hermiths.chatapp.utils.Converts.entityToSendBody
import info.hermiths.chatapp.utils.Converts.protoToEntity
import info.hermiths.chatapp.utils.Converts.sendBodyToEntity
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
    }

    private val _uiState = MutableLiveData(ChatScreenUiState())
    val uiState: LiveData<ChatScreenUiState> = _uiState

    private val _inputMsg = MutableLiveData("")
    val inputMsg: LiveData<String> = _inputMsg

    private var chatService: ChatService? = null

    init {
//        filterLocalMessages("cn-4385obtsiy15")
        prepare()
    }

    private fun prepare() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                fetchConfig()
                createSession()
                fetchSessionList()
                filterLocalMessages()
                fetchHistoryAndSync()
                viewModelScope.launch(Dispatchers.IO) {
                    delay(55)
                    observerConnection()
                }
            } catch (e: Exception) {
                println("Prepare error: $e")
            }
        }
    }

    // TODO check cache, lack Pagination
    private fun filterLocalMessages(sessionId: String = lbeSession) {
        val cacheMessages = IMLocalRepository.filterMessages(sessionId).subList(81, 101)
        Log.d(REALMTAG, "find all msg filter ---->>> ${cacheMessages.map { m -> m.msgBody }}")
        viewModelScope.launch(Dispatchers.Main) {
            val messages = uiState.value?.messages?.toMutableList()
            messages?.clear()
            messages?.addAll(cacheMessages)
            _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
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
        val sessionList = LbeImRepository.fetchSessionList(
            lbeToken = lbeToken, body = SessionListReq(
                pagination = Pagination(
                    pageNumber = 1, showNumber = 1000
                ), sessionType = 0
            )
        )
    }

    private suspend fun fetchHistoryAndSync() {
        // TODO sync history
        val history = LbeImRepository.fetchHistory(
            BuildConfig.lbeSign, lbeToken, HistoryBody(
                sessionId = lbeSession, seqCondition = SeqCondition(startSeq = 0, endSeq = 1000)
            )
        )
        for (content in history.data.content) {
            if (content.msgType == 1 || content.msgType == 2 || content.msgType == 3) {
                val entity = MessageEntity().apply {
                    sessionId = content.sessionId
                    senderUid = content.senderUid
                    msgBody = content.msgBody
                    clientMsgID = content.clientMsgID
                    msgType = content.msgType
                    sendStamp = content.clientMsgID.split("-").last().toLong()
                }
                IMLocalRepository.insertMessage(entity)
            }
        }

        if (history.data.content.isNotEmpty()) {
            seq = history.data.content.last().msgSeq
        }
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            filterLocalMessages()
        }
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
                    // TODO update
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