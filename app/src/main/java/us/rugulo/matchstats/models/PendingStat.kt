package us.rugulo.matchstats.models

import us.rugulo.matchstats.data.StatType

data class PendingStat(
    val statType: Int,
    val homeOrAway: Boolean,
    val timestamp: Long,
    val priorActionId: Int?
)