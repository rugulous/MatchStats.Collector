    package us.rugulo.matchstats

    import android.content.Intent
    import android.os.Bundle
    import android.widget.Toast
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.width
    import androidx.compose.material3.CircularProgressIndicator
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.unit.dp
    import androidx.lifecycle.lifecycleScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import kotlinx.serialization.json.Json
    import okhttp3.Request
    import us.rugulo.matchstats.models.StatSyncDTO

    class StartupActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            lifecycleScope.launch {
                val matchInProgress = (applicationContext as MatchStatsApp).matchSegmentRepository.checkForIncompleteMatch()
                val intent: Intent

                if(matchInProgress == null) {
                    syncStats()
                    intent = Intent(this@StartupActivity, ListMatchesActivity::class.java)
                } else {
                    intent = Intent(this@StartupActivity, MainActivity::class.java)
                    intent.putExtra("ID", matchInProgress)
                }

                startActivity(intent)
                finish()
            }
        }

        private suspend fun syncStats(){
            val app = application as MatchStatsApp
            var toastMessage: String

            withContext(Dispatchers.IO) {
                val request =
                    Request.Builder().url("https://totty.luketaylor.rocks/stat-sync").build()

                app.httpClient.newCall(request).execute()
                    .use { response ->
                        if (!response.isSuccessful) {
                            toastMessage = "Failed to sync stats - API returned ${response.code}"
                            return@use
                        }

                        val dto = Json.decodeFromString<StatSyncDTO>(response.body!!.string())
                        app.matchSegmentRepository.syncStats(dto)
                        toastMessage = "Stat sync completed!"
                    }
            }

            withContext(Dispatchers.Main){
                Toast.makeText(this@StartupActivity, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }