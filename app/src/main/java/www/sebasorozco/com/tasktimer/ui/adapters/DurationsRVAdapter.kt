package www.sebasorozco.com.tasktimer.ui.adapters

import android.content.Context
import android.database.Cursor
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import www.sebasorozco.com.tasktimer.R
import www.sebasorozco.com.tasktimer.data.database.DurationsContract
import www.sebasorozco.com.tasktimer.databinding.TaskDurationItemsBinding
import java.util.*

class ViewHolder(override val containerView: View) :
    RecyclerView.ViewHolder(containerView),
    LayoutContainer

private const val TAG = "DurationsRVAdapter"

class DurationsRVAdapter(context: Context, private var cursor: Cursor?) :
    RecyclerView.Adapter<ViewHolder>() {

    private val dateFormat = DateFormat.getDateFormat(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_duration_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: starts ")

        val binding = TaskDurationItemsBinding.bind(holder.itemView)

        val cursor = cursor     // avoid problems with smart cast

        if (cursor != null && cursor.count != 0) {
            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException("Couldn't move cursor to position $position")
            }

            val name = cursor.getString(cursor.getColumnIndex(DurationsContract.Columns.NAME))
            val description =
                cursor.getString(cursor.getColumnIndex(DurationsContract.Columns.DESCRIPTION))
            val startTime =
                cursor.getLong(cursor.getColumnIndex(DurationsContract.Columns.START_TIME))
            val totalDuration =
                cursor.getLong(cursor.getColumnIndex(DurationsContract.Columns.DURATION))

            val userDate =
                dateFormat.format(startTime * 1000) // The database stores seconds, we need milliseconds

            val totalTime = formatDuration(totalDuration)

            binding.tdName.text = name
            binding.tdDescription?.text = description   // Description is not present in portrait
            binding.tdStart.text = userDate
            binding.tdDuration.text = totalTime

        }
    }

    override fun getItemCount(): Int {

        Log.d(TAG, "${cursor?.count} total of items")
        return cursor?.count ?: 0
    }

    private fun formatDuration(duration: Long): String {
        // duration is in seconds, convert to hours:minutes:seconds
        // allowing for >24 hours - so we can't use a time data type);
        val hours = duration / 3600
        val remainder = duration - hours * 3600
        val minutes = remainder / 60
        //      val seconds = remainder - minutes * 60
        val seconds = remainder % 60

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.
     * The returned old Cursor is *not* closed.
     *
     * @param newCursor The new Cursor to be used
     * @return Returns the previously Set Cursor, or null if there wasn't
     *  one.
     *  If the given new Cursor is the same instance as the previously set
     *  Cursor, null is also returned.
     */
    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursor) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursor
        cursor = newCursor
        if (newCursor != null) {
            // notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }
}