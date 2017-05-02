package it.ibashkimi.lockscheduler.addeditprofile.conditions.picker

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import it.ibashkimi.lockscheduler.R
import it.ibashkimi.lockscheduler.model.WifiItem
import it.ibashkimi.lockscheduler.ui.BaseActivity
import it.ibashkimi.lockscheduler.ui.recyclerview.SelectableAdapter


class WifiPickerActivity : BaseActivity(), WifiInfoProvider {

    private val wifiItems: MutableList<WifiItem> = mutableListOf()

    private val wifiAdapter: WifiAdapter = WifiAdapter(wifiItems)

    private var recyclerView: RecyclerView? = null

    private var turnOnWifi: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_picker)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_toolbar)
        }

        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras != null && extras.containsKey("ssids")) {
                val ssids = extras.getStringArray("ssids")
                for (i in ssids.indices) {
                    val wifi = WifiItem(ssids[i])
                    wifi.isSelected = true
                    wifiItems.add(wifi)
                }
            }
        } else {
            val ssids = savedInstanceState.getStringArray("ssids")
            val selected = savedInstanceState.getBooleanArray("isSelected")
            for (i in ssids.indices) {
                val wifi = WifiItem(ssids[i])
                wifi.isSelected = selected[i]
                wifiItems.add(wifi)
            }
        }

        recyclerView = findViewById(R.id.recyclerView) as RecyclerView
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.isNestedScrollingEnabled = false
        recyclerView?.adapter = wifiAdapter
        turnOnWifi = findViewById(R.id.turnOnWifi) as TextView
        findViewById(R.id.fab).setOnClickListener({ onSave() })
    }

    private val wifiBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            load()
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiBroadcastReceiver, intentFilter)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.let {
            val ssids: MutableList<String> = mutableListOf()
            val selected: MutableList<Boolean> = mutableListOf()
            for (wifi in wifiItems) {
                ssids.add(wifi.SSID)
                selected.add(wifi.isSelected)
            }
            with(outState) {
                putStringArray("ssids", ssids.toTypedArray())
                putBooleanArray("isSelected", selected.toBooleanArray())
            }
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(wifiBroadcastReceiver)
    }

    private fun showTurnOnWifi() {
        recyclerView!!.visibility = View.GONE
        turnOnWifi!!.visibility = View.VISIBLE
    }

    private fun showWifiList() {
        turnOnWifi!!.visibility = View.GONE
        recyclerView!!.visibility = View.VISIBLE

        wifiAdapter.notifyDataSetChanged()
    }

    private fun load() {
        getWifiList(object : WifiInfoProvider.Callback {
            override fun onDataLoaded(items: List<WifiItem>) {
                items.filterNot { wifiItems.contains(it) }.forEach { wifiItems.add(it) }
                showWifiList()
            }

            override fun onDataNotAvailable() {
                showTurnOnWifi()
            }
        })
    }

    override fun getWifiList(callback: WifiInfoProvider.Callback) {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.isWifiEnabled) {
            val wifiList = ArrayList<WifiItem>()
            for (wifiConfiguration in wifiManager.configuredNetworks) {
                var ssid = wifiConfiguration.SSID
                ssid = ssid.substring(1, ssid.length - 1) // Remove " at the start and end.
                wifiList.add(WifiItem(ssid))
            }
            callback.onDataLoaded(wifiList)
        } else {
            callback.onDataNotAvailable()
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

    fun onSave() {
        val intent = Intent()
        if (wifiItems.isNotEmpty()) {
            val ssids: MutableList<String> = mutableListOf()
            wifiItems
                    .filter { it.isSelected }
                    .mapTo(ssids) { it.SSID }
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

    internal inner class WifiAdapter(var wifiList: List<WifiItem>) : SelectableAdapter<WifiAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_wifi_connection, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val wifiItem = wifiList[position]
            holder.title.text = wifiItem.SSID
            holder.checkBox.isChecked = wifiItem.isSelected
            holder.checkBox.setOnCheckedChangeListener { _, isChecked -> wifiList[holder.adapterPosition].isSelected = isChecked }
            holder.rootView.setOnClickListener { holder.checkBox.performClick() }
        }

        override fun getItemCount(): Int {
            return wifiList.size
        }

        internal inner class ViewHolder(var rootView: View) : RecyclerView.ViewHolder(rootView) {
            var title: TextView = rootView.findViewById(R.id.title) as TextView
            var checkBox: CheckBox = rootView.findViewById(R.id.checkbox) as CheckBox
        }
    }
}
