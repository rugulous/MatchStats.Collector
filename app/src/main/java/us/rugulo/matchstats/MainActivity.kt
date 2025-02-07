package us.rugulo.matchstats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import us.rugulo.matchstats.ui.viewmodel.MatchStatsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    private val vm: MatchStatsViewModel by viewModels { MatchStatsViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val matchId = intent.extras?.getInt("ID") ?: throw Error("No match ID specified!")
        vm.setMatchId(matchId)

        setContent {
            FootballStatsApp()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FootballStatsApp(viewModel: MatchStatsViewModel = viewModel()) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text("Record Match Stats")
            })
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        "Totty United vs Glossop\nDevelopment Division Cup",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(0.dp, 5.dp)
                    )

                    if (viewModel.inProgress.value) {
                        val segment = viewModel.currentSegment.value!!

                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                text = segment.code,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${viewModel.elapsedMinutes.value}:${viewModel.elapsedSeconds.value}",
                                fontSize = 22.sp
                            )
                        }

                        Button(onClick = {viewModel.closeSegment()}) {
                            Text(
                                text = "End ${segment.name}"
                            )
                        }

                    } else {
                        Button(onClick = {viewModel.startSegment()}) {
                            Text(
                                text = "Start ${viewModel.nextSegmentName.value}",
                                fontSize = 20.sp
                            )
                        }
                    }
                }

            }

            if(viewModel.inProgress.value) {
                //tabs for actual stat recording
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        modifier = Modifier.padding(12.dp)
                    ) {
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
                TabContent(pagerState)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabContent(pagerState: PagerState, modifier: Modifier = Modifier){
    HorizontalPager(state = pagerState) { index ->
            StatsScreen(
                modifier = modifier,
                isHome = index == 0
            )
    }
}

@Composable
fun StatsScreen(isHome: Boolean, modifier: Modifier = Modifier, vm: MatchStatsViewModel = viewModel()) {
    val currentSegment = vm.currentSegment.value // Observe currentSegment directly

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        currentSegment?.let {
            // Dynamically load homeStats or awayStats based on isHome
            val stats = if (isHome) it.homeStats else it.awayStats

            // Render each stat button dynamically
            stats.forEach { (id, count) ->
                CounterButton(
                    label = vm.statTypes[id] ?: "Unknown", // Handle unknown stat type
                    count = count,
                    onIncrement = { vm.incrementStat(isHome, id) },
                    onDecrement = { vm.decrementStat(isHome, id) }
                )
            }
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

@Preview
@Composable
fun Preview(){
    FootballStatsApp()
}