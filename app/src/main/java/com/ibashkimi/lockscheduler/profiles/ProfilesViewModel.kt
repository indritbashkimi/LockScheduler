package com.ibashkimi.lockscheduler.profiles

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.ProfileManager
import kotlinx.coroutines.launch

class ProfilesViewModel : ViewModel() {

    private val repository = ProfileManager

    val profilesLiveData = MediatorLiveData<List<Profile>>().apply {
        addSource(repository.lastUpdateLiveData) {
            loadData()
        }
    }

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val profiles = repository.getAll()
            profilesLiveData.postValue(profiles)
        }
    }

    fun delete(profileId: String) {
        repository.remove(profileId)
    }

    fun swapProfiles(profile1: Profile, profile2: Profile) {
        repository.swap(profile1, profile2)
    }
}
