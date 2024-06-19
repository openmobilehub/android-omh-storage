package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.openmobilehub.android.storage.core.model.OmhFileRevision
import com.openmobilehub.android.storage.sample.databinding.FileRevisionAdapterBinding

class FileRevisionAdapter(
    private val listener: ItemListener,
) : ListAdapter<OmhFileRevision, FileRevisionAdapter.FileViewHolder>(DiffCallBack()) {

    private class DiffCallBack : DiffUtil.ItemCallback<OmhFileRevision>() {
        override fun areItemsTheSame(oldItem: OmhFileRevision, newItem: OmhFileRevision) =
            oldItem.revisionId == newItem.revisionId

        override fun areContentsTheSame(oldItem: OmhFileRevision, newItem: OmhFileRevision) =
            oldItem == newItem
    }

    interface ItemListener {
        fun onFileClicked(revision: OmhFileRevision)
    }

    abstract class FileViewHolder(binding: View) : RecyclerView.ViewHolder(binding) {

        abstract fun bind(file: OmhFileRevision, listener: ItemListener, position: Int)
    }

    class FileLinearViewHolder(
        private val binding: FileRevisionAdapterBinding // TODO: Change it
    ) : FileViewHolder(binding.root) {

        override fun bind(file: OmhFileRevision, listener: ItemListener, position: Int) {
            with(binding) {
                val versionIndexText = position + 1

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
        FileRevisionAdapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position), listener, position)
    }
}
