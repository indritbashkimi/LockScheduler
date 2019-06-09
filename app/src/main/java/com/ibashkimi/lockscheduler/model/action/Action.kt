package com.ibashkimi.lockscheduler.model.action

open class Action(val type: Type) {

    enum class Type(val value: String) {
        LOCK("lock")
    }
}


