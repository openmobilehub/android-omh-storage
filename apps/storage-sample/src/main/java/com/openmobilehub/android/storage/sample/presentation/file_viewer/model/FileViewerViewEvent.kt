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

package com.openmobilehub.android.storage.sample.presentation.file_viewer.model

import android.content.Context
import android.net.Uri
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.sample.presentation.ViewEvent

sealed class FileViewerViewEvent : ViewEvent {

    object Initialize : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.Initialize"
    }

    object RefreshFileList : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.RefreshList"
    }

    object SwapLayoutManager : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.SwapLayoutManager"
    }

    class FileClicked(val file: OmhStorageEntity) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.FileClicked"
    }

    class FileVersionClicked(val version: OmhFileVersion) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.FileVersionClicked"
    }

    object DownloadFile : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.DownloadFile"
    }

    data class SaveFileResult(val result: Result<OmhStorageEntity>) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.SaveFileResult"
    }

    object BackPressed : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.BackPressed"
    }

    class CreateFileWithMimeType(val name: String, val mimeType: String) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.CreateFileWithMimeType"
    }

    class CreateFileWithExtension(val name: String, val extension: String) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.CreateFileWithExtension"
    }

    class CreateFolder(val name: String) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.CreateFolder"
    }

    class DeleteFile(val file: OmhStorageEntity) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.DeleteFile"
    }

    class PermanentlyDeleteFileClicked(val file: OmhStorageEntity) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.PermanentlyDeleteFileClicked"
    }

    class PermanentlyDeleteFile(val file: OmhStorageEntity) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.PermanentlyDeleteFile"
    }

    class UploadFile(val context: Context, val uri: Uri, val fileName: String) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.UploadFile"
    }

    object SignOut : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.SignOut"
    }

    class UpdateFileClicked(val file: OmhStorageEntity) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.UpdateFileClicked"
    }

    class MoreOptionsClicked(val file: OmhStorageEntity) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.MoreOptionsClicked"
    }

    class FileMetadataClicked(val file: OmhStorageEntity) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.FileMetadataClicked"
    }

    class FileVersionsClicked(val file: OmhStorageEntity) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.FileVersionsClicked"
    }

    class FilePermissionsClicked(val file: OmhStorageEntity) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.FilePermissionsClicked"
    }

    class UpdateFile(val context: Context, val uri: Uri, val fileName: String) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.UpdateFile"
    }

    class UpdateSearchQuery(val query: String?) : FileViewerViewEvent() {

        override fun getEventName() = "FileViewerViewEvent.UpdateSearchQuery"
    }

    class GetFolderSize(val folder: OmhStorageEntity.OmhFolder): FileViewerViewEvent() {
        override fun getEventName(): String = "FileViewerViewEvent.GetFolderSize"
    }
}
