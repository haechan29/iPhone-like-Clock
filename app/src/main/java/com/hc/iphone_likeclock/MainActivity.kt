package com.hc.iphone_likeclock

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hc.iphone_likeclock.MainActivity.Companion.CLOCK_RADIUS
import com.hc.iphone_likeclock.MainActivity.Companion.CLOCK_WIDTH
import com.hc.iphone_likeclock.MainActivity.Companion.DEGREE_OF_ENDPOINT
import com.hc.iphone_likeclock.MainActivity.Companion.DEGREE_VISIBLE
import com.hc.iphone_likeclock.MainActivity.Companion.ITEM_SIZE
import com.hc.iphone_likeclock.ui.theme.IPhoneLikeClockTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IPhoneLikeClockTheme {
                IPhoneLikeClock()
            }
        }
    }

    companion object {
        const val DEGREE_VISIBLE = PI / 3
        const val DEGREE_OF_ENDPOINT = PI / 2.6
        const val CLOCK_WIDTH = 300
        const val CLOCK_RADIUS = 100
        const val ITEM_SIZE = 40
    }
}

@Composable
fun IPhoneLikeClock() {
    val periods = listOf("오전", "오후")
    val hours = (1..12).map { it.toString() }.toList().addedEmptySpaces()
    val minutes = (0..59).map { it.toString() }.toList().addedEmptySpaces()
    Box(
        modifier = Modifier
            .padding(top = 100.dp, bottom = 100.dp)
            .fillMaxWidth()
            .height(calcClockHeight()), contentAlignment = Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.width(CLOCK_WIDTH.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    PeriodClock(periods)
                }
                Box(modifier = Modifier.weight(1f)) {
                    Clock(hours)
                }
                Box(modifier = Modifier.weight(1f)) {
                    Clock(minutes)
                }
            }
        }
        TranslucentScreen()
    }
}

@Composable
fun BoxScope.TranslucentScreen() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((calcClockHeight() - ITEM_SIZE.dp) / 2)
            .alpha(0.05f)
            .background(color = Color.Black)
            .align(TopCenter)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ITEM_SIZE.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width((getScreenWidth().dp - CLOCK_WIDTH.dp) / 2)
                .alpha(0.05f)
                .background(color = Color.Black),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .fillMaxSize()
                    .background(
                        color = Color.White, shape = RoundedCornerShape(10.dp, 0.dp, 0.dp, 10.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width((getScreenWidth().dp - CLOCK_WIDTH.dp) / 2)
                .alpha(0.05f)
                .background(color = Color.Black),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 20.dp)
                    .fillMaxSize()
                    .background(
                        color = Color.White, shape = RoundedCornerShape(0.dp, 10.dp, 10.dp, 0.dp)
                    )
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((calcClockHeight() - ITEM_SIZE.dp) / 2)
            .alpha(0.05f)
            .background(color = Color.Black)
            .align(BottomCenter)
    )
}

@Composable
fun PeriodClock(items: List<String>) {
    val numberOfFrontEmptySpace = 4
    val numberOfBackEmptySpace = 4
    val itemsWithEmptySpace = items.addedEmptySpaces(numberOfFrontEmptySpace, numberOfBackEmptySpace)

    val listState = rememberLazyListState().apply {
        setInitialScroll(numberOfFrontEmptySpace - 2)
    }

    val scope = rememberCoroutineScope()
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    val firstItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(calcClockHeight()),
        state = listState,
        flingBehavior = flingBehaviorWithOnFinished {
            scope.launch {
                listState.animateScrollToItem(firstItemIndex.coerceIn(numberOfFrontEmptySpace - 2, numberOfFrontEmptySpace - 3 + items.size))
            }
        }
    ) {
        items(itemsWithEmptySpace.size) { itemIndex ->
            RotatingText(itemsWithEmptySpace, itemIndex, listState, layoutInfo)
        }
    }
}

@Composable
fun LazyListState.setInitialScroll(index: Int) {
    var initialScrollDone by remember { mutableStateOf(false) }

    LaunchedEffect(initialScrollDone) {
        if (!initialScrollDone) {
            scrollToItem(index)
            initialScrollDone = true
        }
    }
}

@Composable
fun Clock(items: List<String>) {
    val listState = rememberLazyListState()
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(calcClockHeight()),
        state = listState,
    ) {
        items(items.size) { itemIndex ->
            RotatingText(items, itemIndex, listState, layoutInfo)
        }
    }

    listState.onScrollFinished {
        listState.scrollToClosest()
    }
}

@Composable
private fun LazyListState.onScrollFinished(f: suspend () -> Unit) {
    LaunchedEffect(isScrollInProgress) {
        if (!isScrollInProgress) {
            f()
        }
    }
}

private suspend fun LazyListState.scrollToClosest() {
    val candidates = listOf(firstVisibleItemIndex, firstVisibleItemIndex + 1)
    val target = candidates.minBy { itemIndex ->
        try {
            val indexInVisibleItems = itemIndex - firstVisibleItemIndex
            abs(layoutInfo.visibleItemsInfo[indexInVisibleItems].offset)
        } catch(e: Exception) {
            Int.MAX_VALUE
        }
    }
    animateScrollToItem(target)
}

@Composable
fun RotatingText(
    items: List<String>, itemIndex: Int, listState: LazyListState, layoutInfo: LazyListLayoutInfo
) {
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val indexInVisibleItems = itemIndex - firstVisibleItemIndex
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    Text(modifier = Modifier
        .fillMaxWidth()
        .height(
            if (!isItemVisible(layoutInfo, indexInVisibleItems))
                calcItemHeight(ITEM_SIZE.dp, DEGREE_OF_ENDPOINT)
            else {
                val degree = getRotationDegree(layoutInfo, indexInVisibleItems)
                calcItemHeight(ITEM_SIZE.dp, degree)
            }
        )
        .alpha(
            if (!isItemVisible(layoutInfo, indexInVisibleItems)) 0f
            else {
                val degree = getRotationDegree(layoutInfo, indexInVisibleItems)
                toRotatingTextAlpha(degree)
            }
        )
        .graphicsLayer {
            if (!isItemVisible(layoutInfo, indexInVisibleItems)) return@graphicsLayer
            rotationX = getRotationDegree(layoutInfo, indexInVisibleItems)
                .toDegree()
                .toFloat()
        }
        .wrapContentHeight(CenterVertically),
        text = items[itemIndex],
//        text = itemIndex.toString(),
//        text = "$itemIndex: " + try {
//            visibleItemsInfo[indexInVisibleItems].offset.toString()
//        } catch (e: Exception) {
//            "empty"
//        },
//        text = "$indexInVisibleItems: " + if (!isItemVisible(layoutInfo, indexInVisibleItems))
//            calcItemHeight(ITEM_SIZE.dp, DEGREE_OF_ENDPOINT)
//        else {
//            val degree = getRotationDegree(layoutInfo, indexInVisibleItems)
//            calcItemHeight(ITEM_SIZE.dp, degree)
//        }.value.let { (it * 100f).toInt() / 100f }.toString(),
        fontSize = 24.sp,
        textAlign = TextAlign.Center)
}

fun toRotatingTextAlpha(degree: Double) = cos(degree).pow(2).toFloat()

@Composable
fun flingBehaviorWithOnFinished(onFinished: () -> Unit): FlingBehavior {
    val decayAnimSpec = rememberSplineBasedDecay<Float>()
    return remember(decayAnimSpec) {
        FlingBehaviorWithOnFinished(decayAnimSpec, onFinished)
    }
}

@Preview(showBackground = true)
@Composable
fun ClockPreview() {
    IPhoneLikeClockTheme {
        IPhoneLikeClock()
    }
}

@Composable
fun getScreenWidth() = LocalConfiguration.current.screenWidthDp

private fun List<String>.addedEmptySpaces() = listOf(listOf("", ""), this, listOf("", "")).flatten()
private fun List<String>.addedEmptySpaces(numberOfFrontEmptySpace: Int, numberOfBackEmptySpace: Int): List<String> {
    val list = mutableListOf<String>()
    repeat(numberOfFrontEmptySpace) { list.add("") }
    list.addAll(this)
    repeat(numberOfBackEmptySpace) { list.add("") }
    return list
}
fun calcClockHeight() = CLOCK_RADIUS.dp * sin(DEGREE_VISIBLE).toFloat() * 2

fun getItemAtCenter(layoutInfo: LazyListLayoutInfo): LazyListItemInfo? {
    val viewportCenterOffset = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    return layoutInfo.visibleItemsInfo.minByOrNull { abs(calcItemCenterOffset(it) - viewportCenterOffset) }
}

fun isItemVisible(layoutInfo: LazyListLayoutInfo, i: Int) = i in layoutInfo.visibleItemsInfo.indices
fun calcItemHeight(itemSize: Dp, degree: Double) = itemSize * cos(degree).toFloat()

fun getRotationDegree(layoutInfo: LazyListLayoutInfo, indexInVisibleItems: Int): Double {
    val viewportHeight = with(layoutInfo) { viewportEndOffset - viewportStartOffset }
    val itemInfo = layoutInfo.visibleItemsInfo[indexInVisibleItems]
    val itemCenterOffset = calcItemCenterOffset(itemInfo)
    val h = viewportHeight / 2 - itemCenterOffset
    val r = CLOCK_RADIUS.toPx()
    return asin(h.toDouble() / r.toDouble())
}

fun calcItemCenterOffset(itemInfo: LazyListItemInfo) = with(itemInfo) { offset + size / 2 }
fun Double.toRadian() = this / 180f * PI
fun Double.toDegree() = this * 180 / PI
fun Int.toDp() = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()