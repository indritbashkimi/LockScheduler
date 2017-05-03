package it.ibashkimi.lockscheduler.model

import it.ibashkimi.lockscheduler.model.source.ProfilesRepository

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */
class ConditionManager private constructor() {

    val profilesRepository: ProfilesRepository by lazy { ProfilesRepository.getInstance() }

    private object Holder { val INSTANCE = ConditionManager() }

    companion object {
        val instance: ConditionManager by lazy { Holder.INSTANCE }
    }

    fun onWifiConnectionChanged() {

    }

    fun onTimeEvent() {

    }

    fun onGeofenceEvent() {

    }

}