package com.hc.iphone_likeclock

import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hc.iphone_likeclock.MainActivity.Companion.CLOCK_RADIUS
import com.hc.iphone_likeclock.MainActivity.Companion.ITEM_HEIGHT
import com.hc.iphone_likeclock.ui.theme.IPhoneLikeClockTheme
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IPhoneLikeClockTheme {
                Clock()
            }
        }
    }

    companion object {
        const val CLOCK_RADIUS = 120
        const val ITEM_HEIGHT = 50
    }
}

@Composable
fun Clock() {
    val listState = rememberLazyListState()
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    val height = CLOCK_RADIUS.dp * sqrt(3f)
    Column() {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                // shows about 60 degree of clock
                .height(height),
            state = listState
        ) {
            items(100) { itemIndex ->
                val indexInVisibleItems = itemIndex - listState.firstVisibleItemIndex
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            if (indexInVisibleItems !in layoutInfo.visibleItemsInfo.indices)
                                ITEM_HEIGHT.dp * cos(PI / 2.6).toFloat()
                            else {
                                val degree = getRotationDegree(
                                    height,
                                    layoutInfo.visibleItemsInfo[indexInVisibleItems]
                                )
                                ITEM_HEIGHT.dp * cos(degree / 180f * PI).toFloat()
                            }
                        )
                        .graphicsLayer {
                            if (indexInVisibleItems !in listState.layoutInfo.visibleItemsInfo.indices) return@graphicsLayer
                            rotationX = getRotationDegree(
                                height,
                                listState.layoutInfo.visibleItemsInfo[indexInVisibleItems]
                            )
                        },
                    text = "${itemIndex}",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun getRotationDegree(height: Dp, itemInfo: LazyListItemInfo): Float {
    // itemPosition is the position of vertical center of the item
    val itemPosition = itemInfo.offset + itemInfo.size / 2
    val h = (height / 2 - itemPosition.toDp()).value
    val r = CLOCK_RADIUS.toFloat()
    return (asin(h / r) * 180 / PI).toFloat()
}

@Preview(showBackground = true)
@Composable
fun ClockPreview() {
    IPhoneLikeClockTheme {
        Clock()
    }
}

fun Int.toDp(): Dp {
    return (this / Resources.getSystem().displayMetrics.density).toInt().dp
}
fun Dp.toPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).value.toInt()
}