package com.ibashkimi.lockscheduler.addeditprofile

import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.manager.ProfileManager
import com.ibashkimi.lockscheduler.data.ProfileRepository
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.action.Action
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.model.condition.*
import com.ibashkimi.lockscheduler.model.findByType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

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
                    setPlaceCondition(conditions.findByType(Condition.Type.PLACE))
                    setPowerCondition(conditions.findByType(Condition.Type.POWER))
                    setTimeCondition(conditions.findByType(Condition.Type.TIME))
                    setWifiCondition(conditions.findByType(Condition.Type.WIFI))
                    setWhenActiveLockAction(enterActions.findByType(Action.Type.LOCK))
                    setWhenInactiveLockAction(exitActions.findByType(Action.Type.LOCK))
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
        val conditions = EnumMap<Condition.Type, Condition>(Condition.Type::class.java)
        conditions.apply {
            getPlaceCondition().value?.let { this[Condition.Type.PLACE] = it }
            getPowerCondition().value?.let { this[Condition.Type.POWER] = it }
            getTimeCondition().value?.let { this[Condition.Type.TIME] = it }
            getWifiCondition().value?.let { this[Condition.Type.WIFI] = it }
        }
        if (conditions.values.filterNotNull().isEmpty()) {
            Toast.makeText(App.getInstance(), "Add at least one condition", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        val profileName: String =
            getProfileName().value ?: App.getInstance().getString(R.string.default_profile_name)

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

        val enterActions = EnumMap<Action.Type, Action>(Action.Type::class.java)
        enterActions.apply {
            getWhenActiveLockAction().value?.let { this[Action.Type.LOCK] = it }
        }

        val exitActions = EnumMap<Action.Type, Action>(Action.Type::class.java)
        exitActions.apply {
            getWhenInactiveLockAction().value?.let { this[Action.Type.LOCK] = it }
        }

        viewModelScope.launch {
            getProfileId()?.let { profileId ->
                repository.update(
                    updateProfile(
                        profileId,
                        profileName,
                        conditions,
                        enterActions,
                        exitActions
                    )
                )
            } ?: repository.add(createProfile(profileName, conditions, enterActions, exitActions))
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
        conditions: Map<Condition.Type, Condition>,
        enterActions: Map<Action.Type, Action>,
        exitActions: Map<Action.Type, Action>
    ) = Profile(
        System.currentTimeMillis().toString(),
        title,
        conditions,
        enterActions,
        exitActions
    )

    private fun updateProfile(
        id: String,
        title: String,
        conditions: Map<Condition.Type, Condition>,
        enterActions: Map<Action.Type, Action>,
        exitActions: Map<Action.Type, Action>
    ) = Profile(
        id,
        title,
        conditions,
        enterActions,
        exitActions
    )

}