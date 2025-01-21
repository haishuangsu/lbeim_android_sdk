package com.lbe.imsdk.ui.presentation.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.ImageLoader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lbe.imsdk.R
import com.lbe.imsdk.model.MessageEntity
import com.lbe.imsdk.model.req.FaqReqBody
import com.lbe.imsdk.model.resp.FaqAnswer
import com.lbe.imsdk.model.resp.FaqDetail
import com.lbe.imsdk.model.resp.FaqTopic
import com.lbe.imsdk.model.resp.FaqEntryUrl
import com.lbe.imsdk.model.resp.LinkText
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel


@Composable
fun MsgTypeContent(
    message: MessageEntity,
    viewModel: ChatScreenViewModel,
    navController: NavController,
    fromUser: Boolean,
    imageLoader: ImageLoader,
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    when (message.msgType) {
        1 -> {
            if (fromUser) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!message.sendSuccess) {
                        Image(
                            painter = painterResource(R.drawable.send_fail),
                            contentDescription = "send fail",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    viewModel.reSendMessage(message.clientMsgID)
                                },
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        if (message.readed) {
                            Image(
                                painter = painterResource(R.drawable.readed),
                                contentDescription = "read msg",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
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
                            modifier = Modifier
                                .padding(12.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(onLongPress = {
                                        clipboardManager.setText(AnnotatedString(message.msgBody))
                                        Toast
                                            .makeText(
                                                context, "复制成功", Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }, onTap = {
                                        clipboardManager.setText(AnnotatedString(message.msgBody))
                                        Toast
                                            .makeText(
                                                context, "复制成功", Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    })
                                },
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W400,
                                color = Color(0xff000000)
                            )
                        )
                    }
                }
            } else {
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
                        text = message.msgBody,
                        modifier = Modifier
                            .padding(12.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(onLongPress = {
                                    clipboardManager.setText(AnnotatedString(message.msgBody))
                                    Toast
                                        .makeText(
                                            context, "复制成功", Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }, onTap = {
                                    clipboardManager.setText(AnnotatedString(message.msgBody))
                                    Toast
                                        .makeText(
                                            context, "复制成功", Toast.LENGTH_SHORT
                                        )
                                        .show()
                                })
                            },
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W400,
                            color = Color(0xff000000)
                        )
                    )
                }
            }
        }

        2 -> {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (fromUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        if (!message.sendSuccess) {
                            //
                        } else {
                            if (message.readed) {
                                Image(
                                    painter = painterResource(R.drawable.readed),
                                    contentDescription = "read msg",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                        }

                        ThumbDecryptedOrNotImageView(
                            navController = navController,
                            viewModel = viewModel,
                            message = message,
                            imageLoader = imageLoader,
                        )
                    }
                } else {
                    Box(modifier = Modifier.align(Alignment.BottomStart)) {
                        ThumbDecryptedOrNotImageView(
                            navController = navController,
                            viewModel = viewModel,
                            message = message,
                            imageLoader = imageLoader,
                        )
                    }
                }
            }
        }

        3 -> {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()
            ) {
                if (fromUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        if (!message.sendSuccess) {
                            //
                        } else {
                            if (message.readed) {
                                Image(
                                    painter = painterResource(R.drawable.readed),
                                    contentDescription = "read msg",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                        }

                        ThumbDecryptedOrNotImageView(
                            navController = navController,
                            viewModel = viewModel,
                            message = message,
                            imageLoader = imageLoader,
                        )
                    }
                } else {
                    Box(modifier = Modifier.align(Alignment.BottomStart)) {
                        ThumbDecryptedOrNotImageView(
                            navController = navController,
                            viewModel = viewModel,
                            message = message,
                            imageLoader = imageLoader,
                        )
                    }
                }
            }
        }

        8 -> {
            if (!fromUser) {
                Log.d("Faq 8", "Topic body --->>> ${message.msgBody}")
                val faq = Gson().fromJson(message.msgBody, FaqTopic::class.java)

                val gridHeight =
                    if (faq.knowledgeBaseList.size % 3 == 0) 85 * (faq.knowledgeBaseList.size / 3) else 85 * (1 + faq.knowledgeBaseList.size / 3)
                Log.d(
                    "Faq 8",
                    "计算高度 --->>> faq size: ${faq.knowledgeBaseList.size} ,gridHeight: $gridHeight"
                )

                Surface(
                    color = Color.White, modifier = Modifier.clip(
                        RoundedCornerShape(
                            topEnd = 12.dp,
                            bottomStart = 12.dp,
                            bottomEnd = 12.dp,
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            faq.knowledgeBaseTitle.ifEmpty {
                                "Hi~请简单的描述一下你的问题，我们会尽力协助哦。"
                            }, style = TextStyle(
                                color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.W400
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(gridHeight.dp)
                        ) {
                            items(faq.knowledgeBaseList) { item ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        println("history not available 8 --->> message.sessionId: ${message.sessionId}, current session: ${ChatScreenViewModel.sessionList[0].sessionId}")
                                        if (ChatScreenViewModel.sessionList.isNotEmpty() && message.sessionId != ChatScreenViewModel.sessionList[0].sessionId) {
                                            Toast.makeText(
                                                context, "此记录不可用", Toast.LENGTH_SHORT
                                            ).show()
                                            return@clickable
                                        }
                                        viewModel.faq(FaqReqBody(faqType = 1, id = item.id))
                                    }) {
                                    Surface(
                                        color = Color(0xffF3F4F6),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(vertical = 15.dp)
                                        ) {
                                            val topicEntryUrl =
                                                Gson().fromJson(item.url, FaqEntryUrl::class.java)
                                            Log.d(
                                                "Faq",
                                                "after seri topicEntryUrl --->>> $topicEntryUrl"
                                            )
                                            if (topicEntryUrl == null) {
                                                Image(
                                                    painter = painterResource(R.drawable.faq_default_icon),
                                                    contentDescription = "",
                                                    modifier = Modifier.size(
                                                        width = 28.dp, 26.dp
                                                    ),
                                                )
                                            } else {
                                                NormalDecryptedOrNotImageView(
                                                    key = topicEntryUrl.key,
                                                    url = topicEntryUrl.url,
                                                    modifier = Modifier.size(
                                                        width = 28.dp, 26.dp
                                                    ),
                                                    imageLoader
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                item.knowledgeBaseName,
                                                modifier = Modifier.padding(horizontal = 5.dp),
                                                style = TextStyle(
                                                    color = Color(0xff0054FC),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.W400
                                                ),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        9 -> {
            if (!fromUser) {
                val faqDetailListType = object : TypeToken<MutableList<FaqDetail>>() {}.type
                val faqDetailList = Gson().fromJson<MutableList<FaqDetail>>(
                    message.msgBody, faqDetailListType
                )
                Log.d(
                    "Faq",
                    "Topic detail list | msgType: ${message.msgType} | --->>> 序列化前:${message.msgBody}, \n序列化后: $faqDetailList\n\n"
                )
                Surface(
                    color = Color.White, modifier = Modifier.clip(
                        RoundedCornerShape(
                            topEnd = 12.dp,
                            bottomStart = 12.dp,
                            bottomEnd = 12.dp,
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            message.faqListTile, //"点击选择以下常见问题获取便捷自助服务",
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W400,
                            ),
                        )

                        for (detail in faqDetailList) {
                            Text(detail.knowledgePointName,
//                                if (detail.knowledgePointName == null) "" else detail.knowledgePointName,
                                style = TextStyle(
                                    color = Color(0xff0054FC),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.W500,
                                ), modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clickable {
                                        println("history not available 9 --->> message.sessionId: ${message.sessionId}, current session: ${ChatScreenViewModel.sessionList[0].sessionId}")
                                        if (ChatScreenViewModel.sessionList.isNotEmpty() && message.sessionId != ChatScreenViewModel.sessionList[0].sessionId) {
                                            Toast
                                                .makeText(
                                                    context, "此记录不可用", Toast.LENGTH_SHORT
                                                )
                                                .show()
                                            return@clickable
                                        }
                                        viewModel.faq(FaqReqBody(faqType = 2, id = detail.id))
                                    })
                        }
                    }
                }
            }
        }

        10 -> {
            if (!fromUser) {
                val faqAnswerType = object : TypeToken<MutableList<FaqAnswer>>() {}.type
                val faqAnswer =
                    Gson().fromJson<MutableList<FaqAnswer>>(message.msgBody, faqAnswerType)
                Log.d("Faq", "Answer --->>>> $faqAnswer")

                Surface(
                    color = Color.White, modifier = Modifier.clip(
                        RoundedCornerShape(
                            topEnd = 12.dp,
                            bottomStart = 12.dp,
                            bottomEnd = 12.dp,
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center
                    ) {
                        var index = 0
                        for (answerUnit in faqAnswer) {
                            when (answerUnit.type) {
                                0 -> {
                                    Text(
                                        answerUnit.content,
                                        style = TextStyle(
                                            color = Color.Black,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.W400,
                                        ),
                                    )
                                }

                                1 -> {
                                    val faqEntryUrl =
                                        Gson().fromJson(answerUnit.content, FaqEntryUrl::class.java)
                                    val navRoute =
                                        "${NavRoute.MEDIA_VIEWER}/${message.clientMsgID}_$index"
                                    Log.d("Faq", "Answer content --->>> ${answerUnit.content}")
                                    Log.d("Faq", "Answer faqEntryUrl --->>> $faqEntryUrl")
                                    NormalDecryptedOrNotImageView(
                                        key = faqEntryUrl.key,
                                        url = faqEntryUrl.url,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                            .clickable {
                                                navController.navigate(navRoute)
                                            },
                                        imageLoader,
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                2 -> {
                                    if (answerUnit.contents != null && answerUnit.contents.isNotEmpty()) {
                                        val linkTextType =
                                            object : TypeToken<MutableList<LinkText>>() {}.type
                                        val linkTexts = Gson().fromJson<MutableList<LinkText>>(
                                            answerUnit.contents, linkTextType
                                        )
                                        Text(buildAnnotatedString {
                                            for (content in linkTexts) {
                                                if (content.url.isEmpty()) {
                                                    withStyle(
                                                        style = SpanStyle(
                                                            color = Color.Black,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.W400
                                                        )
                                                    ) {
                                                        append(content.content)
                                                    }
                                                } else {
                                                    Log.d(
                                                        "LinkText",
                                                        "${content.content}, ${content.url}"
                                                    )
                                                    withLink(
                                                        LinkAnnotation.Url(url = content.url)
                                                    ) {
                                                        withStyle(
                                                            style = SpanStyle(
                                                                color = Color(0xff0054FC),
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.W400
                                                            )
                                                        ) {
                                                            append(content.content)
                                                        }
                                                    }
                                                }
                                            }
                                        }, modifier = Modifier.padding(0.dp))
                                    }
                                }
                            }
                            index++
                        }
                    }
                }
            }
        }

        12 -> {
            if (!fromUser) {
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

        else -> {
            Text("Not implement yet. --->>> { ${message.msgType} }")
        }
    }
}
