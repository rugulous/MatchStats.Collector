package us.rugulo.matchstats.models

data class StatOccurrence(
    val id: Int,
    val name: String,
    val timestamp: Long,
    val outcomeId: Int,
    val outcomeName: String
)