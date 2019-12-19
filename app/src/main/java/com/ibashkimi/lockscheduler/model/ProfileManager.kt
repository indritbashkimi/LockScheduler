package com.ibashkimi.lockscheduler.model

import android.content.Intent
import androidx.collection.ArrayMap
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.model.api.GeofenceApiHelper
import com.ibashkimi.lockscheduler.model.condition.*
import com.ibashkimi.lockscheduler.model.scheduler.*
import com.ibashkimi.lockscheduler.model.source.ProfilesDataSource
import com.ibashkimi.lockscheduler.model.source.local.DatabaseDataSource
import com.ibashkimi.lockscheduler.service.TransitionsIntentService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.util.*
import kotlin.collections.HashSet

object ProfileManager : ConditionChangeListener, ProfileRepository {

    private val repository: ProfilesDataSource = DatabaseDataSource

    private val geofenceApiHelper: GeofenceApiHelper = GeofenceApiHelper.getInstance()

    private val cachedProfiles: ArrayMap<String, Profile> = ArrayMap()

    private val priorities: List<Condition.Type> = listOf(Condition.Type.TIME, Condition.Type.WIFI, Condition.Type.POWER, Condition.Type.PLACE)

    var isCacheDirty = true

    private val dataChangedObservers = HashSet<(Long) -> Unit>()

    val placeHandler: PlaceConditionScheduler by lazy { PlaceConditionScheduler(geofenceApiHelper, repository, this) }

    val timeHandler: TimeConditionScheduler by lazy { TimeConditionScheduler(repository, this) }

    val wifiHandler: WifiConditionScheduler by lazy { WifiConditionScheduler(repository, this) }

    val powerHandler: PowerConditionScheduler by lazy { PowerConditionScheduler(repository, this) }

    fun init() {
        for (profile in getAll()) {
            val wasActive = profile.isActive()
            for (condition in profile.conditions.all.orderByPriority()) {
                if (!register(profile, condition)) break
            }
            cachedProfiles[profile.id] = profile
            repository.updateProfile(profile)
            if (profile.isActive() != wasActive) {
                notifyProfileStateChanged(profile)
            }
        }
    }

    private fun registerDatabaseObserver(observer: (Long) -> Unit) {
        dataChangedObservers.add(observer)
    }

    private fun unregisterDatabaseObserver(observer: (Long) -> Unit) {
        dataChangedObservers.remove(observer)
    }

    @Synchronized
    override fun add(profile: Profile) {
        repository.beginTransaction()
        repository.saveProfile(profile)
        cachedProfiles[profile.id] = profile
        for (condition in profile.conditions.all.orderByPriority()) {
            if (!register(profile, condition)) break
        }
        repository.updateProfile(profile)
        cachedProfiles[profile.id] = profile
        repository.endTransaction()
        if (profile.isActive()) {
            notifyProfileStateChanged(profile)
        }
        notifyChanged()
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

    private fun refreshedCachedProfiles(): List<Profile> {
        if (isCacheDirty)
            refreshCache()
        return cachedProfiles.values.toList()
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

    fun getProfilesFlow(): Flow<List<Profile>> = callbackFlow {
        val observer: (Long) -> Unit = {
            offer(refreshedCachedProfiles())
        }
        registerDatabaseObserver(observer)
        offer(refreshedCachedProfiles())
        awaitClose { unregisterDatabaseObserver(observer) }
    }.flowOn(Dispatchers.IO)

    private fun refreshCache() {
        cachedProfiles.clear()
        for (profile in repository.profiles)
            cachedProfiles[profile.id] = profile
        isCacheDirty = false
    }

    private fun notifyChanged() {
        val lastChanged = System.currentTimeMillis()
        dataChangedObservers.forEach { it(lastChanged) }
    }

    @Synchronized
    override fun update(profile: Profile) {
        val oldProfile = get(profile.id) ?:
                throw IllegalArgumentException("Cannot find profile with id=${profile.id}.")
        val wasActive = oldProfile.isActive()
        oldProfile.conditions.conditions.orderByPriority().reversed()
                .filter { it !in profile.conditions.all }
                .forEach { unregister(oldProfile, it) }
        if (wasActive) {
            for (condition in profile.conditions.all) {
                val oldCondition = oldProfile.conditions.of(condition.type)
                if (oldCondition != null && oldCondition == condition)
                    condition.isTriggered = oldCondition.isTriggered
                else
                    condition.isTriggered = false
            }
            if (!profile.isActive()) {
                oldProfile.conditions.all.orderByPriority().reversed().forEach { unregister(oldProfile, it) }
                profile.conditions.all.forEach { it.isTriggered = false }
                for (condition in profile.conditions.all.orderByPriority()) {
                    if (!register(profile, condition)) break
                }
            }
        } else {
            oldProfile.conditions.all.orderByPriority().reversed().forEach { unregister(oldProfile, it) }
            for (condition in profile.conditions.all.orderByPriority()) {
                if (!register(profile, condition)) break
            }
        }
        repository.updateProfile(profile)
        cachedProfiles[profile.id] = profile
        if (profile.isActive() != wasActive)
            notifyProfileStateChanged(profile)
        notifyChanged()
    }

    @Synchronized
    override fun remove(id: String) {
        repository.beginTransaction()
        val profile = get(id)
        if (profile != null) {
            for (condition in profile.conditions.all.orderByPriority().reversed()) {
                unregister(profile, condition)
            }
        } else {
            throw IllegalArgumentException("Cannot find profile with id=$id.")
        }
        repository.deleteProfile(id)
        cachedProfiles.remove(id)
        repository.endTransaction()
        notifyChanged()
    }

    @Synchronized
    override fun removeAll() {
        repository.beginTransaction()
        for (profile in repository.profiles) {
            for (condition in profile.conditions.all.orderByPriority().reversed()) {
                unregister(profile, condition)
            }
        }
        repository.deleteProfiles()
        repository.deleteConditions()
        cachedProfiles.clear()
        repository.endTransaction()
        notifyChanged()
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

    private fun getCondition(conditions: List<Condition>, priority: Condition.Type): Condition? {
        for (condition in conditions)
            if (condition.type == priority)
                return condition
        return null
    }

    @Synchronized
    override fun notifyConditionChanged(profile: Profile, condition: Condition, wasActive: Boolean) {
        repository.updateProfile(profile)
        cachedProfiles[profile.id] = profile
        if (condition.isTriggered) {
            for (nextCondition in getNextConditions(profile.conditions.conditions, condition))
                if (!register(profile, nextCondition)) break
        } else {
            for (nextCondition in getNextConditions(profile.conditions.conditions, condition))
                unregister(profile, nextCondition)
        }
        if (profile.isActive() != wasActive) {
            notifyProfileStateChanged(profile)
        }
    }

    private fun notifyProfileStateChanged(profile: Profile) {
        ActionManager.performActions(if (profile.isActive()) profile.enterExitActions.enterActions.actions else profile.enterExitActions.exitActions.actions)
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
                for (index in priorityIndex + 1 until priorities.size) {
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

    private fun List<Condition>.orderByPriority(): List<Condition> {
        val result: ArrayList<Condition> = ArrayList(this.size)
        result.add(this.highestPriority())
        for (condition in getNextConditions(this, result[0])) {
            result.add(condition)
        }
        return result
    }

    private fun List<Condition>.highestPriority(): Condition {
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
}