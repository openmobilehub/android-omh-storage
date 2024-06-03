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
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.viewModelScope
import com.omh.android.auth.api.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhFileType
import com.openmobilehub.android.storage.sample.model.FileType
import com.openmobilehub.android.storage.sample.presentation.BaseViewModel
import com.openmobilehub.android.storage.sample.util.getNameWithExtension
import com.openmobilehub.android.storage.sample.util.isDownloadable
import com.openmobilehub.android.storage.sample.util.normalizeFileName
import com.openmobilehub.android.storage.sample.util.normalizedMimeType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Stack
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class FileViewerViewModel @Inject constructor(
    private val omhStorageClient: OmhStorageClient,
    private val authClient: OmhAuthClient
) : BaseViewModel<FileViewerViewState, FileViewerViewEvent>() {

    companion object {
        private const val ID_ROOT = "root"

        val listOfFileTypes = listOf(
            FileType("Folder", OmhFileType.FOLDER),
            FileType("Document", OmhFileType.DOCUMENT),
            FileType("Sheet", OmhFileType.SPREADSHEET),
            FileType("Presentation", OmhFileType.PRESENTATION),
        )

        const val ANY_MIME_TYPE = "*/*"
        const val DEFAULT_FILE_NAME = "Untitled"
    }

    var isUpload = false
    var isGridLayoutManager = true
    var createFileSelectedType: OmhFileType? = null
    private val parentIdStack = Stack<String>().apply { push(ID_ROOT) }
    private var lastFileClicked: OmhFile? = null

    override fun getInitialState(): FileViewerViewState = FileViewerViewState.Initial

    override fun processEvent(event: FileViewerViewEvent) {
        when (event) {
            FileViewerViewEvent.Initialize -> initializeEvent()
            is FileViewerViewEvent.RefreshFileList -> refreshFileListEvent()
            is FileViewerViewEvent.SwapLayoutManager -> swapLayoutManagerEvent()
            is FileViewerViewEvent.FileClicked -> fileClickedEvent(event)
            FileViewerViewEvent.BackPressed -> backPressedEvent()
            is FileViewerViewEvent.CreateFile -> createFileEvent(event)
            is FileViewerViewEvent.DeleteFile -> deleteFileEvent(event)
            is FileViewerViewEvent.UploadFile -> uploadFile(event)
            is FileViewerViewEvent.UpdateFile -> updateFileEvent(event)
            FileViewerViewEvent.SignOut -> signOut()
            FileViewerViewEvent.DownloadFile -> downloadFileEvent()
            is FileViewerViewEvent.UpdateFileClicked -> updateFileClickEvent(event)
        }
    }

    private fun initializeEvent() {
        refreshFileListEvent()
    }

    private fun refreshFileListEvent() {
        setState(FileViewerViewState.Loading)
        val parentId = parentIdStack.peek()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val files = omhStorageClient.listFiles(parentId)
                    .sortedWith(
                        compareBy<OmhFile> { !it.isFolder() }
                            .thenBy { it.mimeType }
                            .thenBy { it.name }
                    )
                setState(FileViewerViewState.Content(files))
            } catch (exception: Exception) {
                errorDialogMessage.postValue(exception.message)
                toastMessage.postValue(exception.message)
                exception.printStackTrace()

                setState(FileViewerViewState.Content(emptyList()))
            }
        }
    }

    private fun swapLayoutManagerEvent() {
        isGridLayoutManager = !isGridLayoutManager
        setState(FileViewerViewState.SwapLayoutManager)
    }

    private fun fileClickedEvent(event: FileViewerViewEvent.FileClicked) {
        val file = event.file

        if (file.isFolder()) {
            val fileId = file.id

            parentIdStack.push(fileId)
            refreshFileListEvent()
        } else {
            lastFileClicked = file
            setState(FileViewerViewState.CheckPermissions)
        }
    }

    private fun backPressedEvent() {
        if (parentIdStack.peek() == ID_ROOT) {
            setState(FileViewerViewState.Finish)
        } else {
            parentIdStack.pop()
            refreshFileListEvent()
        }
    }

    private fun downloadFileEvent() {
        setState(FileViewerViewState.Loading)

        lastFileClicked?.let { file ->
            if (!file.isDownloadable()) {
                toastMessage.postValue("${file.name} is not downloadable")
                refreshFileListEvent()
                return
            }

            val mimeTypeToSave = file.normalizedMimeType()

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val data = omhStorageClient.downloadFile(file.id, mimeTypeToSave)
                    val fileToSave = file.copy(
                        name = file.normalizeFileName(),
                        mimeType = mimeTypeToSave
                    )
                    try {
                        handleDownloadSuccess(data, fileToSave)
                        toastMessage.postValue("${file.name} was successfully downloaded")
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        setState(FileViewerViewState.ShowDownloadExceptionDialog)
                        errorDialogMessage.postValue(exception.message)
                    }
                    refreshFileListEvent()
                } catch (exception: Exception) {
                    errorDialogMessage.postValue(exception.message)
                    toastMessage.postValue("ERROR: ${file.name} was NOT downloaded")
                    exception.printStackTrace()

                    refreshFileListEvent()
                }
            }
        } ?: run {
            toastMessage.postValue("The file was NOT downloaded")
            refreshFileListEvent()
        }
    }

    private fun updateFileEvent(event: FileViewerViewEvent.UpdateFile) {
        setState(FileViewerViewState.Loading)
        val fileId = lastFileClicked?.id

        if (fileId.isNullOrBlank()) {
            refreshFileListEvent()
            return
        }

        val filePath = getFile(event.context, event.uri, event.fileName)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = omhStorageClient.updateFile(filePath, fileId)

                val resultMessage = if (file == null) {
                    "${event.fileName} was not updated"
                } else {
                    "${event.fileName} was successfully updated"
                }

                toastMessage.postValue(resultMessage)

                refreshFileListEvent()
            } catch (exception: Exception) {
                errorDialogMessage.postValue(exception.message)
                toastMessage.postValue("ERROR: ${event.fileName} was not updated")
                exception.printStackTrace()

                refreshFileListEvent()
            }
        }
    }

    private fun createFileEvent(event: FileViewerViewEvent.CreateFile) {
        setState(FileViewerViewState.Loading)
        val parentId = parentIdStack.peek()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                omhStorageClient.createFile(event.name, event.mimeType, parentId)
                refreshFileListEvent()
            } catch (exception: Exception) {
                errorDialogMessage.postValue(exception.message)
                toastMessage.postValue(exception.message)
                exception.printStackTrace()

                refreshFileListEvent()
            }
        }
    }

    private fun uploadFile(event: FileViewerViewEvent.UploadFile) {
        setState(FileViewerViewState.Loading)

        val parentId = parentIdStack.peek()
        val filePath = getFile(event.context, event.uri, event.fileName)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = omhStorageClient.uploadFile(filePath, parentId)

                val resultMessage = if (file == null) {
                    "${event.fileName} was NOT uploaded"
                } else {
                    "${event.fileName} was successfully uploaded"
                }

                toastMessage.postValue(resultMessage)

                refreshFileListEvent()
            } catch (exception: Exception) {
                errorDialogMessage.postValue(exception.message)
                toastMessage.postValue("ERROR: ${event.fileName} was NOT uploaded")
                exception.printStackTrace()

                refreshFileListEvent()
            }
        }
    }

    private fun getFile(context: Context, uri: Uri, fileName: String): File {
        val tempFile = File(context.cacheDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
        }

        return tempFile
    }

    private fun deleteFileEvent(event: FileViewerViewEvent.DeleteFile) {
        setState(FileViewerViewState.Loading)

        val file = event.file

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isSuccess = omhStorageClient.deleteFile(file.id)
                handleDeleteSuccess(isSuccess, file)
                refreshFileListEvent()
            } catch (exception: Exception) {
                errorDialogMessage.postValue(exception.message)
                toastMessage.postValue("ERROR: ${file.name} was NOT deleted")
                exception.printStackTrace()

                refreshFileListEvent()
            }
        }
    }

    private fun handleDeleteSuccess(isSuccessful: Boolean, file: OmhFile) {
        val toastText = if (isSuccessful) {
            "${file.name} was successfully deleted"
        } else {
            "${file.name} was NOT deleted"
        }

        toastMessage.postValue(toastText)
    }

    private fun signOut() {
        val cancellable = authClient.signOut()
            .addOnSuccess { setState(FileViewerViewState.SignOut) }
            .addOnFailure { setState(FileViewerViewState.Finish) }
            .execute()

        cancellableCollector.addCancellable(cancellable)
    }

    private fun updateFileClickEvent(event: FileViewerViewEvent.UpdateFileClicked) {
        lastFileClicked = event.file
        setState(FileViewerViewState.ShowUpdateFilePicker)
    }

    private fun handleDownloadSuccess(
        bytes: ByteArrayOutputStream,
        file: OmhFile
    ) {
        val downloadFolder = Environment
            .getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
        val fileToSave = File(downloadFolder, file.getNameWithExtension())
        val fileOutputStream = FileOutputStream(fileToSave)

        bytes.use { byteArrayOutputStream ->
            byteArrayOutputStream.writeTo(fileOutputStream)
        }
    }

    fun getFileName(documentFileName: String?): String = documentFileName ?: DEFAULT_FILE_NAME
}
