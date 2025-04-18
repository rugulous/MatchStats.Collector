package us.rugulo.matchstats

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import us.rugulo.matchstats.ui.viewmodel.MatchStatsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import us.rugulo.matchstats.data.MatchSegmentType
import us.rugulo.matchstats.models.StatOccurrence
import us.rugulo.matchstats.models.StatOutcome

class MainActivity : ComponentActivity() {
    private val vm: MatchStatsViewModel by viewModels { MatchStatsViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val matchId = intent.extras?.getInt("ID") ?: throw Error("No match ID specified!")
        vm.setMatchId(matchId)

        setContent {
            FootballStatsApp()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.navigateToNextScreen.collect { v ->
                    if (v) {
                        val intent = Intent(this@MainActivity, ReviewActivity::class.java)
                        intent.putExtra("ID", matchId)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var header = "${vm.homeTeam} vs ${vm.awayTeam}\n${vm.homeGoals.intValue} - ${vm.awayGoals.intValue}"
                        if(vm.notes.trim().isNotEmpty()){
                            header += "\n${vm.notes}"
                        }

                        Text(
                            header,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(0.dp, 5.dp),
                            textAlign = TextAlign.Center
                        )

                        if (viewModel.inProgress.value) {
                            val segment = viewModel.currentSegment.value!!

                            Row(
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

                            Button(onClick = { viewModel.closeSegment() }) {
                                Text(
                                    text = "End ${segment.name}"
                                )
                            }

                        } else {
                            if (viewModel.nextSegmentType.value == MatchSegmentType.ET_FIRST_HALF) {
                                Button(onClick = { viewModel.endMatch() }) {
                                    Text(
                                        text = "End Match",
                                        fontSize = 20.sp
                                    )
                                }
                            }

                            Button(onClick = { viewModel.startSegment() }) {
                                Text(
                                    text = "Start ${viewModel.nextSegmentName.value}",
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }

                }

                if (viewModel.inProgress.value) {
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
                            Text(vm.homeTeam)
                        }
                        Tab(
                            pagerState.currentPage == 1, {
                                scope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            }, Modifier.padding(12.dp)
                        ) {
                            Text(vm.awayTeam)
                        }
                    }
                    TabContent(pagerState)
                }
            }

            if (viewModel.pendingStat.value != null) {
                OutcomeDialog(vm.outcomes)
            }
        }
    }

    @Composable
    fun TabContent(pagerState: PagerState, modifier: Modifier = Modifier) {
        HorizontalPager(state = pagerState, userScrollEnabled = false) { index ->
            StatsScreen(
                modifier = modifier,
                isHome = index == 0
            )
        }
    }

    @Composable
    fun StatsScreen(
        isHome: Boolean,
        modifier: Modifier = Modifier,
        vm: MatchStatsViewModel = viewModel()
    ) {
        val currentSegment = vm.currentSegment.value // Observe currentSegment directly

        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            currentSegment?.let {
                val stats = if (isHome) it.homeStats else it.awayStats

                for(stat in vm.availableStats){
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        StatColumn(stat.name, isHome, stat.id, stats)
                    }
                }
            }
        }
    }

    @Composable
    fun StatColumn(caption: String, isHome: Boolean, statType: Int, stats: Map<Int, List<StatOccurrence>>){
        val featuredStats = stats[statType] ?: return
        val segment = vm.currentSegment.value ?: return

        Button(
            onClick = { vm.openModalForAction(statType, isHome) },
            Modifier.fillMaxWidth()
        ) {
            Text(caption)
        }

        Text(buildAnnotatedString {
            append("This half: ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(featuredStats.size.toString())
            }
        })

        LazyColumn(Modifier.padding(top = 20.dp)) {
            items(featuredStats){
                Text(buildAnnotatedString {
                    val elapsedSeconds = (it.timestamp - segment.startTime) / 1000
                    val minute = elapsedSeconds / 60
                    val second = elapsedSeconds % 60

                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)){
                        append(minute.toString().padStart(2, '0'))
                        append(':')
                        append(second.toString().padStart(2, '0'))
                    }

                    append(" - ${it.outcomeName}")
                }, Modifier.padding(top = 5.dp))
            }
        }
    }

    @Composable
    fun OutcomeDialog(outcomes: List<StatOutcome>) {
        Dialog(onDismissRequest = { vm.closeModal() }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select Outcome",
                        style = MaterialTheme.typography.titleMedium
                    )
                    outcomes.forEach { outcome ->
                        ElevatedButton(
                            onClick = { vm.handleChosenOutcome(outcome) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = outcome.name)
                        }
                    }

                    Button(
                        onClick = { vm.closeModal() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun Preview() {
        FootballStatsApp()
    }
}