package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.sample.databinding.FileVersionAdapterBinding

class FileVersionAdapter(
    private val listener: ItemListener,
    private val itemsCount: Int,
) : ListAdapter<OmhFileVersion, FileVersionAdapter.FileViewHolder>(DiffCallBack()) {

    private class DiffCallBack : DiffUtil.ItemCallback<OmhFileVersion>() {
        override fun areItemsTheSame(oldItem: OmhFileVersion, newItem: OmhFileVersion) =
            oldItem.versionId == newItem.versionId

        override fun areContentsTheSame(oldItem: OmhFileVersion, newItem: OmhFileVersion) =
            oldItem == newItem
    }

    interface ItemListener {
        fun onFileClicked(version: OmhFileVersion)
    }

    abstract class FileViewHolder(binding: View) : RecyclerView.ViewHolder(binding) {

        abstract fun bind(file: OmhFileVersion, listener: ItemListener, position: Int)
    }

    class FileLinearViewHolder(
        private val binding: FileVersionAdapterBinding,
        private val itemsCount: Int,
    ) : FileViewHolder(binding.root) {

        override fun bind(file: OmhFileVersion, listener: ItemListener, position: Int) {
            with(binding) {
                val versionIndexText = itemsCount - position

                fileName.text = file.lastModified.toString()
                versionIndex.text = versionIndexText.toString()
                root.setOnClickListener { listener.onFileClicked(file) }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = FileLinearViewHolder(
        FileVersionAdapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), itemsCount
    )

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position), listener, position)
    }
}
