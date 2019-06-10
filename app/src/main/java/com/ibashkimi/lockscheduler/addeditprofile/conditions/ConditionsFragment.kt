package com.ibashkimi.lockscheduler.addeditprofile.conditions

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateVMFactory
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.addeditprofile.AddEditProfileViewModel
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


class ConditionsFragment : Fragment(), View.OnClickListener {
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

    private lateinit var viewModel: AddEditProfileViewModel

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

        viewModel = ViewModelProviders.of(requireParentFragment(), SavedStateVMFactory(requireParentFragment()))
                .get(AddEditProfileViewModel::class.java)
        viewModel.getPlaceCondition().observe(viewLifecycleOwner, androidx.lifecycle.Observer { placeCondition ->
            placeCondition?.let { showLocationCondition(it) } ?: removeLocationCondition()
        })
        viewModel.getPowerCondition().observe(viewLifecycleOwner, androidx.lifecycle.Observer { powerCondition ->
            powerCondition?.let { showPowerCondition(it) } ?: removePowerCondition()
        })
        viewModel.getTimeCondition().observe(viewLifecycleOwner, androidx.lifecycle.Observer { timeCondition ->
            timeCondition?.let { showTimeCondition(it) } ?: removeTimeCondition()
        })
        viewModel.getWifiCondition().observe(viewLifecycleOwner, androidx.lifecycle.Observer { wifiCondition ->
            wifiCondition?.let { showWifiCondition(it) } ?: removeWifiCondition()
        })

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.locationLayout -> showPlacePicker()
            R.id.locationDelete -> viewModel.setPlaceCondition(null)
            R.id.timeLayout -> showTimePicker()
            R.id.timeDelete -> viewModel.setTimeCondition(null)
            R.id.wifiLayout -> showWifiPicker()
            R.id.wifiDelete -> viewModel.setWifiCondition(null)
            R.id.powerLayout -> showPowerConditionDialog()
            R.id.powerDelete -> viewModel.setPowerCondition(null)
        }
    }

    private fun showPlacePicker() {
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                whenGranted = {
                    val placeCondition = viewModel.getPlaceCondition().value
                    val intent = Intent(requireContext(), PlacePickerActivity::class.java)
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
                                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_LOCATION_PLACE)
                            }
                            .create().show()
                },
                whenDenied = {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_LOCATION_PLACE)
                }
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_LOCATION_PLACE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    showPlacePicker()
            }
            PERMISSION_REQUEST_LOCATION_WIFI -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    showWifiPicker()
            }
        }
    }

    private fun showTimePicker() {
        val timeCondition = viewModel.getTimeCondition().value
        val intent = Intent(requireContext(), TimePickerActivity::class.java)
        if (timeCondition != null) {
            intent.putExtra("time_condition", timeCondition)
        }
        startActivityForResult(intent, REQUEST_TIME_PICKER)
    }

    private fun showWifiPicker() {
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                whenGranted = {
                    val wifiCondition = viewModel.getWifiCondition().value
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
                },
                whenExplanationNeed = {
                    AlertDialog.Builder(requireContext())
                            .setTitle(R.string.location_permission_needed)
                            .setMessage(R.string.location_permission_rationale)
                            .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                                dialogInterface.dismiss()
                            }
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_LOCATION_WIFI)
                            }
                            .create().show()
                },
                whenDenied = {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_LOCATION_WIFI)
                }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_LOCATION_PICKER) {
            if (resultCode == RESULT_OK) {
                val latitude = data!!.getDoubleExtra("latitude", 0.0)
                val longitude = data.getDoubleExtra("longitude", 0.0)
                val radius = data.getIntExtra("radius", 0)
                val address = data.getStringExtra("address")!!

                //TransitionManager.beginDelayedTransition(locationL)
                val placeCondition = PlaceCondition(latitude, longitude, radius, address)
                viewModel.setPlaceCondition(placeCondition) //showLocationCondition(placeCondition)
            }
        } else if (requestCode == REQUEST_WIFI_PICKER) {
            if (resultCode == RESULT_OK) {
                val ssidArray = data!!.getStringArrayExtra("ssids")!!
                val items = ArrayList<WifiItem>(ssidArray.size)
                ssidArray.mapTo(items) { WifiItem(it) }
                viewModel.setWifiCondition(if (items.isNotEmpty()) WifiCondition(items) else null)
            }
        } else if (requestCode == REQUEST_TIME_PICKER) {
            if (resultCode == RESULT_OK) {
                viewModel.setTimeCondition(data!!.getParcelableExtra("time_condition")!!)
            }
        }
    }

    private fun showLocationCondition(condition: PlaceCondition) {
        TransitionManager.beginDelayedTransition(locationLayout)
        locationTitle.setText(R.string.location_condition_title)
        locationSummary.visibility = View.VISIBLE
        locationSummary.text = getString(R.string.location_summary, condition.address, condition.radius)
        locationDelete.visibility = View.VISIBLE
    }

    private fun removeLocationCondition() {
        TransitionManager.beginDelayedTransition(locationLayout)
        locationDelete.visibility = View.GONE
        locationSummary.visibility = View.GONE
        locationSummary.text = null
        locationTitle.setText(R.string.location_condition_add_title)
    }

    private fun showTimeCondition(condition: TimeCondition) {
        TransitionManager.beginDelayedTransition(timeLayout)
        timeSummary.visibility = View.VISIBLE
        timeSummary.text = getString(R.string.time_condition_summary,
                ConditionUtils.daysToString(context, condition),
                Utils.formatTime(condition.startTime.hour, condition.startTime.minute),
                Utils.formatTime(condition.endTime.hour, condition.endTime.minute))
        timeDelete.visibility = View.VISIBLE
    }

    private fun removeTimeCondition() {
        TransitionManager.beginDelayedTransition(timeLayout)
        timeDelete.visibility = View.GONE
        timeSummary.visibility = View.GONE
        timeSummary.text = null
        timeTitle.setText(R.string.time_condition_add_title)
    }

    private fun showWifiCondition(condition: WifiCondition) {
        TransitionManager.beginDelayedTransition(wifiLayout)
        val wifiList = arrayOfNulls<CharSequence>(condition.wifiList.size)
        for (i in wifiList.indices) wifiList[i] = condition.wifiList[i].ssid
        wifiSummary.text = ConditionUtils.concatenate(wifiList, ", ")
        wifiSummary.visibility = View.VISIBLE
        wifiDelete.visibility = View.VISIBLE
        wifiTitle.setText(R.string.wifi_condition_title)
        wifiDelete.visibility = View.VISIBLE
    }

    private fun removeWifiCondition() {
        TransitionManager.beginDelayedTransition(wifiLayout)
        wifiDelete.visibility = View.GONE
        wifiSummary.visibility = View.GONE
        wifiSummary.text = null
        wifiTitle.setText(R.string.wifi_condition_add_title)
    }

    private fun showPowerCondition(condition: PowerCondition) {
        TransitionManager.beginDelayedTransition(powerLayout)
        powerTitle.setText(R.string.power_condition_title)
        powerSummary.setText(if (condition.powerConnected) R.string.power_connected else R.string.power_disconnected)
        powerSummary.visibility = View.VISIBLE
        powerDelete.visibility = View.VISIBLE
        powerDelete.visibility = View.VISIBLE
    }

    private fun removePowerCondition() {
        TransitionManager.beginDelayedTransition(locationLayout)
        powerTitle.setText(R.string.power_condition_add_title)
        powerDelete.visibility = View.GONE
        powerSummary.visibility = View.GONE
        powerSummary.text = null
    }

    private fun showPowerConditionDialog() {
        val powerCondition = viewModel.getPowerCondition().value
        val items = resources.getStringArray(R.array.power_state)
        var selectedItem = -1
        if (powerCondition != null)
            selectedItem = if (powerCondition.powerConnected) 0 else 1
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.power_condition_title)
                .setSingleChoiceItems(items, selectedItem) { dialog, which ->
                    viewModel.setPowerCondition(PowerCondition(which == 0))
                    dialog.dismiss()
                }
                .create().show()
    }

    companion object {

        const val REQUEST_LOCATION_PICKER = 3
        const val REQUEST_TIME_PICKER = 4
        const val REQUEST_WIFI_PICKER = 5
        const val PERMISSION_REQUEST_LOCATION_PLACE = 1
        const val PERMISSION_REQUEST_LOCATION_WIFI = 2

        fun newInstance(): ConditionsFragment {
            return ConditionsFragment()
        }
    }
}
