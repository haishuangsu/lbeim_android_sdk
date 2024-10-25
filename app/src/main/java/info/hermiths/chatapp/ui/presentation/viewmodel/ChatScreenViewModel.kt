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
import com.tinder.scarlet.WebSocket.Event.*
import com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import info.hermiths.chatapp.BuildConfig
import info.hermiths.chatapp.service.ChatService
import info.hermiths.chatapp.data.LbeConfigRepository
import info.hermiths.chatapp.data.LbeImRepository
import info.hermiths.chatapp.model.proto.IMMsg
import info.hermiths.chatapp.model.req.ConfigBody
import info.hermiths.chatapp.model.req.HistoryBody
import info.hermiths.chatapp.model.req.MsgBody
import info.hermiths.chatapp.model.req.SeqCondition
import info.hermiths.chatapp.model.req.SessionBody
import info.hermiths.chatapp.service.DynamicHeaderUrlRequestFactory
import info.hermiths.chatapp.service.RetrofitInstance
import info.hermiths.chatapp.ui.data.enums.ConnectionStatus
import info.hermiths.chatapp.ui.data.model.ChatMessage
import info.hermiths.chatapp.ui.presentation.screen.ChatScreenUiState
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class ChatScreenViewModel : ViewModel() {

    companion object {
        private const val TAG = "ChatScreenViewModel"
        var csNickName = "客服007"
        var customerName = "hermiths"
        var wssHost = ""
        var oss = ""
        var lbeToken = ""
        var lbeSession = ""
        var seq: Long = 9
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
        viewModelScope.launch {
            try {
                val config = LbeConfigRepository.fetchConfig(BuildConfig.lbeSign, ConfigBody(0, 1))
                println("Retrofit fetch config ===>>> $config")
                wssHost = config.data.ws[0]
                oss = config.data.oss[0]
                RetrofitInstance.IM_URL = config.data.rest[0]
                println("http update config ====>>>> restHost: ${RetrofitInstance.IM_URL}, wwsHost: $wssHost")

                val session = LbeImRepository.createSession(
                    BuildConfig.lbeSign, SessionBody(
                        extraInfo = "",
                        headIcon = "",
                        nickId = "9527",
                        nickName = "HermitHs",
                        uid = ""
                    )
                )
                println("Retrofit fetch session ===>>> $session")
                lbeToken = session.data.token
                lbeSession = session.data.sessionId
                println("http update session ====>>>> lbeToken: $lbeToken, lbeSession: $lbeSession")
                observerConnection()

                val history = LbeImRepository.fetchHistory(
                    BuildConfig.lbeSign, HistoryBody(
                        sessionId = session.data.sessionId,
                        seqCondition = SeqCondition(startSeq = 0, endSeq = 10)
                    )
                )
                println("Retrofit fetch history ===>>> $history")
            } catch (e: Exception) {
                println("FetchConfig error: $e")
            }
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
//        Log.d(TAG, "handleOnMessageReceived Byte: ${(message as Message.Bytes).value}")
        try {
            val value = (message as Message.Bytes).value
            viewModelScope.launch {
                // val chatMsg= Gson().fromJson(value, ChatMessage::class.java)
                val msgEntity = IMMsg.MsgEntityToFrontEnd.parseFrom(value)
                Log.d(TAG, "handleOnMessageReceived protobuf bytes: $msgEntity")
                Log.d(
                    TAG, "handleOnMessageReceived protobuf type ===>>  ${msgEntity.msgType}"
                )

                val chatMessage = ChatMessage(
                    fromUser = csNickName,
                    msgType = msgEntity.msgType,
                    message = msgEntity.msgBody.msgBody
                )
                if (chatMessage.fromUser != customerName) {
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
    }

    private fun message(): ChatMessage {
        return ChatMessage(
            fromUser = customerName,
            msgType = IMMsg.MsgType.TextMsgType,
            message = _inputMsg.value ?: ""
        )
    }

    fun sendMessage(messageSent: () -> Unit) {
        val message = message()
        if (message.message.isEmpty()) return

        viewModelScope.launch {
            seq += 1
            val senMsg = LbeImRepository.sendMsg(
                lbeToken = lbeToken,
                lbeSession = lbeSession,
                MsgBody(msgBody = message.message, msgSeq = seq, msgType = 1, source = 100)
            )
            println("Retrofit send msg ===>>> $senMsg")
            attachMsgToUI(message)
            messageSent()
            clearInput()
        }
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
            _inputMsg.postValue("")
        }
    }
}