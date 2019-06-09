package com.ibashkimi.lockscheduler.addeditprofile.conditions.time

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.model.condition.DaysOfWeek
import com.ibashkimi.lockscheduler.model.condition.Time
import com.ibashkimi.lockscheduler.model.condition.TimeCondition
import com.ibashkimi.lockscheduler.util.ConditionUtils
import com.ibashkimi.lockscheduler.util.Utils
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment


class TimeConditionFragment : Fragment() {

    private var daysSummary: TextView? = null

    private var startTimeSummary: TextView? = null

    private var endTimeSummary: TextView? = null

    private var condition: TimeCondition? = null

    private var days: DaysOfWeek? = null

    private var startTime: Time? = null

    private var endTime: Time? = null

    private val isNight: Boolean
        get() = resources.getBoolean(R.bool.night_mode)

    fun assembleCondition(): TimeCondition {
        val result = TimeCondition()
        days?.let { result.daysActive = it }
        startTime?.let { result.startTime = it }
        endTime?.let { result.endTime = it }
        return result
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_condition_time, container, false) as ViewGroup
        daysSummary = root.findViewById(R.id.days_summary)
        startTimeSummary = root.findViewById(R.id.start_time_summary)
        endTimeSummary = root.findViewById(R.id.end_time_summary)
        root.findViewById<View>(R.id.days).setOnClickListener { showWeekDays() }
        root.findViewById<View>(R.id.start_time).setOnClickListener { showStartTimePicker() }
        root.findViewById<View>(R.id.end_time).setOnClickListener { showEndTimePicker() }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("start_time_hour"))
                startTime = Time(savedInstanceState.getInt("start_time_hour"), savedInstanceState.getInt("start_time_minute"))
            if (savedInstanceState.containsKey("end_time_hour"))
                endTime = Time(savedInstanceState.getInt("end_time_hour"), savedInstanceState.getInt("end_time_minute"))
            if (savedInstanceState.containsKey("days")) {
                days = savedInstanceState.getParcelable("days")
            }
        } else {
            val timeCondition = requireActivity().intent.getParcelableExtra<TimeCondition>("time_condition")
            if (timeCondition != null) {
                days = timeCondition.daysActive
                startTime = timeCondition.startTime
                endTime = timeCondition.endTime
            } else {
                days = DaysOfWeek()
                startTime = Time(0, 0)
                endTime = Time(0, 0)
            }
        }
        val timeCondition = TimeCondition(days!!, startTime!!, endTime!!, false)
        daysSummary!!.text = ConditionUtils.daysToString(context, timeCondition)
        startTimeSummary!!.text = Utils.formatTime(startTime!!.hour, startTime!!.minute)
        endTimeSummary!!.text = Utils.formatTime(endTime!!.hour, endTime!!.minute)


        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (startTime != null) {
            outState.putInt("start_time_hour", startTime!!.hour)
            outState.putInt("start_time_minute", startTime!!.minute)
        }
        if (endTime != null) {
            outState.putInt("end_time_hour", endTime!!.hour)
            outState.putInt("end_time_minute", endTime!!.minute)
        }
        if (days != null) {
            outState.putParcelable("day", days)
        }
    }

    private fun showWeekDays() {
        val dialogFragment = DaysPickerDialogFragment()
        dialogFragment.setDays(days)
        dialogFragment.show(childFragmentManager, "days_picker")
    }

    private fun onDaysSelected(days: DaysOfWeek) {
        this.days = days.createCopy()
        val timeCondition = TimeCondition(days, Time(0, 0), Time(0, 0), false)
        daysSummary!!.text = ConditionUtils.daysToString(context, timeCondition)
    }

    private fun showStartTimePicker() {
        showTimePicker(TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute, second ->
            val time = Time(hourOfDay, minute)
            if (endTime != null && !endTime!!.isMidnight && endTime!!.compareTo(time).isNotHigher) {
                showIntervalError()
                return@OnTimeSetListener
            }
            startTimeSummary!!.text = Utils.formatTime(hourOfDay, minute)
            startTime = Time(hourOfDay, minute)
        })
    }

    private fun showIntervalError() {
        Toast.makeText(context, R.string.time_condition_interval_error, Toast.LENGTH_SHORT).show()
    }

    private fun showEndTimePicker() {
        showTimePicker(TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute, second ->
            val time = Time(hourOfDay, minute)
            if (startTime != null && !startTime!!.isMidnight && startTime!!.compareTo(time).isNotLower) {
                showIntervalError()
                return@OnTimeSetListener
            }
            endTimeSummary!!.text = Utils.formatTime(hourOfDay, minute)
            endTime = time
        })
    }

    private fun showTimePicker(callback: TimePickerDialog.OnTimeSetListener) {
        val timePickerDialog = TimePickerDialog.newInstance(
                callback,
                true)
        timePickerDialog.isThemeDark = isNight
        val activity = requireActivity() as AppCompatActivity
        timePickerDialog.show(activity.supportFragmentManager, "time_picker_dialog")
    }

    class DaysPickerDialogFragment : DialogFragment() {

        private var listener: TimeConditionFragment? = null
        private var days: DaysOfWeek? = null

        fun setDays(days: DaysOfWeek?) {
            this.days = days
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            val parent = parentFragment
            if (parent == null || parent !is TimeConditionFragment) {
                throw ClassCastException("Parent fragment must be TimeConditionFragment.")
            }
            listener = parent
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            if (savedInstanceState != null) {
                days = savedInstanceState.getParcelable("days")
            }
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.time_condition_days)
            builder.setPositiveButton(android.R.string.ok) { _, _ -> listener?.onDaysSelected(days!!) }
            builder.setNegativeButton(R.string.cancel, null)
            builder.setMultiChoiceItems(R.array.days_of_week, days!!.asBooleanArray()) { dialog, which, isChecked -> days!![which] = isChecked }
            return builder.create()
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putParcelable("days", days)
        }
    }
}
