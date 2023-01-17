package com.sabina.project.project_manager.presentation.project_overview.group_overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sabina.project.base.external.extensions.getPlurals
import com.sabina.project.base.external.rx.clicksWithDebounce
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.databinding.ItemObjectBinding
import com.sabina.project.project_manager.domain.model.ProjectObject
import com.sabina.project.project_manager.domain.model.ProjectObjectStatus

internal class GroupOverviewAdapter(
    private val arrayTypes: Array<String>,
    private val onClick: ((obj: ProjectObject, index: Int) -> Unit)? = null
) : ListAdapter<ProjectObject, GroupOverviewAdapter.GroupOverviewViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ProjectObject>() {
            override fun areItemsTheSame(oldItem: ProjectObject, newItem: ProjectObject): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ProjectObject, newItem: ProjectObject): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupOverviewViewHolder {
        val binding = ItemObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupOverviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupOverviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroupOverviewViewHolder(
        private val binding: ItemObjectBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        fun bind(obj: ProjectObject) {
            binding.root.clicksWithDebounce {
                onClick?.invoke(obj, currentList.indexOf(obj))
            }
            binding.tvName.text = obj.name
            val count = obj.images.size
            binding.tvImageCount.text = context.getPlurals(R.plurals.images, count, count)
            binding.tvType.text = arrayTypes[obj.type]
            binding.tvStatus.text = when (obj.status) {
                ProjectObjectStatus.CHECKED -> context.getString(R.string.object_overview_status_checked)
                ProjectObjectStatus.CREATED -> context.getString(R.string.object_overview_status_created)
                ProjectObjectStatus.INCOMPLETE -> context.getString(R.string.object_overview_status_incomplete)
                ProjectObjectStatus.READY_FOR_REVIEW -> context.getString(R.string.object_overview_status_ready_for_review)
            }
            binding.tvStatus.setTextColor(
                when (obj.status) {
                    ProjectObjectStatus.CHECKED -> ContextCompat.getColor(context, R.color.green)
                    ProjectObjectStatus.CREATED -> ContextCompat.getColor(context, R.color.textColorSecondary)
                    ProjectObjectStatus.INCOMPLETE -> ContextCompat.getColor(context, R.color.red)
                    ProjectObjectStatus.READY_FOR_REVIEW -> ContextCompat.getColor(context, R.color.colorPrimary)
                }
            )
        }
    }
}