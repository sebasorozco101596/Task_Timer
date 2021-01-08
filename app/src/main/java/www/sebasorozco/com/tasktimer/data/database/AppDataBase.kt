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
private const val DATABASE_VERSION = 2

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

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade: starts")
        when (oldVersion) {
            1 -> {
                //upgrade logic from version 1
                addTimingsTable(db)
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

    companion object : SingletonHolder<AppDataBase, Context>(::AppDataBase)
}