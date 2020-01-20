package com.ibashkimi.lockscheduler.addeditprofile

import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.data.ProfileManager
import com.ibashkimi.lockscheduler.data.ProfileRepository
import com.ibashkimi.lockscheduler.model.*
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.model.condition.PlaceCondition
import com.ibashkimi.lockscheduler.model.condition.PowerCondition
import com.ibashkimi.lockscheduler.model.condition.TimeCondition
import com.ibashkimi.lockscheduler.model.condition.WifiCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddEditProfileViewModel(private val state: SavedStateHandle) : ViewModel() {

    private val repository: ProfileRepository =
        ProfileManager

    init {
        if (state.contains("profile_id")) {
            setProfileId(getProfileId())
        }
    }

    fun getProfileId(): String? = state.get("profile_id")

    fun setProfileId(profileId: String?) {
        if (profileId == null) {
            state.remove<String>("profile_id")
            setProfileName(null)
            setPlaceCondition(null)
            setPowerCondition(null)
            setTimeCondition(null)
            setWifiCondition(null)
            setWhenActiveLockAction(LockAction(LockAction.LockMode.Unchanged))
            setWhenInactiveLockAction(LockAction(LockAction.LockMode.Unchanged))
        } else {
            state["profile_id"] = profileId
            viewModelScope.launch(Dispatchers.Main) {
                repository.get(profileId)?.apply {
                    setProfileName(name)
                    setPlaceCondition(conditions.placeCondition)
                    setPowerCondition(conditions.powerCondition)
                    setTimeCondition(conditions.timeCondition)
                    setWifiCondition(conditions.wifiCondition)
                    setWhenActiveLockAction(enterExitActions.enterActions.lockAction!!)
                    setWhenInactiveLockAction(enterExitActions.exitActions.lockAction!!)
                } ?: setProfileId(null)
            }
        }
    }

    fun getProfileName() = state.getLiveData<String?>("profile_name")

    fun setProfileName(name: String?) {
        getProfileName().value = name
    }

    fun getWhenActiveLockAction() = state.getLiveData<LockAction>("active_lock_action")

    fun setWhenActiveLockAction(lockAction: LockAction?) {
        getWhenActiveLockAction().value = lockAction
    }

    fun getWhenInactiveLockAction() = state.getLiveData<LockAction>("inactive_lock_action")

    fun setWhenInactiveLockAction(lockAction: LockAction?) {
        getWhenInactiveLockAction().value = lockAction
    }

    fun getPlaceCondition() = state.getLiveData<PlaceCondition>("place_condition")

    fun setPlaceCondition(condition: PlaceCondition?) {
        getPlaceCondition().value = condition
    }

    fun getPowerCondition() = state.getLiveData<PowerCondition>("power_condition")

    fun setPowerCondition(condition: PowerCondition?) {
        getPowerCondition().value = condition
    }

    fun getTimeCondition() = state.getLiveData<TimeCondition>("time_condition")

    fun setTimeCondition(condition: TimeCondition?) {
        getTimeCondition().value = condition
    }

    fun getWifiCondition() = state.getLiveData<WifiCondition>("wifi_condition")

    fun setWifiCondition(condition: WifiCondition?) {
        getWifiCondition().value = condition
    }

    fun saveProfile(): Boolean {
        val conditions = Conditions.Builder().apply {
            placeCondition = getPlaceCondition().value
            powerCondition = getPowerCondition().value
            timeCondition = getTimeCondition().value
            wifiCondition = getWifiCondition().value
        }.build()
        val profileName: String = getProfileName().value
            ?: App.getInstance().getString(R.string.default_profile_name)
        if (conditions.all.isEmpty()) {
            Toast.makeText(App.getInstance(), "Add at least one condition", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        val activeAction = getWhenActiveLockAction().value
        if (activeAction == null) {
            Toast.makeText(App.getInstance(), "Active action not specified", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        val inactiveAction = getWhenInactiveLockAction().value
        if (inactiveAction == null) {
            Toast.makeText(App.getInstance(), "Inactive action not specified", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        val actions = EnterExitActions(
            Actions.Builder().apply { lockAction = activeAction }.build(),
            Actions.Builder().apply { lockAction = inactiveAction }.build()
        )
        viewModelScope.launch {
            getProfileId()?.let { profileId ->
                repository.update(updateProfile(profileId, profileName, conditions, actions))
            } ?: repository.add(createProfile(profileName, conditions, actions))
        }
        return true
    }

    fun deleteProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            getProfileId()?.let {
                repository.remove(it)
            }
        }
    }

    private fun createProfile(
        title: String,
        conditions: Conditions,
        actions: EnterExitActions
    ) = Profile(
        System.currentTimeMillis().toString(),
        title,
        conditions,
        actions
    )

    private fun updateProfile(
        id: String,
        title: String,
        conditions: Conditions,
        actions: EnterExitActions
    ) = Profile(
        id,
        title,
        conditions,
        actions
    )

}