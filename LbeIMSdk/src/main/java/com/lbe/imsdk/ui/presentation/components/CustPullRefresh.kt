import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PullToRefreshListWithProgressIndicator() {
    var items by remember { mutableStateOf((1..20).map { "Item $it" }) }
    var refreshing by remember { mutableStateOf(false) }
    var offsetY by remember { mutableStateOf(0f) }
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

    // 滚动状态
    val listState = rememberLazyListState()

    // NestedScrollDispatcher 和 NestedScrollConnection
    // val nestedScrollDispatcher = remember { NestedScrollDispatcher() }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset, source: NestedScrollSource
            ): Offset {
                // 列表在顶部，且非刷新状态下处理下拉逻辑
                if (!refreshing && available.y > 0 && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                    offsetY = (offsetY + available.y).coerceIn(0f, maxOffset * 1f)
                    println("手势 onPreScroll --->>> $offsetY")
                    return Offset(available.x, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset, available: Offset, source: NestedScrollSource
            ): Offset {
                // 上拉逻辑，防止 offsetY 小于 0
                if (!refreshing && available.y < 0) {
                    offsetY = (offsetY + available.y).coerceAtLeast(0f)
                }
                return super.onPostScroll(consumed, available, source)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                println("手势 onPreFling --->>> $offsetY")
                // 松手后判断是否需要刷新或回弹
                if (!refreshing) {
                    if (offsetY >= maxOffset) {
                        refreshing = true
                        coroutineScope.launch {
                            delay(2000) // 模拟网络请求
                            items = (1..20).map { "New Item $it" }
                            refreshing = false
                            offsetY = 0f // 刷新结束后回弹
                        }
                    } else {
                        // 未达到阈值，回弹
                        offsetY = 0f
                    }
                }
                return super.onPreFling(available)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection) // 启用 NestedScroll
    ) {
        // 列表内容
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, animatedOffsetY.roundToInt()) }, // 动画实现下沉效果
            contentPadding = PaddingValues(16.dp)
        ) {
            items(items) { item ->
                ListItem(item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 刷新指示器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(33.dp)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 13.dp)
                    .size(indicatorSize.dp)
                    .alpha(indicatorAlpha)
            )
        }
    }
}

@Composable
fun ListItem(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp), shadowElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = LocalTextStyle.current.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            )
        }
    }
}