package com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WifiPickerViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context

    private val wifiBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiEnabled.value = wifiManager.isWifiEnabled
            scanWifiConnections()
        }
    }

    val wifiScanResults = MutableLiveData<List<ScanResult>>()

    val wifiEnabled = MutableLiveData<Boolean>()

    init {
        context = getApplication()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        context.registerReceiver(wifiBroadcastReceiver, intentFilter)
    }

    override fun onCleared() {
        super.onCleared()
        context.unregisterReceiver(wifiBroadcastReceiver)
    }

    fun scanWifiConnections() {
        viewModelScope.launch {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (wifiManager.isWifiEnabled) {
                wifiScanResults.postValue(wifiManager.scanResults)
            }
        }
    }
}