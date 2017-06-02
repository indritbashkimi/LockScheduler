package com.ibashkimi.lockscheduler.model

import android.content.Intent
import android.support.v4.util.ArrayMap
import android.util.Log
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.model.api.GeofenceApiHelper
import com.ibashkimi.lockscheduler.model.condition.*
import com.ibashkimi.lockscheduler.model.scheduler.*
import com.ibashkimi.lockscheduler.model.source.ProfilesDataSource
import com.ibashkimi.lockscheduler.model.source.local.DatabaseDataSource
import com.ibashkimi.lockscheduler.service.TransitionsIntentService
import java.util.*

class ProfileManager private constructor(val repository: ProfilesDataSource, val geofenceApiHelper: GeofenceApiHelper) : ConditionChangeListener, ProfileRepository {

    val cachedProfiles: ArrayMap<String, Profile> = ArrayMap()

    var isCacheDirty = true

    val priorities: IntArray = intArrayOf(Condition.Type.TIME, Condition.Type.WIFI, Condition.Type.POWER, Condition.Type.PLACE)

    val placeHandler: PlaceConditionScheduler by lazy { PlaceConditionScheduler(geofenceApiHelper, repository, this) }

    val timeHandler: TimeConditionScheduler by lazy { TimeConditionScheduler(repository, this) }

    val wifiHandler: WifiConditionScheduler by lazy { WifiConditionScheduler(repository, this) }

    val powerHandler: PowerConditionScheduler by lazy { PowerConditionScheduler(repository, this) }

    private object Holder {
        val INSTANCE = ProfileManager(DatabaseDataSource.getInstance(), GeofenceApiHelper.getInstance())
    }

    fun init() {
        for (profile in getAll()) {
            val wasActive = profile.isActive()
            for (condition in profile.conditions.orderByPriority()) {
                if (!register(profile, condition)) break
            }
            cachedProfiles[profile.id] = profile
            repository.updateProfile(profile)
            if (profile.isActive() != wasActive) {
                notifyProfileStateChanged(profile)
            }
        }
    }

    @Synchronized
    override fun add(profile: Profile) {
        Log.d(TAG, "add called with profile=$profile")
        repository.beginTransaction()
        repository.saveProfile(profile)
        cachedProfiles[profile.id] = profile
        for (condition in profile.conditions.orderByPriority()) {
            if (!register(profile, condition)) break
        }
        repository.updateProfile(profile)
        cachedProfiles[profile.id] = profile
        repository.endTransaction()
        if (profile.isActive()) {
            notifyProfileStateChanged(profile)
        }
    }

    private fun register(profile: Profile, condition: Condition): Boolean {
        when (condition) {
            is TimeCondition -> {
                return timeHandler.register(profile)
            }
            is PlaceCondition -> {
                return placeHandler.register(profile)
            }
            is WifiCondition -> {
                return wifiHandler.register(profile)
            }
            is PowerCondition -> {
                return powerHandler.register(profile)
            }
            else -> {
                throw RuntimeException("Cannot add unknown condition: $condition.")
            }
        }
    }

    override fun get(id: String): Profile? {
        if (isCacheDirty)
            refreshCache()
        return cachedProfiles[id]
    }

    override fun getAll(): List<Profile> {
        if (isCacheDirty)
            refreshCache()
        return cachedProfiles.values.toList()
    }

    private fun refreshCache() {
        cachedProfiles.clear()
        for (profile in repository.profiles)
            cachedProfiles.put(profile.id, profile)
        isCacheDirty = false
    }

    @Synchronized
    override fun update(profile: Profile) {
        val oldProfile = get(profile.id) ?:
                throw IllegalArgumentException("Cannot find profile with id=${profile.id}.")
        val wasActive = oldProfile.isActive()
        oldProfile.conditions.orderByPriority().reversed()
                .filter { it !in profile.conditions }
                .forEach { unregister(oldProfile, it) }
        if (wasActive) {
            for (condition in profile.conditions) {
                val oldCondition = oldProfile.getCondition(condition.type)
                if (oldCondition != null && oldCondition == condition)
                    condition.isTrue = oldCondition.isTrue
                else
                    condition.isTrue = false
            }
            if (!profile.isActive()) {
                oldProfile.conditions.orderByPriority().reversed().forEach { unregister(oldProfile, it) }
                profile.conditions.forEach { it.isTrue = false }
                for (condition in profile.conditions.orderByPriority()) {
                    if (!register(profile, condition)) break
                }
            }
        } else {
            oldProfile.conditions.orderByPriority().reversed().forEach { unregister(oldProfile, it) }
            for (condition in profile.conditions.orderByPriority()) {
                if (!register(profile, condition)) break
            }
        }
        repository.updateProfile(profile)
        cachedProfiles[profile.id] = profile
        if (profile.isActive() != wasActive)
            notifyProfileStateChanged(profile)
    }

    @Synchronized
    override fun remove(id: String) {
        repository.beginTransaction()
        val profile = get(id)
        if (profile != null) {
            for (condition in profile.conditions.orderByPriority().reversed()) {
                unregister(profile, condition)
            }
        } else {
            throw IllegalArgumentException("Cannot find profile with id=$id.")
        }
        repository.deleteProfile(id)
        cachedProfiles.remove(id)
        repository.endTransaction()
    }

    @Synchronized
    override fun removeAll() {
        repository.beginTransaction()
        for (profile in repository.profiles) {
            for (condition in profile.conditions.orderByPriority().reversed()) {
                unregister(profile, condition)
            }
        }
        repository.deleteProfiles()
        repository.deleteConditions()
        cachedProfiles.clear()
        repository.endTransaction()
    }

    @Synchronized
    override fun swap(profile1: Profile, profile2: Profile) {
        repository.swapProfiles(profile1.id, profile2.id)
        isCacheDirty = true
    }

    private fun unregister(profile: Profile, condition: Condition) {
        when (condition) {
            is PlaceCondition -> {
                placeHandler.unregister(profile.id)
            }
            is TimeCondition -> {
                timeHandler.unregister(profile.id)
            }
            is WifiCondition -> {
                wifiHandler.unregister(profile.id)
            }
            is PowerCondition -> {
                powerHandler.unregister(profile.id)
            }
            else -> throw RuntimeException("Cannot remove unknown condition $condition.")
        }
    }

    private fun getCondition(conditions: List<Condition>, priority: Int): Condition? {
        for (condition in conditions)
            if (condition.type == priority)
                return condition
        return null
    }

    @Synchronized
    override fun notifyConditionChanged(profile: Profile, condition: Condition, wasActive: Boolean) {
        Log.d(TAG, "notifyConditionChanged called with condition=$condition, profile=${profile.name}.")
        repository.updateProfile(profile)
        cachedProfiles[profile.id] = profile
        if (condition.isTrue) {
            for (nextCondition in getNextConditions(profile.conditions, condition))
                if (!register(profile, nextCondition)) break
        } else {
            for (nextCondition in getNextConditions(profile.conditions, condition))
                unregister(profile, nextCondition)
        }
        if (profile.isActive() != wasActive) {
            notifyProfileStateChanged(profile)
        }
    }

    private fun notifyProfileStateChanged(profile: Profile) {
        ActionManager.instance.performActions(if (profile.isActive()) profile.enterActions else profile.exitActions)
        val intent = Intent(App.getInstance(), TransitionsIntentService::class.java)
        intent.action = "profile_state_changed"
        intent.putExtra("profile_id", profile.id)
        intent.putExtra("profile_name", profile.name)
        intent.putExtra("profile_active", profile.isActive())
        App.getInstance().startService(intent)
    }

    private fun getNextConditions(conditions: List<Condition>, condition: Condition): List<Condition> {
        val resConditions: MutableList<Condition> = mutableListOf()
        for (priorityIndex in priorities.indices) {
            if (priorities[priorityIndex] == condition.type) {
                for (index in priorityIndex + 1..priorities.size - 1) {
                    val resCondition = getCondition(conditions, priorities[index])
                    resCondition?.let {
                        resConditions.add(resCondition)
                    }
                }
                break
            }
        }
        return resConditions
    }

    fun List<Condition>.orderByPriority(): List<Condition> {
        val result: ArrayList<Condition> = ArrayList(this.size)
        result.add(this.highestPriority())
        for (condition in getNextConditions(this, result[0])) {
            result.add(condition)
        }
        return result
    }

    fun List<Condition>.highestPriority(): Condition {
        if (priorities.isEmpty()) {
            throw RuntimeException("Priority list cannot be empty.")
        }
        for (priority in priorities) {
            val condition = getCondition(this, priority)
            if (condition != null)
                return condition
        }
        throw RuntimeException("Cannot find the highest priority.")
    }

    companion object {

        private val TAG = "ProfileManager"

        val instance: ProfileManager by lazy { Holder.INSTANCE }
    }
}