package com.ibashkimi.lockscheduler.profiles

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.ProfileManager
import com.ibashkimi.lockscheduler.model.ProfileRepository
import kotlinx.coroutines.launch

class ProfilesViewModel : ViewModel() {

    private val repository: ProfileRepository = ProfileManager

    val profilesLiveData = MutableLiveData<List<Profile>>()

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
