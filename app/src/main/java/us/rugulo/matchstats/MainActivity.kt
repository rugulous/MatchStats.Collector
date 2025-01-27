package us.rugulo.matchstats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FootballStatsApp()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FootballStatsApp() {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    modifier = Modifier.padding(12.dp)) {
                    Text("Home")
                }
                Tab(
                    pagerState.currentPage == 1, {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }, Modifier.padding(12.dp)
                ) {
                    Text("Away")
                }
            }
        }
    ) { padding ->
        TabContent(pagerState, modifier = Modifier.padding(padding))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabContent(pagerState: PagerState, modifier: Modifier = Modifier){
    HorizontalPager(state = pagerState) { index ->
        val stats = remember {
            mutableStateMapOf(
                "Crosses" to (0..10).random(),
                "Shots" to (0..10).random(),
                "Shots on Target" to (0..10).random(),
                "Goals" to (0..10).random(),
                "Corners" to (0..10).random()
            )
        }

        StatsScreen(
            modifier = modifier,
            stats = stats
        )
    }
}

@Composable
fun StatsScreen(stats: MutableMap<String, Int>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stats.forEach { (label, count) ->
            CounterButton(label = label, count = count, onIncrement = {
                stats[label] = count + 1
            }, onDecrement = {
                if (count > 0) stats[label] = count - 1
            })
        }
    }
}

@Composable
fun CounterButton(label: String, count: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, fontSize = 24.sp, modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrement")
            }
            Text(count.toString(), fontSize = 24.sp)
            IconButton(onClick = onIncrement) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increment")
            }
        }
    }
}
