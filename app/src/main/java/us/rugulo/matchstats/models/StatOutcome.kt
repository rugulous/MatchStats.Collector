package us.rugulo.matchstats.models

import us.rugulo.matchstats.data.StatType

data class StatOutcome(
    val name: String,
    val nextAction: StatType?
)