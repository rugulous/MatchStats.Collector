package us.rugulo.matchstats.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE StatTypes (ID INTEGER PRIMARY KEY AUTOINCREMENT, Description TEXT NOT NULL, IsActive INTEGER NOT NULL DEFAULT 1)")
        db.execSQL("INSERT INTO StatTypes (Description) VALUES ('Cross'), ('Shot'), ('Corner')")

        db.execSQL("CREATE TABLE MatchSegmentTypes (ID INTEGER PRIMARY KEY AUTOINCREMENT, Name TEXT NOT NULL, Code TEXT NOT NULL, MinuteOffset INTEGER NOT NULL)")
        db.execSQL("INSERT INTO MatchSegmentTypes (Name, Code, MinuteOffset) VALUES ('First Half', '1H', 0), ('Second Half', '2H', 45), ('Extra Time First Half', '1ET', 90), ('Extra Time Second Half', '2ET', 105)")

        db.execSQL("CREATE TABLE Outcomes (ID INTEGER PRIMARY KEY AUTOINCREMENT, TriggeringStatTypeID INT NOT NULL, Name TEXT NOT NULL, NextActionID INT, IsActive INTEGER NOT NULL DEFAULT 1)")
        db.execSQL("INSERT INTO Outcomes (TriggeringStatTypeID, Name, NextActionID) VALUES (1, 'Shot', 2), (1, 'Controlled', NULL), (1, 'Cleared', NULL), (1, 'Corner', 3), (1, 'Out of Play', NULL), (1, 'Other Wing', NULL), (2, 'Blocked', NULL), (2, 'Saved', NULL), (2, 'Goal', NULL), (2, 'Off Target', NULL), (3, 'Short', NULL), (3, 'Cross', 1)")

        db.execSQL("CREATE TABLE Matches (ID INTEGER PRIMARY KEY AUTOINCREMENT, HomeTeam TEXT NOT NULL, AwayTeam TEXT NOT NULL, Notes TEXT, WebID TEXT)")

        db.execSQL("CREATE TABlE MatchSegments (ID INTEGER PRIMARY KEY AUTOINCREMENT, MatchId INTEGER NOT NULL, SegmentTypeId INTEGER NOT NULL, StartTime INTEGER NOT NULL, EndTime INTEGER)")

        db.execSQL("CREATE TABLE MatchStats (ID INTEGER PRIMARY KEY AUTOINCREMENT, MatchSegmentId INTEGER NOT NULL, HomeOrAway INTEGER NOT NULL, StatTypeId INTEGER NOT NULL, Timestamp INTEGER NOT NULL, OutcomeID INTEGER NOT NULL, PriorStatID INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if(db == null){
            return
        }

        if(oldVersion < 2 && newVersion >= 2) {
            db.execSQL("ALTER TABLE Matches ADD COLUMN WebID TEXT")
        }

        if(oldVersion < 3 && newVersion >= 3){
            db.execSQL("ALTER TABLE StatTypes ADD COLUMN IsActive INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE Outcomes ADD COLUMN IsActive INTEGER NOT NULL DEFAULT 1")
        }
    }

    companion object {
        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "MatchStats.db"
    }
}