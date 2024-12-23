//package com.lbe.imsdk.ui.presentation.components
//
//import androidx.compose.animation.core.animateDpAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import com.lbe.imsdk.R
//import kotlinx.coroutines.delay
//
//const val SPACE = 20
//
//@Composable
//fun Sample() {
//    val refreshTriggerDistance = 180.dp
//    var refreshing by remember { mutableStateOf(false) }
//    val refreshState = rememberSwipeRefreshState(isRefreshing = refreshing)
//    var contentYOffsetDuration by remember { mutableStateOf(0) }
//    var contentYOffsetTarget by remember { mutableStateOf(0.dp) }
//    val contentYOffset by animateDpAsState(
//        targetValue = contentYOffsetTarget,
//        label = "contentYOffset",
//        animationSpec = tween(
//            durationMillis = contentYOffsetDuration
//        )
//    )
//
//    LaunchedEffect(refreshing) {
//        if (refreshing) {
//            delay(2000)
//            refreshing = false
//        }
//    }
//
//    SwipeRefresh(
//        state = refreshState,
//        onRefresh = { refreshing = true },
//        indicator = { state, trigger ->
//            AnimationIndicator(
//                swipeRefreshState = state,
//                refreshTriggerDistance = trigger
//            )
//        },
//        refreshTriggerDistance = refreshTriggerDistance
//    ) {
//        if (refreshState.isRefreshing) {
//            contentYOffsetTarget = refreshTriggerDistance + SPACE.dp
//        }
//        else {
//            if (refreshState.isSwipeInProgress) {
//                with(LocalDensity.current) {
//                    contentYOffsetTarget = refreshState.indicatorOffset.toDp().coerceAtMost(refreshTriggerDistance + SPACE.dp)
//                }
//                contentYOffsetDuration = 0
//            }
//            else {
//                contentYOffsetTarget = 0.dp
//                contentYOffsetDuration = 300
//            }
//        }
//
//        LazyColumn(
//            modifier = Modifier.offset(y = contentYOffset)
//        ) {
//            items(30) {
//                Row(Modifier.padding(16.dp)) {
//                    Text(
//                        text = "Text $it",
//                        modifier = Modifier
//                            .weight(1f)
//                            .align(Alignment.CenterVertically)
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun AnimationIndicator(
//    swipeRefreshState: SwipeRefreshState,
//    refreshTriggerDistance: Dp,
//) {
//    var tipText by remember { mutableStateOf("") }
//
//    val trigger = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
//    val totalProgress = (swipeRefreshState.indicatorOffset / (trigger + with(LocalDensity.current) { SPACE.dp.toPx() * 3 })).coerceIn(0f, 1f)
//
//
//    var animationAlpha by remember { mutableStateOf(1f) }
//
//    var isPlaying by remember { mutableStateOf(false) }
//    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
//    val animationProgress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever, isPlaying = isPlaying)
//
//    var animationTopOffset by remember { mutableStateOf(-refreshTriggerDistance) }
//
//    if (swipeRefreshState.isRefreshing) {
//        tipText = "正在加载中……"
//        animationTopOffset = 0.dp
//        isPlaying = true
//        animationAlpha = 1f
//    }
//    else {
//        animationTopOffset = with(LocalDensity.current) {
//            (swipeRefreshState.indicatorOffset.toDp() - refreshTriggerDistance).coerceAtMost(0.dp)
//        }
//
//        isPlaying = false
//        animationAlpha = totalProgress // FastOutSlowInEasing.transform(totalProgress)
//
//        tipText = if (swipeRefreshState.indicatorOffset < trigger) {
//            "继续下拉以刷新"
//        } else {
//            "松手立即刷新"
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(refreshTriggerDistance)
//            .offset(y = animationTopOffset),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        LottieAnimation(
//            composition = composition,
//            progress = {
//                if (swipeRefreshState.isRefreshing) {
//                    animationProgress
//                }
//                else {
//                    totalProgress
//                }
//            },
//            modifier = Modifier
//                .height(refreshTriggerDistance - SPACE.dp)
//                .alpha(animationAlpha)
//        )
//
//        Text(text = tipText)
//    }
//}