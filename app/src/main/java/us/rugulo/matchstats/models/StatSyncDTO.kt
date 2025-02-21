package us.rugulo.matchstats.models

import kotlinx.serialization.Serializable

@Serializable
class StatSyncDTO {
    val stats: Array<StatsDTO> = arrayOf()
    val outcomes: Array<OutcomeDTO> = arrayOf()

    @Serializable
    data class StatsDTO(
        val id: Int,
        val description: String,
        val isActive: Boolean,
        val sortOrder: Int?
    )

    @Serializable
    data class OutcomeDTO(
        val id: Int,
        val triggeringStatTypeId: Int,
        val name: String,
        val nextActionId: Int?,
        val isActive: Boolean,
        val sortOrder: Int?,
        val isGoal: Boolean
    )
}