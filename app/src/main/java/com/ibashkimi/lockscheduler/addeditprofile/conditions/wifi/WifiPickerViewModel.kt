package com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class WifiPickerViewModel(application: Application) : AndroidViewModel(application) {

    val wifiEnabled = MutableLiveData<Boolean>()

    val wifiItems = MutableLiveData<List<SelectableWifiItem>>()

    init {
        wifiEnabledFlow(getApplication())
            .onEach { wifiEnabled.postValue(it) }
            .flatMapLatest { wifiScansFlow(getApplication()) }
            .map { scans ->
                val items = wifiItems.value?.toMutableList() ?: ArrayList()
                scans.filterNot {
                    items.contains(SelectableWifiItem(it.SSID, it.BSSID, true))
                }.forEach { items.add(SelectableWifiItem(it.SSID, it.BSSID, false)) }
                items
            }
            .filterNotNull()
            .onEach { wifiItems.postValue(it) }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }
}