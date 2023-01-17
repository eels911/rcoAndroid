package com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sabina.project.base.external.images.downloadFromUrlCropped
import com.sabina.project.base.external.rx.clicksWithDebounce
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.databinding.ItemImageBinding
import com.sabina.project.project_manager.domain.model.ProjectImage

internal class ObjectOverviewAdapter(
    private val onClick: ((image: ProjectImage, index: Int) -> Unit)
) : ListAdapter<ProjectImage, ObjectOverviewAdapter.ObjectOverviewViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ProjectImage>() {
            override fun areItemsTheSame(oldItem: ProjectImage, newItem: ProjectImage): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(oldItem: ProjectImage, newItem: ProjectImage): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectOverviewViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ObjectOverviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ObjectOverviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ObjectOverviewViewHolder(
        private val binding: ItemImageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        fun bind(image: ProjectImage) {
            binding.root.clicksWithDebounce {
                onClick.invoke(image, currentList.indexOf(image))
            }
            binding.tvName.text = if (image.name.isEmpty()) context.getString(R.string.object_overview_empty_name_for_list) else image.name
            binding.ivImage.downloadFromUrlCropped(image.link)
        }
    }
}