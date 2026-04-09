package com.buge.box.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.buge.box.data.CloneAppInfo
import com.buge.box.databinding.ItemCloneAppBinding

class CloneAppAdapter(
    private val onLaunch: (CloneAppInfo) -> Unit,
    private val onDelete: (CloneAppInfo) -> Unit
) : ListAdapter<CloneAppInfo, CloneAppAdapter.CloneViewHolder>(CloneDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CloneViewHolder {
        val binding = ItemCloneAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CloneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CloneViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CloneViewHolder(
        private val binding: ItemCloneAppBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cloneInfo: CloneAppInfo) {
            binding.apply {
                appIcon.setImageDrawable(cloneInfo.icon)
                appName.text = cloneInfo.appName
                userId.text = "ID: ${cloneInfo.userId}"
                
                root.setOnClickListener { onLaunch(cloneInfo) }
                deleteButton.setOnClickListener { onDelete(cloneInfo) }
            }
        }
    }

    class CloneDiffCallback : DiffUtil.ItemCallback<CloneAppInfo>() {
        override fun areItemsTheSame(oldItem: CloneAppInfo, newItem: CloneAppInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CloneAppInfo, newItem: CloneAppInfo): Boolean {
            return oldItem == newItem
        }
    }
}
