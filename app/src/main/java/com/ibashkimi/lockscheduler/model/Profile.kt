package com.ibashkimi.lockscheduler.model

data class Profile(val id: String,
                   val name: String,
                   val conditions: List<Condition>,
                   val enterActions: List<Action>,
                   val exitActions: List<Action>) {

    //fun containsCondition(type: Int): Boolean = conditions.any { it.type == type }

    fun isActive(): Boolean = conditions.all { it.isTrue }

    fun getAction(type: Int, fromTrueActions: Boolean): Action? {
        val actions = if (fromTrueActions) enterActions else exitActions
        return actions.firstOrNull { it.type == type }
    }

    fun getCondition(type: Int): Condition? = conditions.firstOrNull { it.type == type }
}
