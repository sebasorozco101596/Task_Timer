package www.sebasorozco.com.tasktimer.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import www.sebasorozco.com.tasktimer.R
import www.sebasorozco.com.tasktimer.data.database.Task
import www.sebasorozco.com.tasktimer.ui.dialogs.showConfirmationDialog
import www.sebasorozco.com.tasktimer.databinding.ActivityMainBinding
import www.sebasorozco.com.tasktimer.ui.fragments.AddEditFragment
import www.sebasorozco.com.tasktimer.ui.fragments.MainActivityFragment
import www.sebasorozco.com.tasktimer.ui.dialogs.AppDialog

private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(),
    AddEditFragment.OnSaveClicked,
    MainActivityFragment.OnTaskEdit,
    AppDialog.DialogEvents{


    // Whether or the activity is in 2-pane mode
    // i.e running in landscape, or on a tablet
    private var mTwoPane = false

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Log.d(TAG, "onCreate: twoPane is $mTwoPane")


        val fragment = supportFragmentManager.findFragmentById(R.id.taskDetailsContainer)
        if (fragment != null) {
            // There was an existing fragment to edit a task, make sure the panes are set correctly
            showEditPane()
        } else {

            binding.contentMain.taskDetailsContainer.visibility =
                if (mTwoPane) View.INVISIBLE else View.GONE
            binding.contentMain.mainFragment.visibility = View.VISIBLE
        }
    }

    private fun showEditPane() {
        binding.contentMain.taskDetailsContainer.visibility = View.VISIBLE
        // hide the left hand pane, if in a single view
        binding.contentMain.mainFragment.visibility = if (mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removedEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removedEditPane: called")
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menumain_addtask -> taskEditRequest(null)
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: home button pressed")
                val fragment = supportFragmentManager.findFragmentById(R.id.taskDetailsContainer)

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

    override fun onBackPressed() {

        Log.d(TAG, "onBackPressed: button back pressed")
        val fragment = supportFragmentManager.findFragmentById(R.id.taskDetailsContainer)
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

        //create a new fragment to edit the task sending all the information about the task to edit
        val newFragment = AddEditFragment.newInstance(task)
        supportFragmentManager.beginTransaction()
            .replace(R.id.taskDetailsContainer, newFragment)
            .commit()

        showEditPane()

        Log.d(TAG, "Exiting taskEditRequest")
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: called")
        val fragment = supportFragmentManager.findFragmentById(R.id.taskDetailsContainer)
        removedEditPane(fragment)
    }

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG,"onPositiveDialogResult: called with dialogId $dialogId")
        if(dialogId == DIALOG_ID_CANCEL_EDIT){
            val fragment = supportFragmentManager.findFragmentById(R.id.taskDetailsContainer)
            removedEditPane(fragment)
        }
    }
}