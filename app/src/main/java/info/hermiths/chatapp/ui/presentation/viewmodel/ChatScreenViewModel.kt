package info.hermiths.chatapp.ui.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinder.scarlet.Message
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.WebSocket.Event.*
import com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import info.hermiths.chatapp.BuildConfig
import info.hermiths.chatapp.service.ChatService
import info.hermiths.chatapp.model.proto.test.Msg
import info.hermiths.chatapp.data.LbeIMRepository
import info.hermiths.chatapp.model.req.ConfigBody
import info.hermiths.chatapp.model.req.HistoryBody
import info.hermiths.chatapp.model.req.MsgBody
import info.hermiths.chatapp.model.req.SeqCondition
import info.hermiths.chatapp.model.req.SessionBody
import info.hermiths.chatapp.ui.data.enums.ConnectionStatus
import info.hermiths.chatapp.ui.data.model.ChatMessage
import info.hermiths.chatapp.ui.presentation.screen.ChatScreenUiState
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class ChatScreenViewModel : ViewModel() {
    private val TAG = "ChatScreenViewModel"

    private val wss_url = "ws://10.40.90.67:16868"

    private val _uiState = MutableLiveData(ChatScreenUiState())
    val uiState: LiveData<ChatScreenUiState> = _uiState

    private val _inputMsg = MutableLiveData("")
    val inputMsg: LiveData<String> = _inputMsg


    private var chatService: ChatService = Scarlet.Builder()
        .webSocketFactory(OkHttpClient.Builder().build().newWebSocketFactory(wss_url))
        .addMessageAdapterFactory(ProtobufMessageAdapter.Factory())
        .addStreamAdapterFactory(RxJava2StreamAdapterFactory()).build().create<ChatService>()

    init {
        // prepare()
        observerConnection()
    }

    private fun prepare() {
        viewModelScope.launch {
            try {
                val config = LbeIMRepository.fetchConfig(BuildConfig.lbeSign, ConfigBody(0, 1))
                println("Retrofit fetch config ===>>> $config")

                val session = LbeIMRepository.createSession(
                    BuildConfig.lbeSign,
                    SessionBody(extraInfo = "", headIcon = "", nickId = "", nickName = "", uid = "")
                )
                println("Retrofit fetch session ===>>> $session")

                val history = LbeIMRepository.fetchHistory(
                    BuildConfig.lbeSign, HistoryBody(
                        sessionId = session.data.sessionId,
                        seqCondition = SeqCondition(startSeq = 0, endSeq = 10)
                    )
                )
                println("Retrofit fetch history ===>>> $history")

                val senMsg = LbeIMRepository.sendMsg(
                    lbeToken = session.data.token,
                    lbeSession = session.data.sessionId,
                    MsgBody(msgBody = "Send by retrofit", msgSeq = 1, msgType = 1, source = 100)
                )
                println("Retrofit send msg ===>>> $senMsg")
            } catch (e: Exception) {
                println("FetchConfig error: $e")
            }
        }
    }


    private fun observerConnection() {
        Log.d(TAG, "Observing Connection")
        updateConnectionStatus(ConnectionStatus.CONNECTING)
        chatService.observeConnection().subscribe({ response ->
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
        Log.d(TAG, "handleOnMessageReceived Byte: ${(message as Message.Bytes).value}")
        try {
            val value = (message as Message.Bytes).value
            viewModelScope.launch {
                val hxMsgEntity = Msg.HxMsgEntity.parseFrom(value)
                Log.d(TAG, "handleOnMessageReceived protobuf bytes: $hxMsgEntity")
                // val chatMsg= Gson().fromJson(value, ChatMessage::class.java)
                val chatMessage =
                    ChatMessage(fromUser = hxMsgEntity.user, message = hxMsgEntity.msg)
                if (chatMessage.fromUser != uiState.value?.user) {
                    attachMsgToUI(chatMessage)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleOnMessageReceived: ", e)
        }
    }

    private fun updateConnectionStatus(connectionStatus: ConnectionStatus) {
        _uiState.postValue(_uiState.value?.copy(connectionStatus = connectionStatus))
    }

    fun onMessageChange(message: String) {
        _inputMsg.postValue(message)
//        _uiState.postValue(_uiState.value?.copy(inputMsg = message))
    }

    private fun message(): ChatMessage {
//        return _uiState.value?.let { ChatMessage(fromUser = it.user, message = it.inputMsg) }
//            ?: ChatMessage("", "")
        return ChatMessage(fromUser = _uiState.value?.user ?: "", message = _inputMsg.value ?: "")
    }


    fun sendMessage(messageSent: () -> Unit) {
        val message = message()
        if (message.message.isEmpty()) return

        val hxMsgEntity =
            Msg.HxMsgEntity.newBuilder().setUser(_uiState.value?.user).setMsg(message.message)
                .build()
        val sendBuff = hxMsgEntity.toByteArray();

        chatService.sendMessage(sendBuff).also {
            messageSent()
        }
        attachMsgToUI(message)
        clearInput()
    }

    private fun attachMsgToUI(message: ChatMessage) {
        Log.d(TAG, "attachMsgToUI: $message")
        val messages = uiState.value?.messages?.toMutableList()
        messages?.add(message)
        _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
    }


    private fun clearInput() {
        viewModelScope.launch {
            delay(50)
//            _uiState.postValue(_uiState.value?.copy(inputMsg = ""))
            _inputMsg.postValue("")
        }
    }
}