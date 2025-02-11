package us.rugulo.matchstats

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import us.rugulo.matchstats.data.CornerOutcome
import us.rugulo.matchstats.data.CrossOutcome
import us.rugulo.matchstats.data.ShotOutcome
import us.rugulo.matchstats.data.StatType
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
                Modifier.fillMaxWidth().padding(padding).verticalScroll(rememberScrollState())
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var header = "${vm.homeTeam} (${vm.homeGoals.intValue}) vs ${vm.awayTeam} (${vm.awayGoals.intValue})"
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

                segments.forEach { segment ->
                    Text(
                        segment.name,
                        Modifier.padding(vertical = 12.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(Modifier.fillMaxWidth()) {
                        Text("Stat", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(vm.homeTeam, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(vm.awayTeam, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    for (i in segment.homeStats.keys) {
                        val homeStats = segment.homeStats[i]!!
                        val awayStats = segment.awayStats[i]!!
                        var label = vm.statTypes[i] ?: "Unknown"

                        label += if(label.endsWith('s')){
                            "es"
                        } else {
                            "s"
                        }

                        var modifier = Modifier.weight(1f)

                        if(i != 1){
                            modifier = modifier.padding(top = 10.dp)
                        }

                        Row(Modifier.fillMaxWidth()) {
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
