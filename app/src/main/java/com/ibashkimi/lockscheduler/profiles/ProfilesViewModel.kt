package com.ibashkimi.lockscheduler.profiles

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.data.ProfileManager

class ProfilesViewModel : ViewModel() {

    private val repository = ProfileManager

    val profiles: LiveData<List<Profile>> = repository.getProfilesFlow().asLiveData()

    fun delete(profileId: String) {
        repository.remove(profileId)
    }

    fun swapProfiles(profile1: Profile, profile2: Profile) {
        repository.swap(profile1, profile2)
    }
}
