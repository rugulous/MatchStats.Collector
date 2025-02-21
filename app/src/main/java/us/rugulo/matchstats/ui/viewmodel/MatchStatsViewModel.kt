package us.rugulo.matchstats.ui.viewmodel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import us.rugulo.matchstats.MatchStatsApp
import us.rugulo.matchstats.data.MatchSegmentType
import us.rugulo.matchstats.data.ShotOutcome
import us.rugulo.matchstats.data.repository.MatchSegmentRepository
import us.rugulo.matchstats.models.MatchSegment
import us.rugulo.matchstats.models.PendingStat
import us.rugulo.matchstats.models.StatOccurrence
import us.rugulo.matchstats.models.StatOutcome

class MatchStatsViewModel(matchSegmentRepository: MatchSegmentRepository) : ViewModel() {
    private var segmentRepo = matchSegmentRepository
    private var timerJob: Job? = null
    private var matchId: Int? = null
    private val isMatchFinished = MutableStateFlow(false)
    private val allOutcomes = matchSegmentRepository.getAvailableOutcomes()

    val navigateToNextScreen: SharedFlow<Boolean> = isMatchFinished

    val inProgress = mutableStateOf(false)
    val currentSegment = mutableStateOf<MatchSegment?>(null)
    val elapsedMinutes = mutableStateOf("")
    val elapsedSeconds = mutableStateOf("")
    var homeTeam = ""
    var awayTeam = ""
    var notes = ""
    val homeGoals = mutableIntStateOf(0)
    val awayGoals = mutableIntStateOf(0)

    val pendingStat = mutableStateOf<PendingStat?>(null)
    val outcomes = mutableListOf<StatOutcome>()

    val nextSegmentType = mutableStateOf(MatchSegmentType.FIRST_HALF)
    val nextSegmentName = mutableStateOf("Match")

    fun setMatchId(id: Int){
        this.matchId = id
        val details = segmentRepo.getMatchDetails(id)
        homeTeam = details.homeTeam
        awayTeam = details.awayTeam
        notes = details.notes
        homeGoals.intValue = details.homeGoals
        awayGoals.intValue = details.awayGoals

        currentSegment.value = segmentRepo.getIncompleteMatchSegment(id)

        currentSegment.value?.let {
            startTimer()
            inProgress.value = true
        }

        val nextSegment = segmentRepo.getNextSegment(id)
        nextSegmentType.value = nextSegment.first
        nextSegmentName.value = nextSegment.second
    }

    fun startSegment(){
        if(matchId == null){
            throw Error("You must pass the match ID before attempting to do anything!")
        }

        this.inProgress.value = true
        this.currentSegment.value = segmentRepo.initialiseSegment(matchId!!, nextSegmentType.value)
        startTimer()
    }

    fun closeSegment(){
        timerJob?.cancel()

        currentSegment.value?.let {
            segmentRepo.finaliseSegment(it.id)

            if(it.type == MatchSegmentType.ET_SECOND_HALF){
                endMatch()
            } else {
                nextSegmentType.value = MatchSegmentType.fromInt(it.type.value + 1)
                nextSegmentName.value = segmentRepo.getSegmentName(nextSegmentType.value)
            }
        }

        currentSegment.value = null
        inProgress.value = false
    }

    fun openModalForAction(action: Int, homeOrAway: Boolean, priorAction: Int? = null){
        outcomes.clear()
        allOutcomes[action]?.let {
            outcomes.addAll(it)
        }

        pendingStat.value = PendingStat(
            action,
            homeOrAway,
            System.currentTimeMillis(),
            priorAction
        )
    }

    fun handleChosenOutcome(outcome: StatOutcome){
        val pending = pendingStat.value ?: throw Error("Missing pending stat")
        val segment = currentSegment.value ?: throw Error("Missing current segment")

        val statId = segmentRepo.recordStat(segment.id, pending, outcome)
        val occurrence = StatOccurrence(
            statId,
            pending.homeOrAway,
            pending.statType,
            "",
            pending.timestamp,
            outcome.id,
            outcome.name
        )

        val modifiedHomeStats = segment.homeStats.toMutableMap()
        val modifiedAwayStats = segment.awayStats.toMutableMap()

        if (pending.homeOrAway) {
            val updatedList = modifiedHomeStats[pending.statType]?.toMutableList() ?: mutableListOf()
            updatedList.add(occurrence)
            modifiedHomeStats[pending.statType] = updatedList
        } else {
            val updatedList = modifiedAwayStats[pending.statType]?.toMutableList() ?: mutableListOf()
            updatedList.add(occurrence)
            modifiedAwayStats[pending.statType] = updatedList
        }

        currentSegment.value = segment.copy(homeStats = modifiedHomeStats, awayStats = modifiedAwayStats)

        if(outcome.id == ShotOutcome.GOAL.value){
            if(pending.homeOrAway){
                homeGoals.intValue += 1
            } else {
                awayGoals.intValue += 1
            }
        }

        if(outcome.nextAction != null){
            openModalForAction(outcome.nextAction, pending.homeOrAway, statId)
        } else {
            closeModal()
        }
    }

    fun closeModal(){
        pendingStat.value = null
    }

    fun endMatch(){
        isMatchFinished.value = true
    }

    private fun startTimer(){
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