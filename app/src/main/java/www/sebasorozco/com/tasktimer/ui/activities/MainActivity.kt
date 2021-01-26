package www.sebasorozco.com.tasktimer.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import www.sebasorozco.com.tasktime.debug.TestData
import www.sebasorozco.com.tasktimer.BuildConfig
import www.sebasorozco.com.tasktimer.R
import www.sebasorozco.com.tasktimer.data.database.Task
import www.sebasorozco.com.tasktimer.data.viewmodel.TaskTimerViewModel
import www.sebasorozco.com.tasktimer.databinding.AboutBinding
import www.sebasorozco.com.tasktimer.databinding.ActivityMainBinding
import www.sebasorozco.com.tasktimer.ui.dialogs.*
import www.sebasorozco.com.tasktimer.ui.fragments.AddEditFragment
import www.sebasorozco.com.tasktimer.ui.fragments.MainActivityFragment

private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(),
    AddEditFragment.OnSaveClicked,
    MainActivityFragment.OnTaskEdit,
    AppDialog.DialogEvents {

    // Whether or the activity is in 2-pane mode
    // i.e running in landscape, or on a tablet
    private var mTwoPane = false

    // Module scope because we need to dismiss if it onStop (e.g. when orientation changes) to avoid memory leaks.
    private var aboutDialog: AlertDialog? = null

    private val viewModel: TaskTimerViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Log.d(TAG, "onCreate: twoPane is $mTwoPane")


        val fragment = findFragmentById(R.id.taskDetailsContainer)
        if (fragment != null) {
            // There was an existing fragment to edit a task, make sure the panes are set correctly
            showEditPane()
        } else {

            binding.contentMain.taskDetailsContainer.visibility =
                if (mTwoPane) View.INVISIBLE else View.GONE
            binding.contentMain.mainFragment.visibility = View.VISIBLE
        }

        viewModel.timing.observe(this, { timing ->
            binding.contentMain.mainFragment.findViewById<TextView>(R.id.currentTask).text =
                if (timing != null) {
                    getString(R.string.timing_message, timing)
                } else {
                    getString(R.string.no_task_message)
                }
        })
    }

    private fun showEditPane() {
        binding.contentMain.taskDetailsContainer.visibility = View.VISIBLE
        // hide the left hand pane, if in a single view
        binding.contentMain.mainFragment.visibility = if (mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removedEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removedEditPane: called")
        if (fragment != null) {
            removeFragment(fragment)
        }

        // Set the visibility of the right hand pane
        binding.contentMain.taskDetailsContainer.visibility =
            if (mTwoPane) View.INVISIBLE else View.GONE
        // and show the left hand pane
        binding.contentMain.mainFragment.visibility = View.VISIBLE

        //With this code line i drop the back arrow from the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        if (BuildConfig.DEBUG) {
            val generate = menu.findItem(R.id.menumain_generate)
            generate.isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menumain_addtask -> taskEditRequest(null)
            R.id.menumain_showAbout -> showAboutDialog()
            R.id.menumain_generate -> TestData.generateTestData(contentResolver)
            R.id.menumain_showDurations -> startActivity(
                Intent(
                    this,
                    DurationsReportActivity::class.java
                )
            )
            R.id.menumain_settings -> {
                val dialog = SettingsDialog()
                dialog.show(supportFragmentManager, null)
            }
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: home button pressed")
                val fragment = findFragmentById(R.id.taskDetailsContainer)

                if ((fragment is AddEditFragment) && fragment.isDirty()) {
                    showConfirmationDialog(
                        DIALOG_ID_CANCEL_EDIT,
                        getString(R.string.cancelEditDialog_mess),
                        R.string.cancelEditDialog_message,
                        R.string.cancelEDitDiag_positive_caption
                    )
                } else {
                    removedEditPane(fragment)
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAboutDialog() {

        val binding = AboutBinding.inflate(layoutInflater, null, false)

        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.app_name)  // This is working because is before to set the builder to the adapter
        builder.setIcon(R.mipmap.ic_launcher)

        builder.setPositiveButton(R.string.ok) { _, _ ->
            Log.d(TAG, "onClick")
            if (aboutDialog != null && aboutDialog?.isShowing == true) {
                aboutDialog?.dismiss()
            }
        }

        aboutDialog = builder.setView(binding.root).create()
        aboutDialog?.setCanceledOnTouchOutside(true)


        binding.root.setOnClickListener {
            Log.d(TAG, "Entering messageView.onClick")
            if (aboutDialog != null && aboutDialog?.isShowing == true) {
                aboutDialog?.dismiss()
            }
        }

        binding.aboutVersion.text = BuildConfig.VERSION_NAME

        // In case that i need to make some textView send to a web page
        // For this use i need to implement some versioning on the [aboutVersion (TextView)] for separate
        // The link and the description and just to make clickable the link with the onClickListener
        // As is show in the next code lines
        binding.aboutVersion.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            val s = (it as TextView).text.toString()
            intent.data = Uri.parse(s)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.about_url_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }


        aboutDialog?.show()
    }

    override fun onBackPressed() {

        Log.d(TAG, "onBackPressed: button back pressed")
        val fragment = findFragmentById(R.id.taskDetailsContainer)
        if (fragment == null || mTwoPane) {
            super.onBackPressed()
        } else {
            if ((fragment is AddEditFragment) && fragment.isDirty()) {
                showConfirmationDialog(
                    DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEditDialog_mess),
                    R.string.cancelEditDialog_message,
                    R.string.cancelEDitDiag_positive_caption
                )
            } else {
                removedEditPane(fragment)
            }
        }

    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")

        showEditPane()
        replaceFragment(AddEditFragment.newInstance(task), R.id.taskDetailsContainer)

        Log.d(TAG, "Exiting taskEditRequest")
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: called")
        removedEditPane(findFragmentById(R.id.taskDetailsContainer))
    }

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: called with dialogId $dialogId")
        if (dialogId == DIALOG_ID_CANCEL_EDIT) {
            removedEditPane(findFragmentById(R.id.taskDetailsContainer))
        }
    }

    override fun onStop() {
        super.onStop()
        if (aboutDialog?.isShowing == true) {
            aboutDialog?.dismiss()
        }
    }
}