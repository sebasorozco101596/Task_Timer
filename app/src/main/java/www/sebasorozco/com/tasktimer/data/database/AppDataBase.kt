package www.sebasorozco.com.tasktimer.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Basic database class for the application
 *
 * The only class that should use this is [AppProvider].
 */
private const val TAG = "AppDataBase"
private const val DATABASE_NAME = "TaskTimer.db"
private const val DATABASE_VERSION = 4

//Making the constructor private is the first step to create a Singleton class
internal class AppDataBase private constructor(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null,
    DATABASE_VERSION
) {

    //Is not recommendable to use for make a professional code
    init {
        Log.d(TAG, "AppDataBase: initializing ")
    }

    override fun onCreate(db: SQLiteDatabase) {
        //CREATE TABLE Tasks (_id INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, description TEXT, sort_order INTEGER);
        Log.d(TAG, "onCreate: starts")
        val sSQL = """CREATE TABLE ${TasksContract.TABLE_NAME} (
            ${TasksContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
            ${TasksContract.Columns.TASK_DESCRIPTION} TEXT,
            ${TasksContract.Columns.TASK_SORT_ORDER} INTEGER);""".replaceIndent(" ")
        Log.d(TAG, sSQL)
        db.execSQL(sSQL)

        addTimingsTable(db)
        addCurrentTimingView(db)
        addDurationsView(db)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade: starts")
        when (oldVersion) {
            1 -> {
                //upgrade logic from version 1
                addTimingsTable(db)
                addCurrentTimingView(db)
                addDurationsView(db)
            }
            2 -> {
                addCurrentTimingView(db)
                addDurationsView(db)
            }
            3 -> {
                addDurationsView(db)
            }
            else -> throw IllegalStateException("onUpgrade() with unknown newVersion: $newVersion")
        }
    }

    private fun addTimingsTable(db: SQLiteDatabase) {
        val sSQLTiming = """CREATE TABLE ${TimingsContract.TABLE_NAME} (
            ${TimingsContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL, 
            ${TimingsContract.Columns.TIMING_TASK_ID} INTEGER NOT NULL,
            ${TimingsContract.Columns.TIMING_START_TIME} INTEGER,
            ${TimingsContract.Columns.TIMING_DURATION} INTEGER);
        """.replaceIndent(" ")
        Log.d(TAG, sSQLTiming)
        db.execSQL(sSQLTiming)

        val sSQLTrigger = """CREATE TRIGGER Remove_Task
            AFTER DELETE ON ${TasksContract.TABLE_NAME}
            FOR EACH ROW
            BEGIN
            DELETE FROM ${TimingsContract.TABLE_NAME}
            WHERE ${TimingsContract.Columns.TIMING_TASK_ID} = OLD.${TasksContract.Columns.ID};
            END;
        """.replaceIndent(" ")
        Log.d(TAG, sSQLTrigger)
        db.execSQL(sSQLTrigger)
    }

    private fun addCurrentTimingView(db: SQLiteDatabase) {
        /*
        CREATE VIEW vwCurrentTiming
             AS SELECT Timings._id,
                 Timings.TaskId,
                 Timings.StartTime,
                 Tasks.Name
             FROM Timings
             JOIN Tasks
             ON Timings.TaskId = Tasks._id
             WHERE Timings.Duration = 0
             ORDER BY Timings.StartTime DESC;
         */
        val sSQLTimingView = """CREATE VIEW ${CurrentTimingContract.TABLE_NAME}
        AS SELECT ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME},
            ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_NAME}
        FROM ${TimingsContract.TABLE_NAME}
        JOIN ${TasksContract.TABLE_NAME}
        ON ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID} = ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID}
        WHERE ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_DURATION} = 0
        ORDER BY ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME} DESC;
    """.replaceIndent(" ")
        Log.d(TAG, sSQLTimingView)
        db.execSQL(sSQLTimingView)
    }

    private fun addDurationsView(db: SQLiteDatabase) {
        /*
        CREATE VIEW vwTaskDuration AS
        SELECT Tasks.name,
        Tasks.description,
        Timings.start_time,
        DATE(Timings.start_time, 'unixepoch', 'localtime') AS startDate,
        SUM(Timings.duration) AS duration
        FROM Tasks INNER JOIN Timings
        ON Tasks._id = Timings.task_id
        GROUP BY Tasks._id, startDate
         */

        val sSQL = """CREATE VIEW ${DurationsContract.TABLE_NAME}
        AS SELECT ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_NAME},
        ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_DESCRIPTION},
        ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME},
        DATE(${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME},'unixepoch', 'localtime') 
        AS ${DurationsContract.Columns.START_DATE},
        SUM(${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_DURATION})
        AS ${DurationsContract.Columns.DURATION}
        FROM ${TasksContract.TABLE_NAME} INNER JOIN ${TimingsContract.TABLE_NAME}
        ON ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID} = 
        ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID}
        GROUP BY ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID}, ${DurationsContract.Columns.START_DATE};
    """.replaceIndent(" ")

        Log.d(TAG, sSQL)
        db.execSQL(sSQL)
    }

    companion object : SingletonHolder<AppDataBase, Context>(::AppDataBase)
}