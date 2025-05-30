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

import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.sample.presentation.ViewState
import java.io.ByteArrayOutputStream

sealed class FileViewerViewState : ViewState {

    object Initial : FileViewerViewState() {

        override fun getName() = "FileViewerViewState.Initial"
    }

    object Loading : FileViewerViewState() {

        override fun getName() = "FileViewerViewState.Loading"
    }

    data class Content(val files: List<OmhStorageEntity>, val isSearching: Boolean, val quotaAllocated: Long, val quotaUsed: Long) :
        FileViewerViewState() {

        override fun getName() = "FileViewerViewState.Content"
    }

    object SwapLayoutManager : FileViewerViewState() {

        override fun getName() = "FileViewerViewState.SwapLayoutManager"
    }

    object Finish : FileViewerViewState() {

        override fun getName() = "FileViewerViewState.Finish"
    }

    object CheckDownloadPermissions : FileViewerViewState() {

        override fun getName() = "FileViewerViewState.CheckDownloadPermissions"
    }

    data class SaveFile(
        val file: OmhStorageEntity.OmhFile,
        val bytes: ByteArrayOutputStream
    ) : FileViewerViewState() {

        override fun getName() = "FileViewerViewState.SaveFile"
    }

    object SignOut : FileViewerViewState() {

        override fun getName() = "FileViewerViewState.SignOut"
    }

    object ShowUpdateFilePicker : FileViewerViewState() {

        override fun getName() = "FileViewerViewState.FilePicker"
    }

    object ShowDownloadExceptionDialog : FileViewerViewState() {

        override fun getName() = "FileViewerViewState.ShowDownloadExceptionDialog"
    }

    object ClearSearch : FileViewerViewState() {

        override fun getName() = "FileViewerViewState.ClearSearch"
    }

    data class ShowPermanentlyDeleteDialog(val file: OmhStorageEntity) : FileViewerViewState() {

        override fun getName() = "FileViewerViewState.ShowPermanentlyDeleteDialog"
    }
}
