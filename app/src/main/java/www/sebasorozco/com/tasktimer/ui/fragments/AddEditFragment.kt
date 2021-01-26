package www.sebasorozco.com.tasktimer.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import www.sebasorozco.com.tasktimer.data.database.Task
import www.sebasorozco.com.tasktimer.data.viewmodel.TaskTimerViewModel
import www.sebasorozco.com.tasktimer.databinding.FragmentAddEditBinding
import www.sebasorozco.com.tasktimer.ui.dialogs.AppDialog

private const val TAG = "AddEditFragment"

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_TASK = "task"

/**
 * A simple [Fragment] subclass.
 * Use the [AddEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditFragment : Fragment(),
    AppDialog.DialogEvents{
    private var task: Task? = null
    private var listener: OnSaveClicked? = null
    private val viewModel: TaskTimerViewModel by activityViewModels()

    private var binding: FragmentAddEditBinding? = null

    /*
    Called when a fragment is first attached to its context
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach: starts")
        if (context is OnSaveClicked) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSaveClicked")
        }
    }

    /*
    Called to do initial creation of a fragment
    Does not involve UI elements
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: starts")
        task = arguments?.getParcelable(ARG_TASK)
    }

    /*
    Called to have the fragment instantiate its user interface view
    If you return a View from here, you will later be called in onDestroyView when the view
    is being released
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: starts")
        // Inflate the layout for this fragment
        binding = FragmentAddEditBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    /*
    This gives subclasses a chance to initialize themselves once they know their view hierarchy
    has been completely created
     */
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: starts")
        if (savedInstanceState == null) {
            val task = task
            if (task != null) {
                Log.d(TAG, "onViewCreated: Task details found, editing task ${task.id}")
                binding?.addeditName?.setText(task.name)
                binding?.addeditDescription?.setText(task.description)
                binding?.addeditSortorder?.setText(task.sortOrder.toString())
            } else {
                // No task, so we must be adding a new task, and NOT editing an existing one
                Log.d(TAG, "onViewCreated: No arguments, adding new record")
            }
        }
    }

    private fun taskFromUI(): Task {
        val sortOrder = if (binding?.addeditSortorder?.text?.isNotEmpty() == true) {
            Integer.parseInt(binding?.addeditSortorder?.text.toString())
        } else {
            0
        }

        val newTask =
            Task(
                binding?.addeditName?.text.toString(),
                binding?.addeditDescription?.text.toString(),
                sortOrder
            )
        newTask.id = task?.id ?: 0

        return newTask
    }

    fun isDirty() : Boolean{
        val newTask = taskFromUI()
        return ((newTask != task) &&
                (newTask.name.isNotBlank() ||
                        newTask.description!!.isNotBlank() ||
                        newTask.sortOrder != 0)
                )
    }

    private fun saveTask() {
        // Create a newTask object with the details to be saved, then
        // call the viewModel's saveTask function
        // Task is now a data class, so we can compare the new details with the original task
        // and only save if they are different.

        val newTask = taskFromUI()
        if (newTask != task) {
            Log.d(TAG, "saveTask: id is ${newTask.id}")
            task = viewModel.saveTask(newTask)
            Log.d(TAG, "saveTask: id is ${task?.id}")
        }
    }

    /*
    Called when the fragment's activity has been created and this fragment's view hierarchy
    instantiated
    It can be used to do final initialization once these pieces are in place, such as retrieving views
    or restoring state
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: starts")
        super.onActivityCreated(savedInstanceState)

        val listener = listener
        if (activity is AppCompatActivity) {
            val actionBar = (activity as AppCompatActivity?)?.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding?.addeditSave?.setOnClickListener {
            saveTask()
            listener?.onSaveClicked()
        }
    }

    /*
    Called when all saved state has been restored into the view hierarchy of the fragment
    This can be used to do initialization bases on saved state that you are letting the view
    hierarchy track itself, such as whether checkbox widgets are currently checked
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewStateRestored: starts")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        Log.d(TAG, "onStart: called")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume: called")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause: called")
        super.onPause()
    }

    /*
    Called to ask the fragment to save its current state, so it can be later reconstructed in a new
    instance if its process is restarted
    if a new instance is called is necessary to use the onViewStateRestore
     */
    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState: starts")
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        Log.d(TAG, "onStop: called")
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
    }

    /*
    Called when the fragment is no longer attached to its activity
     */
    override fun onDetach() {
        Log.d(TAG, "onDetach: starts")
        super.onDetach()
        listener = null
    }

    /*
     * That interface is gonna used for obtain information of the Activity where i'll use this fragment
     */
    interface OnSaveClicked {
        fun onSaveClicked()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task The task to be edited, or null to add a new task.
         * @return A new instance of fragment AddEditFragment.
         */
        @JvmStatic
        fun newInstance(task: Task?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Toast.makeText(requireContext(),"Is a test",Toast.LENGTH_LONG).show()
    }
}

/**
 * The next functions are how could to create a instance of a fragment
 */
/*
fun createFrag(task: Task) {
    val args = Bundle()
    args.putParcelable(ARG_TASK, task)
    val fragment = AddEditFragment()
    fragment.arguments = args
}

fun createFrag2(task: Task) {
    val fragment = AddEditFragment().apply {
        arguments = Bundle().apply {
            putParcelable(ARG_TASK, task)
        }
    }
}


fun simpler(task: Task){
    val fragment= AddEditFragment.newInstance(task)
}
 */