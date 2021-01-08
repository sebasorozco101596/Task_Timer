package www.sebasorozco.com.tasktimer.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import www.sebasorozco.com.tasktimer.R
import www.sebasorozco.com.tasktimer.databinding.SettingsDialogBinding
import java.util.*

private const val TAG = "SettingsDialog"

private const val SETTINGS_FIRST_DAY_OF_WEEK = "FirstDay"
const val SETTINGS_IGNORE_LESS_THAN = "IgnoreLessThan"
const val SETTINGS_DEFAULT_IGNORE_LESS_THAN = 0


private val deltas = intArrayOf(
    0,
    5,
    10,
    15,
    20,
    25,
    30,
    35,
    40,
    45,
    50,
    55,
    60,
    120,
    180,
    240,
    300,
    360,
    420,
    480,
    540,
    600,
    900,
    1800,
    2700
)

class SettingsDialog : AppCompatDialogFragment() {

    private var binding: SettingsDialogBinding? = null

    private val defaultFirstDayOfWeek = GregorianCalendar(Locale.getDefault()).firstDayOfWeek
    private var firstDay = defaultFirstDayOfWeek
    private var ignoreLessThan = SETTINGS_DEFAULT_IGNORE_LESS_THAN

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.SettingsDialogStyle)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsDialogBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        super.onViewCreated(view, savedInstanceState)

        dialog?.setTitle(R.string.menutitle_settings)

        binding?.okButton?.setOnClickListener {
            saveValues()
            dismiss()
        }

        binding?.cancelButton?.setOnClickListener {
            dismiss()
        }

        binding?.ignoreSeconds?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < 12) {
                    binding?.ignoreSecondsTitle?.text = getString(
                        R.string.settingsIgnoreSecondsTitle,
                        deltas[progress],
                        resources.getQuantityString(R.plurals.settingsLittleUnits, deltas[progress])
                    )
                } else {
                    val minutes = deltas[progress] / 60
                    binding?.ignoreSecondsTitle?.text = getString(
                        R.string.settingsIgnoreSecondsTitle,
                        minutes,
                        resources.getQuantityString(R.plurals.settingsBigUnits, minutes)
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // We don't need this
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Nor this
            }

        })
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewStateRestored: called")

        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) {
            readValues()

            binding?.firstDaySpinner?.setSelection(firstDay - GregorianCalendar.SUNDAY) // Spinner values are zero-based

            // Convert seconds into an index into the time values array
            val seekBarValue = deltas.binarySearch(ignoreLessThan)
            if (seekBarValue < 0) {
                // This shouldn't happen, the programmer's made a mistake
                throw IndexOutOfBoundsException("Value $seekBarValue not found in deltas array")
            }
            binding?.ignoreSeconds?.max = deltas.size - 1
            Log.d(TAG, "onViewStateRestored: setting slider to $seekBarValue")
            binding?.ignoreSeconds?.progress = seekBarValue

            if (ignoreLessThan < 60) {
                binding?.ignoreSecondsTitle?.text = getString(
                    R.string.settingsIgnoreSecondsTitle,
                    ignoreLessThan,
                    resources.getQuantityString(R.plurals.settingsLittleUnits, ignoreLessThan)
                )
            } else {
                val minutes = ignoreLessThan / 60
                binding?.ignoreSecondsTitle?.text = getString(
                    R.string.settingsIgnoreSecondsTitle,
                    minutes,
                    resources.getQuantityString(R.plurals.settingsBigUnits, minutes)
                )
            }
        }
    }

    private fun readValues() {
        with(getDefaultSharedPreferences(context)) {
            firstDay = getInt(SETTINGS_FIRST_DAY_OF_WEEK, defaultFirstDayOfWeek)
            ignoreLessThan = getInt(SETTINGS_IGNORE_LESS_THAN, SETTINGS_DEFAULT_IGNORE_LESS_THAN)
        }

        Log.d(TAG, "Retrieving first day = $firstDay, ignoreLessThan = $ignoreLessThan")
    }

    private fun saveValues() {
        val newFirstDayOfWeek =
            binding!!.firstDaySpinner.selectedItemPosition + GregorianCalendar.SUNDAY
        val newIgnoreLessThan = deltas[binding!!.ignoreSeconds.progress]

        Log.d(TAG, "Saving first day = $newFirstDayOfWeek, ignore seconds = $newIgnoreLessThan ")

        with(getDefaultSharedPreferences(context).edit()) {
            if (newFirstDayOfWeek != firstDay) {
                putInt(SETTINGS_FIRST_DAY_OF_WEEK, newFirstDayOfWeek)
            }
            if (newIgnoreLessThan != ignoreLessThan) {
                putInt(SETTINGS_IGNORE_LESS_THAN, newIgnoreLessThan)
            }
            apply()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()

    }
}