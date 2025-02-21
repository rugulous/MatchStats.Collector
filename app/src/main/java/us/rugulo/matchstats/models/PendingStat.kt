package us.rugulo.matchstats.models

data class PendingStat(
    val statType: Int,
    val homeOrAway: Boolean,
    val timestamp: Long,
    val priorActionId: Int?
)