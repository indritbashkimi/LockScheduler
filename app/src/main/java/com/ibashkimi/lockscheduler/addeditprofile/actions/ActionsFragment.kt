package com.ibashkimi.lockscheduler.addeditprofile.actions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.model.Actions
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.util.*

class ActionsFragment : Fragment() {
    private var lockType = LockAction.LockType.UNCHANGED

    private var input: String? = null

    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    private var lockTypeIfGranted: String
        get() = sharedPreferences.getString("lock_if_granted", LockAction.LockType.UNCHANGED.value)!!
        set(value) = sharedPreferences.edit().putString("lock_if_granted", value).apply()

    private var isEnter = false

    fun setData(actions: Actions) {
        val action: LockAction = actions.lockAction!!
        lockType = action.lockMode.lockType
        input = when(action.lockMode) {
            is LockAction.LockMode.Pin -> action.lockMode.input
            is LockAction.LockMode.Password -> action.lockMode.input
            else -> null
        }
    }

    fun assembleData(): Actions {
        return Actions.Builder().apply {
            lockAction = LockAction(when (lockType) {
                LockAction.LockType.PIN -> LockAction.LockMode.Pin(input!!)
                LockAction.LockType.PASSWORD -> LockAction.LockMode.Password(input!!)
                LockAction.LockType.SWIPE -> LockAction.LockMode.Swipe
                LockAction.LockType.UNCHANGED -> LockAction.LockMode.Unchanged
            })
        }.build()
    }

    private lateinit var lockSummary: TextView

    private lateinit var lockSettings: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEnter = arguments!!.getBoolean("is_enter")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_actions, container, false) as ViewGroup
        lockSummary = rootView.findViewById(R.id.lockSummary)
        lockSettings = rootView.findViewById(R.id.lockSettings)
        if (savedInstanceState != null) {
            val savedLockType = savedInstanceState.getString("enter_lock_type", LockAction.LockType.UNCHANGED.value)!!
            lockType = LockAction.LockType.valueOf(savedLockType)
            input = savedInstanceState.getString("enter_input")
        }
        val titleView: TextView = rootView.findViewById(R.id.title)
        titleView.setText(if (isEnter) R.string.title_condition_enter else R.string.title_condition_exit)
        lockSettings.setOnClickListener {
            showPasswordDialog(lockType) { which -> onLockTypeSelected(positionToLockType(which)) }
        }
        updateSummary()
        return rootView
    }

    private fun onLockTypeSelected(lockType: LockAction.LockType) {
        if (lockType == LockAction.LockType.UNCHANGED) {
            this.lockType = LockAction.LockType.UNCHANGED
            updateSummary()
        } else {
            checkAdminPermission(
                    onGranted = {
                        when (lockType) {
                            LockAction.LockType.PIN -> showPinChooser(REQUEST_PIN)
                            LockAction.LockType.PASSWORD -> showPasswordChooser(REQUEST_PASSWORD)
                            LockAction.LockType.SWIPE -> {
                                this.lockType = LockAction.LockType.SWIPE
                                updateSummary()
                            }
                            else -> throw IllegalStateException("Unknown lock type $lockType.")
                        }
                    },
                    onRationaleNeeded = {
                        lockTypeIfGranted = lockType.value
                        showAdminPermissionRationale(
                                onOk = { askAdminPermission(REQUEST_ADMIN_PERMISSION) },
                                onCancel = { onAdminPermissionDenied() }
                        )
                    },
                    onDenied = {
                        lockTypeIfGranted = lockType.value
                        askAdminPermission(REQUEST_ADMIN_PERMISSION)
                    }
            )
        }
    }

    private fun updateSummary() {
        lockSummary.setText(lockTypeToTextRes(lockType))
    }

    private fun onAdminPermissionDenied() {
        Toast.makeText(context, "Admin permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("enter_lock_type", lockType.value)
        outState.putString("enter_input", input)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PIN -> if (resultCode == Activity.RESULT_OK) {
                lockType = LockAction.LockType.PIN
                input = data!!.getStringExtra("input")
                updateSummary()
            }
            REQUEST_PASSWORD -> if (resultCode == Activity.RESULT_OK) {
                lockType = LockAction.LockType.PASSWORD
                input = data!!.getStringExtra("input")
                updateSummary()
            }
            REQUEST_ADMIN_PERMISSION -> handleAdminPermissionResult(
                    resultCode = resultCode,
                    onGranted = {
                        Toast.makeText(context, "Admin permission granted", Toast.LENGTH_SHORT).show()
                        when (lockTypeIfGranted) {
                            LockAction.LockType.PIN.value -> showPinChooser(REQUEST_PASSWORD)
                            LockAction.LockType.PASSWORD.value -> showPasswordChooser(REQUEST_PIN)
                        }
                    },
                    onDenied = { onAdminPermissionDenied() })
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val REQUEST_PIN = 1
        private const val REQUEST_PASSWORD = 2
        private const val REQUEST_ADMIN_PERMISSION = 3

        fun newInstance(isEnter: Boolean): ActionsFragment {
            val fragment = ActionsFragment()
            val args = Bundle()
            args.putBoolean("is_enter", isEnter)
            fragment.arguments = args
            return fragment
        }
    }
}
