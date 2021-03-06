package www.sebasorozco.com.tasktimer.data.database

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

object TimingsContract {

    internal const val TABLE_NAME = "Timings"


    /**
     * The URI to access the Timings table.
     */
    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE= "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    //Timings fields
    object Columns {

        const val ID = BaseColumns._ID
        const val TIMING_TASK_ID = "task_id"
        const val TIMING_START_TIME = "start_time"
        const val TIMING_DURATION = "duration"
    }

    fun getId(uri: Uri): Long{
        return ContentUris.parseId(uri)
    }

    fun buildUriFromId(id: Long):Uri{
        return ContentUris.withAppendedId(CONTENT_URI,id)

    }

}