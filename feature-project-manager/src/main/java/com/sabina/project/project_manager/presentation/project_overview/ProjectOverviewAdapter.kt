package com.sabina.project.project_manager.presentation.project_overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sabina.project.base.external.extensions.getPlurals
import com.sabina.project.base.external.rx.clicksWithDebounce
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.databinding.ItemGroupBinding
import com.sabina.project.project_manager.domain.model.ProjectGroup

internal class ProjectOverviewAdapter(
    private val onClick: ((group: ProjectGroup, index: Int) -> Unit)
) : ListAdapter<ProjectGroup, ProjectOverviewAdapter.ProjectOverviewViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ProjectGroup>() {
            override fun areItemsTheSame(oldItem: ProjectGroup, newItem: ProjectGroup): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ProjectGroup, newItem: ProjectGroup): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectOverviewViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProjectOverviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectOverviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProjectOverviewViewHolder(
        private val binding: ItemGroupBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        fun bind(group: ProjectGroup) {
            binding.root.clicksWithDebounce {
                onClick.invoke(group, currentList.indexOf(group))
            }
            binding.tvName.text = group.name
            val count = group.objects.size
            binding.tvObjectCount.text = context.getPlurals(R.plurals.objects, count, count)
        }
    }
}