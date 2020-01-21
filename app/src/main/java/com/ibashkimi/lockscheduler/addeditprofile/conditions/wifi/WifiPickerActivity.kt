package com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.databinding.ActivityWifiPickerBinding
import com.ibashkimi.lockscheduler.databinding.ItemWifiConnectionBinding
import com.ibashkimi.lockscheduler.ui.BaseActivity

// TODO: must have location permission for scanning wifi starting from api 28?
// TODO: use the change wifi state dialog if wifi is off

class WifiPickerActivity : BaseActivity() {

    private val viewModel: WifiPickerViewModel by viewModels()

    private lateinit var binding: ActivityWifiPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWifiPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_clear)
        }

        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras != null && extras.containsKey("wifi_items")) {
                viewModel.wifiItems.value =
                    extras.getParcelableArray("wifi_items")?.map { it as SelectableWifiItem }
                        ?: emptyList()
            }
        } else {
            viewModel.wifiItems.value = savedInstanceState.getParcelableArrayList("wifi_items")
        }

        val wifiAdapter = WifiAdapter()
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
            recyclerView.isNestedScrollingEnabled = false
            recyclerView.adapter = wifiAdapter
            fab.setOnClickListener { onSave() }
        }

        viewModel.wifiEnabled.observe(this, Observer { isWifiEnabled ->
            if (isWifiEnabled) {
                showWifiList()
            } else {
                showTurnOnWifi()
            }
        })
        viewModel.wifiItems.observe(this, Observer { scanResult ->
            wifiAdapter.wifiList = scanResult
            wifiAdapter.notifyDataSetChanged()
        })
    }

    // todo use saved state view model
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.wifiItems.value?.let {
            outState.putParcelableArray("wifi_items", it.toTypedArray())
        }
    }

    private fun showTurnOnWifi() {
        binding.apply {
            recyclerView.visibility = View.GONE
            turnOnWifi.visibility = View.VISIBLE
        }
    }

    private fun showWifiList() {
        binding.apply {
            turnOnWifi.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
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

    private fun onSave() {
        val wifiItems = (viewModel.wifiItems.value ?: emptyList()).filter { it.isSelected }
        if (wifiItems.isNotEmpty()) {
            val intent = Intent()
            intent.putExtra("wifi_items", wifiItems.toTypedArray())
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    private fun onCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    internal inner class WifiAdapter(var wifiList: List<SelectableWifiItem> = emptyList()) :
        SelectableAdapter<WifiAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemWifiConnectionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val wifiItem = wifiList[position]
            holder.binding.apply {
                ssid.text = wifiItem.SSID
                bssid.text = wifiItem.BSSID
                checkbox.isChecked = wifiItem.isSelected
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    wifiList[holder.adapterPosition].isSelected = isChecked
                }
                root.setOnClickListener { checkbox.performClick() }
            }
        }

        override fun getItemCount(): Int {
            return wifiList.size
        }

        internal inner class ViewHolder(val binding: ItemWifiConnectionBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
