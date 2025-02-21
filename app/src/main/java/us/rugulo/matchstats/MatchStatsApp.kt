package us.rugulo.matchstats

import android.app.Application
import okhttp3.OkHttpClient
import us.rugulo.matchstats.data.Database
import us.rugulo.matchstats.data.repository.MatchSegmentRepository

class MatchStatsApp : Application() {
    private lateinit var database: Database

    lateinit var matchSegmentRepository: MatchSegmentRepository
        private set

    val httpClient: OkHttpClient by lazy {
        OkHttpClient()
    }

    override fun onCreate() {
        super.onCreate()
        database = Database(this)
        matchSegmentRepository = MatchSegmentRepository(database)
    }
}