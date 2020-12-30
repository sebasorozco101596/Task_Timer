package www.sebasorozco.com.tasktimer.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.IllegalStateException

/**
 * Basic database class for the application
 *
 * The only class that should use this is [AppProvider].
 */
private const val TAG= "AppDataBase"
private const val DATABASE_NAME="TaskTimer.db"
private const val DATABASE_VERSION=1
//Making the constructor private is the first step to create a Singleton class
internal class AppDataBase private constructor(context: Context): SQLiteOpenHelper(context, DATABASE_NAME,null,
    DATABASE_VERSION) {

    //Is not recommendable to use for make a professional code
    init {
        Log.d(TAG,"AppDataBase: initializing ")
    }

    override fun onCreate(db: SQLiteDatabase) {
        //CREATE TABLE Tasks (_id INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, description TEXT, sort_order INTEGER);
        Log.d(TAG,"onCreate: starts")
        val sSQL= """CREATE TABLE ${TasksContract.TABLE_NAME} (
            ${TasksContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
            ${TasksContract.Columns.TASK_DESCRIPTION} TEXT,
            ${TasksContract.Columns.TASK_SORT_ORDER} INTEGER);""".replaceIndent(" ")
        Log.d(TAG,sSQL)
        db.execSQL(sSQL)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG,"onUpgrade: starts")
        when(oldVersion){
            1->{
                //upgrade logic from version 1

            }else -> throw IllegalStateException("onUpgrade() with unknown newVersion: $newVersion")
        }
    }

    companion object:SingletonHolder<AppDataBase,Context>(::AppDataBase)
}