package www.sebasorozco.com.tasktimer.ui.adapters

import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import www.sebasorozco.com.tasktimer.R
import www.sebasorozco.com.tasktimer.data.database.Task
import www.sebasorozco.com.tasktimer.data.database.TasksContract
import www.sebasorozco.com.tasktimer.databinding.TaskListItemsBinding

class TaskViewHolder(private val containerView: View) :
    RecyclerView.ViewHolder(containerView){

    lateinit var task: Task

    private val binding = TaskListItemsBinding.bind(containerView)

    fun bind(task: Task, listener: CursorRecyclerViewAdapter.OnTaskClickListener) = with(binding) {

        this@TaskViewHolder.task = task

        tliName.text = task.name
        tliDescription.text = task.description
        tliEdit.visibility = View.VISIBLE
        //tliDelete.visibility = View.VISIBLE

        tliEdit.setOnClickListener {
            listener.onEditClick(task)
        }

        /*
        tliDelete.setOnClickListener {
            listener.onDeleteClick(task)
        }
         */

        containerView.setOnLongClickListener {
            listener.onTaskLongClick(task)
            true
        }
    }
}

private const val TAG = "CursorRecyclerViewAdapt"

class CursorRecyclerViewAdapter(private var cursor: Cursor?, private val listener: OnTaskClickListener) :
    RecyclerView.Adapter<TaskViewHolder>() {

    interface OnTaskClickListener {
        fun onEditClick(task: Task)

        //fun onDeleteClick(task: Task)
        fun onTaskLongClick(task: Task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        Log.d(TAG, "onCreateViewHolder: new view requested")
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.task_list_items, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int)  {
        Log.d(TAG, "onBindViewHolder: starts ")

        val binding = TaskListItemsBinding.bind(holder.itemView)

        val cursor = cursor     // avoid problems with smart cast

        if (cursor == null || cursor.count == 0) {
            Log.d(TAG, "onBindViewHolder: providing instructions")
            binding.tliName.setText(R.string.instruction_heading)
            binding.tliDescription.setText(R.string.description_heading)
            binding.tliEdit.visibility = View.GONE
            //binding.tliDelete.visibility = View.GONE
        } else {
            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException("Couldn't move to position $position")
            }

            // Create a Task object from the data in the cursor

            with(cursor) {
                val task = Task(
                    getString(getColumnIndex(TasksContract.Columns.TASK_NAME)),
                    getString(getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION)),
                    getInt(getColumnIndex(TasksContract.Columns.TASK_SORT_ORDER))
                )

                // Remember that the id isn't set in the constructor
                task.id = getLong(getColumnIndex(TasksContract.Columns.ID))

                holder.bind(task, listener)
            }


        }

    }

    override fun getItemCount(): Int {
        val cursor = cursor
        return if (cursor == null || cursor.count == 0) {
            1       // fib, because we populate a single ViewHolder with instructions
        } else {
            cursor.count
        }
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