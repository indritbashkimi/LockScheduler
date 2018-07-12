package com.ibashkimi.lockscheduler.addeditprofile.conditions

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.transition.TransitionManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.addeditprofile.conditions.location.LocationConditionFragment
import com.ibashkimi.lockscheduler.addeditprofile.conditions.location.LocationConditionFragment.PLACE_PICKER_REQUEST
import com.ibashkimi.lockscheduler.addeditprofile.conditions.location.PlacePickerActivity
import com.ibashkimi.lockscheduler.addeditprofile.conditions.time.TimeConditionFragment
import com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi.WifiPickerActivity
import com.ibashkimi.lockscheduler.extention.bindView
import com.ibashkimi.lockscheduler.model.condition.*
import com.ibashkimi.lockscheduler.model.condition.Condition.Type.*
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper
import com.ibashkimi.lockscheduler.util.ConditionUtils
import java.util.*

/**
 * Spaghetti code
 */
class ConditionsFragment : androidx.fragment.app.Fragment(), View.OnClickListener {
    private val placeLayout: ViewGroup by bindView(R.id.locationLayout)
    private val placeDelete: View by bindView(R.id.locationDelete)
    private val placeSummary: TextView by bindView(R.id.location_summary)
    private val locationTitle: TextView by bindView(R.id.location_title)
    private val timeLayout: ViewGroup by bindView(R.id.timeLayout)
    private val timeTitle: TextView by bindView(R.id.time_title)
    private val timeDelete: View by bindView(R.id.timeDelete)
    private val wifiLayout: ViewGroup by bindView(R.id.wifiLayout)
    private val wifiTitle: TextView by bindView(R.id.wifi_title)
    private val wifiDelete: View by bindView(R.id.wifiDelete)
    private val wifiSummary: TextView by bindView(R.id.wifi_summary)
    private val powerLayout: View by bindView(R.id.powerLayout)
    private val powerSummary: TextView by bindView(R.id.power_summary)
    private val powerDelete: View by bindView(R.id.powerDelete)

    private var conditions: List<Condition>? = null
    private var wifiItems: List<WifiItem>? = null
    private var powerWhenConnected: Boolean = false
    private var placeConditionAdded = false
    private var timeConditionAdded = false
    private var wifiConditionAdded = false
    private var powerConditionAdded = false
    private var conditionChangeListener: ConditionChangeListener? = null

    fun setData(conditions: List<Condition>) {
        this.conditions = conditions
    }

    fun assembleConditions(): List<Condition> {
        val conditions = ArrayList<Condition>(3)
        if (placeConditionAdded) {
            val placeCondition = locationConditionFragment.assembleCondition()
            if (placeCondition != null)
                conditions.add(placeCondition)
        }
        if (timeConditionAdded) {
            val timeCondition = timeConditionFragment.assembleCondition()
            if (timeCondition != null)
                conditions.add(timeCondition)
        }
        if (wifiConditionAdded) {
            if (wifiItems != null && wifiItems!!.isNotEmpty())
                conditions.add(WifiCondition(wifiItems!!))
        }
        if (powerConditionAdded) {
            conditions.add(PowerCondition(powerWhenConnected))
        }
        return conditions
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_conditions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        placeLayout.setOnClickListener(this)
        placeDelete.setOnClickListener(this)
        powerLayout.setOnClickListener(this)
        powerDelete.setOnClickListener(this)
        timeLayout.setOnClickListener(this)
        timeDelete.setOnClickListener(this)
        wifiLayout.setOnClickListener(this)
        wifiDelete.setOnClickListener(this)

        if (savedInstanceState == null) {
            val fragmentTransaction = childFragmentManager.beginTransaction()
            if (conditions != null) {
                for (condition in conditions!!) {
                    when (condition.type) {
                        PLACE -> {
                            showLocationCondition(condition as PlaceCondition, fragmentTransaction, false)
                        }
                        TIME -> {
                            showTimeCondition(condition as TimeCondition, fragmentTransaction, false)
                        }
                        WIFI -> {
                            showWifiCondition(condition as WifiCondition, false)
                        }
                        POWER -> {
                            showPowerCondition(condition as PowerCondition, false)
                        }
                    }
                }
            }
            fragmentTransaction.commit()
        } else {
            placeConditionAdded = savedInstanceState.getBoolean("place_added")
            timeConditionAdded = savedInstanceState.getBoolean("time_added")
            wifiConditionAdded = savedInstanceState.getBoolean("wifi_added")
            powerConditionAdded = savedInstanceState.getBoolean("power_added")
            if (savedInstanceState.containsKey("wifi_items_size")) {
                val size = savedInstanceState.getInt("wifi_items_size")
                val items: MutableList<WifiItem> = ArrayList(size)
                for (i in 0 until size) {
                    val ssid = savedInstanceState.getString("wifi_item_" + i, null)
                            ?: throw IllegalStateException("Cannot restore wifi item at position $i.")
                    items.add(WifiItem(ssid))
                }
                wifiItems = items
                showWifiCondition(wifiItems!!)
            }
            powerWhenConnected = savedInstanceState.getBoolean("when_power_connected")
            showPowerCondition(PowerCondition(powerWhenConnected), false)
        }
    }

    override fun onAttach(context: Context?) {
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
        outState.putBoolean("place_added", placeConditionAdded)
        outState.putBoolean("time_added", timeConditionAdded)
        outState.putBoolean("wifi_added", wifiConditionAdded)
        outState.putBoolean("power_added", powerConditionAdded)
        if (wifiItems != null) {
            for (i in wifiItems!!.indices) {
                outState.putString("wifi_item_" + i, wifiItems!![i].ssid)
            }
            outState.putInt("wifi_items_size", wifiItems!!.size)
        }
        outState.putBoolean("when_power_connected", powerWhenConnected)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.locationLayout -> {
                placeConditionAdded = true
                TransitionManager.beginDelayedTransition(placeLayout)
                showPlacePicker()
            }
            R.id.locationDelete -> removeLocationCondition()
            R.id.timeLayout -> {
                timeDelete.visibility = View.VISIBLE
                timeTitle.setText(R.string.time_condition_title)
                TransitionManager.beginDelayedTransition(timeLayout)
                showTimeCondition(TimeCondition())
                timeConditionAdded = true
            }
            R.id.timeDelete -> removeTimeCondition()
            R.id.wifiLayout -> {
                wifiConditionAdded = true
                //TransitionManager.beginDelayedTransition(wifiLayout);
                showWifiPicker(wifiItems)
            }
            R.id.wifiDelete -> removeWifiCondition()
            R.id.powerLayout -> showPowerConditionDialog()
            R.id.powerDelete -> removePowerCondition()
        }
    }

    private fun showPlacePicker() {
        val intent = Intent(context, PlacePickerActivity::class.java)
        intent.putExtra("radius", 300)
        startActivityForResult(intent, PLACE_PICKER_REQUEST)
    }

    fun showPlacePicker(placeCondition: PlaceCondition) {
        val intent = Intent(context, PlacePickerActivity::class.java)
        intent.putExtra("latitude", placeCondition.latitude)
        intent.putExtra("longitude", placeCondition.longitude)
        intent.putExtra("radius", placeCondition.radius)
        intent.putExtra("map_type", AppPreferencesHelper.mapStyle)
        startActivityForResult(intent, PLACE_PICKER_REQUEST)
    }

    private fun showWifiPicker(items: List<WifiItem>?) {
        val intent = Intent(context, WifiPickerActivity::class.java)

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
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val latitude = data!!.getDoubleExtra("latitude", 0.0)
                val longitude = data.getDoubleExtra("longitude", 0.0)
                val radius = data.getIntExtra("radius", 0)
                val address = data.getStringExtra("address")

                //TransitionManager.beginDelayedTransition(locationL)
                val placeCondition = PlaceCondition(latitude, longitude, radius, address)
                val transaction = childFragmentManager.beginTransaction()
                showLocationCondition(placeCondition, transaction, true)
                //http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa
                transaction.commitAllowingStateLoss()
            }
        } else if (requestCode == REQUEST_WIFI_PICKER) {
            if (resultCode == RESULT_OK) {
                val ssidArray = data!!.getStringArrayExtra("ssids")
                val items = ArrayList<WifiItem>(ssidArray.size)
                ssidArray.mapTo(items) { WifiItem(it) }
                wifiItems = items
                if (items.isNotEmpty())
                    showWifiCondition(WifiCondition(wifiItems!!), true)
                else
                    removeWifiCondition()
            }
        }
    }

    private fun showWifiCondition(networks: List<WifiItem>) {
        val wifiList = arrayOfNulls<CharSequence>(networks.size)
        for (i in wifiList.indices) wifiList[i] = networks[i].ssid
        wifiSummary.text = ConditionUtils.concatenate(wifiList, ", ")
        wifiSummary.visibility = View.VISIBLE
        wifiDelete.visibility = View.VISIBLE
        wifiTitle.setText(R.string.wifi_condition_title)
    }

    private fun showLocationCondition(condition: PlaceCondition, transaction: androidx.fragment.app.FragmentTransaction? = null, notify: Boolean = true) {
        locationTitle.setText(R.string.location_condition_title)
        placeConditionAdded = true
        placeSummary.visibility = View.VISIBLE
        placeSummary.text = getString(R.string.location_summary, condition.address, condition.radius)
        placeDelete.visibility = View.VISIBLE

        if (notify)
            notifyConditionChanged(condition)
    }

    private fun removeLocationCondition() {
        placeConditionAdded = false
        TransitionManager.beginDelayedTransition(placeLayout)
        childFragmentManager.beginTransaction().remove(locationConditionFragment).commit()
        placeDelete.visibility = View.GONE
        locationTitle.setText(R.string.location_condition_add_title)
        notifyConditionRemoved(Condition.Type.PLACE)
    }

    private fun showTimeCondition(condition: TimeCondition, transaction: androidx.fragment.app.FragmentTransaction? = null, notify: Boolean = true) {
        timeConditionAdded = true
        val fragment = timeConditionFragment
        fragment.setData(condition)
        if (transaction != null)
            transaction.replace(R.id.time_condition_container, fragment, "time_condition")
        else {
            childFragmentManager.beginTransaction()
                    .replace(R.id.time_condition_container, fragment, "time_condition")
                    .commit()
        }
        timeDelete.visibility = View.VISIBLE
        if (notify)
            notifyConditionChanged(condition)
    }

    private fun removeTimeCondition() {
        TransitionManager.beginDelayedTransition(timeLayout)
        timeConditionAdded = false
        fragmentManager!!.beginTransaction().remove(timeConditionFragment).commit()
        timeDelete.visibility = View.GONE
        timeTitle.setText(R.string.time_condition_add_title)
        notifyConditionRemoved(Condition.Type.TIME)
    }

    private fun showWifiCondition(condition: WifiCondition, notify: Boolean) {
        wifiConditionAdded = true
        wifiItems = condition.wifiList
        showWifiCondition(wifiItems!!)
        wifiDelete.visibility = View.VISIBLE
        if (notify)
            notifyConditionChanged(condition)
    }

    private fun removeWifiCondition() {
        TransitionManager.beginDelayedTransition(wifiLayout)
        wifiConditionAdded = false
        //fragmentManager.beginTransaction().remove(getWifiConditionFragment()).commit();
        wifiDelete.visibility = View.GONE
        wifiSummary.visibility = View.GONE
        wifiTitle.setText(R.string.wifi_condition_add_title)
        wifiItems = null
        notifyConditionRemoved(Condition.Type.WIFI)
    }

    private fun showPowerCondition(condition: PowerCondition, notify: Boolean) {
        powerSummary.setText(if (condition.powerConnected) R.string.power_connected else R.string.power_disconnected)
        powerSummary.visibility = View.VISIBLE
        powerDelete.visibility = View.VISIBLE
        powerConditionAdded = true
        powerDelete.visibility = View.VISIBLE
        if (notify)
            notifyConditionChanged(condition)
    }

    private fun removePowerCondition() {
        powerConditionAdded = false
        powerDelete.visibility = View.GONE
        powerSummary.visibility = View.GONE
        notifyConditionRemoved(Condition.Type.POWER)
    }

    private fun showPowerConditionDialog() {
        val items = resources.getStringArray(R.array.power_state)
        var selectedItem = -1
        if (powerConditionAdded)
            selectedItem = if (powerWhenConnected) 0 else 1
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.power_condition_title)
                .setSingleChoiceItems(items, selectedItem) { dialog, which ->
                    powerWhenConnected = which == 0
                    showPowerCondition(PowerCondition(powerWhenConnected), true)
                    dialog.dismiss()
                }
                .create().show()
    }

    private val locationConditionFragment: LocationConditionFragment
        get() {
            return childFragmentManager.findFragmentByTag("place_condition") as LocationConditionFragment?
                    ?: LocationConditionFragment()
        }

    private val timeConditionFragment: TimeConditionFragment
        get() {
            return childFragmentManager.findFragmentByTag("time_condition") as TimeConditionFragment?
                    ?: TimeConditionFragment()
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

        val REQUEST_WIFI_PICKER = 5

        fun newInstance(): ConditionsFragment {
            return ConditionsFragment()
        }
    }
}
