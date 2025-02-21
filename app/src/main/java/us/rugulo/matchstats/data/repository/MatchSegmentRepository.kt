package us.rugulo.matchstats.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import us.rugulo.matchstats.data.Database
import us.rugulo.matchstats.data.MatchSegmentType
import us.rugulo.matchstats.data.StatType
import us.rugulo.matchstats.models.Match
import us.rugulo.matchstats.models.MatchSegment
import us.rugulo.matchstats.models.PendingStat
import us.rugulo.matchstats.models.StatOccurrence
import us.rugulo.matchstats.models.StatOutcome
import java.util.UUID

class MatchSegmentRepository(db: Database) {
    private val _db: Database = db

    fun initialiseSegment(match: Int, type: MatchSegmentType): MatchSegment{
        val content = ContentValues()
        content.put("MatchId", match)
        content.put("SegmentTypeId", type.value)
        content.put("StartTime", System.currentTimeMillis())

        val con = _db.writableDatabase
        val id = con.insert("MatchSegments", null, content)
        val record = loadMatchSegment(id.toInt(), con)
        con.close()

        return record
    }

    fun finaliseSegment(matchSegmentId: Int){
        val con = _db.writableDatabase
        con.execSQL("UPDATE MatchSegments SET EndTime = ? WHERE ID = ?", arrayOf(System.currentTimeMillis(), matchSegmentId))
        con.close()
    }

    fun getSegmentName(segmentType: MatchSegmentType): String{
        val con = _db.readableDatabase
        val cursor = con.query("MatchSegmentTypes", arrayOf("Name"), "ID = ?", arrayOf(segmentType.value.toString()), null, null, null)

        cursor.moveToNext()
        val name = cursor.getString(0)

        cursor.close()
        con.close()

        return name
    }

    fun recordStat(segmentId: Int, pending: PendingStat, outcome: StatOutcome): Int{
        val content = ContentValues()
        content.put("MatchSegmentId", segmentId)
        content.put("HomeOrAway", pending.homeOrAway)
        content.put("StatTypeId", pending.statType.value)
        content.put("Timestamp", pending.timestamp)
        content.put("PriorStatID", pending.priorActionId)
        content.put("OutcomeID", outcome.id)

        val con = _db.writableDatabase
        val id = con.insert("MatchStats", null, content)
        con.close()

        return id.toInt()
    }

    fun checkForIncompleteMatch(): Int?{
        var matchId: Int? = null

        val con = _db.readableDatabase
        //search for segments without an end, or for matches with an odd number of segments (1H without 2H, 1H/2H/1ET without 2ET), or 0 segments (not started)
        val cursor = con.rawQuery("SELECT MatchId FROM MatchSegments GROUP BY MatchId HAVING COUNT(*) % 2 = 1 OR COUNT(*) = 0 OR SUM(CASE WHEN EndTime IS NULL THEN 1 ELSE 0 END) > 0;", null)

        if(cursor.moveToNext()){
            matchId = cursor.getInt(0)
        }

        cursor.close()
        con.close()

        return matchId
    }

    //todo: move this somewhere better
    fun getStatTypes(): Map<Int, String> {
        val con = _db.readableDatabase
        val statsMap = mutableMapOf<Int, String>()

        val cursor = con.query("StatTypes", null, null, null, null, null, null)
        cursor.use {
            while(cursor.moveToNext()){
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("Description"))
                statsMap[id] = name
            }
        }

        return statsMap
    }

    fun getIncompleteMatchSegment(matchId: Int): MatchSegment?{
        var segment: MatchSegment? = null

        val con = _db.readableDatabase
        val cursor = con.rawQuery("SELECT ID FROM MatchSegments WHERE EndTime IS NULL AND MatchID = ?", arrayOf(matchId.toString()))

        if(cursor.moveToNext()){
            val id = cursor.getInt(0)
            segment = loadMatchSegment(id, con)
        }

        cursor.close()
        con.close()
        return segment
    }

    fun getNextSegment(matchId: Int): Pair<MatchSegmentType, String>{
        var name = "Match"
        var type = MatchSegmentType.FIRST_HALF

        val con = _db.readableDatabase
        val cursor = con.rawQuery("SELECT SegmentTypeId FROM MatchSegments WHERE MatchID = ? ORDER BY StartTime DESC LIMIT 1", arrayOf(matchId.toString()))

        if(cursor.moveToNext()){
            type = MatchSegmentType.fromInt(cursor.getInt(0) + 1)
            name = getSegmentName(type)
        }

        cursor.close()
        con.close()

        return Pair(type, name)
    }

    fun createMatch(home: String, away: String, notes: String): Int{
        val values = ContentValues()
        values.put("HomeTeam", home)
        values.put("AwayTeam", away)
        values.put("Notes", notes)

        val con = _db.writableDatabase
        val id = con.insert("Matches", null, values)
        con.close()

        return id.toInt()
    }

    //todo: move this somewhere better
    fun getMatchDetails(id: Int): Match{
        val con = _db.readableDatabase
        val cursor = con.rawQuery("SELECT ID, HomeTeam, AwayTeam, Notes, ms.StartTime, WebID, (SELECT COUNT(*) FROM MatchStats ms INNER JOIN MatchSegments s ON s.ID = ms.MatchSegmentId WHERE StatTypeId = 2 AND OutcomeId = 9 AND HomeOrAway = 1 AND s.MatchId = m.ID) HomeGoals, (SELECT COUNT(*) FROM MatchStats ms INNER JOIN MatchSegments s ON s.ID = ms.MatchSegmentId WHERE StatTypeId = 2 AND OutcomeId = 9 AND HomeOrAway = 0 AND s.MatchId = m.ID) AwayGoals FROM Matches m LEFT OUTER JOIN (SELECT MatchID, MIN(StartTime) StartTime FROM MatchSegments ms GROUP BY MatchID) ms ON ms.MatchID = m.ID WHERE ID = ?", arrayOf(id.toString()))

        cursor.moveToNext()
        var webId: UUID? = null

        try {
            webId = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow("WebID")))
        } catch (exception: Exception) {
            //it's okay if this fails, we might not have uploaded it yet
        }


        val result = Match(
            cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
            cursor.getString(cursor.getColumnIndexOrThrow("HomeTeam")),
            cursor.getString(cursor.getColumnIndexOrThrow("AwayTeam")),
            cursor.getString(cursor.getColumnIndexOrThrow("Notes")),
            cursor.getInt(cursor.getColumnIndexOrThrow("StartTime")),
            cursor.getInt(cursor.getColumnIndexOrThrow("HomeGoals")),
            cursor.getInt(cursor.getColumnIndexOrThrow("AwayGoals")),
            webId
        )
        cursor.close()
        con.close()
        return result
    }

    fun getAllSegmentsForMatch(matchId: Int): List<MatchSegment>{
        val list = mutableListOf<MatchSegment>()

        val con = _db.readableDatabase
        val cursor = con.query("MatchSegments", arrayOf("ID"), "MatchID = ?", arrayOf(matchId.toString()), null, null, null)

        while(cursor.moveToNext()){
            val id = cursor.getInt(0)
            list.add(loadMatchSegment(id, con))
        }

        cursor.close()
        con.close()

        return list
    }

    fun listMatches(): List<Match>{
        val list = mutableListOf<Match>()

        val con = _db.readableDatabase
        val cursor = con.rawQuery("SELECT ID, HomeTeam, AwayTeam, Notes, ms.StartTime, (SELECT COUNT(*) FROM MatchStats ms INNER JOIN MatchSegments s ON s.ID = ms.MatchSegmentId WHERE StatTypeId = 2 AND OutcomeId = 9 AND HomeOrAway = 1 AND s.MatchId = m.ID) HomeGoals, (SELECT COUNT(*) FROM MatchStats ms INNER JOIN MatchSegments s ON s.ID = ms.MatchSegmentId WHERE StatTypeId = 2 AND OutcomeId = 9 AND HomeOrAway = 0 AND s.MatchId = m.ID) AwayGoals FROM Matches m LEFT OUTER JOIN (SELECT MatchID, MIN(StartTime) StartTime FROM MatchSegments ms GROUP BY MatchID) ms ON ms.MatchID = m.ID ORDER BY ms.StartTime DESC", arrayOf())


        while(cursor.moveToNext()){
            list.add(Match(
                cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                cursor.getString(cursor.getColumnIndexOrThrow("HomeTeam")),
                cursor.getString(cursor.getColumnIndexOrThrow("AwayTeam")),
                cursor.getString(cursor.getColumnIndexOrThrow("Notes")),
                cursor.getInt(cursor.getColumnIndexOrThrow("StartTime")),
                cursor.getInt(cursor.getColumnIndexOrThrow("HomeGoals")),
                cursor.getInt(cursor.getColumnIndexOrThrow("AwayGoals")),
                null //for now, we don't care about web IDs in lists
            ))
        }

        cursor.close()
        con.close()

        return list
    }

    fun getAvailableOutcomes(): Map<Int, List<StatOutcome>>{
        val map = mutableMapOf<Int, MutableList<StatOutcome>>()

        val con = _db.readableDatabase
        val cursor = con.query("Outcomes", arrayOf("ID", "TriggeringStatTypeID", "Name", "NextActionID"), null, null, null, null, null, null)

        while(cursor.moveToNext()){
            val trigger = cursor.getInt(cursor.getColumnIndexOrThrow("TriggeringStatTypeID"))

            if(!map.containsKey(trigger)){
                map[trigger] = mutableListOf()
            }

            map[trigger]!!.add(
                StatOutcome(
                    cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Name")),
                    StatType.fromInt(cursor.getInt(cursor.getColumnIndexOrThrow("NextActionID")))
                )
            )
        }

        cursor.close()
        con.close()

        return map
    }

    fun storeUploadId(matchId: Int, uploadId: UUID){
        val con = _db.writableDatabase
        con.execSQL("UPDATE Matches SET WebID = ? WHERE ID = ?", arrayOf(uploadId, matchId))
        con.close()
    }

    private fun loadMatchSegment(id: Int, con: SQLiteDatabase): MatchSegment{
        val cursor = con.rawQuery("SELECT ms.ID, ms.SegmentTypeId, ms.StartTime, s.Name, s.Code, s.MinuteOffset FROM MatchSegments ms INNER JOIN MatchSegmentTypes s ON s.ID = ms.SegmentTypeId WHERE ms.ID = ?", arrayOf(id.toString()))
        cursor.moveToNext()

        val segment = MatchSegment(
            cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
            MatchSegmentType.fromInt(cursor.getInt(cursor.getColumnIndexOrThrow("SegmentTypeId"))),
            cursor.getString(cursor.getColumnIndexOrThrow("Name")),
            cursor.getString(cursor.getColumnIndexOrThrow("Code")),
            countStats(id, con, true),
            countStats(id, con, false),
            cursor.getLong(cursor.getColumnIndexOrThrow("StartTime")),
            cursor.getInt(cursor.getColumnIndexOrThrow("MinuteOffset"))
        )

        cursor.close()
        return segment
    }

    //todo: move this somewhere better
    private fun countStats(segmentId: Int, con: SQLiteDatabase, homeOrAway: Boolean): MutableMap<Int, MutableList<StatOccurrence>>{
        val map = mutableMapOf<Int, MutableList<StatOccurrence>>()
        StatType.entries.forEach {
            map[it.value] = mutableListOf()
        }

        val cursor = con.rawQuery("SELECT s.ID, s.StatTypeId, st.Description, s.Timestamp, s.OutcomeID, o.Name FROM MatchSegments ms INNER JOIN MatchStats s ON s.MatchSegmentId = ms.ID INNER JOIN StatTypes st ON st.ID = s.StatTypeId INNER JOIN Outcomes o ON o.ID = s.OutcomeID WHERE MatchSegmentId = ? AND HomeOrAway = ? ORDER BY ms.ID", arrayOf(segmentId.toString(), if(homeOrAway) "1" else "0"))

        while(cursor.moveToNext()){
            val statType = cursor.getInt(cursor.getColumnIndexOrThrow("StatTypeId"))
            val stat = StatOccurrence(
                cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                homeOrAway,
                statType,
                cursor.getString(cursor.getColumnIndexOrThrow("Description")),
                cursor.getLong(cursor.getColumnIndexOrThrow("Timestamp")),
                cursor.getInt(cursor.getColumnIndexOrThrow("OutcomeID")),
                cursor.getString(cursor.getColumnIndexOrThrow("Name"))
            )

            if(!map.containsKey(statType)){
                map[statType] = mutableListOf()
            }

            map[statType]!!.add(stat)
        }

        cursor.close()
        return map
    }
}