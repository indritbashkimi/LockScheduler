package com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.ui.BaseActivity

// TODO: must have location permission for scanning wifi starting from api 28?
// TODO: use the new change wifi state dialog if wifi is off

class WifiPickerActivity : BaseActivity() {

    private val wifiItems: MutableList<SelectableWifiItem> = mutableListOf()

    private val wifiAdapter: WifiAdapter = WifiAdapter(wifiItems)

    private lateinit var recyclerView: RecyclerView

    private lateinit var turnOnWifi: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_picker)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_toolbar)
        }

        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras != null && extras.containsKey("ssids")) {
                val ssids = extras.getStringArray("ssids") ?: emptyArray()
                ssids.indices.mapTo(wifiItems) { SelectableWifiItem(ssids[it], true) }
            }
        } else {
            val ssids = savedInstanceState.getStringArray("ssids") ?: emptyArray()
            val selected = savedInstanceState.getBooleanArray("isSelected")!!
            ssids.indices.mapTo(wifiItems) { SelectableWifiItem(ssids[it], selected[it]) }
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = wifiAdapter
        turnOnWifi = findViewById(R.id.turnOnWifi)
        findViewById<View>(R.id.fab).setOnClickListener { onSave() }

        val viewModel = ViewModelProvider(this).get(WifiPickerViewModel::class.java)
        viewModel.wifiEnabled.observe(this, Observer { isWifiEnabled ->
            if (isWifiEnabled) {
                showWifiList()
            } else {
                showTurnOnWifi()
            }
        })
        viewModel.wifiScanResults.observe(this, Observer { scanResult ->
            scanResult.filterNot { wifiItems.contains(SelectableWifiItem(it.SSID, true)) }
                .forEach { wifiItems.add(SelectableWifiItem(it.SSID, false)) }
            wifiAdapter.notifyDataSetChanged()
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.let {
            val ssids: MutableList<String> = mutableListOf()
            val selected: MutableList<Boolean> = mutableListOf()
            for (wifi in wifiItems) {
                ssids.add(wifi.ssid)
                selected.add(wifi.isSelected)
            }
            with(outState) {
                putStringArray("ssids", ssids.toTypedArray())
                putBooleanArray("isSelected", selected.toBooleanArray())
            }
        }
    }

    private fun showTurnOnWifi() {
        recyclerView.visibility = View.GONE
        turnOnWifi.visibility = View.VISIBLE
    }

    private fun showWifiList() {
        turnOnWifi.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        wifiAdapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onCancel()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onSave() {
        val intent = Intent()
        if (wifiItems.isNotEmpty()) {
            val ssids: MutableList<String> = mutableListOf()
            wifiItems
                .filter { it.isSelected }
                .mapTo(ssids) { it.ssid }
            intent.putExtra("ssids", ssids.toTypedArray())
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    fun onCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    data class SelectableWifiItem(val ssid: String, var isSelected: Boolean = false)

    internal inner class WifiAdapter(private var wifiList: List<SelectableWifiItem>) :
        SelectableAdapter<WifiAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_wifi_connection, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val wifiItem = wifiList[position]
            holder.title.text = wifiItem.ssid
            holder.checkBox.isChecked = wifiItem.isSelected
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                wifiList[holder.adapterPosition].isSelected = isChecked
            }
            holder.rootView.setOnClickListener { holder.checkBox.performClick() }
        }

        override fun getItemCount(): Int {
            return wifiList.size
        }

        internal inner class ViewHolder(var rootView: View) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(rootView) {
            var title: TextView = rootView.findViewById(R.id.title)
            var checkBox: CheckBox = rootView.findViewById(R.id.checkbox)
        }
    }
}
