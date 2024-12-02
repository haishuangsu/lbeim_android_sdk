package info.hermiths.lbesdk.ui.presentation.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
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
import info.hermiths.lbesdk.model.MessageEntity
import info.hermiths.lbesdk.model.req.FaqReqBody
import info.hermiths.lbesdk.model.resp.FaqAnswer
import info.hermiths.lbesdk.model.resp.FaqDetail
import info.hermiths.lbesdk.model.resp.FaqTopic
import info.hermiths.lbesdk.model.resp.FaqEntryUrl
import info.hermiths.lbesdk.model.resp.LinkText
import info.hermiths.lbesdk.ui.presentation.viewmodel.ChatScreenViewModel
import info.hermiths.mylibrary.R


@Composable
fun MsgTypeContent(
    message: MessageEntity,
    viewModel: ChatScreenViewModel,
    navController: NavController,
    fromUser: Boolean,
    imageLoader: ImageLoader,
) {
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
                        } else {
                            Image(
                                painter = painterResource(R.drawable.no_read),
                                contentDescription = "unread",
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
                            modifier = Modifier.padding(12.dp),
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
                            // TODO
                        } else {
                            if (message.readed) {
                                Image(
                                    painter = painterResource(R.drawable.readed),
                                    contentDescription = "read msg",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            } else {
                                Image(
                                    painter = painterResource(R.drawable.no_read),
                                    contentDescription = "unread",
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
                            // TODO
                        } else {
                            if (message.readed) {
                                Image(
                                    painter = painterResource(R.drawable.readed),
                                    contentDescription = "read msg",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            } else {
                                Image(
                                    painter = painterResource(R.drawable.no_read),
                                    contentDescription = "unread",
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
                Log.d("Faq", "Topic body --->>> ${message.msgBody}")
                val faq = Gson().fromJson(message.msgBody, FaqTopic::class.java)
//                faq.knowledgeBaseList.removeAt(1)
//                faq.knowledgeBaseList.add(faq.knowledgeBaseList[0])
//                faq.knowledgeBaseList.add(faq.knowledgeBaseList[0])
//                faq.knowledgeBaseList.add(faq.knowledgeBaseList[0])

                val gridHeight = 105 * (1 + faq.knowledgeBaseList.size / 3)
                Log.d("Faq", "计算高度 --->>> $gridHeight")

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
                                if (item.url.isNotEmpty()) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            viewModel.faq(FaqReqBody(faqType = 1, id = item.id))
                                        }) {
                                        val topicEntryUrl =
                                            Gson().fromJson(item.url, FaqEntryUrl::class.java)
                                        Log.d("Faq", "topicEntryUrl --->>> $topicEntryUrl")
                                        Surface(
                                            color = Color(0xffF3F4F6),
                                            modifier = Modifier
                                                .height(76.dp)
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                NormalDecryptedOrNotImageView(
                                                    key = topicEntryUrl.key,
                                                    url = topicEntryUrl.url,
                                                    modifier = Modifier.size(width = 28.dp, 26.dp),
                                                    imageLoader
//                                                    .clip(CircleShape)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            item.knowledgeBaseName, style = TextStyle(
                                                color = Color(0xff0054FC),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.W500
                                            ), maxLines = 2, overflow = TextOverflow.Ellipsis
                                        )
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
                Log.d("Faq", "Topic detail list --->>> $faqDetailList")
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
                            "点击选择以下常见问题获取便捷自助服务",
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W400,
                            ),
                        )

                        for (detail in faqDetailList) {
                            Text(detail.knowledgePointName, style = TextStyle(
                                color = Color(0xff0054FC),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W500,
                            ), modifier = Modifier
                                .padding(top = 8.dp)
                                .clickable {
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
                val faqAnswer = Gson().fromJson<MutableList<FaqAnswer>>(
                    message.msgBody, faqAnswerType
                )
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
                                        ), //modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                1 -> {
                                    val faqEntryUrl =
                                        Gson().fromJson(answerUnit.content, FaqEntryUrl::class.java)
                                    val navRoute =
                                        "${NavRoute.MEDIA_VIEWER}/${message.clientMsgID}_$index"
                                    Log.d("Faq", "Answer faqEntryUrl --->>> $faqEntryUrl")
                                    NormalDecryptedOrNotImageView(
                                        key = faqEntryUrl.key,
                                        url = faqEntryUrl.url,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                navController.navigate(navRoute)
                                            },
                                        imageLoader
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
//                                                        LinkAnnotation.Url(url = "https://${content.url}")
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

        else -> {
            Text("Not implement yet. --->>> { ${message.msgType} }")
        }
    }
}
