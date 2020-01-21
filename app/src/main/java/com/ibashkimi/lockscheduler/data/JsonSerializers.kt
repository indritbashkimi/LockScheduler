package com.ibashkimi.lockscheduler.data

import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.action.Action
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.model.condition.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

fun Profile.toJson(): JSONObject = JSONObject()
    .put("id", "" + id)
    .put("name", name)
    .put("enter_actions", enterActions.toActionsJson())
    .put("exit_actions", exitActions.toActionsJson())
    .put("conditions", conditions.toJson())


fun String.toProfile() = JSONObject(this).toProfile()

fun JSONObject.toProfile(): Profile {
    return Profile(
        getString("id"),
        getString("name"),
        getJSONObject("conditions").toConditions(),
        getJSONObject("enter_actions").toActions(),
        getJSONObject("exit_actions").toActions()
    )
}

fun Map<Action.Type, Action>.toActionsJson(): JSONObject {
    val jsonObject = JSONObject()
    jsonObject.put("actions_len", this.size)
    val actions = this.values.toList()
    for (i in 0 until this.size) {
        jsonObject.put("action_$i", actions[i].toJson())
    }
    return jsonObject
}

fun JSONObject.toActions(): Map<Action.Type, Action> {
    val size = getInt("actions_len")
    val actions = EnumMap<Action.Type, Action>(Action.Type::class.java)
    for (i in 0 until size) {
        val actionJson = getJSONObject("action_$i")
        val type = Action.Type.valueOf(actionJson.getString("type"))
        val action: Action = when (type) {
            Action.Type.LOCK -> actionJson.toLockAction()
        }
        actions[type] = (action)
    }
    return actions
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

fun Map<Condition.Type, Condition>.toJson(): JSONObject {
    val jsonObject = JSONObject()
    jsonObject.put("conditions_len", this.size)
    val conditions = this.values.toList()
    for (i in 0 until this.size) {
        jsonObject.put("condition_$i", conditions[i].toJson())
    }
    return jsonObject
}

fun JSONObject.toConditions(): Map<Condition.Type, Condition> {
    val size = getInt("conditions_len")
    val conditions = EnumMap<Condition.Type, Condition>(Condition.Type::class.java)
    for (i in 0 until size) {
        val conditionJson = getJSONObject("condition_$i")
        val type = Condition.Type.valueOf(conditionJson.getString("type"))
        val condition: Condition = when (type) {
            Condition.Type.PLACE -> conditionJson.toPlaceCondition()
            Condition.Type.TIME -> conditionJson.toTimeCondition()
            Condition.Type.WIFI -> conditionJson.toWifiCondition()
            Condition.Type.POWER -> conditionJson.toPowerCondition()
        }
        conditions[type] = condition
    }
    return conditions
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
    for (i in wifiList.indices) {
        json.put("wifi_item_ssid$i", wifiList[i].ssid)
        json.put("wifi_item_bssid$i", wifiList[i].bssid)
    }
    return json
}

fun JSONObject.toWifiCondition(): WifiCondition {
    val wifiItemSize = getInt("wifi_items_len")
    val items = ArrayList<WifiItem>(wifiItemSize)
    for (i in 0 until wifiItemSize) {
        val ssid = getString("wifi_item_ssid$i")
        val bssid = getString("wifi_item_bssid$i")
        val item = WifiItem(ssid, bssid)
        items.add(item)
    }
    return WifiCondition(items, getBoolean("triggered"))
}