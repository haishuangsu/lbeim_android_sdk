package info.hermiths.chatapp.ui.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.SubcomposeAsyncImage

import info.hermiths.chatapp.R
import info.hermiths.chatapp.model.MessageEntity

import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel
import info.hermiths.chatapp.ui.presentation.components.MsgTypeContent
import info.hermiths.chatapp.ui.presentation.viewmodel.ConnectionStatus

data class ChatScreenUiState(
    var messages: List<MessageEntity> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.NOT_STARTED
)


enum class MessagePosition {
    LEFT, RIGHT
}

@Composable
fun ChatScreen(
    viewModel: ChatScreenViewModel = viewModel()
) {
    val context = LocalContext.current
//    ImageLoader.Builder(LocalContext.current)

    val uiState by viewModel.uiState.observeAsState(ChatScreenUiState())
    val input by viewModel.inputMsg.observeAsState("init")

    val currentFocus = LocalFocusManager.current
    val lazyListState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = "Connection Status: ${uiState.connectionStatus.name}")
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(top = 20.dp),
            state = lazyListState
        ) {
            items(uiState.messages) { message ->
                MessageItem(
                    message = message,
                    if (message.senderUid == ChatScreenViewModel.uid) MessagePosition.RIGHT
                    else MessagePosition.LEFT,
                    viewModel
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White, modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            ) {
                Image(painter = painterResource(R.drawable.open_file),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(5.dp)
                        .size(21.dp, 16.dp)
                        .clickable {
                            Toast
                                .makeText(
                                    context, "send pic || video", Toast.LENGTH_SHORT
                                )
                                .show()
                        })
            }

            Spacer(modifier = Modifier.width(12.dp))

            BasicTextField(value = input,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = viewModel::onMessageChange,
                maxLines = 5,
                decorationBox = { innerTextField ->
                    Surface(
                        color = Color.White,
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.weight(1f)) {
                                if (input.isEmpty()) Text(
                                    "请输入你想咨询的问题", style = TextStyle(
                                        color = Color(0xffEBEBEB),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.W400,
                                    )
                                )
                                innerTextField()
                            }

                            Image(painter = painterResource(R.drawable.send),
                                contentDescription = "Send Button",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        viewModel.sendMessage(messageSent = {
                                            currentFocus.clearFocus()
                                        })
                                    })
                        }
                    }
                })
        }
    }

    LaunchedEffect(key1 = uiState.messages) {
//        lazyListState.animateScrollToItem(uiState.messages.size)
        lazyListState.requestScrollToItem(uiState.messages.size)
    }
}


@Composable
fun MessageItem(
    message: MessageEntity, messagePosition: MessagePosition, viewModel: ChatScreenViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (messagePosition == MessagePosition.LEFT) 0.dp else 24.dp,
                end = if (messagePosition == MessagePosition.RIGHT) 0.dp else 24.dp
            ),
        contentAlignment = if (messagePosition == MessagePosition.LEFT) Alignment.TopStart else Alignment.BottomEnd
    ) {
        if (messagePosition == MessagePosition.LEFT) {
            CsRecived(message, messagePosition)
        } else {
            UserInput(message, messagePosition, viewModel = viewModel)
        }
    }
}

@Composable
fun CsRecived(message: MessageEntity, messagePosition: MessagePosition) {
    Row {
        Image(
            painter = painterResource(id = R.drawable.cs_avatar),
            contentDescription = "",
            modifier = Modifier.size(32.dp)
        )

        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = message.senderUid,
                modifier = Modifier.align(if (messagePosition == MessagePosition.LEFT) Alignment.Start else Alignment.End),
                style = TextStyle(
                    fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff979797)
                )
            )
            Spacer(Modifier.height(8.dp))
            MsgTypeContent(message)
        }
    }
}

@Composable
fun UserInput(
    message: MessageEntity, messagePosition: MessagePosition, viewModel: ChatScreenViewModel
) {
    Row(horizontalArrangement = Arrangement.End) {
        Column(
            horizontalAlignment = Alignment.End, modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = message.senderUid,
                modifier = Modifier.align(if (messagePosition == MessagePosition.LEFT) Alignment.Start else Alignment.End),
                style = TextStyle(
                    fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff979797)
                )
            )
            Spacer(Modifier.height(8.dp))


            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!message.sendSuccess) {
                    Image(painter = painterResource(R.drawable.send_fail),
                        contentDescription = "send fail",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                viewModel.reSendMessage(message.clientMsgID)
                            })
                    Spacer(Modifier.width(9.dp))
                }

                Surface(
                    color = Color(0xff0054FC).copy(alpha = 0.1f), modifier = Modifier.clip(
                        RoundedCornerShape(
                            topStart = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp,
                        )
                    )
                ) {
                    Text(
                        text = message.msgBody,
                        modifier = Modifier.padding(12.dp),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W400,
                            color = Color(0xff000000)
                        )
                    )
                }
            }
        }


        SubcomposeAsyncImage(
            model = "https://k.sinaimg.cn/n/sinakd20117/0/w800h800/20240127/889b-4c8a7876ebe98e4d619cdaf43fceea7c.jpg/w700d1q75cms.jpg",
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            loading = {
                CircularProgressIndicator()
            },
            onLoading = { loading ->

            },
        )

//        GlideSubcomposition(
//            model = "https://qiniu-web.aiwei365.com/@/upload/0/image/20170321/1490085940504055412.gif?imageView2/2/w/720",
//            modifier = Modifier
//                .size(129.dp)
//                .clip(CircleShape),
//        ) {
//
//        }
    }
}





