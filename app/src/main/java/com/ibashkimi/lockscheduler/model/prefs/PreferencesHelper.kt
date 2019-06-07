package com.ibashkimi.lockscheduler.model.prefs

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

    var lockAtBoot: Int

    var lockAtBootInput: String

    var isAdminRationaleNeeded: Boolean
}
