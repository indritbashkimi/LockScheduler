package com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

fun wifiEnabledFlow(context: Context): Flow<Boolean> = callbackFlow {
    val wifiBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            offer(wifiManager.isWifiEnabled)
        }
    }
    val intentFilter = IntentFilter()
    intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
    context.registerReceiver(wifiBroadcastReceiver, intentFilter)
    awaitClose {
        context.unregisterReceiver(wifiBroadcastReceiver)
    }
}

fun wifiScansFlow(context: Context) = flow<List<ScanResult>> {
    val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    if (wifiManager.isWifiEnabled) {
        emit(wifiManager.scanResults)
    } else
        emit(emptyList())
}