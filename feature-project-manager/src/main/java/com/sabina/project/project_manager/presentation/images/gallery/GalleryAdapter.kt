package com.sabina.project.project_manager.presentation.images.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sabina.project.base.external.images.downloadFromUrlCropped
import com.sabina.project.base.external.rx.clicksWithDebounce
import com.sabina.project.project_manager.databinding.ItemSelectorImageBinding

internal class GalleryAdapter(
    private val onClick: ((image: String) -> Unit)
) : ListAdapter<String, GalleryAdapter.ProjectOverviewViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectOverviewViewHolder {
        val binding = ItemSelectorImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProjectOverviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectOverviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProjectOverviewViewHolder(
        private val binding: ItemSelectorImageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        fun bind(link: String) {
            binding.root.clicksWithDebounce {
                onClick.invoke(link)
            }
            binding.ivImage.downloadFromUrlCropped(link)
        }
    }
}