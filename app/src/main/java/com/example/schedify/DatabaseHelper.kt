package com.example.schedify

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Schedify.db"
        private const val DATABASE_VERSION = 1

        // Configuration Table
        private const val TABLE_CONFIG = "config"
        private const val COL_ID = "id"
        private const val COL_SPARE_TIME = "spare_time"
        private const val COL_MAX_SUBJECTS = "max_subjects"

        // Subjects Table
        private const val TABLE_SUBJECTS = "subjects"
        private const val COL_SUB_NAME = "name"
        private const val COL_HOURS = "hours"
        private const val COL_DIFFICULTY = "difficulty"
        private const val COL_PRIORITY = "priority"

        // Schedule Table
        private const val TABLE_SCHEDULE = "schedule"
        private const val COL_DAY = "day"
        private const val COL_COMPLETED = "completed"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createConfig = "CREATE TABLE $TABLE_CONFIG ($COL_ID INTEGER PRIMARY KEY, $COL_SPARE_TIME REAL, $COL_MAX_SUBJECTS INTEGER)"
        val createSubjects = "CREATE TABLE $TABLE_SUBJECTS ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_SUB_NAME TEXT, $COL_HOURS REAL, $COL_DIFFICULTY INTEGER, $COL_PRIORITY TEXT)"
        val createSchedule = "CREATE TABLE $TABLE_SCHEDULE ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_SUB_NAME TEXT, $COL_DAY TEXT, $COL_COMPLETED INTEGER)"

        db?.execSQL(createConfig)
        db?.execSQL(createSubjects)
        db?.execSQL(createSchedule)

        // Default Config
        val values = ContentValues().apply {
            put(COL_ID, 1)
            put(COL_SPARE_TIME, 4.0)
            put(COL_MAX_SUBJECTS, 3)
        }
        db?.insert(TABLE_CONFIG, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CONFIG")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUBJECTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SCHEDULE")
        onCreate(db)
    }

    // Config Methods
    fun saveConfig(spareTime: Double, maxSubs: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SPARE_TIME, spareTime)
            put(COL_MAX_SUBJECTS, maxSubs)
        }
        db.update(TABLE_CONFIG, values, "$COL_ID = 1", null)
    }

    fun getConfig(): Pair<Double, Int> {
        val db = readableDatabase
        val cursor = db.query(TABLE_CONFIG, null, "$COL_ID = 1", null, null, null, null)
        return if (cursor.moveToFirst()) {
            val time = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SPARE_TIME))
            val max = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MAX_SUBJECTS))
            cursor.close()
            Pair(time, max)
        } else {
            Pair(4.0, 3)
        }
    }

    // Subject Methods
    fun addSubject(subject: Subject) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SUB_NAME, subject.name)
            put(COL_HOURS, subject.weeklyHours)
            put(COL_DIFFICULTY, subject.difficulty)
            put(COL_PRIORITY, subject.priority)
        }
        db.insert(TABLE_SUBJECTS, null, values)
    }

    fun getSubjects(): List<Subject> {
        val list = mutableListOf<Subject>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SUBJECTS", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Subject(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SUB_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_HOURS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_DIFFICULTY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PRIORITY))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun clearSubjects() {
        writableDatabase.delete(TABLE_SUBJECTS, null, null)
        writableDatabase.delete(TABLE_SCHEDULE, null, null)
    }

    // Schedule Methods
    fun saveSchedule(schedule: List<StudySession>) {
        val db = writableDatabase
        db.delete(TABLE_SCHEDULE, null, null)
        for (session in schedule) {
            val values = ContentValues().apply {
                put(COL_SUB_NAME, session.subjectName)
                put(COL_DAY, session.day)
                put(COL_COMPLETED, if (session.isCompleted) 1 else 0)
            }
            db.insert(TABLE_SCHEDULE, null, values)
        }
    }

    fun getSchedule(): List<StudySession> {
        val list = mutableListOf<StudySession>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SCHEDULE", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(StudySession(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SUB_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DAY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLETED)) == 1
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateSessionStatus(subjectName: String, day: String, isCompleted: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_COMPLETED, if (isCompleted) 1 else 0)
        }
        db.update(TABLE_SCHEDULE, values, "$COL_SUB_NAME = ? AND $COL_DAY = ?", arrayOf(subjectName, day))
    }
}
