import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.lbe.imsdk.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun LightPullToRefreshList(
    modifier: Modifier,
    listState: LazyListState,
    onRefresh: suspend () -> Unit,
    lazyColumn: @Composable ColumnScope.() -> Unit,
    onScroll: (Float) -> Unit,
) {
    var refreshing by remember { mutableStateOf(false) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val maxOffset = 110f
    val coroutineScope = rememberCoroutineScope()

    // Offset 动画
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY, animationSpec = spring(), label = "animatedOffsetY"
    )

    // 指示器的透明度和尺寸
    val indicatorAlpha by animateFloatAsState(
        targetValue = (offsetY / maxOffset).coerceIn(0f, 1f), label = "indicatorAlpha"
    )
    val indicatorSize by animateFloatAsState(
        targetValue = (offsetY / maxOffset * 19).coerceIn(10f, 19f), label = "indicatorSize"
    )

    // 处理释放或快速滑动后的逻辑
    fun handleFlingOrRelease() {
//        println("手势回收 ---->>> isDragging: $isDragging, refreshing: $refreshing, offsetY: $offsetY")
        if (!isDragging && !refreshing) {
            if (offsetY >= maxOffset) {
                // 触发刷新
                refreshing = true
                coroutineScope.launch {
                    onRefresh()
                    delay(500)
                    refreshing = false
                    offsetY = 0f // 刷新结束后回弹
                }
            } else {
                // 未达到阈值，直接回弹
                offsetY = 0f
            }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!refreshing && available.y > 0 && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                    offsetY = (offsetY + available.y).coerceIn(0f, maxOffset * 1f)
                    isDragging = true
                    return Offset(available.x, available.y)
                }
                // 非下拉刷新逻辑，传递滚动偏移量给外部
                onScroll(available.y)
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset, available: Offset, source: NestedScrollSource
            ): Offset {
                if (!refreshing && available.y < 0) {
                    offsetY = (offsetY + available.y).coerceAtLeast(0f)
                }
                onScroll(available.y) // 传递滚动偏移量给外部
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
//                println("手势 onPreFling --->>> offsetY: $offsetY, refreshing: $refreshing")
                isDragging = false
                handleFlingOrRelease()
                return Velocity.Zero
            }
        }
    }

    // 用于检测拖动结束后的状态
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            handleFlingOrRelease()
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .nestedScroll(nestedScrollConnection)
    ) {

        Column(modifier = modifier
            .fillMaxHeight()
            .offset { IntOffset(0, animatedOffsetY.roundToInt()) }) {
            lazyColumn()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(33.dp)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center
        ) {
            RotatingImage(
                modifier = Modifier
                    .padding(top = 13.dp)
                    .size(indicatorSize.dp)
                    .alpha(indicatorAlpha),
                imageRes = R.drawable.loding_new,
            )
        }
    }
}

@Composable
fun RotatingImage(modifier: Modifier = Modifier, imageRes: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        modifier = modifier.graphicsLayer(rotationZ = rotation) // 应用旋转动画
    )
}
