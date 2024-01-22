package com.hc.iphone_likeclock

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hc.iphone_likeclock.ui.theme.IPhoneLikeClockTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
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
}

@Composable
fun IPhoneLikeClock() {
    val amAndPm = listOf("오전", "오후").addedEmptySpaces()
    val hours = (1 .. 12).map { it.toString() }.toList().addedEmptySpaces()
    val minutes = (1 .. 59).map { it.toString() }.toList().addedEmptySpaces()
    Column {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)) {}
        Row(modifier = Modifier.fillMaxWidth()) {
//            Box(modifier = Modifier.weight(1f)) {
//                Clock(120.dp, 50.dp, amAndPm)
//            }
//            Box(modifier = Modifier.weight(1f)) {
//                Clock(120.dp, 50.dp, hours)
//            }
            Box(modifier = Modifier.weight(1f)) {
                Clock(120.dp, 50.dp, minutes)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Clock(clockRadius: Dp, itemSize: Dp, items: List<String>) {
    val listState = rememberLazyListState()
    val height = clockRadius * sin(PI / 3).toFloat() * 2
    val scope = rememberCoroutineScope()
    val fling = rememberSnapFlingBehavior(lazyListState = listState)
    val centerItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

            visibleItems.minByOrNull { abs((it.offset + it.size / 2) - viewportCenter) }?.index ?: 0
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        state = listState,
        flingBehavior = maxScrollFlingBehavior {
            scope.launch {
                listState.animateScrollToItem(centerItemIndex - 2)
            }
        }
    ) {
        items(items.size) { itemIndex ->
            val indexInVisibleItems = itemIndex - listState.firstVisibleItemIndex
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        if (indexInVisibleItems !in listState.layoutInfo.visibleItemsInfo.indices)
                            itemSize * cos(PI / 2.6).toFloat()
                        else {
                            val degree = getRotationDegree(
                                clockRadius,
                                height,
                                listState.layoutInfo.visibleItemsInfo[indexInVisibleItems]
                            )
                            itemSize * cos(degree / 180f * PI).toFloat()
                        }
                    )
                    .graphicsLayer {
                        if (indexInVisibleItems !in listState.layoutInfo.visibleItemsInfo.indices) return@graphicsLayer
                        rotationX = getRotationDegree(
                            clockRadius,
                            height,
                            listState.layoutInfo.visibleItemsInfo[indexInVisibleItems]
                        )
                    }
                    .wrapContentHeight(CenterVertically),
                text = "${
//                    items[itemIndex]
                    if (indexInVisibleItems !in listState.layoutInfo.visibleItemsInfo.indices)
                        ""
                    else {
                        getRotationDegree(
                            clockRadius,
                            height,
                            listState.layoutInfo.visibleItemsInfo[indexInVisibleItems]
                        ).run { String.format("%.1f", this) }.run { "각도: ${this}" }
                    }
                }",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun maxScrollFlingBehavior(function: () -> Job): FlingBehavior {
    val flingSpec = rememberSplineBasedDecay<Float>()
    return remember(flingSpec) {
        ScrollSpeedFlingBehavior(flingSpec, function)
    }
}

private class ScrollSpeedFlingBehavior(
    private val flingDecay: DecayAnimationSpec<Float>,
    val function: () -> Job
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        Log.d("hhcc", "fling start with velocity: $initialVelocity")
        var isAnimationRunning = true
        var velocityLeft = initialVelocity
        var lastValue = 0f
        val animationState = AnimationState(
            initialValue = 0f,
            initialVelocity = initialVelocity,
        )
        animationState.animateDecay(flingDecay) {
            Log.d("hhcc", "isRunning: $isRunning")
            val delta = value - lastValue
            val consumed = scrollBy(delta)
            lastValue = value
            velocityLeft = this.velocity
            if (isAnimationRunning != isRunning) {
                if (!isRunning) {
                    Log.d("hhcc", "fling stopped")
                    function()
                }
                isAnimationRunning = isRunning
            }
            // avoid rounding errors and stop if anything is unconsumed
            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
        }
        return velocityLeft
    }
}

fun getRotationDegree(clockRadius: Dp, height: Dp, itemInfo: LazyListItemInfo): Float {
    // itemPosition is the position of vertical center of the item
    val itemPosition = itemInfo.offset + itemInfo.size / 2
    val h = (height / 2 - itemPosition.toDp()).value
    val r = clockRadius.value
    return (asin(h / r) * 180 / PI).toFloat()
}

@Preview(showBackground = true)
@Composable
fun ClockPreview() {
    IPhoneLikeClockTheme {
        Clock(120.dp, 50.dp, (0 .. 100).map { it.toString() }.toList())
    }
}

private fun List<String>.addedEmptySpaces(): List<String> {
    val list = mutableListOf<String>()
    repeat(2) { list.add("") }
    list.addAll(this)
    repeat(2) { list.add("") }
    return list
}

fun Int.toDp(): Dp {
    return (this / Resources.getSystem().displayMetrics.density).toInt().dp
}
fun Dp.toPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).value.toInt()
}