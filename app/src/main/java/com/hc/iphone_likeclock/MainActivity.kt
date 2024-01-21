package com.hc.iphone_likeclock

import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hc.iphone_likeclock.ui.theme.IPhoneLikeClockTheme
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

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
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f)) {
            Clock(120.dp, 50.dp, amAndPm)
        }
        Box(modifier = Modifier.weight(1f)) {
            Clock(120.dp, 50.dp, hours)
        }
        Box(modifier = Modifier.weight(1f)) {
            Clock(120.dp, 50.dp, minutes)
        }
    }
}

@Composable
fun Clock(clockRadius: Dp, itemSize: Dp, items: List<String>) {
    val listState = rememberLazyListState()
    val height = clockRadius * sin(PI * (70.0 / 180.0)).toFloat() * 2
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        state = listState
    ) {
        items(items.size) { itemIndex ->
            val indexInVisibleItems = itemIndex - listState.firstVisibleItemIndex
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        if (indexInVisibleItems !in listState.layoutInfo.visibleItemsInfo.indices)
                            itemSize * cos(PI * (77.0 / 180.0)).toFloat()
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
                text = "${items[itemIndex]}",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
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