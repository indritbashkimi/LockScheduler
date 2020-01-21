package com.ibashkimi.lockscheduler.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.databinding.FragmentHelpBinding
import com.ibashkimi.lockscheduler.databinding.ItemHelpBinding
import com.ibashkimi.lockscheduler.help.HelpFragment.HelpAdapter.HelpViewHolder
import com.ibashkimi.lockscheduler.util.PlatformUtils
import com.ibashkimi.lockscheduler.util.uninstall

class HelpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHelpBinding.inflate(inflater, container, false)
        binding.feedback.setOnClickListener {
            PlatformUtils.sendFeedback(requireActivity())
        }
        binding.uninstall.setOnClickListener {
            uninstall(requireContext())
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerView.isNestedScrollingEnabled = false
        val items = arrayOf(
            HelpItem(R.string.uninstall_title, R.string.uninstall_content),
            HelpItem(R.string.forgot_password_title, R.string.forgot_password_content),
            HelpItem(R.string.device_admin_title, R.string.device_admin_content),
            HelpItem(R.string.location_permission_title, R.string.location_permission_content),
            HelpItem(R.string.access_to_wifi_title, R.string.access_to_wifi_content)
        )
        val adapter = HelpAdapter(items)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    class HelpItem constructor(@field:StringRes val title: Int, @field:StringRes val content: Int)

    internal class HelpAdapter(private val items: Array<HelpItem>) :
        RecyclerView.Adapter<HelpViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
            return HelpViewHolder(
                ItemHelpBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
            holder.binding.apply {
                title.setText(items[position].title)
                content.setText(items[position].content)
            }
        }

        override fun getItemCount(): Int = items.size

        class HelpViewHolder(var binding: ItemHelpBinding) : RecyclerView.ViewHolder(binding.root)

    }
}