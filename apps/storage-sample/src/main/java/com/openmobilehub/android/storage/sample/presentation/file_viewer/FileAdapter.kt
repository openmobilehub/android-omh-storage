/*
 * Copyright 2023 Open Mobile Hub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openmobilehub.android.storage.sample.presentation.file_viewer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.FileGridAdapterBinding
import com.openmobilehub.android.storage.sample.databinding.FileLinearAdapterBinding
import com.openmobilehub.android.storage.sample.domain.model.FileType
import com.openmobilehub.android.storage.sample.util.getFileType

class FileAdapter(
    private val listener: GridItemListener,
    private val isGridLayout: Boolean
) : ListAdapter<OmhStorageEntity, FileAdapter.FileViewHolder>(DiffCallBack()) {

    companion object {

        private const val URL_FOLDER =
            "https://drive-thirdparty.googleusercontent.com/32/type/application/vnd.google-apps.folder"
        private const val URL_DOCUMENT =
            "https://drive-thirdparty.googleusercontent.com/32/type/application/vnd.google-apps.document"
        private const val URL_SHEET =
            "https://drive-thirdparty.googleusercontent.com/32/type/application/vnd.google-apps.spreadsheet"
        private const val URL_PRESENTATION =
            "https://drive-thirdparty.googleusercontent.com/32/type/application/vnd.google-apps.presentation"
        private const val URL_PDF =
            "https://drive-thirdparty.googleusercontent.com/32/type/application/pdf"
        private const val URL_PNG =
            "https://drive-thirdparty.googleusercontent.com/32/type/image/png"
        private const val URL_ZIP =
            "https://drive-thirdparty.googleusercontent.com/32/type/application/zip"
        private const val URL_VIDEO =
            "https://drive-thirdparty.googleusercontent.com/32/type/video/mp4"
        private const val URL_OTHER = "https://static.thenounproject.com/png/3482632-200.png"

        private fun getFileIconUrl(file: OmhStorageEntity) = when(file) {
            is OmhStorageEntity.OmhFile -> getFileIconUrl(file.getFileType())
            is OmhStorageEntity.OmhFolder -> URL_FOLDER
        }

        private fun getFileIconUrl(fileType: FileType) = when (fileType) {
            FileType.PDF -> URL_PDF

            FileType.GOOGLE_DOCUMENT,
            FileType.MICROSOFT_WORD,
            FileType.OPEN_DOCUMENT_TEXT -> URL_DOCUMENT

            FileType.GOOGLE_SPREADSHEET,
            FileType.MICROSOFT_EXCEL,
            FileType.OPEN_DOCUMENT_SPREADSHEET -> URL_SHEET

            FileType.GOOGLE_PRESENTATION,
            FileType.MICROSOFT_POWERPOINT,
            FileType.OPEN_DOCUMENT_PRESENTATION -> URL_PRESENTATION

            FileType.PNG,
            FileType.JPEG -> URL_PNG

            FileType.ZIP -> URL_ZIP

            FileType.GOOGLE_VIDEO,
            FileType.MP4 -> URL_VIDEO

            else -> URL_OTHER
        }

        private fun loadFileIcon(
            context: Context,
            iconUrl: String,
            imageView: ImageView
        ) {
            Glide.with(context)
                .asBitmap()
                .load(iconUrl)
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(imageView)
        }
    }

    private class DiffCallBack : ItemCallback<OmhStorageEntity>() {

        override fun areItemsTheSame(oldItem: OmhStorageEntity, newItem: OmhStorageEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OmhStorageEntity, newItem: OmhStorageEntity) =
            oldItem == newItem
    }

    interface GridItemListener {

        fun onFileClicked(file: OmhStorageEntity)

        fun onMoreOptionsClicked(file: OmhStorageEntity)

    }

    abstract class FileViewHolder(binding: View) : RecyclerView.ViewHolder(binding) {

        abstract fun bind(file: OmhStorageEntity, listener: GridItemListener)
    }

    class FileGridViewHolder(
        private val binding: FileGridAdapterBinding
    ) : FileViewHolder(binding.root) {

        override fun bind(file: OmhStorageEntity, listener: GridItemListener) {
            val context = binding.root.context
            val iconLink = getFileIconUrl(file)

            with(binding) {
                fileName.text = file.name
                loadFileIcon(context, iconLink, fileIcon)
                root.setOnClickListener { listener.onFileClicked(file) }
                buttonMoreOptions.setOnClickListener { listener.onMoreOptionsClicked(file) }
            }
        }
    }

    class FileLinearViewHolder(
        private val binding: FileLinearAdapterBinding
    ) : FileViewHolder(binding.root) {

        override fun bind(file: OmhStorageEntity, listener: GridItemListener) {
            val context = binding.root.context
            val iconLink = getFileIconUrl(file)

            with(binding) {
                fileName.text = file.name
                loadFileIcon(context, iconLink, fileIcon)
                root.setOnClickListener { listener.onFileClicked(file) }
                buttonMoreOptions.setOnClickListener { listener.onMoreOptionsClicked(file) }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = if (isGridLayout) {
        FileGridViewHolder(
            FileGridAdapterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    } else {
        FileLinearViewHolder(
            FileLinearAdapterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }
}
