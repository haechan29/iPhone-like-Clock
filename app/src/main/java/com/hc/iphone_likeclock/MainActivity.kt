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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hc.iphone_likeclock.MainActivity.Companion.CLOCK_RADIUS
import com.hc.iphone_likeclock.MainActivity.Companion.DEGREE_OF_ENDPOINT
import com.hc.iphone_likeclock.MainActivity.Companion.ITEM_SIZE
import com.hc.iphone_likeclock.ui.theme.IPhoneLikeClockTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    companion object {
        const val DEGREE_OF_ENDPOINT = PI / 2.6
        const val CLOCK_RADIUS = 120
        const val ITEM_SIZE = 50
    }
}

@Composable
fun IPhoneLikeClock() {
    val periods = listOf("오전", "오후").addedEmptySpaces()
    val hours = (1 .. 12).map { it.toString() }.toList().addedEmptySpaces()
    val minutes = (1 .. 59).map { it.toString() }.toList().addedEmptySpaces()
    Column {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black)
        ) {
//            Box(modifier = Modifier.weight(1f)) {
//                Clock(120.dp, 50.dp, periods)
//            }
//            Box(modifier = Modifier.weight(1f)) {
//                Clock(120.dp, 50.dp, hours)
//            }
            Box(modifier = Modifier.weight(1f)) {
                Clock(minutes)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Clock(items: List<String>) {
    val listState = rememberLazyListState()
    val height = CLOCK_RADIUS.dp * sin(PI / 3).toFloat() * 2
    val scope = rememberCoroutineScope()
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    val centerItemIndex by remember {
        derivedStateOf {
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
            val indexInVisibleItems = itemIndex - firstVisibleItemIndex
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        if (!isItemVisible(layoutInfo, indexInVisibleItems))
                            calcItemHeight(ITEM_SIZE.dp, DEGREE_OF_ENDPOINT)
                        else {
                            val degree = getRotationDegree(layoutInfo, indexInVisibleItems)
                            calcItemHeight(ITEM_SIZE.dp, degree)
                        }
                    )
                    .graphicsLayer {
                        if (!isItemVisible(layoutInfo, indexInVisibleItems)) return@graphicsLayer
                        rotationX = getRotationDegree(layoutInfo, indexInVisibleItems).toDegree().toFloat()
                    }
                    .wrapContentHeight(CenterVertically),
                text = items[itemIndex],
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun getRotationDegree(layoutInfo: LazyListLayoutInfo, indexInVisibleItems: Int): Double {
    val viewportHeight = with(layoutInfo) { viewportEndOffset - viewportStartOffset }
    val itemInfo = layoutInfo.visibleItemsInfo[indexInVisibleItems]
    val itemCenterOffset = itemInfo.offset + itemInfo.size / 2
    val h = viewportHeight / 2 - itemCenterOffset
    val r = CLOCK_RADIUS.toPx()
    return asin(h.toDouble() / r.toDouble())
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

fun isItemVisible(layoutInfo: LazyListLayoutInfo, i: Int) = i in layoutInfo.visibleItemsInfo.indices
fun calcItemHeight(itemSize: Dp, degree: Double) = itemSize * cos(degree).toFloat()


@Preview(showBackground = true)
@Composable
fun ClockPreview() {
    IPhoneLikeClockTheme {
        Clock((0 .. 100).map { it.toString() }.toList())
    }
}

private fun List<String>.addedEmptySpaces() = listOf(listOf("", ""), this, listOf("", "")).flatten()

fun Double.toRadian() = this / 180f * PI
fun Double.toDegree() = this * 180 / PI
fun Int.toDp() = (this / Resources.getSystem().displayMetrics.density).toInt().dp
fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()