package us.rugulo.matchstats.models

import kotlinx.serialization.Serializable

@Serializable
data class StatOccurrence(
    val id: Int,
    val isHome: Boolean,
    val typeId: Int,
    val name: String,
    val timestamp: Long,
    val outcomeId: Int,
    val outcomeName: String
)