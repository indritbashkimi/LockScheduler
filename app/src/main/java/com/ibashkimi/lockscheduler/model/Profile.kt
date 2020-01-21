package com.ibashkimi.lockscheduler.model

import com.ibashkimi.lockscheduler.model.action.Action
import com.ibashkimi.lockscheduler.model.condition.Condition
import kotlin.reflect.KClass

typealias Conditions = Map<Condition.Type, Condition>

typealias Actions = Map<Action.Type, Action>

data class Profile(
    val id: String,
    val name: String,
    val conditions: Conditions,
    val enterActions: Actions,
    val exitActions: Actions
) {

    fun isActive(): Boolean = conditions.values.all { it.isTriggered }
}

inline fun <reified T : Condition> Profile.findCondition(): T? {
    return conditions.findByClass(T::class)
}

fun Map<Condition.Type, Condition>.asList(): List<Condition> {
    return this.values.toList()
}

fun <T : Condition?> Map<Condition.Type, Condition>.findByType(type: Condition.Type): T? {
    return this[type] as T?
}

fun <T : Condition?> Map<Condition.Type, Condition>.findByClass(kClass: KClass<*>?): T? {
    return values.firstOrNull { it::class == kClass } as? T?
}

inline fun <reified T : Condition> Map<Condition.Type, Condition>.find(): T? {
    return findByClass(T::class)
}

fun <T : Action?> Actions.findByType(type: Action.Type): T? {
    return this[type] as T?
}

fun <T : Action?> Actions.findByClass(kClass: KClass<*>?): T? {
    return values.firstOrNull { it::class == kClass } as? T?
}

inline fun <reified T : Action> Actions.find(): T? {
    return findByClass(T::class)
}

