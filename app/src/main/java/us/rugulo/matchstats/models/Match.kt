package us.rugulo.matchstats.models

data class Match(
    val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val notes: String,
    val startTimestamp: Int?
)