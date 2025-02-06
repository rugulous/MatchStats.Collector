package us.rugulo.matchstats.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE StatTypes (ID INT PRIMARY KEY, Description TEXT NOT NULL)")
        db.execSQL("INSERT INTO StatTypes (Description) VALUES ('Crosses'), ('Shots'), ('Shots on Target'), ('Goals'), ('Corners')")

        db.execSQL("CREATE TABLE MatchSegmentTypes (ID INT PRIMARY KEY, Name TEXT NOT NULL, Code TEXT NOT NULL)")
        db.execSQL("INSERT INTO MatchSegmentTypes (Name, Code) VALUES ('First Half', '1H'), ('Second Half', '2H'), ('Extra Time First Half', '1ET'), ('Extra Time Second Half', '2ET')")

        db.execSQL("CREATE TABLE Matches (ID INT PRIMARY KEY, HomeTeam TEXT NOT NULL, AwayTeam TEXT NOT NULL, Notes TEXT)")
        db.execSQL("INSERT INTO Matches (HomeTeam, AwayTeam, Notes) VALUES ('Glossop', 'Totty United', 'Development Division Cup')")

        db.execSQL("CREATE TABlE MatchSegment (ID INT PRIMARY KEY, MatchId INT NOT NULL, SegmentTypeId INT NOT NULL, StartTime INT NOT NULL, EndTime INT NOT NULL)")
        db.execSQL("CREATE TABLE MatchStat (ID INT PRIMARY KEY, MatchSegmentId INT NOT NULL, HomeOrAway INT NOT NULL, StatTypeId INT NOT NULL, Timestamp INT NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "MatchStats.db"
    }
}