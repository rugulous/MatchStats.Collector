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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
                        Text(
                            "${vm.homeTeam} vs ${vm.awayTeam}\n${vm.notes}",
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
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

                    Row {
                        Text("Stat", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text(vm.homeTeam, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text(vm.awayTeam, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    }

                    for (i in segment.homeStats.keys) {
                        Row(Modifier.fillMaxWidth()) {
                            Text(vm.statTypes[i] ?: "Unknown", Modifier.weight(1f))
                            Text(segment.homeStats[i]?.toString() ?: "0", Modifier.weight(1f))
                            Text(segment.awayStats[i]?.toString() ?: "0", Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
