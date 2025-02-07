package us.rugulo.matchstats

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class StartupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resumeIntent = Intent(this, MainActivity::class.java)

        val matchInProgress = (applicationContext as MatchStatsApp).matchSegmentRepository.checkForIncompleteMatch()
        if(matchInProgress != null){
            resumeIntent.putExtra("ID", matchInProgress)
        }

        startActivity(resumeIntent)
        finish()
    }
}