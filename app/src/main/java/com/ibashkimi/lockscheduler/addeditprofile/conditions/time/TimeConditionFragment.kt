package com.ibashkimi.lockscheduler.addeditprofile.conditions.time

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.DateTimeCallback
import com.afollestad.materialdialogs.datetime.timePicker
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.databinding.FragmentConditionTimeBinding
import com.ibashkimi.lockscheduler.extention.toDaysString
import com.ibashkimi.lockscheduler.model.condition.DaysOfWeek
import com.ibashkimi.lockscheduler.model.condition.Time
import com.ibashkimi.lockscheduler.model.condition.TimeCondition
import com.ibashkimi.lockscheduler.util.Utils


class TimeConditionFragment : Fragment() {
    private var _binding: FragmentConditionTimeBinding? = null
    private val binding: FragmentConditionTimeBinding get() = _binding!!
    private val viewModel: TimeConditionViewModel by viewModels()

    fun assembleCondition() = TimeCondition().apply {
        viewModel.daysLiveData.value?.let { daysActive = it }
        viewModel.startTimeLiveData.value?.let { startTime = it }
        viewModel.endTimeLiveData.value?.let { endTime = it }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return FragmentConditionTimeBinding.inflate(inflater, container, false).run {
            _binding = this

            days.setOnClickListener { showWeekDays() }
            startTime.setOnClickListener { showStartTimePicker() }
            endTime.setOnClickListener { showEndTimePicker() }

            var startTime: Time? = null
            var endTime: Time? = null
            var days: DaysOfWeek? = null

            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey("start_time_hour"))
                    startTime = Time(
                        savedInstanceState.getInt("start_time_hour"),
                        savedInstanceState.getInt("start_time_minute")
                    )
                if (savedInstanceState.containsKey("end_time_hour"))
                    endTime = Time(
                        savedInstanceState.getInt("end_time_hour"),
                        savedInstanceState.getInt("end_time_minute")
                    )
                if (savedInstanceState.containsKey("days")) {
                    days = savedInstanceState.getParcelable("days")
                }
            } else {
                val timeCondition =
                    requireActivity().intent.getParcelableExtra<TimeCondition>("time_condition")
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
            viewModel.apply {
                daysLiveData.value = days
                startTimeLiveData.value = startTime
                endTimeLiveData.value = endTime

                daysLiveData.observe(viewLifecycleOwner, Observer {
                    daysSummary.text = it.toDaysString(requireContext())
                })
                startTimeLiveData.observe(viewLifecycleOwner, Observer {
                    startTimeSummary.text = Utils.formatTime(it.hour, it.minute)
                })
                endTimeLiveData.observe(viewLifecycleOwner, Observer {
                    endTimeSummary.text = Utils.formatTime(it.hour, it.minute)
                })
            }
            root
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.startTimeLiveData.value?.apply {
            outState.putInt("start_time_hour", hour)
            outState.putInt("start_time_minute", minute)
        }
        viewModel.endTimeLiveData.value?.apply {
            outState.putInt("end_time_hour", hour)
            outState.putInt("end_time_minute", minute)
        }
        viewModel.daysLiveData.value?.let {
            outState.putParcelable("days", it)
        }
    }

    private fun showWeekDays() {
        val dialogFragment = DaysPickerDialogFragment()
        dialogFragment.show(childFragmentManager, "days_picker")
    }

    private fun showIntervalError() {
        Toast.makeText(context, R.string.time_condition_interval_error, Toast.LENGTH_SHORT).show()
    }

    private fun showTimePicker(callback: DateTimeCallback) {
        MaterialDialog(requireContext()).show {
            timePicker(timeCallback = callback)
        }
    }

    private fun showStartTimePicker() = showTimePicker { dialog, calendar ->
        val startTime = Time.fromCalendar(calendar)
        viewModel.endTimeLiveData.value?.let { endTime ->
            if (!isRangeValid(startTime, endTime)) {
                showIntervalError()
                dialog.dismiss()
                return@showTimePicker
            }
        }
        viewModel.startTimeLiveData.value = startTime
    }

    private fun showEndTimePicker() = showTimePicker { dialog, calendar ->
        val endTime = Time.fromCalendar(calendar)
        viewModel.startTimeLiveData.value?.let { startTime ->
            if (!isRangeValid(startTime, endTime)) {
                showIntervalError()
                dialog.dismiss()
                return@showTimePicker
            }
        }
        viewModel.endTimeLiveData.value = endTime
    }

    private fun isRangeValid(startTime: Time, endTime: Time): Boolean {
        return (!startTime.isMidnight && !endTime.isMidnight && startTime.isLowerThan(endTime))
    }

    class DaysPickerDialogFragment : DialogFragment() {
        private val viewModel: TimeConditionViewModel by viewModels(
            ownerProducer = { requireParentFragment() }
        )
        private lateinit var days: DaysOfWeek

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            days = if (savedInstanceState != null) {
                savedInstanceState.getParcelable("days")
            } else {
                viewModel.daysLiveData.value
            } ?: DaysOfWeek()

            return AlertDialog.Builder(requireContext()).run {
                setTitle(R.string.time_condition_days)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.daysLiveData.value = days
                }
                setNegativeButton(android.R.string.cancel, null)
                setMultiChoiceItems(
                    R.array.days_of_week,
                    days.asBooleanArray()
                ) { _, which, isChecked -> days[which] = isChecked }
                create()
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putParcelable("days", days)
        }
    }
}
