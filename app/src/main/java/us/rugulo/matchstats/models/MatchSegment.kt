package us.rugulo.matchstats.models

import us.rugulo.matchstats.data.MatchSegmentType

data class MatchSegment (
    val id: MatchSegmentType,
    val name: String,
    val code: String,
    var homeStats: MutableMap<Int, Int>,
    var awayStats: MutableMap<Int, Int>
)