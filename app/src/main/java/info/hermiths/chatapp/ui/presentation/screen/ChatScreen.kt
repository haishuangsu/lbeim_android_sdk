package info.hermiths.chatapp.ui.presentation.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter.State.Empty.painter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.GlideSubcomposition
import info.hermiths.chatapp.R
import info.hermiths.chatapp.ui.data.enums.ConnectionStatus
import info.hermiths.chatapp.ui.data.enums.MessagePosition
import info.hermiths.chatapp.ui.data.model.ChatMessage
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel

data class ChatScreenUiState(
    var messages: List<ChatMessage> = listOf(),
    val userId: String = "hermits",
    val message: String = "",
    val connectionStatus: ConnectionStatus = ConnectionStatus.NOT_STARTED
)

@Composable
fun ChatScreen(
    viewModel: ChatScreenViewModel = viewModel()
) {
    ImageLoader.Builder(LocalContext.current)

    val uiState by viewModel.uiState.observeAsState(ChatScreenUiState())
    val currentFocus = LocalFocusManager.current
    val lazyListState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = "Connection Status: ${uiState.connectionStatus.name}")
        Text(text = "User: ${uiState.userId}")
        Divider(
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
                    message = message, if (message.fromUser == uiState.userId) MessagePosition.RIGHT
                    else MessagePosition.LEFT
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.message,
                onValueChange = viewModel::onMessageChange,
                modifier = Modifier.weight(1f),
                maxLines = 5,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = "Send Button",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        viewModel.sendMessage(messageSent = {
                            currentFocus.clearFocus()
                        })
                    })
        }
    }
    // Ask user to enter a unique userId before using the chat functionality
    AnimatedVisibility(visible = uiState.userId.isEmpty()) {
        UserIdPrompt() {
            viewModel.setUserId(it)
        }
    }

    // When ever a new message is added to the list we want to scroll to the latest message
    LaunchedEffect(key1 = uiState.messages) {
        lazyListState.animateScrollToItem(uiState.messages.size)
    }
}

@Composable
fun UserIdPrompt(onStart: (String) -> Unit) {
    var userId by remember { mutableStateOf("") }
    Dialog(onDismissRequest = { }) {
        Card {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(value = userId,
                    onValueChange = { userId = it },
                    label = { Text(text = "User Id") })
                Button(onClick = { onStart(userId) }) {
                    Text(text = "Start")
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage, messagePosition: MessagePosition) {
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
            UserInput(message, messagePosition)
        }
    }
}

@Composable
fun CsRecived(message: ChatMessage, messagePosition: MessagePosition) {
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
                text = message.fromUser,
                modifier = Modifier.align(if (messagePosition == MessagePosition.LEFT) Alignment.Start else Alignment.End),
                style = TextStyle(
                    fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff979797)
                )
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                color = Color.White, modifier = Modifier.clip(
                    RoundedCornerShape(
                        topEnd = 12.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp,
                    )
                )
            ) {
                Text(
                    text = message.message, modifier = Modifier.padding(12.dp), style = TextStyle(
                        fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff000000)
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun UserInput(message: ChatMessage, messagePosition: MessagePosition) {
    Row(horizontalArrangement = Arrangement.End) {
        Column(
            horizontalAlignment = Alignment.End, modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = message.fromUser,
                modifier = Modifier.align(if (messagePosition == MessagePosition.LEFT) Alignment.Start else Alignment.End),
                style = TextStyle(
                    fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff979797)
                )
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                color = Color(0xff0054FC).copy(alpha = 0.1f), modifier = Modifier.clip(
                    RoundedCornerShape(
                        topStart = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp,
                    )
                )
            ) {
                Text(
                    text = message.message, modifier = Modifier.padding(12.dp), style = TextStyle(
                        fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff000000)
                    )
                )
            }
        }
//        Image(
//            painter = painterResource(id = R.drawable.user_avatar),
//            contentDescription = "",
//            modifier = Modifier.size(32.dp)
//        )

        SubcomposeAsyncImage(
//            model = "https://k.sinaimg.cn/n/sinakd20117/0/w800h800/20240127/889b-4c8a7876ebe98e4d619cdaf43fceea7c.jpg/w700d1q75cms.jpg",
            model = "https://qiniu-web.aiwei365.com/@/upload/0/image/20170321/1490085940504055412.gif?imageView2/2/w/720",
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

//        GlideImage(
//            model = "https://qiniu-web.aiwei365.com/@/upload/0/image/20170321/1490085940504055412.gif?imageView2/2/w/720",
//            contentDescription = "Yo",
//            contentScale = ContentScale.FillBounds,
//            modifier = Modifier
//                .size(32.dp)
//                .clip(CircleShape),
//        )

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

