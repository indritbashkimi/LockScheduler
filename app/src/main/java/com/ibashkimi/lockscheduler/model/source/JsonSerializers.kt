package com.ibashkimi.lockscheduler.model.source

import com.ibashkimi.lockscheduler.model.Actions
import com.ibashkimi.lockscheduler.model.Conditions
import com.ibashkimi.lockscheduler.model.EnterExitActions
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.action.Action
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.model.condition.*
import org.json.JSONObject

fun Profile.toJson(): JSONObject {
    return JSONObject()
        .put("id", "" + id)
        .put("name", name)
        .put("actions", enterExitActions.toJson())
        .put("conditions", conditions.toJson())
}

fun String.toProfile() = JSONObject(this).toProfile()

fun JSONObject.toProfile(): Profile {
    return Profile(
        getString("id"),
        getString("name"),
        getJSONObject("conditions").toConditions(),
        getJSONObject("actions").toEnterExitActions()
    )
}

fun EnterExitActions.toJson(): JSONObject {
    return JSONObject()
        .put("enter_actions", enterActions.toJson())
        .put("exit_actions", exitActions.toJson())
}

fun JSONObject.toEnterExitActions(): EnterExitActions {
    return EnterExitActions(
        getJSONObject("enter_actions").toActions(),
        getJSONObject("exit_actions").toActions()
    )
}

fun Actions.toJson(): JSONObject {
    val jsonObject = JSONObject()
    jsonObject.put("actions_len", actions.size)
    for (i in 0 until actions.size) {
        jsonObject.put("action_$i", actions[i].toJson())
    }
    return jsonObject
}

fun JSONObject.toActions(): Actions {
    val size = getInt("actions_len")
    val actions = ArrayList<Action>(size)
    for (i in 0 until size) {
        val actionJson = getJSONObject("action_$i")
        val action: Action = when (Action.Type.valueOf(actionJson.getString("type"))) {
            Action.Type.LOCK -> actionJson.toLockAction()
        }
        actions.add(action)
    }
    return Actions(actions)
}

fun Action.toJson(): JSONObject {
    return when (this) {
        is LockAction -> this.toJson()
        else -> throw IllegalArgumentException("Unknown action: ${this::class.java.simpleName}.")
    }
}

fun LockAction.toJson(): JSONObject {
    val json = JSONObject()
        .put("type", type)
        .put("lockType", lockMode.lockType.name)
    when (lockMode) {
        is LockAction.LockMode.Pin -> {
            json.put("input", lockMode.input)
        }
        is LockAction.LockMode.Password -> {
            json.put("input", lockMode.input)
        }
    }
    return json
}

fun JSONObject.toLockAction(): LockAction {
    val lockMode = when (LockAction.LockType.valueOf(getString("lockType"))) {
        LockAction.LockType.PASSWORD -> LockAction.LockMode.Password(getString("input"))
        LockAction.LockType.PIN -> LockAction.LockMode.Pin(getString("input"))
        LockAction.LockType.SWIPE -> LockAction.LockMode.Swipe
        LockAction.LockType.UNCHANGED -> LockAction.LockMode.Unchanged
    }
    return LockAction(lockMode)
}

fun Conditions.toJson(): JSONObject {
    val jsonObject = JSONObject()
    jsonObject.put("conditions_len", all.size)
    for (i in 0 until all.size) {
        jsonObject.put("condition_$i", all[i].toJson())
    }
    return jsonObject
}

fun JSONObject.toConditions(): Conditions {
    val size = getInt("conditions_len")
    val conditions = ArrayList<Condition>(size)
    for (i in 0 until size) {
        val conditionJson = getJSONObject("condition_$i")
        val condition: Condition = when (Condition.Type.valueOf(conditionJson.getString("type"))) {
            Condition.Type.PLACE -> conditionJson.toPlaceCondition()
            Condition.Type.TIME -> conditionJson.toTimeCondition()
            Condition.Type.WIFI -> conditionJson.toWifiCondition()
            Condition.Type.POWER -> conditionJson.toPowerCondition()
        }
        conditions.add(condition)
    }
    return Conditions(conditions)
}

fun Condition.toJson(): JSONObject {
    return when (this) {
        is PlaceCondition -> this.toJson()
        is PowerCondition -> this.toJson()
        is TimeCondition -> this.toJson()
        is WifiCondition -> this.toJson()
        else -> throw IllegalArgumentException("Unknown condition: ${this::class.java.simpleName}.")
    }
}

fun PlaceCondition.toJson(): JSONObject {
    return JSONObject()
        .put("type", type.name)
        .put("triggered", isTriggered)
        .put("latitude", latitude)
        .put("longitude", longitude)
        .put("radius", radius)
        .put("address", address)
}

fun JSONObject.toPlaceCondition() = PlaceCondition(
    isTriggered = getBoolean("triggered"),
    latitude = getDouble("latitude"),
    longitude = getDouble("longitude"),
    radius = getInt("radius"),
    address = getString("address")
)

fun TimeCondition.toJson(): JSONObject {
    val jsonObject = JSONObject()
        .put("type", type.name)
        .put("triggered", isTriggered)
        .put("start_time_hour", startTime.hour)
        .put("start_time_minute", startTime.minute)
        .put("end_time_hour", endTime.hour)
        .put("end_time_minute", endTime.minute)
    for (i in 0..6) {
        jsonObject.put("day_$i", daysActive[i])
    }
    return jsonObject
}

fun JSONObject.toTimeCondition(): TimeCondition {
    val daysActive = DaysOfWeek()
    for (i in 0..6) {
        daysActive[i] = getBoolean("day_$i")
    }
    val startTime = Time(getInt("start_time_hour"), getInt("start_time_minute"))
    val endTime = Time(getInt("end_time_hour"), getInt("end_time_minute"))
    return TimeCondition(daysActive, startTime, endTime, getBoolean("triggered"))
}

fun PowerCondition.toJson(): JSONObject {
    return JSONObject()
        .put("type", type.name)
        .put("triggered", isTriggered)
        .put("power_connected", powerConnected)
}

fun JSONObject.toPowerCondition() =
    PowerCondition(getBoolean("power_connected"), getBoolean("triggered"))

fun WifiCondition.toJson(): JSONObject {
    val json = JSONObject()
        .put("type", type.name)
        .put("triggered", isTriggered)
        .put("wifi_items_len", wifiList.size)
    for (i in 0 until wifiList.size) {
        json.put("wifi_item_$i", wifiList[i].ssid)
    }
    return json
}

fun JSONObject.toWifiCondition(): WifiCondition {
    val wifiItemSize = getInt("wifi_items_len")
    val items = ArrayList<WifiItem>(wifiItemSize)
    for (i in 0 until wifiItemSize) {
        val ssid = getString("wifi_item_$i")
        val item = WifiItem(ssid)
        items.add(item)
    }
    return WifiCondition(items, getBoolean("triggered"))
}