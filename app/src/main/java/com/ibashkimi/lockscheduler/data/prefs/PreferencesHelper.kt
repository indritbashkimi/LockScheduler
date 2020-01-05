package com.ibashkimi.lockscheduler.data.prefs

import com.ibashkimi.lockscheduler.model.action.LockAction

interface PreferencesHelper {
    var mapStyle: String

    var minPasswordLength: Int

    var minPinLength: Int

    var showNotifications: Boolean

    var notificationsRingtone: String

    var isVibrateActive: Boolean

    var bootDelay: String

    var loiteringDelay: String

    var passwordExpiration: String

    var lockAtBoot: LockAction.LockType

    var lockAtBootInput: String

    var isAdminRationaleNeeded: Boolean
}
