package www.sebasorozco.com.tasktimer.data.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.SharedPreferences
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import learnprogramming.academy.tasktimer.ParametersContract
import www.sebasorozco.com.tasktimer.BuildConfig
import www.sebasorozco.com.tasktimer.data.database.*
import www.sebasorozco.com.tasktimer.ui.dialogs.SETTINGS_DEFAULT_IGNORE_LESS_THAN
import www.sebasorozco.com.tasktimer.ui.dialogs.SETTINGS_IGNORE_LESS_THAN

private const val TAG = "TaskTimerViewModel"

class TaskTimerViewModel(application: Application) : AndroidViewModel(application) {

    //On the next code lines i gonna create the listener for the changes on the Adapter
    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "contentObserver.onChange: called. uri is $uri")
            loadTask()
        }
    }

    private val settings = PreferenceManager.getDefaultSharedPreferences(application)
    private var ignoreLessThan =
        settings.getInt(SETTINGS_IGNORE_LESS_THAN, SETTINGS_DEFAULT_IGNORE_LESS_THAN)

    private val settingsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                SETTINGS_IGNORE_LESS_THAN -> {
                    ignoreLessThan =
                        sharedPreferences.getInt(key, SETTINGS_DEFAULT_IGNORE_LESS_THAN)
                    Log.d(
                        TAG,
                        "settingsListener: now ignoring timings less than $ignoreLessThan seconds"
                    )

                    // Update the database parameters table
                    val values = ContentValues()
                    values.put(ParametersContract.Columns.VALUE, ignoreLessThan)
                    viewModelScope.launch(Dispatchers.IO) {
                        val uri = getApplication<Application>().contentResolver
                            .update(
                                ParametersContract.buildUriFromId(ParametersContract.ID_SHORT_TIMING),
                                values,
                                null,
                                null
                            )
                    }

                }
            }
        }

    private var currentTiming: Timing? = null
    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor>
        get() = databaseCursor

    private val taskTiming = MutableLiveData<String>()
    val timing: LiveData<String>
        get() = taskTiming

    // Variable for check if the task is being edited
    var editedTaskId = 0L
        private set

    init {
        Log.d(TAG, "TaskTimerViewModel: created")
        // On the next line i register the observer with the URI and the Observer
        getApplication<Application>().contentResolver.registerContentObserver(
            TasksContract.CONTENT_URI,
            true,
            contentObserver
        )

        Log.d(TAG, "Ignoring timings less than $ignoreLessThan seconds")
        settings.registerOnSharedPreferenceChangeListener(settingsListener)

        currentTiming = retrieveTiming()
        loadTask()
    }


    private fun loadTask() {

        val projection = arrayOf(
            TasksContract.Columns.ID,
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_DESCRIPTION,
            TasksContract.Columns.TASK_SORT_ORDER
        )

        // <Order by> Tasks.sort_order, Tasks.name COLLATE NOCASE

        val sortOrder =
            "${TasksContract.Columns.TASK_SORT_ORDER}, ${TasksContract.Columns.TASK_NAME} COLLATE NOCASE"

        //The GlobalScore.launch is the way to create a thread in a coroutine
        viewModelScope.launch(Dispatchers.IO) {
            val cursor = getApplication<Application>().contentResolver.query(
                TasksContract.CONTENT_URI,
                projection, null, null,
                sortOrder
            )

            databaseCursor.postValue(cursor)
        }
    }

    /*
    Function for know if the task is being edited
     */
    fun startEditing(taskId: Long) {
        if (BuildConfig.DEBUG && editedTaskId != 0L) throw IllegalStateException(
            "startEditing called without stopping previous edit. editedTaskId: $editedTaskId, taskId: $taskId"
        )

        editedTaskId = taskId
    }

    /*
    Function for know where the task is finished of editing
     */
    fun stopEditing() {
        editedTaskId = 0L
    }

    fun deleteTask(taskId: Long) {
        Log.d(TAG, "Deleting task")

        //The GlobalScore.launch is the way to create a thread in a coroutine
        viewModelScope.launch(Dispatchers.IO) {
            getApplication<Application>().contentResolver.delete(
                TasksContract.buildUriFromId(taskId),
                null,
                null
            )
        }
        // Fix display when the currently timed task is deleted
        if (currentTiming?.taskId == taskId) {
            currentTiming = null
            taskTiming.value = null
        }
    }

    fun timeTask(task: Task) {
        Log.d(TAG, "timeTask: called")
        // Use local variable, to allow smart casts
        val timingRecord = currentTiming

        if (timingRecord == null) {
            // No task being, start timing the new task
            currentTiming = Timing(task.id)
            saveTiming(currentTiming!!)
        } else {
            // We have a task being timed, so save it
            timingRecord.setDuration()
            saveTiming(timingRecord)
            currentTiming = if (task.id == timingRecord.taskId) {
                // The current task was tapped a second time, stop timing
                null
            } else {
                // A new task is being timed
                val newTiming = Timing(task.id)
                saveTiming(newTiming)
                newTiming
            }
        }

        // Update the liveData
        taskTiming.value = if (currentTiming != null) task.name else null

    }

    private fun saveTiming(currentTiming: Timing) {
        Log.d(TAG, "saveTiming: called")

        // Are we updating, or inserting a new row?
        val inserting = (currentTiming.duration == 0L)

        val values = ContentValues().apply {
            if (inserting) {
                put(TimingsContract.Columns.TIMING_TASK_ID, currentTiming.taskId)
                put(TimingsContract.Columns.TIMING_START_TIME, currentTiming.startTime)
            }
            put(TimingsContract.Columns.TIMING_DURATION, currentTiming.duration)
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (inserting) {
                val uri = getApplication<Application>().contentResolver.insert(
                    TimingsContract.CONTENT_URI,
                    values
                )
                if (uri != null) {
                    currentTiming.id = TimingsContract.getId(uri)
                }
            } else {
                Log.d(TAG, "saveTiming: saving timing with duration ${currentTiming.duration}")
                getApplication<Application>().contentResolver.update(
                    TimingsContract.buildUriFromId(
                        currentTiming.id
                    ), values, null, null
                )
            }
        }
    }

    fun saveTask(task: Task): Task {
        val values = ContentValues()

        if (task.name.isNotEmpty()) {
            // Don't save a task without name

            values.put(TasksContract.Columns.TASK_NAME, task.name)
            values.put(TasksContract.Columns.TASK_DESCRIPTION, task.description)
            values.put(TasksContract.Columns.TASK_SORT_ORDER, task.sortOrder)

            if (task.id == 0L) {
                viewModelScope.launch(Dispatchers.IO) {
                    Log.d(TAG, "saveTask: adding new Task")
                    val uri = getApplication<Application>().contentResolver.insert(
                        TasksContract.CONTENT_URI,
                        values
                    )
                    if (uri != null) {
                        task.id = TasksContract.getId(uri)
                        Log.d(TAG, "saveTask:  new id is ${task.id}")
                    }
                }
            } else {
                // Task has an id, so we're updating
                viewModelScope.launch(Dispatchers.IO) {
                    Log.d(TAG, "saveTask: updating Task")
                    getApplication<Application>().contentResolver.update(
                        TasksContract.buildUriFromId(task.id),
                        values,
                        null,
                        null
                    )

                }
            }
        }
        return task
    }

    private fun retrieveTiming(): Timing? {
        Log.d(TAG, "retrieveTiming starts")
        val timing: Timing?

        val timingCursor: Cursor? = getApplication<Application>().contentResolver.query(
            CurrentTimingContract.CONTENT_URI,
            null, // Passing null for the projection returns all columns.
            null,
            null,
            null
        )

        if (timingCursor != null && timingCursor.moveToFirst()) {
            // We have an un-timed record
            val id =
                timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TIMING_ID))
            val taskId =
                timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TASK_ID))
            val startTime =
                timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.START_TIME))
            val name =
                timingCursor.getString(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TASK_NAME))
            timing = Timing(taskId, startTime, id)

            // Update the liveData
            taskTiming.value = name
        } else {
            // No timing record found with zero duration
            timing = null
        }
        timingCursor?.close()
        Log.d(TAG, "retrieveTiming returning")

        return timing
    }

    /**
     * On the follow function is necessary to drop the connection with the observer for don't trash
     * all the memory of the phone
     */
    override fun onCleared() {
        Log.d(TAG, "onCleared: called")
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)

        settings.unregisterOnSharedPreferenceChangeListener(settingsListener)

        databaseCursor.value?.close()
    }
}