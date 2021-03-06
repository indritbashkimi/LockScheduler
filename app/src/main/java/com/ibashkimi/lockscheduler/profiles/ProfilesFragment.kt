package com.ibashkimi.lockscheduler.profiles

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.adapter.ProfileAdapter
import com.ibashkimi.lockscheduler.databinding.FragmentProfilesBinding
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.util.uninstall
import kotlinx.coroutines.launch
import java.util.*

/**
 * Fragment used to display profile list.
 */
class ProfilesFragment : Fragment(), ProfileAdapter.Callback {

    private val viewModel: ProfilesViewModel by viewModels()

    private lateinit var binding: FragmentProfilesBinding

    private lateinit var adapter: ProfileAdapter

    private var actionMode: ActionMode? = null

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeFlag(
                ItemTouchHelper.ACTION_STATE_DRAG,
                ItemTouchHelper.DOWN or ItemTouchHelper.UP
            )
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (NavigationUI.onNavDestinationSelected(item, findNavController())) {
            return true
        } else if (item.itemId == R.id.action_uninstall) {
            lifecycleScope.launch {
                uninstall(requireContext())
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfilesBinding.inflate(inflater, container, false)

        val layoutManager: RecyclerView.LayoutManager
        val columnCount = resources.getInteger(R.integer.profiles_column_count)
        if (columnCount == 1)
            layoutManager = LinearLayoutManager(context)
        else
            layoutManager = GridLayoutManager(context, columnCount)

        binding.recyclerView.layoutManager = layoutManager
        adapter = ProfileAdapter(
            ArrayList(0),
            this
        )
        binding.recyclerView.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.fab.setOnClickListener { showAddProfile() }

        viewModel.profiles.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                showNoProfiles()
            } else {
                showProfiles(it)
            }
        })

        return binding.root
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

        actionMode?.let {
            val count = adapter.selectedItemCount
            if (count == 0) {
                it.finish()
            } else {
                it.title = count.toString()
                it.invalidate()
            }
        }
    }

    private fun showProfiles(profiles: List<Profile>) {
        adapter.setData(profiles.toMutableList())

        binding.recyclerView.visibility = View.VISIBLE
        binding.noProfiles.visibility = View.GONE
    }

    private fun showAddProfile() {
        findNavController()
            .navigate(ProfilesFragmentDirections.actionProfilesToAddEditProfile(null))
    }

    private fun showProfileDetailsUi(profile: Profile) {
        findNavController()
            .navigate(ProfilesFragmentDirections.actionProfilesToAddEditProfile(profile.id))
    }

    /*private fun showLoadingProfilesError() {
        showMessage(getString(R.string.loading_profiles_error))
    }*/

    private fun showNoProfiles() {
        binding.recyclerView.visibility = View.GONE
        binding.noProfiles.visibility = View.VISIBLE
    }

    override fun onProfileClick(position: Int) {
        if (actionMode != null) {
            toggleSelection(position)
        } else {
            val p = adapter.profiles[position]
            showProfileDetailsUi(p)
        }
    }

    override fun onProfileLongClick(position: Int) {
        if (actionMode == null) {
            actionMode =
                (requireActivity() as AppCompatActivity).startSupportActionMode(ActionModeCallback())
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
