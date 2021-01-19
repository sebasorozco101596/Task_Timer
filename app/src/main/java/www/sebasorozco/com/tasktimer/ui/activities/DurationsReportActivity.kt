package www.sebasorozco.com.tasktimer.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

private const val TAG = "DurationsReportAct"
private var isLandscape = true

class DurationsReportActivity : AppCompatActivity(),
    View.OnClickListener {

    private val reportAdapter by lazy { DurationsRVAdapter(this, null) }

    private lateinit var binding: ActivityDurationsReportBinding

    private val viewModel: DurationsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDurationsReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel.cursor.observe(this, {
            reportAdapter.swapCursor(it)?.close()
        })

        isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val tdList = binding.root.findViewById<RecyclerView>(R.id.tdList)

        tdList.layoutManager = LinearLayoutManager(this)
        tdList.adapter = reportAdapter

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
            R.id.rmDelete -> {
            }
            R.id.rmFilterDate -> {
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

    /*
    override fun onDestroy() {
        reportAdapter.swapCursor(null)?.close()
        super.onDestroy()
    }

 */
}