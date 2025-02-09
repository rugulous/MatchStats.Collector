package us.rugulo.matchstats

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class StartupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val matchInProgress = (applicationContext as MatchStatsApp).matchSegmentRepository.checkForIncompleteMatch()
        val intent: Intent

        if(matchInProgress != null){
            intent = Intent(this, MainActivity::class.java)
            intent.putExtra("ID", matchInProgress)
        } else {
            intent = Intent(this, ListMatchesActivity::class.java)
        }

        startActivity(intent)

        finish()
    }
}