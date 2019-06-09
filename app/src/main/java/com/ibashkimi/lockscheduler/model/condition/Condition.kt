package com.ibashkimi.lockscheduler.model.condition

open class Condition(open val type: Type, open var isTriggered: Boolean) {
    enum class Type(val value: String) {
        PLACE("place"), TIME("time"), WIFI("wifi"), POWER("power")
    }
}
