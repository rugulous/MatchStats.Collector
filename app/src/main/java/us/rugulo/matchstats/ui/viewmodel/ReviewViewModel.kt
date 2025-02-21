package us.rugulo.matchstats.ui.viewmodel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import us.rugulo.matchstats.MatchStatsApp
import us.rugulo.matchstats.data.repository.MatchSegmentRepository
import us.rugulo.matchstats.models.MatchSegment

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import java.util.UUID

class ReviewViewModel(matchSegmentRepo: MatchSegmentRepository, httpClient: OkHttpClient) : ViewModel() {
    private val segmentRepo = matchSegmentRepo
    private val client = httpClient
    private var matchId: Int? = null

    val statTypes: Map<Int, String> = matchSegmentRepo.getStatTypes()

    // Use MutableStateFlow to notify UI of changes
    private val _segments = MutableStateFlow<List<MatchSegment>>(emptyList())
    val segments = _segments.asStateFlow()

    var homeTeam = ""
    var awayTeam = ""
    var notes = ""
    val homeGoals = mutableIntStateOf(0)
    val awayGoals = mutableIntStateOf(0)

    val uploadId = mutableStateOf<UUID?>(null)

    fun setMatchId(id: Int) {
        matchId = id

        viewModelScope.launch {
            _segments.value = segmentRepo.getAllSegmentsForMatch(id)

            val details = segmentRepo.getMatchDetails(id)
            homeTeam = details.homeTeam
            awayTeam = details.awayTeam
            notes = details.notes
            homeGoals.intValue = details.homeGoals
            awayGoals.intValue = details.awayGoals
            uploadId.value = details.webId
        }
    }

    fun uploadMatch(){
        val json = encodeMatch()
        val request = Request.Builder()
            .url("https://totty.luketaylor.rocks/record-match")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException){
                throw e
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    val id = UUID.fromString(it.string())

                    segmentRepo.storeUploadId(matchId!!, id)
                    uploadId.value = id
                }
            }
        })
    }

    private fun encodeMatch(): String{
        val segments = _segments.value.map { seg ->
            mapOf(
                "code" to JsonPrimitive(seg.code),
                "startTime" to JsonPrimitive(seg.startTime),
                "events" to JsonArray((seg.homeStats.values.flatten() + seg.awayStats.values.flatten()).map { Json.encodeToJsonElement(it) })
            )
        }

        val match = buildJsonObject {
            put("homeTeam", Json.encodeToJsonElement(homeTeam))
            put("awayTeam", Json.encodeToJsonElement(awayTeam))
            put("notes", Json.encodeToJsonElement(notes))
            put("homeGoals", Json.encodeToJsonElement(homeGoals.intValue))
            put("awayGoals", Json.encodeToJsonElement(awayGoals.intValue))
            put("segments", Json.encodeToJsonElement(segments))
        }

        return Json.encodeToJsonElement(match).toString()
    }

    companion object {
        val Factory : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = (checkNotNull(extras[APPLICATION_KEY]) as MatchStatsApp)

                return ReviewViewModel(
                    application.matchSegmentRepository,
                    application.httpClient
                ) as T
            }
        }
    }
}
