package www.sebasorozco.com.tasktimer.ui.activities

import android.app.DatePickerDialog
import android.content.res.Configuration
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import www.sebasorozco.com.tasktimer.R
import www.sebasorozco.com.tasktimer.data.viewmodel.DurationsViewModel
import www.sebasorozco.com.tasktimer.data.viewmodel.SortColumns
import www.sebasorozco.com.tasktimer.databinding.ActivityDurationsReportBinding
import www.sebasorozco.com.tasktimer.ui.adapters.DurationsRVAdapter
import www.sebasorozco.com.tasktimer.ui.dialogs.AppDialog
import www.sebasorozco.com.tasktimer.ui.dialogs.DIALOG_ID
import www.sebasorozco.com.tasktimer.ui.dialogs.DIALOG_MESSAGE
import www.sebasorozco.com.tasktimer.ui.dialogs.SettingsDialog
import www.sebasorozco.com.tasktimer.ui.fragments.*
import java.util.*

private const val TAG = "DurationsReportAct"
private var isLandscape = true

private const val DIALOG_FILTER = 1
private const val DIALOG_DELETE = 2

private const val DELETION_DATE = "Deletion date"

class DurationsReportActivity : AppCompatActivity(),
    View.OnClickListener,
    AppDialog.DialogEvents,
    DatePickerDialog.OnDateSetListener {

    private val reportAdapter by lazy { DurationsRVAdapter(this, null) }

    private lateinit var binding: ActivityDurationsReportBinding

    private val viewModel: DurationsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDurationsReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.cursor.observe(this, {
            reportAdapter.swapCursor(it)?.close()
        })

        isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val tdList = binding.root.findViewById<RecyclerView>(R.id.tdList)

        tdList.layoutManager = LinearLayoutManager(this)
        tdList.adapter = reportAdapter

        // ==
        // Set the listener for the buttons so we can sort the report

        binding.root.findViewById<TextView>(R.id.tdNameHeading).setOnClickListener(this)
        if (isLandscape) {
            binding.root.findViewById<TextView>(R.id.tdDescriptionHeading).setOnClickListener(this)
        }
        binding.root.findViewById<TextView>(R.id.tdStartHeading).setOnClickListener(this)
        binding.root.findViewById<TextView>(R.id.tdDurationHeading).setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tdNameHeading -> viewModel.sortOrder = SortColumns.NAME
            R.id.tdDescriptionHeading -> viewModel.sortOrder = SortColumns.DESCRIPTION
            R.id.tdStartHeading -> viewModel.sortOrder = SortColumns.START_DATE
            R.id.tdDurationHeading -> viewModel.sortOrder = SortColumns.DURATION

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_report, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.rmFilterPeriod -> {
                viewModel.toggleDisplayWeek() // was showing a week, so now show a day - or vice versa
                invalidateOptionsMenu() // Force call to onPrepareOptionsMenu to redraw our changed menu
                return true
            }
            R.id.rmFilterDate -> {
                showDatePickerDialog(getString(R.string.date_title_filter), DIALOG_FILTER)
                return true
            }
            R.id.rmDelete -> {
                showDatePickerDialog(getString(R.string.date_title_delete), DIALOG_DELETE)
                return true
            }
            R.id.rmSettings -> {
                val dialog = SettingsDialog()
                dialog.show(supportFragmentManager, "settings")
            }
            android.R.id.home -> {
                finish()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.rmFilterPeriod)
        if (item != null) {
            // switch icon and title to represent 7 days or 1 day, as appropriate to the future function of the menu item.
            if (viewModel.displayWeek) {
                item.setIcon(R.drawable.ic_baseline_filter_1_24)
                item.setTitle(R.string.rm_title_filter_day)
            } else {
                item.setIcon(R.drawable.ic_baseline_filter_7_24)
                item.setTitle(R.string.rm_title_filter_week)
            }

        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun showDatePickerDialog(title: String, dialogId: Int) {

        val dialogFragment = DatePickerFragment()

        val arguments = Bundle()
        arguments.putInt(DATE_PICKER_ID, dialogId)
        arguments.putString(DATE_PICKER_TITLE, title)
        arguments.putSerializable(DATE_PICKER_DATE, viewModel.getFilterDate())

        arguments.putInt(DATE_PICKER_FDOW, viewModel.firstDayOfWeek)
        dialogFragment.arguments = arguments
        dialogFragment.show(supportFragmentManager, "datePicker")

    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        Log.d(TAG, "onDataSet: called")

        // Check the id, so we know what to do with the result
        val dialogId = view.tag as Int

        when (dialogId) {
            DIALOG_FILTER -> {
                viewModel.setReportDate(year, month, dayOfMonth)
            }
            DIALOG_DELETE -> {
                // We need to format the date for the user's locale
                val cal = GregorianCalendar()
                cal.set(year, month, dayOfMonth, 0, 0, 0)
                val fromDate = DateFormat.getDateFormat(this).format(cal.time)

                val dialog = AppDialog()
                val args = Bundle()
                args.putInt(DIALOG_ID, DIALOG_DELETE)       // Use the same id value
                args.putString(DIALOG_MESSAGE, getString(R.string.delete_timings_message, fromDate))

                args.putLong(DELETION_DATE, cal.timeInMillis)
                dialog.arguments = args
                dialog.show(supportFragmentManager, null)
            }
            else -> throw IllegalArgumentException("Invalid mode when receiving DatePickerDialog")
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: button back pressed")
        finish()
        super.onBackPressed()
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: called with id $dialogId")
        // Retrieve the date from the Bundle
        val deleteDate = args.getLong(DELETION_DATE)
        viewModel.deleteRecords(deleteDate)
    }

    /*
    override fun onDestroy() {
        reportAdapter.swapCursor(null)?.close()
        super.onDestroy()
    }

 */
}