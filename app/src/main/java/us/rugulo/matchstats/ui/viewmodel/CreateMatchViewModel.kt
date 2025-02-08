package us.rugulo.matchstats.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import us.rugulo.matchstats.MatchStatsApp
import us.rugulo.matchstats.data.repository.MatchSegmentRepository

class CreateMatchViewModel(matchSegmentRepository: MatchSegmentRepository) : ViewModel() {
    private val segmentRepo = matchSegmentRepository
    private val matchId = MutableStateFlow<Int?>(null)

    val startMatch: SharedFlow<Int?> = matchId

    val homeTeamName = mutableStateOf("")
    val awayTeamName = mutableStateOf("")
    val notes = mutableStateOf("")

    fun create(){
        matchId.value = segmentRepo.createMatch(homeTeamName.value, awayTeamName.value, notes.value)
    }

    companion object {
        val Factory : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])

                return CreateMatchViewModel(
                    (application as MatchStatsApp).matchSegmentRepository,
                ) as T
            }
        }
    }
}