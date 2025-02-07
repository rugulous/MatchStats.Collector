package us.rugulo.matchstats.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import us.rugulo.matchstats.MatchStatsApp
import us.rugulo.matchstats.data.MatchSegmentType
import us.rugulo.matchstats.data.repository.MatchSegmentRepository
import us.rugulo.matchstats.models.MatchSegment

class MatchStatsViewModel(matchSegmentRepository: MatchSegmentRepository) : ViewModel() {
    private var segmentRepo = matchSegmentRepository
    private var timerJob: Job? = null

    var inProgress = mutableStateOf(false)
    var currentSegment = mutableStateOf<MatchSegment?>(null)
    var statTypes = mapOf<Int, String>()
    var elapsedMinutes = mutableStateOf("")
    var elapsedSeconds = mutableStateOf("")

    fun startSegment(type: MatchSegmentType){
        this.inProgress.value = true
        this.currentSegment.value = segmentRepo.initialiseSegment(1, type)
        startTimer()
    }

    fun incrementStat(isHome: Boolean, statTypeId: Int){
        val segment = currentSegment.value ?: return

        segmentRepo.recordStat(segment.id, isHome, statTypeId)
        updateUiStatCount(isHome, statTypeId, 1)
    }

    fun decrementStat(isHome: Boolean, statTypeId: Int){
        val segment = currentSegment.value ?: return

        val change = segmentRepo.removeStat(segment.id, isHome, statTypeId)
        updateUiStatCount(isHome, statTypeId, -change)
    }

    private fun updateUiStatCount(isHome: Boolean, statTypeId: Int, changeBy: Int) {
        if(changeBy == 0){
            return
        }

        val segment = currentSegment.value ?: return

        val updatedHomeStats = segment.homeStats.toMutableMap()
        val updatedAwayStats = segment.awayStats.toMutableMap()

        if (isHome) {
            updatedHomeStats[statTypeId] = updatedHomeStats.getOrDefault(statTypeId, 0) + changeBy
        } else {
            updatedAwayStats[statTypeId] = updatedAwayStats.getOrDefault(statTypeId, 0) + changeBy
        }

        // Create a new MatchSegment with updated stats and reassign it, so that the UI knows to update
        currentSegment.value = segment.copy(homeStats = updatedHomeStats, awayStats = updatedAwayStats)
    }

    private fun startTimer(){
        elapsedSeconds.value = "00"
        elapsedMinutes.value = "00"

        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while(inProgress.value){
                val segment = currentSegment.value ?: continue

                val diff: Int = ((System.currentTimeMillis() - segment.startTime) / 1000).toInt()
                elapsedMinutes.value = (diff / 60).toString().padStart(2, '0')
                elapsedSeconds.value = (diff % 60).toString().padStart(2, '0')

                delay(1000)
            }
        }
    }

    // Stop the timer when ViewModel is cleared to avoid memory leaks
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        val Factory : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])

                return MatchStatsViewModel(
                    (application as MatchStatsApp).matchSegmentRepository,
                ) as T
            }
        }
    }
}