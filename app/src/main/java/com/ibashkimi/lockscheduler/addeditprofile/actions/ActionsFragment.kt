package com.ibashkimi.lockscheduler.addeditprofile.actions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.addeditprofile.AddEditProfileViewModel
import com.ibashkimi.lockscheduler.databinding.FragmentActionsBinding
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.util.*

class ActionsFragment : Fragment() {

    private lateinit var binding: FragmentActionsBinding

    private lateinit var viewModel: AddEditProfileViewModel

    private val sharedPreferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    private var lockTypeIfGranted: String
        get() = sharedPreferences.getString("lock_if_granted", LockAction.LockType.UNCHANGED.value)!!
        set(value) = sharedPreferences.edit().putString("lock_if_granted", value).apply()

    private var isEnter = false

    private val lockActionLiveData: MutableLiveData<LockAction>
        get() {
            return (if (isEnter) viewModel.getWhenActiveLockAction() else viewModel.getWhenInactiveLockAction())
        }

    private fun setLockAction(lockAction: LockAction) {
        if (isEnter)
            viewModel.setWhenActiveLockAction(lockAction)
        else
            viewModel.setWhenInactiveLockAction(lockAction)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEnter = arguments!!.getBoolean("is_enter")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentActionsBinding.inflate(inflater, container, false)

        binding.title.setText(if (isEnter) R.string.title_condition_enter else R.string.title_condition_exit)
        binding.lockSettings.setOnClickListener {
            showPasswordDialog(lockActionLiveData.value!!.lockMode.lockType) { which -> onLockTypeSelected(positionToLockType(which)) }
        }

        viewModel = ViewModelProvider(requireParentFragment(), SavedStateViewModelFactory(App.getInstance(), requireParentFragment()))
                .get(AddEditProfileViewModel::class.java)
        lockActionLiveData.observe(viewLifecycleOwner, Observer {
            updateSummary(it)
        })

        return binding.root
    }

    private fun onLockTypeSelected(lockType: LockAction.LockType) {
        if (lockType == LockAction.LockType.UNCHANGED) {
            setLockAction(LockAction(LockAction.LockMode.Unchanged))
        } else {
            checkAdminPermission(
                    onGranted = {
                        when (lockType) {
                            LockAction.LockType.PIN -> showPinChooser(REQUEST_PIN)
                            LockAction.LockType.PASSWORD -> showPasswordChooser(REQUEST_PASSWORD)
                            LockAction.LockType.SWIPE -> {
                                setLockAction(LockAction(LockAction.LockMode.Swipe))
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

    private fun updateSummary(lockAction: LockAction) {
        binding.lockSummary.setText(lockTypeToTextRes(lockAction.lockMode.lockType))
    }

    private fun onAdminPermissionDenied() {
        Toast.makeText(context, "Admin permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PIN -> if (resultCode == Activity.RESULT_OK) {
                setLockAction(LockAction(LockAction.LockMode.Pin(data!!.getStringExtra("input")!!)))
            }
            REQUEST_PASSWORD -> if (resultCode == Activity.RESULT_OK) {
                setLockAction(LockAction(LockAction.LockMode.Password(data!!.getStringExtra("input")!!)))
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
