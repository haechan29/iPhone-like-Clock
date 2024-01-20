package com.hc.iphone_likeclock

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hc.iphone_likeclock.MainActivity.Companion.HEIGHT
import com.hc.iphone_likeclock.ui.theme.IPhoneLikeClockTheme

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
        const val HEIGHT = 100
    }
}

@Composable
fun Clock() {
    val state = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(HEIGHT.dp * 3),
        state = state
    ) {
        items(100) {
            val idx = state.firstVisibleItemIndex
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HEIGHT.dp),
                text = "${it % 10}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClockPreview() {
    IPhoneLikeClockTheme {
        Clock()
    }
}