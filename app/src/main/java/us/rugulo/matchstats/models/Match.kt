package us.rugulo.matchstats.models

import java.util.UUID

data class Match(
    val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val notes: String,
    val startTimestamp: Int?,
    val homeGoals: Int,
    val awayGoals: Int,
    val webId: UUID?
)