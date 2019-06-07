package com.ibashkimi.lockscheduler.addeditprofile.conditions

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.transition.TransitionManager
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.addeditprofile.conditions.location.PlacePickerActivity
import com.ibashkimi.lockscheduler.addeditprofile.conditions.time.TimePickerActivity
import com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi.WifiPickerActivity
import com.ibashkimi.lockscheduler.extention.bindView
import com.ibashkimi.lockscheduler.extention.checkPermission
import com.ibashkimi.lockscheduler.extention.requestPermission
import com.ibashkimi.lockscheduler.model.condition.*
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper
import com.ibashkimi.lockscheduler.util.ConditionUtils
import com.ibashkimi.lockscheduler.util.Utils
import java.util.*


class ConditionsFragment : androidx.fragment.app.Fragment(), View.OnClickListener {
    private val locationLayout: ViewGroup by bindView(R.id.locationLayout)
    private val locationTitle: TextView by bindView(R.id.location_title)
    private val locationDelete: View by bindView(R.id.locationDelete)
    private val locationSummary: TextView by bindView(R.id.location_summary)
    private val timeLayout: ViewGroup by bindView(R.id.timeLayout)
    private val timeTitle: TextView by bindView(R.id.time_title)
    private val timeDelete: View by bindView(R.id.timeDelete)
    private val timeSummary: TextView by bindView(R.id.time_summary)
    private val wifiLayout: ViewGroup by bindView(R.id.wifiLayout)
    private val wifiTitle: TextView by bindView(R.id.wifi_title)
    private val wifiDelete: View by bindView(R.id.wifiDelete)
    private val wifiSummary: TextView by bindView(R.id.wifi_summary)
    private val powerLayout: ViewGroup by bindView(R.id.powerLayout)
    private val powerTitle: TextView by bindView(R.id.powerTitle)
    private val powerDelete: View by bindView(R.id.powerDelete)
    private val powerSummary: TextView by bindView(R.id.power_summary)

    private var conditionChangeListener: ConditionChangeListener? = null

    private var placeCondition: PlaceCondition? = null
    private var timeCondition: TimeCondition? = null
    private var wifiCondition: WifiCondition? = null
    private var powerCondition: PowerCondition? = null

    fun setData(conditions: List<Condition>) {
        for (condition in conditions) {
            when (condition) {
                is PlaceCondition -> placeCondition = condition
                is TimeCondition -> timeCondition = condition
                is WifiCondition -> wifiCondition = condition
                is PowerCondition -> powerCondition = condition
            }
        }
    }

    fun assembleConditions(): List<Condition> {
        val conditions = ArrayList<Condition>(3)
        placeCondition?.let { conditions.add(it) }
        timeCondition?.let { conditions.add(it) }
        wifiCondition?.let { conditions.add(it) }
        powerCondition?.let { conditions.add(it) }
        return conditions
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_conditions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        locationLayout.setOnClickListener(this)
        locationDelete.setOnClickListener(this)
        powerLayout.setOnClickListener(this)
        powerDelete.setOnClickListener(this)
        timeLayout.setOnClickListener(this)
        timeDelete.setOnClickListener(this)
        wifiLayout.setOnClickListener(this)
        wifiDelete.setOnClickListener(this)

        if (savedInstanceState != null) {
            placeCondition = savedInstanceState.getParcelable("location_condition")
            timeCondition = savedInstanceState.getParcelable("time_condition")
            wifiCondition = savedInstanceState.getParcelable("wifi_condition")
            powerCondition = savedInstanceState.getParcelable("power_condition")
        }

        placeCondition?.let { showLocationCondition(it, false) }
        timeCondition?.let { showTimeCondition(it, false) }
        wifiCondition?.let { showWifiCondition(it, false) }
        powerCondition?.let { showPowerCondition(it, false) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ConditionChangeListener)
            conditionChangeListener = context
    }

    override fun onDetach() {
        super.onDetach()
        conditionChangeListener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("location_condition", placeCondition)
        outState.putParcelable("time_condition", timeCondition)
        outState.putParcelable("wifi_condition", wifiCondition)
        outState.putParcelable("power_condition", powerCondition)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.locationLayout -> showPlacePicker()
            R.id.locationDelete -> removeLocationCondition()
            R.id.timeLayout -> showTimePicker()
            R.id.timeDelete -> removeTimeCondition()
            R.id.wifiLayout -> showWifiPicker()
            R.id.wifiDelete -> removeWifiCondition()
            R.id.powerLayout -> showPowerConditionDialog()
            R.id.powerDelete -> removePowerCondition()
        }
    }

    private fun showPlacePicker() {
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                whenGranted = {
                    val intent = Intent(context, PlacePickerActivity::class.java)
                    intent.putExtra("latitude", placeCondition?.latitude)
                    intent.putExtra("longitude", placeCondition?.longitude)
                    intent.putExtra("radius", placeCondition?.radius ?: 300)
                    intent.putExtra("map_type", AppPreferencesHelper.mapStyle)
                    startActivityForResult(intent, REQUEST_LOCATION_PICKER)
                },
                whenExplanationNeed = {
                    AlertDialog.Builder(requireContext())
                            .setTitle(R.string.location_permission_needed)
                            .setMessage(R.string.location_permission_rationale)
                            .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                                dialogInterface.dismiss()
                            }
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_LOCATION)
                            }
                            .create().show()
                },
                whenDenied = {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_LOCATION)
                }
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    showPlacePicker()
            }
        }
    }

    private fun showTimePicker() {
        val intent = Intent(requireContext(), TimePickerActivity::class.java)
        if (timeCondition != null) {
            intent.putExtra("time_condition", timeCondition)
        }
        startActivityForResult(intent, REQUEST_TIME_PICKER)
    }

    private fun showWifiPicker() {
        val intent = Intent(context, WifiPickerActivity::class.java)
        val items = wifiCondition?.wifiList
        if (items != null && items.isNotEmpty()) {
            val itemReps = arrayOfNulls<String>(items.size)
            for (i in items.indices) {
                itemReps[i] = items[i].ssid
            }
            intent.putExtra("ssids", itemReps)
        }
        startActivityForResult(intent, REQUEST_WIFI_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_LOCATION_PICKER) {
            if (resultCode == RESULT_OK) {
                val latitude = data!!.getDoubleExtra("latitude", 0.0)
                val longitude = data.getDoubleExtra("longitude", 0.0)
                val radius = data.getIntExtra("radius", 0)
                val address = data.getStringExtra("address")

                //TransitionManager.beginDelayedTransition(locationL)
                val placeCondition = PlaceCondition(latitude, longitude, radius, address)
                showLocationCondition(placeCondition, true)
            }
        } else if (requestCode == REQUEST_WIFI_PICKER) {
            if (resultCode == RESULT_OK) {
                val ssidArray = data!!.getStringArrayExtra("ssids")
                val items = ArrayList<WifiItem>(ssidArray.size)
                ssidArray.mapTo(items) { WifiItem(it) }
                if (items.isNotEmpty())
                    showWifiCondition(WifiCondition(items), true)
                else
                    removeWifiCondition()
            }
        } else if (requestCode == REQUEST_TIME_PICKER) {
            if (resultCode == RESULT_OK) {
                showTimeCondition(data!!.getParcelableExtra("time_condition"), true)
            }
        }
    }

    private fun showLocationCondition(condition: PlaceCondition, notify: Boolean = true) {
        placeCondition = condition
        TransitionManager.beginDelayedTransition(locationLayout)
        locationTitle.setText(R.string.location_condition_title)
        locationSummary.visibility = View.VISIBLE
        locationSummary.text = getString(R.string.location_summary, condition.address, condition.radius)
        locationDelete.visibility = View.VISIBLE
        if (notify)
            notifyConditionChanged(condition)
    }

    private fun removeLocationCondition() {
        placeCondition = null
        TransitionManager.beginDelayedTransition(locationLayout)
        locationDelete.visibility = View.GONE
        locationSummary.visibility = View.GONE
        locationSummary.text = null
        locationTitle.setText(R.string.location_condition_add_title)
        notifyConditionRemoved(Condition.Type.PLACE)
    }

    private fun showTimeCondition(condition: TimeCondition, notify: Boolean = true) {
        timeCondition = condition
        TransitionManager.beginDelayedTransition(timeLayout)
        timeSummary.visibility = View.VISIBLE
        timeSummary.text = getString(R.string.time_condition_summary,
                ConditionUtils.daysToString(context, timeCondition),
                Utils.formatTime(condition.startTime.hour, condition.startTime.minute),
                Utils.formatTime(condition.endTime.hour, condition.endTime.minute))
        timeDelete.visibility = View.VISIBLE
        if (notify)
            notifyConditionChanged(condition)
    }

    private fun removeTimeCondition() {
        timeCondition = null
        TransitionManager.beginDelayedTransition(timeLayout)
        timeDelete.visibility = View.GONE
        timeSummary.visibility = View.GONE
        timeSummary.text = null
        timeTitle.setText(R.string.time_condition_add_title)
        notifyConditionRemoved(Condition.Type.TIME)
    }

    private fun showWifiCondition(condition: WifiCondition, notify: Boolean) {
        wifiCondition = condition
        TransitionManager.beginDelayedTransition(wifiLayout)
        val wifiList = arrayOfNulls<CharSequence>(condition.wifiList.size)
        for (i in wifiList.indices) wifiList[i] = condition.wifiList[i].ssid
        wifiSummary.text = ConditionUtils.concatenate(wifiList, ", ")
        wifiSummary.visibility = View.VISIBLE
        wifiDelete.visibility = View.VISIBLE
        wifiTitle.setText(R.string.wifi_condition_title)
        wifiDelete.visibility = View.VISIBLE
        if (notify)
            notifyConditionChanged(condition)
    }

    private fun removeWifiCondition() {
        wifiCondition = null
        TransitionManager.beginDelayedTransition(wifiLayout)
        wifiDelete.visibility = View.GONE
        wifiSummary.visibility = View.GONE
        wifiSummary.text = null
        wifiTitle.setText(R.string.wifi_condition_add_title)
        notifyConditionRemoved(Condition.Type.WIFI)
    }

    private fun showPowerCondition(condition: PowerCondition, notify: Boolean) {
        powerCondition = condition
        TransitionManager.beginDelayedTransition(powerLayout)
        powerTitle.setText(R.string.power_condition_title)
        powerSummary.setText(if (condition.powerConnected) R.string.power_connected else R.string.power_disconnected)
        powerSummary.visibility = View.VISIBLE
        powerDelete.visibility = View.VISIBLE
        powerDelete.visibility = View.VISIBLE
        if (notify)
            notifyConditionChanged(condition)
    }

    private fun removePowerCondition() {
        powerCondition = null
        TransitionManager.beginDelayedTransition(locationLayout)
        powerTitle.setText(R.string.power_condition_add_title)
        powerDelete.visibility = View.GONE
        powerSummary.visibility = View.GONE
        powerSummary.text = null
        notifyConditionRemoved(Condition.Type.POWER)
    }

    private fun showPowerConditionDialog() {
        val items = resources.getStringArray(R.array.power_state)
        var selectedItem = -1
        if (powerCondition != null)
            selectedItem = if (powerCondition != null && powerCondition!!.powerConnected) 0 else 1
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.power_condition_title)
                .setSingleChoiceItems(items, selectedItem) { dialog, which ->
                    showPowerCondition(PowerCondition(which == 0), true)
                    dialog.dismiss()
                }
                .create().show()
    }

    private fun notifyConditionRemoved(@Condition.Type type: Int) {
        if (conditionChangeListener != null)
            conditionChangeListener!!.onConditionRemoved(type)
    }

    private fun notifyConditionChanged(condition: Condition) {
        if (conditionChangeListener != null)
            conditionChangeListener!!.onConditionChanged(condition)
    }

    interface ConditionChangeListener {

        fun onConditionChanged(condition: Condition)

        fun onConditionRemoved(@Condition.Type type: Int)
    }

    companion object {

        const val REQUEST_LOCATION_PICKER = 3
        const val REQUEST_TIME_PICKER = 4
        const val REQUEST_WIFI_PICKER = 5
        const val PERMISSION_REQUEST_LOCATION = 1

        fun newInstance(): ConditionsFragment {
            return ConditionsFragment()
        }
    }
}
