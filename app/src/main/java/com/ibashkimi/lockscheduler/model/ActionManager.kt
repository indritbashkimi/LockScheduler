package com.ibashkimi.lockscheduler.model

import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.model.action.Action
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.model.api.LockManager

object ActionManager {

    @Synchronized fun performAction(action: Action) {
        if (action !is LockAction)
            throw RuntimeException("Unknown actions: $action. Only LockAction is supported atm.")
        val context = App.getInstance()
        when (action.lockType) {
            LockAction.LockType.PASSWORD -> LockManager.setPassword(context, action.input)
            LockAction.LockType.PIN -> LockManager.setPin(context, action.input)
            LockAction.LockType.SWIPE -> LockManager.resetPassword(context)
            LockAction.LockType.UNCHANGED -> { /* Do nothing */
            }
        }
    }

    fun performActions(actions: List<Action>) {
        for (action in actions)
            performAction(action)
    }
}