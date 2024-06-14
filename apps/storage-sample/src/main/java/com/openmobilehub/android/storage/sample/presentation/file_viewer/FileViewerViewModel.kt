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
import androidx.lifecycle.viewModelScope
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhFileType
import com.openmobilehub.android.storage.sample.domain.model.FileType
import com.openmobilehub.android.storage.sample.presentation.BaseViewModel
import com.openmobilehub.android.storage.sample.util.coSignOut
import com.openmobilehub.android.storage.sample.util.isDownloadable
import com.openmobilehub.android.storage.sample.util.normalizedMimeType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@HiltViewModel
class FileViewerViewModel @Inject constructor(
    private val authClient: OmhAuthClient,
    private val omhStorageClient: OmhStorageClient
) : BaseViewModel<FileViewerViewState, FileViewerViewEvent>() {

    companion object {
        val listOfFileTypes = listOf(
            FileType("Folder", OmhFileType.FOLDER),
            FileType("Document", OmhFileType.DOCUMENT),
            FileType("Sheet", OmhFileType.SPREADSHEET),
            FileType("Presentation", OmhFileType.PRESENTATION),
        )

        const val ANY_MIME_TYPE = "*/*"
        const val DEFAULT_FILE_NAME = "Untitled"
        private const val SEARCH_QUERY_DEBOUNCE_MILLIS = 250L
    }

    var isGridLayoutManager = true
    var createFileSelectedType: OmhFileType? = null
    private var lastFileClicked: OmhFile? = null

    private val parentId = StackWithFlow(omhStorageClient.rootFolder)
    private var searchQuery: MutableStateFlow<String?> = MutableStateFlow(null)
    private var forceRefresh: MutableStateFlow<Int> = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            combine(
                parentId.peekFlow,
                searchQuery
                    .debounce(SEARCH_QUERY_DEBOUNCE_MILLIS),
                forceRefresh
            ) { parentId, searchQuery, _ ->
                setState(FileViewerViewState.Loading)

                val files = if (searchQuery.isNullOrBlank()) {
                    omhStorageClient.listFiles(parentId)
                } else {
                    omhStorageClient.search(searchQuery)
                }

                return@combine files.sortedWith(
                    compareBy<OmhFile> { !it.isFolder() }
                        .thenBy { it.mimeType }
                        .thenBy { it.name }
                )
            }
                .catch {
                    errorDialogMessage.postValue(it.message)
                    toastMessage.postValue(it.message)
                    it.printStackTrace()
                    emit(emptyList())
                }
                .onEach {
                    setState(FileViewerViewState.Content(it, !searchQuery.value.isNullOrEmpty()))
                }
                .flowOn(Dispatchers.IO)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
                .collect {
                    // we use onEach to update state, even it the result is the same
                }
        }
    }

    override fun getInitialState(): FileViewerViewState = FileViewerViewState.Initial

    override fun processEvent(event: FileViewerViewEvent) {
        when (event) {
            is FileViewerViewEvent.Initialize -> initializeEvent()
            is FileViewerViewEvent.RefreshFileList -> refreshFileListEvent()
            is FileViewerViewEvent.SwapLayoutManager -> swapLayoutManagerEvent()
            is FileViewerViewEvent.FileClicked -> fileClickedEvent(event)
            is FileViewerViewEvent.BackPressed -> backPressedEvent()
            is FileViewerViewEvent.CreateFile -> createFileEvent(event)
            is FileViewerViewEvent.DeleteFile -> deleteFileEvent(event)
            is FileViewerViewEvent.UploadFile -> uploadFile(event)
            is FileViewerViewEvent.UpdateFile -> updateFileEvent(event)
            is FileViewerViewEvent.SignOut -> signOut()
            is FileViewerViewEvent.DownloadFile -> downloadFileEvent()
            is FileViewerViewEvent.SaveFileResult -> saveFileResultEvent(event)
            is FileViewerViewEvent.UpdateFileClicked -> updateFileClickEvent(event)
            is FileViewerViewEvent.UpdateSearchQuery -> updateSearchQuery(event)
        }
    }

    private fun initializeEvent() {
        refreshFileListEvent()
    }

    private fun refreshFileListEvent() {
        // We use a flow of random values to force emitting new value from files flow
        forceRefresh.value = Random.Default.nextInt()
    }

    private fun swapLayoutManagerEvent() {
        isGridLayoutManager = !isGridLayoutManager
        setState(FileViewerViewState.SwapLayoutManager)
    }

    private fun fileClickedEvent(event: FileViewerViewEvent.FileClicked) {
        val file = event.file

        if (file.isFolder()) {
            val fileId = file.id
            searchQuery.value = ""
            parentId.push(fileId)
        } else {
            lastFileClicked = file
            setState(FileViewerViewState.CheckDownloadPermissions)
        }
    }

    private fun backPressedEvent() {
        if (parentId.peek() == omhStorageClient.rootFolder) {
            setState(FileViewerViewState.Finish)
        } else {
            parentId.pop()
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
                    setState(FileViewerViewState.SaveFile(file, data))
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
        val parentId = parentId.peek()

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

        val parentId = parentId.peek()
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
        viewModelScope.launch {
            try {
                authClient.coSignOut()
                setState(FileViewerViewState.SignOut)
            } catch (e: Exception) {
                setState(FileViewerViewState.Finish)
            }
        }
    }

    private fun updateFileClickEvent(event: FileViewerViewEvent.UpdateFileClicked) {
        lastFileClicked = event.file
        setState(FileViewerViewState.ShowUpdateFilePicker)
    }

    private fun updateSearchQuery(event: FileViewerViewEvent.UpdateSearchQuery) {
        searchQuery.value = event.query
    }

    private fun saveFileResultEvent(event: FileViewerViewEvent.SaveFileResult) {
        event.result.run {
            if (isSuccess) {
                toastMessage.postValue("${getOrThrow().name} was successfully downloaded")
                refreshFileListEvent()
            } else {
                setState(FileViewerViewState.ShowDownloadExceptionDialog)
            }
        }
    }

    fun getFileName(documentFileName: String?): String = documentFileName ?: DEFAULT_FILE_NAME

    // Utility class for encapsulating stack operation and exposing a flow with the current peek value
    private class StackWithFlow<T>(first: T) {
        private val stack = ArrayDeque<T>().apply { add(first) }
        private val _flow: MutableStateFlow<T> = MutableStateFlow(stack.first())
        val peekFlow: StateFlow<T> = _flow

        fun push(value: T) {
            stack.addFirst(value)
            _flow.value = stack.first()
        }

        fun pop() {
            stack.removeFirst()
            _flow.value = stack.first()
        }

        fun peek(): T {
            return _flow.value
        }
    }
}
