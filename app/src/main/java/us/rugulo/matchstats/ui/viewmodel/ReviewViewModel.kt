package us.rugulo.matchstats.ui.viewmodel

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import us.rugulo.matchstats.MatchStatsApp
import us.rugulo.matchstats.data.repository.MatchSegmentRepository
import us.rugulo.matchstats.models.MatchSegment

class ReviewViewModel(matchSegmentRepo: MatchSegmentRepository) : ViewModel() {
    private var segmentRepo = matchSegmentRepo
    private var matchId: Int? = null

    val statTypes: Map<Int, String> = matchSegmentRepo.getStatTypes()

    // Use MutableStateFlow to notify UI of changes
    private val _segments = MutableStateFlow<List<MatchSegment>>(emptyList())
    val segments = _segments.asStateFlow()

    var homeTeam = ""
    var awayTeam = ""
    var notes = ""
    val homeGoals = mutableIntStateOf(0)
    val awayGoals = mutableIntStateOf(0)

    fun setMatchId(id: Int) {
        matchId = id

        viewModelScope.launch {
            _segments.value = segmentRepo.getAllSegmentsForMatch(id)

            val details = segmentRepo.getMatchDetails(id)
            homeTeam = details.homeTeam
            awayTeam = details.awayTeam
            notes = details.notes
            homeGoals.intValue = details.homeGoals
            awayGoals.intValue = details.awayGoals
        }
    }

    companion object {
        val Factory : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])

                return ReviewViewModel(
                    (application as MatchStatsApp).matchSegmentRepository,
                ) as T
            }
        }
    }
}
