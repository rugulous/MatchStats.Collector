package us.rugulo.matchstats.models

import kotlinx.serialization.Serializable
import us.rugulo.matchstats.data.MatchSegmentType

@Serializable
data class MatchSegment (
    val id: Int,
    val type: MatchSegmentType,
    val name: String,
    val code: String,
    var homeStats: MutableMap<Int, MutableList<StatOccurrence>>,
    var awayStats: MutableMap<Int, MutableList<StatOccurrence>>,
    val startTime: Long,
    val minuteOffset: Int
)