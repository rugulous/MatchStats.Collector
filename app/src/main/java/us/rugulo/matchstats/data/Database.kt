package us.rugulo.matchstats.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE StatTypes (ID INTEGER PRIMARY KEY AUTOINCREMENT, Description TEXT NOT NULL)")
        db.execSQL("INSERT INTO StatTypes (Description) VALUES ('Crosses'), ('Shots'), ('Shots on Target'), ('Goals'), ('Corners')")

        db.execSQL("CREATE TABLE MatchSegmentTypes (ID INTEGER PRIMARY KEY AUTOINCREMENT, Name TEXT NOT NULL, Code TEXT NOT NULL)")
        db.execSQL("INSERT INTO MatchSegmentTypes (Name, Code) VALUES ('First Half', '1H'), ('Second Half', '2H'), ('Extra Time First Half', '1ET'), ('Extra Time Second Half', '2ET')")

        db.execSQL("CREATE TABLE Matches (ID INTEGER PRIMARY KEY AUTOINCREMENT, HomeTeam TEXT NOT NULL, AwayTeam TEXT NOT NULL, Notes TEXT)")
        db.execSQL("INSERT INTO Matches (HomeTeam, AwayTeam, Notes) VALUES ('Glossop', 'Totty United', 'Development Division Cup')")

        db.execSQL("CREATE TABlE MatchSegment (ID INTEGER PRIMARY KEY AUTOINCREMENT, MatchId INTEGER NOT NULL, SegmentTypeId INTEGER NOT NULL, StartTime INTEGER NOT NULL, EndTime INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE MatchStat (ID INTEGER PRIMARY KEY AUTOINCREMENT, MatchSegmentId INTEGER NOT NULL, HomeOrAway INTEGER NOT NULL, StatTypeId INTEGER NOT NULL, Timestamp INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "MatchStats.db"
    }
}