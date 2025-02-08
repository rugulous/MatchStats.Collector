package us.rugulo.matchstats

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import us.rugulo.matchstats.ui.viewmodel.CreateMatchViewModel

class CreateMatchActivity : ComponentActivity() {
    private val vm: CreateMatchViewModel by viewModels { CreateMatchViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CreateMatch()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.startMatch.collect{ v ->
                    if(v != null){
                        intent = Intent(this@CreateMatchActivity, MainActivity::class.java)
                        intent.putExtra("ID", v)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun CreateMatch() {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = {
                    Text("Create Match")
                })
            }
        ) { padding ->

            Column(modifier = Modifier.fillMaxWidth().padding(padding)) {
                TextField(
                    value = vm.homeTeamName.value,
                    onValueChange = { vm.homeTeamName.value = it },
                    label = { Text("Home Team") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )

                TextField(
                    value = vm.awayTeamName.value,
                    onValueChange = { vm.awayTeamName.value = it },
                    label = { Text("Away Team") },
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 16.dp),
                    maxLines = 1
                )

                TextField(
                    value = vm.notes.value,
                    onValueChange = {vm.notes.value = it},
                    label = {Text("Notes")},
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {vm.create()}) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
