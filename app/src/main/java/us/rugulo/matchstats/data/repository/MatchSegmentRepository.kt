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

    private fun loadMatchSegment(id: Int, con: SQLiteDatabase): MatchSegment{
        val cursor = con.rawQuery("SELECT ms.ID, ms.SegmentTypeId, ms.StartTime, s.Name, s.Code FROM MatchSegments ms INNER JOIN MatchSegmentTypes s ON s.ID = ms.SegmentTypeId WHERE ms.ID = ?", arrayOf(id.toString()))
        cursor.moveToNext()

        val segment = MatchSegment(
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