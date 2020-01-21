package com.ibashkimi.lockscheduler.profiles

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.manager.ProfileManager
import kotlinx.coroutines.launch

class ProfilesViewModel : ViewModel() {

    private val repository = ProfileManager

    val profiles: LiveData<List<Profile>> = repository.getProfilesFlow().asLiveData()

    fun delete(profileId: String) {
        viewModelScope.launch { repository.remove(profileId) }
    }

    fun swapProfiles(profile1: Profile, profile2: Profile) {
        viewModelScope.launch { repository.swap(profile1, profile2) }
    }
}
