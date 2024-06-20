package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.sample.databinding.FileVersionAdapterBinding

class FileVersionAdapter(
    private val listener: ItemListener,
    private val itemsCount: Int,
) : ListAdapter<OmhFileVersion, FileVersionAdapter.FileLinearViewHolder>(DiffCallBack()) {

    private class DiffCallBack : DiffUtil.ItemCallback<OmhFileVersion>() {
        override fun areItemsTheSame(oldItem: OmhFileVersion, newItem: OmhFileVersion) =
            oldItem.versionId == newItem.versionId

        override fun areContentsTheSame(oldItem: OmhFileVersion, newItem: OmhFileVersion) =
            oldItem == newItem
    }

    interface ItemListener {
        fun onFileVersionClicked(version: OmhFileVersion)
    }

    class FileLinearViewHolder(
        private val binding: FileVersionAdapterBinding,
        private val itemsCount: Int,
    ) : RecyclerView.ViewHolder(binding.root)  {

        fun bind(file: OmhFileVersion, listener: ItemListener, position: Int) {
            with(binding) {
                val versionIndexText = itemsCount - position

                fileName.text = file.lastModified.toString()
                versionIndex.text = versionIndexText.toString()
                root.setOnClickListener { listener.onFileVersionClicked(file) }
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

    override fun onBindViewHolder(holder: FileLinearViewHolder, position: Int) {
        holder.bind(getItem(position), listener, position)
    }
}
