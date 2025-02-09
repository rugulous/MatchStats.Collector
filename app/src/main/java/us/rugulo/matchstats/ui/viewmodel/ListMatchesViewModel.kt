package us.rugulo.matchstats.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import us.rugulo.matchstats.MatchStatsApp
import us.rugulo.matchstats.data.repository.MatchSegmentRepository
import us.rugulo.matchstats.models.Match

class ListMatchesViewModel(matchSegmentRepository: MatchSegmentRepository) : ViewModel() {
    val matches: List<Match> = matchSegmentRepository.listMatches()

    companion object {
        val Factory : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])

                return ListMatchesViewModel(
                    (application as MatchStatsApp).matchSegmentRepository,
                ) as T
            }
        }
    }
}