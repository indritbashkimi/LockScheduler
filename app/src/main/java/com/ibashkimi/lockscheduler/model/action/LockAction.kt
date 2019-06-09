package com.ibashkimi.lockscheduler.model.action

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LockAction(val lockMode: LockMode) : Action(Type.LOCK), Parcelable {

    enum class LockType(val value: String) {
        PIN("pin"), PASSWORD("password"), SWIPE("swipe"), UNCHANGED("unchanged")
    }

    sealed class LockMode(val lockType: LockType) : Parcelable {
        @Parcelize
        data class Pin(val input: String) : LockMode(LockType.PIN)

        @Parcelize
        data class Password(val input: String) : LockMode(LockType.PASSWORD)

        @Parcelize
        object Swipe : LockMode(LockType.SWIPE)

        @Parcelize
        object Unchanged : LockMode(LockType.UNCHANGED)
    }
}
