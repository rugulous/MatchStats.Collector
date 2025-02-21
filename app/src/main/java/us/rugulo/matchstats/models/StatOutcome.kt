package us.rugulo.matchstats.models

import us.rugulo.matchstats.data.StatType

data class StatOutcome(
    val id: Int,
    val name: String,
    val nextAction: Int?
)