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

import android.Manifest
import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogCreateFileBinding
import com.openmobilehub.android.storage.sample.databinding.DialogUploadFileBinding
import com.openmobilehub.android.storage.sample.databinding.FragmentFileViewerBinding
import com.openmobilehub.android.storage.sample.domain.model.StorageAuthProvider
import com.openmobilehub.android.storage.sample.presentation.BaseFragment
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.menu.FileMenuDialog
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.FilePermissionsDialog
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions.FileVersionsDialog
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.metadata.FileMetadataDialog
import com.openmobilehub.android.storage.sample.presentation.file_viewer.model.FileViewerViewAction
import com.openmobilehub.android.storage.sample.presentation.file_viewer.model.FileViewerViewEvent
import com.openmobilehub.android.storage.sample.presentation.file_viewer.model.FileViewerViewState
import com.openmobilehub.android.storage.sample.presentation.util.navigateTo
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FileViewerFragment :
    BaseFragment<FileViewerViewModel, FileViewerViewState, FileViewerViewEvent>(),
    FileAdapter.GridItemListener {

    override val viewModel: FileViewerViewModel by viewModels()
    private lateinit var binding: FragmentFileViewerBinding

    private var filesAdapter: FileAdapter? = null

    private lateinit var filePickerUpload: ActivityResultLauncher<String>
    private lateinit var filePickerUpdate: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    private val menuProvider = FileViewerMenuProvider(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            dispatchEvent(FileViewerViewEvent.UpdateSearchQuery(query))
            // Force refresh on onQueryTextSubmit, even if the query is the same
            dispatchEvent(FileViewerViewEvent.RefreshFileList)
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            dispatchEvent(FileViewerViewEvent.UpdateSearchQuery(newText))
            return true
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFileViewerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filePickerUpload = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                val titleText = getString(R.string.text_upload_file_title)
                val positiveText = getString(R.string.text_upload)

                showBeforeSubmitFileDialog(
                    uri, titleText, positiveText, ::configureUploadFilePositiveButtonEvent
                )
            }
        }

        filePickerUpdate = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                val titleText = getString(R.string.text_update_file_title)
                val positiveText = getString(R.string.text_update)

                showBeforeSubmitFileDialog(
                    uri, titleText, positiveText, ::configureUpdateFilePositiveButtonEvent
                )
            }
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions: Map<String, Boolean> ->
            val deniedPermissions = mutableListOf<String>()

            permissions.entries.forEach { entry: Map.Entry<String, Boolean> ->
                val permission = entry.key
                val isGranted = entry.value

                if (!isGranted) {
                    deniedPermissions.add(permission)
                }
            }

            if (deniedPermissions.isNotEmpty()) {
                requestDownloadPermissions()
            }
        }

        setupToolbar()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.action.collect {
                    when (it) {
                        FileViewerViewAction.ShowFileMetadata -> showFileMetadata()
                        FileViewerViewAction.ShowFilePermissions -> showFilePermissions()
                        FileViewerViewAction.ShowFileVersions -> showFileVersions()
                        FileViewerViewAction.ShowMoreOptions -> showMoreOptions()
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        val fragmentActivity: FragmentActivity = activity ?: return
        fragmentActivity.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onBackPressed() {
        dispatchEvent(FileViewerViewEvent.BackPressed)
    }

    private fun swapLayout() = dispatchEvent(FileViewerViewEvent.SwapLayoutManager)
    private fun createFile() = showCreateFileDialog()
    private fun uploadFile() = filePickerUpload.launch(FileViewerViewModel.ANY_MIME_TYPE)
    private fun signOut() = dispatchEvent(FileViewerViewEvent.SignOut)

    private fun showFileMetadata() =
        FileMetadataDialog().show(childFragmentManager, FILE_METADATA_DIALOG_TAG)

    private fun showFilePermissions() = FilePermissionsDialog().show(
        childFragmentManager,
        FILE_PERMISSIONS_DIALOG_TAG
    )

    private fun showFileVersions() = FileVersionsDialog().show(
        childFragmentManager,
        FILE_VERSIONS_DIALOG_TAG
    )

    private fun showMoreOptions() = FileMenuDialog().show(
        childFragmentManager,
        FILE_MENU_DIALOG_TAG
    )

    override fun buildState(state: FileViewerViewState) = when (state) {
        FileViewerViewState.Initial -> buildInitialState()
        FileViewerViewState.Loading -> buildLoadingState()
        is FileViewerViewState.Content -> buildContentState(state)
        is FileViewerViewState.SwapLayoutManager -> buildSwapLayoutManagerState()
        FileViewerViewState.Finish -> finishApplication()
        FileViewerViewState.CheckDownloadPermissions -> requestDownloadPermissions()
        FileViewerViewState.SignOut -> buildSignOutState()
        is FileViewerViewState.ShowUpdateFilePicker -> launchUpdateFilePicker()
        FileViewerViewState.ShowDownloadExceptionDialog -> showDownloadExceptionDialog()
        is FileViewerViewState.SaveFile -> saveFile(state)
        FileViewerViewState.ClearSearch -> clearSearch()
        is FileViewerViewState.ShowPermanentlyDeleteDialog -> showPermanentlyDeleteDialog(state)
    }

    private fun saveFile(state: FileViewerViewState.SaveFile) {
        val (file, bytes) = state

        val downloadFolder = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        val fileToSave = File(downloadFolder, file.name)

        // On Android 10 and higher, apps can only write to files in Downloads they have created
        // before, and to make them, they need to use MediaStore.
        if (fileToSave.exists() && fileToSave.canWrite()
            || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        ) {
            writeToFile(fileToSave, bytes)
        } else {
            createInDownloads(file, bytes)
        }

        dispatchEvent(FileViewerViewEvent.SaveFileResult(Result.success(file)))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createInDownloads(file: OmhStorageEntity.OmhFile, bytes: ByteArrayOutputStream) {
        val resolver = context?.contentResolver ?: return

        val downloadsCollection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

        val fileDetails = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, file.name)
            put(MediaStore.Downloads.MIME_TYPE, file.mimeType.orEmpty())
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val fileContentUri = resolver.insert(downloadsCollection, fileDetails) ?: run {
            dispatchEvent(
                FileViewerViewEvent.SaveFileResult(
                    Result.failure(IllegalStateException("Insert to download collection failed"))
                )
            )
            return
        }
        resolver.openOutputStream(fileContentUri, "w")?.use { output ->
            output as FileOutputStream
            output.channel.truncate(0)

            bytes.use { byteArrayOutputStream ->
                byteArrayOutputStream.writeTo(output)
            }
        }

        fileDetails.clear()
        fileDetails.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(fileContentUri, fileDetails, null, null)
    }


    private fun writeToFile(file: File, bytes: ByteArrayOutputStream) {
        val fileOutputStream = FileOutputStream(file)

        bytes.use { byteArrayOutputStream ->
            byteArrayOutputStream.writeTo(fileOutputStream)
        }
    }

    private fun showDownloadExceptionDialog() {
        context?.let { context ->
            val downloadExceptionDialogBuilder =
                AlertDialog.Builder(context).setTitle(getString(R.string.text_download_error_title))
                    .setMessage(getString(R.string.text_download_error_message))
                    .setPositiveButton(getString(R.string.text_accept)) { dialog, _ -> dialog.dismiss() }

            val downloadExceptionDialog = downloadExceptionDialogBuilder.create().apply {
                setCancelable(false)
            }

            downloadExceptionDialog.show()
        }
    }

    private fun showPermanentlyDeleteDialog(state: FileViewerViewState.ShowPermanentlyDeleteDialog) {
        val (file) = state

        context?.let { context ->
            val permanentDeleteDialogBuilder =
                AlertDialog.Builder(context).setTitle(getString(R.string.text_delete_dialog_title))
                    .setMessage(getString(R.string.text_delete_dialog_message))
                    .setPositiveButton(getString(R.string.text_delete)) { _, _ ->
                        dispatchEvent(
                            FileViewerViewEvent.PermanentlyDeleteFile(file)
                        )
                    }
                    .setNegativeButton(getString(R.string.text_cancel)) { dialog, _ -> dialog.dismiss() }

            val downloadExceptionDialog = permanentDeleteDialogBuilder.create().apply {
                setCancelable(false)
            }

            downloadExceptionDialog.show()
        }
    }

    private fun buildInitialState() {
        initializeAdapter()
        dispatchEvent(FileViewerViewEvent.Initialize)
    }

    private fun buildLoadingState() = with(binding) {
        progressBar.visibility = View.VISIBLE
        noContentLayout.visibility = View.GONE
        topPanel.visibility = View.GONE
        filesRecyclerView.visibility = View.GONE
    }

    private fun buildContentState(state: FileViewerViewState.Content) {
        val (files, isSearching) = state

        val (emptyFolderVisibility, recyclerVisibility) = if (files.isEmpty()) {
            Pair(View.VISIBLE, View.GONE)
        } else {
            Pair(View.GONE, View.VISIBLE)
        }

        initializeAdapter()

        with(binding) {
            progressBar.visibility = View.GONE
            noContentLayout.visibility = emptyFolderVisibility
            noContentText.text = resources.getString(
                if (isSearching) R.string.empty_search else R.string.empty_folder
            )
            topPanel.visibility = recyclerVisibility
            filesRecyclerView.visibility = recyclerVisibility
            quota.text = resources.getString(
                R.string.text_quota,
                Formatter.formatFileSize(requireContext(), state.quotaAllocated),
                Formatter.formatFileSize(requireContext(), state.quotaUsed)
            )
        }

        filesAdapter?.submitList(files)
    }

    private fun initializeAdapter() {
        if (filesAdapter != null) {
            return
        }

        filesAdapter = FileAdapter(this, viewModel.isGridLayoutManager)

        context?.let { context ->

            with(binding.filesRecyclerView) {
                layoutManager = if (viewModel.isGridLayoutManager) {
                    GridLayoutManager(context, 2)
                } else {
                    LinearLayoutManager(context)
                }
                adapter = filesAdapter
            }

        }
    }

    private fun buildSwapLayoutManagerState() {
        filesAdapter = null
        initializeAdapter()
        dispatchEvent(FileViewerViewEvent.Initialize)
    }

    override fun onFileClicked(file: OmhStorageEntity) {
        dispatchEvent(FileViewerViewEvent.FileClicked(file))
    }

    override fun onMoreOptionsClicked(file: OmhStorageEntity) {
        dispatchEvent(FileViewerViewEvent.MoreOptionsClicked(file))
    }

    private fun launchUpdateFilePicker() {
        filePickerUpdate.launch(FileViewerViewModel.ANY_MIME_TYPE)
    }

    private fun showCreateFileDialog() {
        val dialogCreateFileView = DialogCreateFileBinding.inflate(layoutInflater)

        configureCreateFileDialogSpinner(dialogCreateFileView)

        context?.let { context ->

            val createFileDialogBuilder =
                AlertDialog.Builder(context).setTitle(getString(R.string.text_create_file_title))
                    .setPositiveButton("Create") { dialog, _ ->
                        configureCreateFilePositiveButtonEvent(dialogCreateFileView, dialog)
                    }.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    }

            val createFileAlertDialog = createFileDialogBuilder.create().apply {
                setCancelable(false)
                setView(dialogCreateFileView.root)
            }

            createFileAlertDialog.show()
        }
    }

    private fun configureCreateFilePositiveButtonEvent(
        view: DialogCreateFileBinding, dialog: DialogInterface
    ) {
        val isGoogleDrive =
            viewModel.storageAuthProvider === StorageAuthProvider.GOOGLE

        val fileName = view.fileName.text.toString()
        val fileType = viewModel.createFileSelectedType

        // Empty file name is not allowed
        if (fileName.isBlank()) {
            dialog.dismiss()
        }

        // Folder creation
        if (fileType == null) {
            dispatchEvent(FileViewerViewEvent.CreateFolder(fileName))
            return dialog.dismiss()
        }

        // Google Drive file creation
        if (isGoogleDrive) {
            dispatchEvent(FileViewerViewEvent.CreateFileWithMimeType(fileName, fileType.mimeType))
            return dialog.dismiss()
        }

        // Extension for other providers cannot be null
        if (fileType.extension == null) {
            dialog.dismiss()
            throw IllegalStateException("File type extension cannot be null")
        }

        // Other providers file creation
        dispatchEvent(FileViewerViewEvent.CreateFileWithExtension(fileName, fileType.extension))
        dialog.dismiss()
    }

    private fun configureCreateFileDialogSpinner(view: DialogCreateFileBinding) {
        val isGoogleDrive =
            viewModel.storageAuthProvider === StorageAuthProvider.GOOGLE
        val fileTypes =
            if (isGoogleDrive) FileViewerViewModel.googleListOfFileTypes else FileViewerViewModel.commonListOfFileTypes

        context?.let { context ->

            val fileTypesSpinnerAdapter = ArrayAdapter(context,
                android.R.layout.simple_spinner_item,
                fileTypes.map { fileType -> fileType.name }).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            with(view.fileType) {
                adapter = fileTypesSpinnerAdapter
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        val fileType = fileTypes[position]
                        viewModel.createFileSelectedType = fileType.fileType
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        val fileType = fileTypes[0]
                        viewModel.createFileSelectedType = fileType.fileType
                    }
                }
            }
        }
    }

    private fun showBeforeSubmitFileDialog(
        uri: Uri,
        titleText: String,
        positiveTextButton: String,
        positiveAction: (DialogInterface, String, Uri) -> Unit
    ) {

        context?.let { context ->

            val dialogUploadFileView = DialogUploadFileBinding.inflate(layoutInflater)
            val documentFileName = DocumentFile.fromSingleUri(context, uri)?.name
            val fileName = viewModel.getFileName(documentFileName)

            dialogUploadFileView.fileName.text = fileName

            val uploadFileDialogBuilder = AlertDialog.Builder(context).setTitle(titleText)
                .setPositiveButton(positiveTextButton) { dialog, _ ->
                    positiveAction(dialog, fileName, uri)
                }.setNegativeButton(getString(R.string.text_cancel)) { dialog, _ ->
                    dialog.cancel()
                }

            val createFileAlertDialog = uploadFileDialogBuilder.create().apply {
                setCancelable(false)
                setView(dialogUploadFileView.root)
            }

            createFileAlertDialog.show()
        }
    }

    private fun configureUploadFilePositiveButtonEvent(
        dialog: DialogInterface, fileName: String, uri: Uri
    ) {
        context?.let { context ->

            if (fileName.isNotBlank()) {
                dispatchEvent(FileViewerViewEvent.UploadFile(context, uri, fileName))
            }

            dialog.dismiss()
        }
    }

    private fun configureUpdateFilePositiveButtonEvent(
        dialog: DialogInterface, fileName: String, uri: Uri
    ) {
        context?.let { context ->

            dispatchEvent(FileViewerViewEvent.UpdateFile(context, uri, fileName))
            dialog.dismiss()
        }
    }

    private fun requestDownloadPermissions() {
        val permissionsToRequest: Array<String> = emptyArray()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        } else emptyArray()


        context?.let { context ->
            val permissionsToAsk: Array<String> =
                permissionsToRequest.filter { permission: String ->
                    ContextCompat.checkSelfPermission(
                        context, permission
                    ) != PackageManager.PERMISSION_GRANTED
                }.toTypedArray()

            if (permissionsToAsk.isNotEmpty()) {
                requestPermissionLauncher.launch(permissionsToAsk)
            } else {
                dispatchEvent(FileViewerViewEvent.DownloadFile)
            }
        }
    }

    private fun buildSignOutState() {
        navigateTo(R.id.action_file_viewer_fragment_to_login_fragment)
    }

    private fun clearSearch() {
        menuProvider.searchView.apply {
            setQuery("", false)
            clearFocus()
            isIconified = true
        }
    }


    companion object {
        const val FILE_MENU_DIALOG_TAG = "file_menu_dialog_tag"
        const val FILE_VERSIONS_DIALOG_TAG = "file_versions_dialog_tag"
        const val FILE_METADATA_DIALOG_TAG = "file_metadata_dialog_tag"
        const val FILE_PERMISSIONS_DIALOG_TAG = "file_permissions_dialog_tag"
    }

    inner class FileViewerMenuProvider(
        private val queryListener: SearchView.OnQueryTextListener
    ) : MenuProvider {

        lateinit var searchView: SearchView
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.file_viewer_menu, menu)

            searchView = menu.findItem(R.id.search).actionView as SearchView
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.queryHint = resources.getString(R.string.text_search_hint)

            searchView.setOnQueryTextListener(queryListener)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.swapGridOrLinear -> swapLayout()
                R.id.createFile -> createFile()
                R.id.uploadFile -> uploadFile()
                R.id.signOut -> signOut()
            }
            return true
        }
    }
}
