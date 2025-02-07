package us.rugulo.matchstats.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import us.rugulo.matchstats.data.Database
import us.rugulo.matchstats.data.MatchSegmentType
import us.rugulo.matchstats.models.MatchSegment

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

    fun recordStat(segmentId: Int, homeOrAway: Boolean, statTypeId: Int){
        val content = ContentValues()
        content.put("MatchSegmentId", segmentId)
        content.put("HomeOrAway", homeOrAway)
        content.put("StatTypeId", statTypeId)
        content.put("Timestamp", System.currentTimeMillis())

        val con = _db.writableDatabase
        con.insert("MatchStats", null, content)
        con.close()
    }

    fun removeStat(segmentId: Int, homeOrAway: Boolean, statTypeId: Int): Int{
        val con = _db.writableDatabase

        val statement = con.compileStatement("DELETE FROM MatchStats WHERE ID = (SELECT ID FROM MatchStats WHERE MatchSegmentId = ? AND HomeOrAway = ? AND StatTypeId = ? ORDER BY Timestamp DESC LIMIT 1)")
        statement.bindLong(1, segmentId.toLong())
        statement.bindLong(2, if(homeOrAway) 1L else 0L)
        statement.bindLong(3, statTypeId.toLong())

        val affected = statement.executeUpdateDelete()

        con.close()

        return affected
    }

    fun checkForIncompleteMatch(): Int?{
        var matchId: Int? = null

        val con = _db.readableDatabase
        val cursor = con.rawQuery("SELECT m.ID FROM Matches m INNER JOIN MatchSegments ms ON ms.MatchId = m.ID WHERE ms.EndTime IS NULL", null)

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

    private fun loadMatchSegment(id: Int, con: SQLiteDatabase): MatchSegment{
        val cursor = con.rawQuery("SELECT ms.ID, ms.SegmentTypeId, ms.StartTime, s.Name, s.Code FROM MatchSegments ms INNER JOIN MatchSegmentTypes s ON s.ID = ms.SegmentTypeId WHERE ms.ID = ?", arrayOf(id.toString()))
        cursor.moveToNext()

        val segment = MatchSegment(
            cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
            MatchSegmentType.fromInt(cursor.getInt(cursor.getColumnIndexOrThrow("SegmentTypeId"))),
            cursor.getString(cursor.getColumnIndexOrThrow("Name")),
            cursor.getString(cursor.getColumnIndexOrThrow("Code")),
            listStats(id, con, true),
            listStats(id, con, false),
            cursor.getLong(cursor.getColumnIndexOrThrow("StartTime"))
        )

        cursor.close()
        return segment
    }

    //todo: move this somewhere better
    private fun listStats(segmentId: Int, con: SQLiteDatabase, homeOrAway: Boolean): MutableMap<Int, Int>{
        val map = mutableMapOf<Int, Int>()

        val cursor = con.rawQuery("SELECT st.ID, SUM(CASE WHEN ms.ID IS NOT NULL THEN 1 ELSE 0 END) Total FROM StatTypes st LEFT OUTER JOIN MatchStats ms ON ms.StatTypeId = st.ID AND ms.MatchSegmentId = ? AND HomeOrAway = ? GROUP BY st.ID", arrayOf(segmentId.toString(), homeOrAway.toString()))

        while(cursor.moveToNext()){
            map[cursor.getInt(cursor.getColumnIndexOrThrow("ID"))] = cursor.getInt(cursor.getColumnIndexOrThrow("Total"))
        }

        cursor.close()
        return map
    }
}