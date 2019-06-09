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
        when (action.lockMode) {
            is LockAction.LockMode.Password -> LockManager.setPassword(context, action.lockMode.input)
            is LockAction.LockMode.Pin -> LockManager.setPin(context, action.lockMode.input)
            is LockAction.LockMode.Swipe -> LockManager.resetPassword(context)
            is LockAction.LockMode.Unchanged -> { /* Do nothing */
            }
        }
    }

    fun performActions(actions: List<Action>) {
        for (action in actions)
            performAction(action)
    }
}