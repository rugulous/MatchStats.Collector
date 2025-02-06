package us.rugulo.matchstats.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import us.rugulo.matchstats.data.MatchSegmentType
import us.rugulo.matchstats.models.MatchSegment

class MatchStatsViewModel : ViewModel() {
    var inProgress = mutableStateOf(false)
    var currentSegment = mutableStateOf<MatchSegment?>(null)
    var statTypes = mapOf<Int, String>()

    fun startMatch(){
        this.inProgress.value = true;
        this.currentSegment.value = MatchSegment(
            MatchSegmentType.FIRST_HALF,
            "First Half",
            "1H",
            statTypes.mapValues { 0 }.toMutableMap(),
            statTypes.mapValues { 0 }.toMutableMap()
        )
    }

    fun updateStatCount(isHome: Boolean, statTypeId: Int, changeBy: Int) {
        val segment = currentSegment.value ?: return

        // Copy homeStats or awayStats and update the value
        val updatedHomeStats = segment.homeStats.toMutableMap()
        val updatedAwayStats = segment.awayStats.toMutableMap()

        if (isHome) {
            updatedHomeStats[statTypeId] = updatedHomeStats.getOrDefault(statTypeId, 0) + changeBy
        } else {
            updatedAwayStats[statTypeId] = updatedAwayStats.getOrDefault(statTypeId, 0) + changeBy
        }

        // Create a new MatchSegment with updated stats and reassign it
        currentSegment.value = segment.copy(homeStats = updatedHomeStats, awayStats = updatedAwayStats)
    }

}