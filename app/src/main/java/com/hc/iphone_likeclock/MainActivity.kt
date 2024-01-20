package com.hc.iphone_likeclock

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hc.iphone_likeclock.MainActivity.Companion.CLOCK_RADIUS
import com.hc.iphone_likeclock.MainActivity.Companion.HEIGHT
import com.hc.iphone_likeclock.ui.theme.IPhoneLikeClockTheme
import java.lang.Exception
import kotlin.math.PI
import kotlin.math.atan

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
fun Clock() {
    val state = rememberLazyListState()
    val height = CLOCK_RADIUS.dp / 1.4f
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            // shows about 45 degree of clock
            .height(CLOCK_RADIUS.dp / 1.4f * 2),
        state = state
    ) {
        items(100) { itemIndex ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HEIGHT.dp)
                    .graphicsLayer {
                        val indexInVisibleItems = itemIndex - state.firstVisibleItemIndex
                        if (indexInVisibleItems !in state.layoutInfo.visibleItemsInfo.indices) return@graphicsLayer
                        val itemInfo = state.layoutInfo.visibleItemsInfo[indexInVisibleItems]
                        // itemPosition is the position of vertical center of the item
                        val itemPosition = itemInfo.offset + itemInfo.size / 2
                        val realHeight = (height - itemPosition.toDp()) / height
                        val degree = getDegree(realHeight.toDouble())
                        rotationX = degree.toFloat()
                    },
                text = "${itemIndex}",
                fontSize = 30.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getDegree(height: Double): Double {
    return atan(height) * 180 / PI
}

@Preview(showBackground = true)
@Composable
fun ClockPreview() {
    IPhoneLikeClockTheme {
        Clock()
    }
}