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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.utils.folderSize
import com.openmobilehub.android.storage.sample.domain.model.FileType
import com.openmobilehub.android.storage.sample.domain.model.StorageAuthProvider
import com.openmobilehub.android.storage.sample.domain.repository.SessionRepository
import com.openmobilehub.android.storage.sample.presentation.BaseViewModel
import com.openmobilehub.android.storage.sample.presentation.file_viewer.model.DisplayFileType
import com.openmobilehub.android.storage.sample.presentation.file_viewer.model.FileViewerViewAction
import com.openmobilehub.android.storage.sample.presentation.file_viewer.model.FileViewerViewEvent
import com.openmobilehub.android.storage.sample.presentation.file_viewer.model.FileViewerViewState
import com.openmobilehub.android.storage.sample.util.coSignOut
import com.openmobilehub.android.storage.sample.util.isDownloadable
import com.openmobilehub.android.storage.sample.util.isFile
import com.openmobilehub.android.storage.sample.util.isFolder
import com.openmobilehub.android.storage.sample.util.validateSession
import com.openmobilehub.android.storage.sample.util.normalizedFileType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.net.ssl.HttpsURLConnection.HTTP_UNAUTHORIZED

@Suppress("TooManyFunctions")
@OptIn(FlowPreview::class)
@HiltViewModel
class FileViewerViewModel @Inject constructor(
    private val authClient: OmhAuthClient,
    private val omhStorageClient: OmhStorageClient,
    sessionRepository: SessionRepository
) : BaseViewModel<FileViewerViewState, FileViewerViewEvent>(), SessionDelegate {

    companion object {
        val googleListOfFileTypes = listOf(
            DisplayFileType("Folder"),
            DisplayFileType("Document", FileType.GOOGLE_DOCUMENT),
            DisplayFileType("Sheet", FileType.GOOGLE_SPREADSHEET),
            DisplayFileType("Presentation", FileType.GOOGLE_PRESENTATION),
        )

        val commonListOfFileTypes = listOf(
            DisplayFileType("Folder"),
            DisplayFileType("Document", FileType.MICROSOFT_WORD),
            DisplayFileType("Sheet", FileType.MICROSOFT_EXCEL),
            DisplayFileType("Presentation", FileType.MICROSOFT_POWERPOINT),
        )

        const val ANY_MIME_TYPE = "*/*"
        const val DEFAULT_FILE_NAME = "Untitled"
        private const val SEARCH_QUERY_DEBOUNCE_MILLIS = 250L
    }

    private val _action = Channel<FileViewerViewAction>()
    val action = _action.receiveAsFlow()

    var createFileSelectedType: FileType? = null

    var isGridLayoutManager = true
        private set
    var lastFileClicked: OmhStorageEntity? = null
        private set

    val storageAuthProvider = sessionRepository.getStorageAuthProvider()

    private var lastFileVersionClicked: OmhFileVersion? = null

    private val parentId = StackWithFlow(omhStorageClient.rootFolder)
    private var searchQuery: MutableStateFlow<String?> = MutableStateFlow(null)
    private var forceRefresh: MutableStateFlow<Int> = MutableStateFlow(0)
    private var quotaAllocated: MutableStateFlow<Long> = MutableStateFlow(0L)
    private var quotaUsed: MutableStateFlow<Long> = MutableStateFlow(0L)

    val folderSize: MutableLiveData<Long> = MutableLiveData(-1L)

    private val isPermanentlyDeleteSupported: Boolean =
        when (storageAuthProvider) {
            StorageAuthProvider.GOOGLE -> true
            StorageAuthProvider.DROPBOX -> false
            StorageAuthProvider.MICROSOFT -> false
        }

    private val isFolderUpdateSupported: Boolean =
        when (storageAuthProvider) {
            StorageAuthProvider.GOOGLE -> true
            StorageAuthProvider.DROPBOX -> false
            StorageAuthProvider.MICROSOFT -> false
        }

    init {
        viewModelScope.launch {
            combine(
                parentId.peekFlow,
                searchQuery
                    .debounce(SEARCH_QUERY_DEBOUNCE_MILLIS),
                forceRefresh,
                quotaAllocated,
                quotaUsed
            ) { parentId, searchQuery, _, _, _ ->
                setState(FileViewerViewState.Loading)

                val files = if (searchQuery.isNullOrBlank()) {
                    omhStorageClient.listFiles(parentId)
                } else {
                    omhStorageClient.search(searchQuery)
                }

                val quotaAllocated = omhStorageClient.getStorageQuota()
                val quotaUsed = omhStorageClient.getStorageUsage()

                return@combine FileViewerViewState.Content(
                    files.sortedWith(compareBy<OmhStorageEntity> { it is OmhStorageEntity.OmhFile }
                        .thenBy { (it as? OmhStorageEntity.OmhFile)?.mimeType }
                        .thenBy { it.name }
                    ),
                    !searchQuery.isNullOrEmpty(),
                    quotaAllocated,
                    quotaUsed)
            }
                .catch {
                    handleException(it)
                    emit(FileViewerViewState.Content(emptyList(), false, 0L, 0L))
                }
                .onEach {
                    setState(FileViewerViewState.Content(
                            it.files,
                            !searchQuery.value.isNullOrEmpty(),
                            it.quotaAllocated,
                            it.quotaUsed
                        )
                    )
                }
                .flowOn(Dispatchers.IO)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), FileViewerViewState.Content(
                    emptyList(), false, 0L, 0L
                ))
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
            is FileViewerViewEvent.FileVersionClicked -> fileVersionClicked(event)
            is FileViewerViewEvent.BackPressed -> backPressedEvent()
            is FileViewerViewEvent.CreateFileWithMimeType -> createFileWithMimeTypeEvent(event)
            is FileViewerViewEvent.CreateFileWithExtension -> createFileWithExtensionEvent(event)
            is FileViewerViewEvent.CreateFolder -> createFolderEvent(event)
            is FileViewerViewEvent.DeleteFile -> deleteFileEvent(event)
            is FileViewerViewEvent.PermanentlyDeleteFileClicked -> permanentlyDeleteFileEventClicked(
                event
            )

            is FileViewerViewEvent.PermanentlyDeleteFile -> permanentlyDeleteFileEvent(event)
            is FileViewerViewEvent.UploadFile -> uploadFile(event)
            is FileViewerViewEvent.UpdateFile -> updateFileEvent(event)
            is FileViewerViewEvent.SignOut -> signOut()
            is FileViewerViewEvent.DownloadFile -> downloadFileEvent()
            is FileViewerViewEvent.SaveFileResult -> saveFileResultEvent(event)
            is FileViewerViewEvent.UpdateFileClicked -> updateFileClickEvent(event)
            is FileViewerViewEvent.UpdateSearchQuery -> updateSearchQuery(event)
            is FileViewerViewEvent.FileMetadataClicked -> onFileMetadata(event)
            is FileViewerViewEvent.FilePermissionsClicked -> onFilePermissions(event)
            is FileViewerViewEvent.FileVersionsClicked -> onFileVersions(event)
            is FileViewerViewEvent.MoreOptionsClicked -> onMoreOptions(event)
            is FileViewerViewEvent.GetFolderSize -> onGetFolderSize(event)
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
            setState(FileViewerViewState.ClearSearch)
            parentId.push(fileId)
        } else {
            lastFileClicked = file
            setState(FileViewerViewState.CheckDownloadPermissions)
        }
    }

    private fun fileVersionClicked(event: FileViewerViewEvent.FileVersionClicked) {
        lastFileVersionClicked = event.version
        setState(FileViewerViewState.CheckDownloadPermissions)
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

        (lastFileClicked as? OmhStorageEntity.OmhFile)?.let { file ->
            if (!file.isDownloadable()) {
                toastMessage.postValue("${file.name} is not downloadable")
                refreshFileListEvent()
                return
            }

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val data: ByteArrayOutputStream?
                    var fileToSave = file

                    if (lastFileVersionClicked !== null) {
                        data = omhStorageClient.downloadFileVersion(
                            file.id,
                            lastFileVersionClicked!!.versionId
                        )
                    } else {
                        val isGoogleWorkspaceFile =
                            file.mimeType?.startsWith("application/vnd.google-apps")

                        if (isGoogleWorkspaceFile == true) {
                            val normalizedFileType = file.normalizedFileType()
                            data = omhStorageClient.exportFile(file.id, normalizedFileType.mimeType)
                            fileToSave = file.copy(
                                name = "${file.name}.${normalizedFileType.extension}",
                                mimeType = normalizedFileType.mimeType,
                                extension = normalizedFileType.extension
                            )
                        } else {
                            data = omhStorageClient.downloadFile(file.id)
                        }
                    }

                    setState(FileViewerViewState.SaveFile(fileToSave, data))
                } catch (exception: Exception) {
                    handleException(exception, "ERROR: ${file.name} was NOT downloaded")
                    refreshFileListEvent()
                } finally {
                    lastFileVersionClicked = null
                }
            }
        } ?: run {
            toastMessage.postValue("The file was NOT downloaded")
            refreshFileListEvent()
            lastFileVersionClicked = null
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
            } catch (exception: Exception) {
                handleException(exception, "ERROR: ${event.fileName} was not updated")
            }
            refreshFileListEvent()
        }
    }

    private fun createFileWithExtensionEvent(event: FileViewerViewEvent.CreateFileWithExtension) {
        handleFileCreationEvent {
            omhStorageClient.createFileWithExtension(
                event.name,
                event.extension,
                parentId.peek()
            )
        }
    }

    private fun createFileWithMimeTypeEvent(event: FileViewerViewEvent.CreateFileWithMimeType) {
        handleFileCreationEvent {
            omhStorageClient.createFileWithMimeType(
                event.name,
                event.mimeType,
                parentId.peek()
            )
        }
    }

    private fun createFolderEvent(event: FileViewerViewEvent.CreateFolder) {
        handleFileCreationEvent { omhStorageClient.createFolder(event.name, parentId.peek()) }
    }

    private fun handleFileCreationEvent(fileCreation: suspend () -> Unit) {
        setState(FileViewerViewState.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                fileCreation()
                refreshFileListEvent()
            } catch (exception: Exception) {
                handleException(exception)
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
                handleException(exception, "ERROR: ${event.fileName} was NOT uploaded")
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
                omhStorageClient.deleteFile(file.id)
                handleDeleteSuccess(file)
                refreshFileListEvent()
            } catch (exception: OmhStorageException.ApiException) {
                handleException(exception, "ERROR: ${file.name} was NOT deleted")
                refreshFileListEvent()
            }
        }
    }

    private fun permanentlyDeleteFileEventClicked(event: FileViewerViewEvent.PermanentlyDeleteFileClicked) {
        if (isPermanentlyDeleteSupported) {
            setState(FileViewerViewState.ShowPermanentlyDeleteDialog(event.file))
        } else {
            toastMessage.postValue("Permanent deleting is not supported by provider")
        }
    }

    private fun permanentlyDeleteFileEvent(event: FileViewerViewEvent.PermanentlyDeleteFile) {
        setState(FileViewerViewState.Loading)

        val file = event.file

        viewModelScope.launch(Dispatchers.IO) {
            try {
                omhStorageClient.permanentlyDeleteFile(file.id)
                handleDeleteSuccess(file)
                refreshFileListEvent()
            } catch (exception: OmhStorageException.ApiException) {
                handleException(exception, "ERROR: ${file.name} was NOT permanently deleted")
                refreshFileListEvent()
            }
        }
    }

    private fun handleDeleteSuccess(file: OmhStorageEntity) {
        toastMessage.postValue("${file.name} was successfully deleted")
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
        if (event.file.isFolder() && !isFolderUpdateSupported) {
            toastMessage.postValue("Updating folders is not supported by provider")
            return
        }

        if (event.file.isFile()) {
            val file = event.file as OmhStorageEntity.OmhFile
            val isGoogleWorkspaceFile =
                file.mimeType?.startsWith("application/vnd.google-apps") == true

            if (isGoogleWorkspaceFile) {
                toastMessage.postValue("Updating Google Workspace files is not supported")
                return
            }
        }

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

    private fun onFileVersions(event: FileViewerViewEvent.FileVersionsClicked) {
        lastFileClicked = event.file
        viewModelScope.launch {
            _action.send(FileViewerViewAction.ShowFileVersions)
        }
    }

    private fun onFilePermissions(event: FileViewerViewEvent.FilePermissionsClicked) {
        lastFileClicked = event.file
        viewModelScope.launch {
            _action.send(FileViewerViewAction.ShowFilePermissions)
        }
    }

    private fun onFileMetadata(event: FileViewerViewEvent.FileMetadataClicked) {
        lastFileClicked = event.file
        viewModelScope.launch {
            _action.send(FileViewerViewAction.ShowFileMetadata)
        }
    }

    private fun onMoreOptions(event: FileViewerViewEvent.MoreOptionsClicked) {
        lastFileClicked = event.file
        viewModelScope.launch {
            _action.send(FileViewerViewAction.ShowMoreOptions)
        }
    }

    private fun onGetFolderSize(event: FileViewerViewEvent.GetFolderSize) {
        viewModelScope.launch(Dispatchers.IO) {
            folderSize.postValue(-1L)
            folderSize.postValue(omhStorageClient.folderSize(event.folder.id))
        }
    }

    private suspend fun handleException(exception: Throwable, message: String? = null) {
        if (exception is OmhStorageException && handleUnauthorized(exception)) {
            return
        }

        errorDialogMessage.postValue(exception.message)
        toastMessage.postValue(message ?: exception.message)
        exception.printStackTrace()
    }

    @Suppress("ReturnCount")
    override suspend fun handleUnauthorized(exception: Exception): Boolean {
        if (exception !is OmhStorageException.ApiException || exception.statusCode != HTTP_UNAUTHORIZED) {
            return false
        }

        if (authClient.validateSession()) {
            toastMessage.postValue("Access token refreshed. Please retry.")
            return true
        }

        toastMessage.postValue("Session expired, singing out.")
        setState(FileViewerViewState.SignOut)
        return true
    }

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
