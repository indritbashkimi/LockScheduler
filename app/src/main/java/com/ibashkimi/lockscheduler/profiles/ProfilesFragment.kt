package com.ibashkimi.lockscheduler.profiles

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.addeditprofile.AddEditProfileActivity
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.util.PlatformUtils
import java.util.*

/**
 * Fragment used to display profile list.
 */
class ProfilesFragment : Fragment(), ProfileAdapter.Callback {

    private lateinit var rootView: ViewGroup

    private lateinit var recyclerView: RecyclerView

    private lateinit var noTasksView: View

    private lateinit var adapter: ProfileAdapter

    private var actionMode: ActionMode? = null

    private lateinit var viewModel: ProfilesViewModel

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val targetPosition = target.adapterPosition
            val pos1 = viewHolder.adapterPosition
            viewModel.swapProfiles(adapter.profiles[pos1], adapter.profiles[targetPosition])

            if (adapter.isSelected(viewHolder.adapterPosition) != adapter.isSelected(targetPosition)) {
                adapter.toggleSelection(viewHolder.adapterPosition)
                adapter.toggleSelection(targetPosition)
            }
            val profiles = adapter.profiles
            val profile = profiles[viewHolder.adapterPosition]
            profiles[viewHolder.adapterPosition] = profiles[targetPosition]
            profiles[targetPosition] = profile

            adapter.notifyItemMoved(viewHolder.adapterPosition, targetPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }
    })

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // If a profile was successfully added, show snackbar
        if (resultCode == Activity.RESULT_CANCELED)
            return
        if (AddEditProfileActivity.REQUEST_ADD_PROFILE == requestCode) {
            viewModel.loadData()
            showSuccessfullySavedMessage()
        } else if (AddEditProfileActivity.REQUEST_EDIT_PROFILE == requestCode) {
            val extras = data?.getStringExtra("extra")
            if (extras != null) {
                viewModel.loadData()
                when (extras) {
                    "deleted" -> showSuccessfullyRemovedMessage()
                    "updated" -> showSuccessfullyUpdatedMessage()
                    else -> throw RuntimeException("Unhandled case.")
                }
            } else {
                throw RuntimeException("This should not be possible.")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = Navigation.findNavController(requireActivity(), R.id.main_nav_host_fragment)
        if (NavigationUI.onNavDestinationSelected(item, navController)) {
            return true
        } else if (item.itemId == R.id.action_uninstall) {
            PlatformUtils.uninstall(requireContext())
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_profiles, container, false)
        this.rootView = rootView.findViewById(R.id.root)
        recyclerView = rootView.findViewById(R.id.recyclerView)
        noTasksView = rootView.findViewById(R.id.no_profiles)

        val layoutManager: RecyclerView.LayoutManager
        val columnCount = resources.getInteger(R.integer.profiles_column_count)
        if (columnCount == 1)
            layoutManager = LinearLayoutManager(context)
        else
            layoutManager = GridLayoutManager(context, columnCount)

        recyclerView.layoutManager = layoutManager
        adapter = ProfileAdapter(ArrayList(0), R.layout.item_profile, this)
        recyclerView.adapter = adapter
        itemTouchHelper.attachToRecyclerView(recyclerView)

        rootView.findViewById<View>(R.id.fab).setOnClickListener { showAddProfile() }

        viewModel = ViewModelProviders.of(this).get(ProfilesViewModel::class.java)
        viewModel.profilesLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                showNoProfiles()
            } else {
                showProfiles(it)
            }
        })

        return rootView
    }

    /**
     * Toggle the selection state of an item.
     *
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private fun toggleSelection(position: Int) {
        adapter.toggleSelection(position)
        adapter.notifyDataSetChanged()
        // Bug: It's better to call adapter.notifyItemChanged(position) but it shows the wrong item.

        val count = adapter.selectedItemCount
        if (count == 0) {
            actionMode!!.finish()
        } else {
            actionMode!!.title = count.toString()
            actionMode!!.invalidate()
        }
    }

    private fun showProfiles(profiles: List<Profile>) {
        adapter.setData(profiles)

        recyclerView.visibility = View.VISIBLE
        noTasksView.visibility = View.GONE
    }

    private fun showAddProfile() {
        val intent = Intent(context, AddEditProfileActivity::class.java)
        startActivityForResult(intent, AddEditProfileActivity.REQUEST_ADD_PROFILE)
    }

    private fun showProfileDetailsUi(profileId: String) {
        val intent = Intent(context, AddEditProfileActivity::class.java)
        intent.putExtra(AddEditProfileActivity.ARGUMENT_EDIT_PROFILE_ID, profileId)
        startActivityForResult(intent, AddEditProfileActivity.REQUEST_EDIT_PROFILE)
    }

    /*private fun setLoadingIndicator(active: Boolean) {

    }

    private fun showLoadingProfilesError() {
        showMessage(getString(R.string.loading_profiles_error))
    }*/

    private fun showNoProfiles() {
        recyclerView.visibility = View.GONE
        noTasksView.visibility = View.VISIBLE
    }

    private fun showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_profile_message))
    }

    private fun showSuccessfullyRemovedMessage() {
        showMessage(getString(R.string.successfully_removed_profile_message))
    }

    private fun showSuccessfullyUpdatedMessage() {
        showMessage("Profile updated.")
    }

    private fun showMessage(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onProfileClick(position: Int) {
        if (actionMode != null) {
            toggleSelection(position)
        } else {
            val p = adapter.profiles[position]
            showProfileDetailsUi(p.id)
        }
    }

    override fun onProfileLongClick(position: Int) {
        if (actionMode == null) {
            actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(ActionModeCallback())
        }
        toggleSelection(position)
    }


    private inner class ActionModeCallback : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.profile_selected, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (item.itemId == R.id.action_delete) {
                val items = adapter.selectedItems
                for (i in items.size - 1 downTo -1 + 1) {
                    val position = items[i]
                    viewModel.delete(adapter.profiles[position].id)
                }
                adapter.clearSelection()
                viewModel.loadData()
                mode.finish()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            adapter.clearSelection()
            actionMode = null
        }
    }
}
