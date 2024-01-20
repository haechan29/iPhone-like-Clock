package com.hc.iphone_likeclock

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hc.iphone_likeclock.MainActivity.Companion.CLOCK_RADIUS
import com.hc.iphone_likeclock.MainActivity.Companion.HEIGHT
import com.hc.iphone_likeclock.ui.theme.IPhoneLikeClockTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.sin

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
        const val CLOCK_RADIUS = 300
        const val HEIGHT = 100
    }
}

@Composable
fun TestLazyColumn() {
    val listState = rememberLazyListState()
    var myHeight by remember { mutableStateOf(100.dp) }
    LazyColumn(state = listState) {
        items(count = 100) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable {
                        myHeight -= 10.dp
                        Log.d("hhcc", "idx: ${listState.firstVisibleItemIndex}, scroll: ${listState.firstVisibleItemScrollOffset.toDp()}")
                    },
                text = "$it",
                fontSize = 30.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun Clock() {
    val listState = rememberLazyListState()
    val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
    val height = CLOCK_RADIUS.dp / 1.4f
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            // shows about 45 degree of clock
            .height(CLOCK_RADIUS.dp / 1.4f * 2)
            .padding(top = 100.dp),
        state = listState
    ) {
        items(100) { itemIndex ->
            val indexInVisibleItems = itemIndex - listState.firstVisibleItemIndex
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        if (indexInVisibleItems !in layoutInfo.visibleItemsInfo.indices) 0.dp
                        else {
                            val degree = getRotationDegree(
                                height,
                                layoutInfo.visibleItemsInfo[indexInVisibleItems]
                            )
                            HEIGHT.dp * sin(90 - degree)
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
                fontSize = 30.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun getRotationDegree(height: Dp, itemInfo: LazyListItemInfo): Float {
    // itemPosition is the position of vertical center of the item
    val itemPosition = itemInfo.offset + itemInfo.size / 2
    val realHeight = (height - itemPosition.toDp()) / height
    return (atan(realHeight.toDouble()) * 180 / PI).toFloat()
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