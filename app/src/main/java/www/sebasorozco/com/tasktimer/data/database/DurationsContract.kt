package www.sebasorozco.com.tasktimer.data.database

import android.net.Uri

object DurationsContract {

    internal const val TABLE_NAME = "vwTaskDurations"


    /**
     * The URI to access the Durations view.
     */
    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    // Durations fields
    object Columns {

        const val NAME = TasksContract.Columns.TASK_NAME
        const val DESCRIPTION = TasksContract.Columns.TASK_DESCRIPTION
        const val START_TIME = TimingsContract.Columns.TIMING_START_TIME
        const val START_DATE = "startDate"
        const val DURATION = TimingsContract.Columns.TIMING_DURATION

    }
}