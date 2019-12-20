package com.ibashkimi.lockscheduler.model

import com.ibashkimi.lockscheduler.model.action.Action
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.model.condition.*

data class Profile(
    val id: String,
    val name: String,
    val conditions: Conditions,
    val enterExitActions: EnterExitActions
) {

    fun isActive(): Boolean = conditions.areAllTriggered()
}

data class EnterExitActions(val enterActions: Actions, val exitActions: Actions)

data class Actions(val actions: List<Action>) {

    val lockAction: LockAction?
        get() = actionOf(Action.Type.LOCK) as? LockAction?

    fun actionOf(type: Action.Type): Action? {
        return actions.firstOrNull { it.type == type }
    }

    class Builder {
        var lockAction: LockAction? = null

        fun build(): Actions {
            val actions = ArrayList<Action>()
            lockAction?.let { actions.add(it) }
            return Actions(actions)
        }
    }
}

data class Conditions(val conditions: List<Condition>) {

    val all: List<Condition>
        get() = conditions

    fun isEmpty(): Boolean = conditions.isEmpty()

    fun areAllTriggered(): Boolean {
        return conditions.all { it.isTriggered }
    }

    fun of(type: Condition.Type): Condition? {
        return conditions.firstOrNull { it.type == type }
    }

    val placeCondition: PlaceCondition?
        get() = of(Condition.Type.PLACE) as? PlaceCondition?

    val powerCondition: PowerCondition?
        get() = of(Condition.Type.POWER) as? PowerCondition?

    val timeCondition: TimeCondition?
        get() = of(Condition.Type.TIME) as? TimeCondition?

    val wifiCondition: WifiCondition?
        get() = of(Condition.Type.WIFI) as? WifiCondition?

    class Builder {
        var placeCondition: PlaceCondition? = null

        var powerCondition: PowerCondition? = null

        var timeCondition: TimeCondition? = null

        var wifiCondition: WifiCondition? = null

        fun build(): Conditions {
            val conditions = ArrayList<Condition>()
            placeCondition?.let { conditions.add(it) }
            powerCondition?.let { conditions.add(it) }
            timeCondition?.let { conditions.add(it) }
            wifiCondition?.let { conditions.add(it) }
            return Conditions(conditions)
        }
    }
}
