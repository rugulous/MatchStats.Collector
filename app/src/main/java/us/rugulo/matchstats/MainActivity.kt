package us.rugulo.matchstats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FootballStatsApp()
        }
    }
}

@Composable
fun FootballStatsApp() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Home")
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Away")
                }
            }
        }
    ) { padding ->
        val stats = remember {
            mutableStateMapOf(
                "Crosses" to 0,
                "Shots" to 0,
                "Shots on Target" to 0,
                "Goals" to 0,
                "Corners" to 0
            )
        }

        StatsScreen(
            modifier = Modifier.padding(padding),
            stats = stats
        )
    }
}

@Composable
fun StatsScreen(modifier: Modifier = Modifier, stats: MutableMap<String, Int>) {
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

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { saveStatsToDatabase(stats) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Save Stats")
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
        Text(label, fontSize = 18.sp, modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrement")
            }
            Text(count.toString(), fontSize = 18.sp)
            IconButton(onClick = onIncrement) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increment")
            }
        }
    }
}

fun saveStatsToDatabase(stats: Map<String, Int>) {
    // Implement database logic here
    println("Saved stats: $stats")
}
