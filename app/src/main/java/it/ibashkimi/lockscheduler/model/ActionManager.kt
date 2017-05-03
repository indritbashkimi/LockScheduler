package it.ibashkimi.lockscheduler.model

import it.ibashkimi.lockscheduler.App
import it.ibashkimi.lockscheduler.model.api.LockManager

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */
class ActionManager private constructor() {

    val lockManager: LockManager by lazy { App.getLockManager() }

    private object Holder {
        val INSTANCE = ActionManager()
    }

    companion object {
        val instance: ActionManager by lazy { Holder.INSTANCE }
    }

    @Synchronized fun performAction(action: Action) {
        if (action !is LockAction)
            throw RuntimeException("Unknown actions: $action. Only LockAction is supported atm.")
        when (action.lockMode.lockType) {
            LockMode.LockType.PASSWORD -> lockManager.setPassword(action.lockMode.input)
            LockMode.LockType.PIN -> lockManager.setPin(action.lockMode.input)
            LockMode.LockType.SWIPE -> lockManager.resetPassword()
            LockMode.LockType.UNCHANGED -> { /* Do nothing */
            }
        }
    }

    fun performActions(actions: List<Action>) {
        for (action in actions)
            performAction(action)
    }
}