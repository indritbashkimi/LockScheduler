package com.ibashkimi.lockscheduler.addeditprofile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.addeditprofile.actions.ActionsFragment
import com.ibashkimi.lockscheduler.addeditprofile.conditions.ConditionsFragment

class AddEditProfileFragment : Fragment() {

    private lateinit var viewModel: AddEditProfileViewModel

    private lateinit var toolbar: Toolbar

    private lateinit var profileName: EditText

    private var showDelete: Boolean = false

    private val args: AddEditProfileFragmentArgs by navArgs()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_add_edit_profile, container, false) as ViewGroup

        toolbar = requireActivity().findViewById(R.id.toolbar)

        profileName = rootView.findViewById(R.id.profile_name)

        rootView.findViewById<View>(R.id.fab).setOnClickListener { onSave() }

        viewModel = ViewModelProvider(this, SavedStateViewModelFactory(App.getInstance(), this))
                .get(AddEditProfileViewModel::class.java)

        if (savedInstanceState == null) {
            viewModel.setProfileId(args.profileId)

            profileName.setText(viewModel.getProfileName())
            profileName.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(text: Editable) {}
                override fun beforeTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
                    viewModel.setProfileName(text.toString())
                }
            })
            attachChildFragments()
        }
        showDelete = if (viewModel.getProfileId() == null) {
            showTitle(R.string.new_profile)
            false
        } else {
            showTitle(R.string.edit_profile)
            true
        }

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val deleteMenu = menu.findItem(R.id.action_delete)
        deleteMenu.isVisible = showDelete
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            viewModel.deleteProfile()
            showMessage(R.string.successfully_removed_profile_message)
            findNavController().navigateUp()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun showLoadProfileError() {
        showMessage(R.string.profile_not_found_msg)
    }

    private fun attachChildFragments() {
        childFragmentManager.apply {
            beginTransaction()
                    .replace(R.id.enter_actions_container, findFragmentByTag(ENTER_ACTIONS_FRAGMENT) as ActionsFragment?
                            ?: ActionsFragment.newInstance(true), ENTER_ACTIONS_FRAGMENT)
                    .replace(R.id.exit_actions_container, findFragmentByTag(EXIT_ACTIONS_FRAGMENT) as ActionsFragment?
                            ?: ActionsFragment.newInstance(false), EXIT_ACTIONS_FRAGMENT)
                    .replace(R.id.conditions_container, findFragmentByTag(CONDITIONS_FRAGMENT) as ConditionsFragment?
                            ?: ConditionsFragment.newInstance(), CONDITIONS_FRAGMENT)
                    .commit()
        }

    }

    private fun showTitle(title: Int) {
        toolbar.setTitle(title)
    }

    private fun onSave() {
        if (viewModel.saveProfile()) {
            showMessage(R.string.successfully_saved_profile_message)
            //showMessage(R.string.successfully_updated_profile_message)
            findNavController().navigateUp()
        }
    }

    private fun showMessage(message: Int) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val ENTER_ACTIONS_FRAGMENT = "enter_actions_fragment"
        const val EXIT_ACTIONS_FRAGMENT = "exit_actions_fragment"
        const val CONDITIONS_FRAGMENT = "conditions_fragment"
    }
}
