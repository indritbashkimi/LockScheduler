package com.ibashkimi.lockscheduler.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.adapter.ProfileAdapter.ProfileViewHolder
import com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi.SelectableAdapter
import com.ibashkimi.lockscheduler.databinding.ItemProfileBinding
import com.ibashkimi.lockscheduler.extention.concatenate
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.model.condition.*
import com.ibashkimi.lockscheduler.model.find
import com.ibashkimi.lockscheduler.model.findCondition
import com.ibashkimi.lockscheduler.util.intervalToString
import com.ibashkimi.lockscheduler.util.toDaysString
import java.util.*

class ProfileAdapter(var profiles: MutableList<Profile>, private val itemListener: Callback) :
    SelectableAdapter<ProfileViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ItemProfileBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileViewHolder(binding, itemListener)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = profiles[position]
        holder.init(profile)
        holder.setSelected(isSelected(position))
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    fun setData(data: MutableList<Profile>) {
        profiles = data
        notifyDataSetChanged()
    }

    interface Callback {
        fun onProfileClick(position: Int)
        fun onProfileLongClick(position: Int)
    }

    class ProfileViewHolder(
        var binding: ItemProfileBinding,
        listener: Callback
    ) : RecyclerView.ViewHolder(binding.root) {
        var listener: Callback
        fun init(profile: Profile) {
            if (profile.name != "") {
                binding.name.text = profile.name
            } else {
                binding.name.visibility = View.GONE
            }
            val (lockMode) = profile.enterActions.find<LockAction>()
                ?: throw IllegalArgumentException("Profile " + profile.name + " doesn't contain an enter lock action.")
            binding.enterLockMode.text = lockMode.lockType.value
            val (lockMode1) = profile.exitActions.find<LockAction>()
                ?: throw IllegalArgumentException("Profile " + profile.name + " doesn't contain an exit lock action.")
            binding.exitLockMode.text = lockMode1.lockType.value
            val placeCondition: PlaceCondition? = profile.findCondition()
            if (placeCondition != null) {
                binding.placeSummary.text = placeCondition.address
                binding.locationLayout.visibility = View.VISIBLE
            } else {
                binding.locationLayout.visibility = View.GONE
            }
            val timeCondition: TimeCondition? = profile.findCondition()
            if (timeCondition != null) {
                binding.days.text = timeCondition.daysActive.toDaysString(itemView.context)
                binding.interval.text = intervalToString(
                    timeCondition.startTime,
                    timeCondition.endTime
                )
                binding.timeLayout.visibility = View.VISIBLE
            } else {
                binding.timeLayout.visibility = View.GONE
            }
            val wifiCondition: WifiCondition? = profile.findCondition()
            if (wifiCondition != null) {
                binding.wifiSummary.text = wifiCondition.wifiList.map { it.ssid }.concatenate(", ")
                binding.wifiLayout.visibility = View.VISIBLE
            } else {
                binding.wifiLayout.visibility = View.GONE
            }
            val powerCondition: PowerCondition? = profile.findCondition()
            if (powerCondition != null) {
                binding.powerLayout.visibility = View.VISIBLE
                binding.powerSummary.setText(if (powerCondition.powerConnected) R.string.power_connected else R.string.power_disconnected)
            }
        }

        fun setSelected(selected: Boolean) {
            binding.cover.visibility = if (selected) View.VISIBLE else View.GONE
        }

        init {
            binding.root.setOnClickListener {
                listener.onProfileClick(adapterPosition)
            }
            binding.root.setOnLongClickListener {
                listener.onProfileLongClick(adapterPosition)
                true
            }
            this.listener = listener
        }
    }
}