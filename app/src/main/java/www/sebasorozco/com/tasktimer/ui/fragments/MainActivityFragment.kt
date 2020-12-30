package www.sebasorozco.com.tasktimer.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import www.sebasorozco.com.tasktimer.BuildConfig
import www.sebasorozco.com.tasktimer.R
import www.sebasorozco.com.tasktimer.data.database.Task
import www.sebasorozco.com.tasktimer.data.viewmodel.TaskTimerViewModel
import www.sebasorozco.com.tasktimer.databinding.FragmentMainBinding
import www.sebasorozco.com.tasktimer.ui.adapters.CursorRecyclerViewAdapter
import www.sebasorozco.com.tasktimer.ui.dialogs.*


private const val TAG = "MainActivityFragment"
private const val DIALOG_ID_DELETE = 2
private const val DIALOG_TASK_ID = "task_id"


class MainActivityFragment : Fragment(),
    CursorRecyclerViewAdapter.OnTaskClickListener,
    AppDialog.DialogEvents{

    interface OnTaskEdit {
        fun onTaskEdit(task: Task)
    }

    private val viewModel: TaskTimerViewModel by activityViewModels()
    private val mAdapter = CursorRecyclerViewAdapter(null, this)
    private var binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: called")
        super.onAttach(context)

        if (context !is OnTaskEdit) {
            throw RuntimeException("$context must implement OnTaskEdit")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)
        viewModel.cursor.observe(this, { cursor -> mAdapter.swapCursor(cursor)?.close() })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        super.onViewCreated(view, savedInstanceState)

        binding?.taskList?.layoutManager = LinearLayoutManager(context)
        binding?.taskList?.adapter = mAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: called")
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewStateRestored: called")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        Log.d(TAG, "onStart: called")
        super.onStart()
    }

    override fun onEditClick(task: Task) {
        (activity as OnTaskEdit).onTaskEdit(task)
    }

    override fun onDeleteClick(task: Task) {

        val args = Bundle().apply {
            putInt(DIALOG_ID, DIALOG_ID_DELETE)
            putString(DIALOG_MESSAGE,getString(R.string.deldiag_message,task.id,task.name))
            putInt(DIALOG_POSITIVE_RID,R.string.deldiag_positive_caption)
            putLong(DIALOG_TASK_ID,task.id)
        }
        val dialog= AppDialog()
        dialog.arguments= args
        dialog.show(childFragmentManager,null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onTaskLongClick(task: Task) {
        Toast.makeText(context, "please implement the onTaskLongClick", Toast.LENGTH_LONG).show()
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG,"onPositiveDialogResult: called with id $dialogId")

        if(dialogId == DIALOG_ID_DELETE){
            val taskId = args.getLong(DIALOG_TASK_ID)
            if(BuildConfig.DEBUG && taskId == 0L) throw AssertionError("TaskId is Zero")
            viewModel.deleteTask(taskId)
        }
    }
}