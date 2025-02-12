package us.rugulo.matchstats

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import us.rugulo.matchstats.models.Match
import us.rugulo.matchstats.ui.viewmodel.ListMatchesViewModel

class ListMatchesActivity : ComponentActivity() {
    private val vm: ListMatchesViewModel by viewModels { ListMatchesViewModel.Factory  }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            ListMatches()
        }
    }

    private fun startMatch(){
        val intent = Intent(this, CreateMatchActivity::class.java)
        startActivity(intent)
        finish()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    @Preview
    fun ListMatches() {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = {
                    Text("Matches")
                })
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {

                Button(onClick = {startMatch()}) {
                    Text("Start New Match")
                }

                if (vm.matches.isEmpty()) {
                    Text(
                        "No matches found. Create one to get started!",
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn {
                        items(vm.matches) { match ->
                            MatchCard(match)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MatchCard(match: Match){
        Card(modifier = Modifier.padding(8.dp).fillMaxWidth(), onClick = {openMatch(match.id)}){
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${match.homeTeam} vs ${match.awayTeam}\n${match.homeGoals} - ${match.awayGoals}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if(match.notes.trim().isNotEmpty()){
                    Text(match.notes, fontSize = 20.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }

    private fun openMatch(matchId: Int){
        val intent = Intent(this, ReviewActivity::class.java)
        intent.putExtra("ID", matchId)
        startActivity(intent)
        finish()
    }
}