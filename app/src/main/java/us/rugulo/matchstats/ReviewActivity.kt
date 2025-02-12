package us.rugulo.matchstats

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import us.rugulo.matchstats.data.CornerOutcome
import us.rugulo.matchstats.data.CrossOutcome
import us.rugulo.matchstats.data.MatchSegmentType
import us.rugulo.matchstats.data.ShotOutcome
import us.rugulo.matchstats.data.StatType
import us.rugulo.matchstats.models.MatchSegment
import us.rugulo.matchstats.models.StatOccurrence
import us.rugulo.matchstats.ui.viewmodel.ReviewViewModel

class ReviewActivity : ComponentActivity() {
    private val vm: ReviewViewModel by viewModels { ReviewViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val matchId = intent.extras?.getInt("ID") ?: throw Error("No match ID specified!")
        vm.setMatchId(matchId)

        setContent { ReviewStats(vm) }
    }

    private fun returnToList() {
        val intent = Intent(this, ListMatchesActivity::class.java)
        startActivity(intent)
        finish()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    @Preview
    fun ReviewStats(vm: ReviewViewModel = viewModel()) {
        val segments by vm.segments.collectAsState() // Observe state

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Review Stats") },
                    navigationIcon = {
                        IconButton(onClick = { returnToList() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier =
                Modifier.fillMaxWidth().padding(padding)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
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
                    }
                }

                TabContent(segments)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun TabContent(segments: List<MatchSegment>) {
        val scope = rememberCoroutineScope()
        val pagerState = rememberPagerState(pageCount = { 2 })

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
                Text("Summary")
            }
            Tab(
                pagerState.currentPage == 1, {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }, Modifier.padding(12.dp)
            ) {
                Text("Timeline")
            }
        }

        HorizontalPager(state = pagerState) { index ->
            if(index == 0){
                StatSummary(segments)
            } else {
                Timeline(segments)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Timeline(segments: List<MatchSegment>){
        LazyColumn(Modifier.padding(horizontal = 10.dp).padding(bottom = 10.dp).fillMaxWidth()) {
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            vm.homeTeam, Modifier.weight(1f), textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                        Text(
                            vm.awayTeam, Modifier.weight(1f), textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }
                }
            }
            items(segments){ segment ->
                DividerWithText(segment.name)

                val unsortedEvents = segment.homeStats.flatMap { it.value.map { stat -> Pair(stat, true) } } + segment.awayStats.flatMap { it.value.map { stat -> Pair(stat, false)} }

                val events = unsortedEvents.sortedBy { it.first.timestamp }

                events.forEach {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    )  {
                        Column (Modifier.weight(2f), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (it.second) {
                                StatItem(it.first, segment)
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }

                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(40.dp)
                                    .background(Color.Gray)
                            )
                        }

                        Column(Modifier.weight(2f), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (!it.second) {
                                StatItem(it.first, segment)
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DividerWithText(text: String) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray,
                thickness = 1.dp
            )
            Text(
                text = text,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 8.dp),
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    @Composable
    fun StatItem(stat: StatOccurrence, segment: MatchSegment) {
        val time = (stat.timestamp - segment.startTime) / 1000
        val minutes = (segment.minuteOffset + (time / 60)).toString().padStart(2, '0')
        val seconds = (time % 60).toString().padStart(2, '0')

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$minutes:$seconds")
                }

                append(" - ${stat.name} ")

                withStyle(SpanStyle(fontStyle = FontStyle.Italic)){
                    append("(${stat.outcomeName})")
                }
            },
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Center
        )
    }

    @Composable
    private fun StatSummary(segments: List<MatchSegment>) {
        val overallHomeStats = mutableMapOf<Int, MutableList<StatOccurrence>>()
        val overallAwayStats = mutableMapOf<Int, MutableList<StatOccurrence>>()

        segments.forEach{ segment ->
            segment.homeStats.keys.forEach {statType ->
                if(!overallHomeStats.containsKey(statType)){
                    overallHomeStats[statType] = mutableListOf()
                }

                segment.homeStats[statType]?.let { overallHomeStats[statType]!!.addAll(it) }
            }

            segment.awayStats.keys.forEach {statType ->
                if(!overallAwayStats.containsKey(statType)){
                    overallAwayStats[statType] = mutableListOf()
                }

                segment.awayStats[statType]?.let { overallAwayStats[statType]!!.addAll(it) }
            }
        }

        val newSegments = segments.toMutableList()
        newSegments.add(0, MatchSegment(-1, MatchSegmentType.FIRST_HALF, "Overall", "O", overallHomeStats, overallAwayStats, 0, 0))

        LazyColumn(Modifier.padding(horizontal = 10.dp)) {
            items(newSegments) { segment ->
                Text(
                    segment.name,
                    Modifier.padding(vertical = 12.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(Modifier.fillMaxWidth()) {
                    Text(
                        "Stat",
                        Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        vm.homeTeam,
                        Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        vm.awayTeam,
                        Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                for (i in segment.homeStats.keys) {
                    val homeStats = segment.homeStats[i]!!
                    val awayStats = segment.awayStats[i]!!
                    var label = vm.statTypes[i] ?: "Unknown"

                    label += if (label.endsWith('s')) {
                        "es"
                    } else {
                        "s"
                    }

                    Row(Modifier.fillMaxWidth()) {

                        var modifier = Modifier.weight(1f)

                        if (i != 1) {
                            modifier = modifier.padding(top = 10.dp)
                        }

                        Text(label, modifier, fontWeight = FontWeight.Bold)
                        Text(homeStats.size.toString(), modifier, fontWeight = FontWeight.Bold)
                        Text(awayStats.size.toString(), modifier, fontWeight = FontWeight.Bold)
                    }

                    when (i) {
                        StatType.CORNER.value -> {
                            CornerSubstats(homeStats, awayStats)
                        }

                        StatType.CROSS.value -> {
                            CrossSubstats(homeStats, awayStats)
                        }

                        StatType.SHOT.value -> {
                            ShotSubstats(homeStats, awayStats)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CornerSubstats(homeStats: List<StatOccurrence>, awayStats: List<StatOccurrence>){
        val types = mutableMapOf<String, Array<Int>>()
        types["Short"] = arrayOf(CornerOutcome.SHORT.value)
        types["Crossed"] = arrayOf(CornerOutcome.CROSS.value)

        SubstatRowWithDefault(homeStats, awayStats, types)
    }

    @Composable
    fun CrossSubstats(homeStats: List<StatOccurrence>, awayStats: List<StatOccurrence>){
        val types = mutableMapOf<String, Array<Int>>()
        types["Won"] = arrayOf(CrossOutcome.SHOT.value, CrossOutcome.CONTROLLED.value)
        types["Lost"] = arrayOf(CrossOutcome.CORNER.value, CrossOutcome.CLEARED.value)
        types["Missed"] = arrayOf(CrossOutcome.OTHER_WING.value, CrossOutcome.OUT_OF_PLAY.value)

        SubstatRowWithDefault(homeStats, awayStats, types)
    }

    @Composable
    fun ShotSubstats(homeStats: List<StatOccurrence>, awayStats: List<StatOccurrence>){
        val onTargetTypes = arrayOf(ShotOutcome.GOAL.value, ShotOutcome.SAVED.value)

        val types = mutableMapOf<String, Pair<Array<Int>, (list: List<StatOccurrence>) -> Int>>()
        types["On Target"] = Pair(
            onTargetTypes,
            ::countList
        )

        types["Goals"] = Pair(
            arrayOf(ShotOutcome.GOAL.value)
        ) { list -> list.count { onTargetTypes.contains(it.outcomeId) } }

        types["Blocked"] = Pair(
            arrayOf(ShotOutcome.BLOCKED.value),
            ::countList
        )

        types["Off Target"] = Pair(
            arrayOf(ShotOutcome.OFF_TARGET.value),
            ::countList
        )

        SubstatRow(homeStats, awayStats, types)
    }

    @Composable
    fun SubstatRow(homeStats: List<StatOccurrence>, awayStats: List<StatOccurrence>, substats: Map<String, Pair<Array<Int>, (list: List<StatOccurrence>) -> Int>>){
        substats.forEach{ s ->
            val home = homeStats.count { s.value.first.contains(it.outcomeId) }
            val homePc = if(homeStats.isEmpty()) "" else "(" + Math.round((home.toDouble() / s.value.second(homeStats)) * 100).toString() + "%)"
            val away = awayStats.count { s.value.first.contains(it.outcomeId) }
            val awayPc = if(awayStats.isEmpty()) "" else "(" + Math.round((away.toDouble() / s.value.second(awayStats)) * 100).toString() + "%)"

            Row{
                Text("- ${s.key}", fontStyle = FontStyle.Italic, color = Color.DarkGray, modifier = Modifier.weight(1f))
                Text("$home $homePc", fontStyle = FontStyle.Italic, color = Color.DarkGray, modifier = Modifier.weight(1f))
                Text("$away $awayPc", fontStyle = FontStyle.Italic, color = Color.DarkGray, modifier = Modifier.weight(1f))
            }
        }
    }

    @Composable
    fun SubstatRowWithDefault(homeStats: List<StatOccurrence>, awayStats: List<StatOccurrence>, substats: Map<String, Array<Int>>){
        val stats = substats.map {
            Pair<String, Pair<Array<Int>, (list: List<StatOccurrence>) -> Int>>(
                it.key,
                Pair(it.value, ::countList)
            )
        }.toMap()

        SubstatRow(homeStats, awayStats, stats)
    }

    private fun countList(list: List<StatOccurrence>): Int = list.size
}
