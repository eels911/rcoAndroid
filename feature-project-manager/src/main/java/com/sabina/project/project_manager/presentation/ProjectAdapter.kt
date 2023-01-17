package com.sabina.project.project_manager.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sabina.project.base.external.extensions.getPlurals
import com.sabina.project.base.external.rx.clicksWithDebounce
import com.sabina.project.base.external.ui.UtilsUI
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.databinding.ItemProjectBinding
import com.sabina.project.project_manager.domain.model.Project

internal class ProjectAdapter(
    private val onClick: ((project: Project) -> Unit)
) : ListAdapter<Project, ProjectAdapter.ProjectViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Project>() {
            override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProjectViewHolder(
        private val binding: ItemProjectBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        fun bind(project: Project) {
            binding.root.clicksWithDebounce {
                onClick.invoke(project)
            }
            binding.tvName.text = project.name
            binding.tvCreateAt.text = context.getString(R.string.project_overview_create_at, UtilsUI.convertTimestampToDate(project.createAt))
            val count = project.objectGroupList.size
            binding.tvGroupCount.text = context.getPlurals(R.plurals.groups, count, count)
        }
    }
}