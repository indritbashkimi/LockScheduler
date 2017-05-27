package com.ibashkimi.lockscheduler.addeditprofile.actions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.model.Action
import com.ibashkimi.lockscheduler.model.LockAction
import com.ibashkimi.lockscheduler.model.api.AdminApiHelper
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper
import java.util.*

class ActionsFragment : Fragment() {
    @LockAction.LockType
    private var lockType = LockAction.LockType.UNCHANGED
    private var input: String? = null

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    private var lockTypeIfGranted: Int
        get() = sharedPreferences.getInt("lock_if_granted", LockAction.LockType.UNCHANGED)
        set(value) = sharedPreferences.edit().putInt("lock_if_granted", value).apply()

    private val adminApiHelper: AdminApiHelper by lazy { AdminApiHelper(context) }

    private var isEnter = false

    fun setData(actions: List<Action>) {
        val action = actions[0] as LockAction
        lockType = action.lockType
        if (lockType == LockAction.LockType.PIN || lockType == LockAction.LockType.PASSWORD)
            input = action.input
    }

    fun assembleData(): List<Action> {
        val actions = ArrayList<Action>(1)
        val action = LockAction(lockType)
        if (lockType == LockAction.LockType.PIN || lockType == LockAction.LockType.PASSWORD)
            action.input = input
        actions.add(action)
        return actions
    }

    private var lockSummary: TextView? = null

    private var lockSettings: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEnter = arguments.getBoolean("is_enter")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_actions, container, false) as ViewGroup
        if (savedInstanceState != null) {
            lockType = savedInstanceState.getInt("enter_lock_type", LockAction.LockType.UNCHANGED)
            input = savedInstanceState.getString("enter_input")
        }
        val titleView = rootView.findViewById(R.id.title) as TextView
        titleView.setText(if (isEnter) R.string.title_condition_enter else R.string.title_condition_exit)
        lockSummary = rootView.findViewById(R.id.lockSummary) as TextView
        lockSettings = rootView.findViewById(R.id.lockSettings) as View
        lockSettings!!.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val items = resources.getStringArray(R.array.lock_types)
            val selectedItem = when(lockType) {
                LockAction.LockType.UNCHANGED -> 0
                LockAction.LockType.SWIPE -> 1
                LockAction.LockType.PIN -> 2
                LockAction.LockType.PASSWORD -> 3
                else -> throw IllegalArgumentException("Cannot determine selected item.")
            }
            builder.setTitle(R.string.dialog_lock_settings_title)
                    .setSingleChoiceItems(items, selectedItem, { dialog, which ->
                        val selected = when (which) {
                            0 -> LockAction.LockType.UNCHANGED
                            1 -> LockAction.LockType.SWIPE
                            2 -> LockAction.LockType.PIN
                            3 -> LockAction.LockType.PASSWORD
                            else -> throw IllegalStateException("Cannot determine lock type. which=$which.")
                        }
                        if (selected != LockAction.LockType.UNCHANGED && !adminPermissionGranted()) {
                            lockTypeIfGranted = selected
                            if (shouldShowAdminPermissionRationale())
                                showAdminPermissionRationale()
                            else
                                askAdminPermission()
                        } else {
                            when (selected) {
                                LockAction.LockType.UNCHANGED -> {
                                    lockType = LockAction.LockType.UNCHANGED
                                    updateSummary()
                                }
                                LockAction.LockType.PIN -> showPinChooser()
                                LockAction.LockType.PASSWORD -> showPasswordChooser()
                                LockAction.LockType.SWIPE -> {
                                    lockType = LockAction.LockType.SWIPE
                                    updateSummary()
                                }
                                else -> throw IllegalStateException("Unknown lock type $selected.")
                            }
                        }
                        updateSummary()
                        dialog.dismiss()
                    })
            builder.create().show()
        }
        updateSummary()
        return rootView
    }

    private fun updateSummary() {
        val textResId = when (lockType) {
            LockAction.LockType.UNCHANGED -> R.string.lock_mode_nothing
            LockAction.LockType.PIN -> R.string.lock_mode_pin
            LockAction.LockType.PASSWORD -> R.string.lock_mode_password
            LockAction.LockType.SWIPE -> R.string.lock_mode_swipe
            else -> throw IllegalStateException("Cannot determine summary.")
        }
        lockSummary!!.setText(textResId)
    }

    private fun adminPermissionGranted(): Boolean {
        return adminApiHelper.isAdminActive
    }

    private fun shouldShowAdminPermissionRationale(): Boolean {
        return sharedPreferences.getBoolean("show_admin_permission_rationale", false)
    }

    private fun showAdminPermissionRationale() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.admin_permission_rationale_title)
                .setMessage(R.string.admin_permission_rationale)
                .setPositiveButton(R.string.ok) { _, _ -> askAdminPermission() }
                .setNegativeButton(R.string.cancel) { _, _ -> onAdminPermissionDenied() }
        builder.create().show()
    }

    private fun askAdminPermission() {
        startActivityForResult(adminApiHelper.buildAddAdminIntent(), REQUEST_ADMIN_PERMISSION)
    }

    private fun onAdminPermissionDenied() {
        Toast.makeText(context, "Admin permission denied", Toast.LENGTH_SHORT).show()
    }

    private fun onAdminPermissionGranted() {
        Toast.makeText(context, "Admin permission granted", Toast.LENGTH_SHORT).show()
        when (lockTypeIfGranted) {
            LockAction.LockType.PIN -> showPinChooser()
            LockAction.LockType.PASSWORD -> showPasswordChooser()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.let {
            outState.putInt("enter_lock_type", lockType)
            outState.putString("enter_input", input)
        }
    }

    private fun showPasswordChooser() {
        val intent = Intent(context, PinChooserActivity::class.java)
        intent.putExtra("type", "password")
        intent.putExtra("min_length", AppPreferencesHelper.minPasswordLength)
        startActivityForResult(intent, REQUEST_PASSWORD)
    }

    private fun showPinChooser() {
        val intent = Intent(context, PinChooserActivity::class.java)
        intent.putExtra("type", "pin")
        intent.putExtra("min_length", AppPreferencesHelper.minPinLength)
        startActivityForResult(intent, REQUEST_PIN)
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
            REQUEST_ADMIN_PERMISSION -> if (resultCode == Activity.RESULT_CANCELED) {
                sharedPreferences.edit().putBoolean("show_admin_permission_rationale", true).apply()
                onAdminPermissionDenied()
            } else if (resultCode == Activity.RESULT_OK) {
                onAdminPermissionGranted()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private val REQUEST_PIN = 1
        private val REQUEST_PASSWORD = 2
        private val REQUEST_ADMIN_PERMISSION = 3

        fun newInstance(isEnter: Boolean): ActionsFragment {
            val fragment = ActionsFragment()
            val args = Bundle()
            args.putBoolean("is_enter", isEnter)
            fragment.arguments = args
            return fragment
        }
    }
}
