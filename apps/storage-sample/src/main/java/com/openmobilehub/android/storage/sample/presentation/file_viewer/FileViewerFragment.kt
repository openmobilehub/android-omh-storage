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
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.openmobilehub.android.storage.core.domain.model.OmhFile
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogCreateFileBinding
import com.openmobilehub.android.storage.sample.databinding.DialogUploadFileBinding
import com.openmobilehub.android.storage.sample.databinding.FragmentFileViewerBinding
import com.openmobilehub.android.storage.sample.presentation.BaseFragment
import com.openmobilehub.android.storage.sample.presentation.util.navigateTo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FileViewerFragment :
    BaseFragment<FileViewerViewModel, FileViewerViewState, FileViewerViewEvent>(),
    FileAdapter.GridItemListener {

    interface FileViewerFragmentListener {

        fun finishApplication()
    }

    override val viewModel: FileViewerViewModel by viewModels()
    private lateinit var binding: FragmentFileViewerBinding
    private var filesAdapter: FileAdapter? = null
    private lateinit var filePickerUpload: ActivityResultLauncher<String>
    private lateinit var filePickerUpdate: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
                    uri,
                    titleText,
                    positiveText,
                    ::configureUploadFilePositiveButtonEvent
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
                    uri,
                    titleText,
                    positiveText,
                    ::configureUpdateFilePositiveButtonEvent
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
                requestPermissions()
            }
        }

        setupToolbar()
    }

    private fun setupToolbar() {
        val fragmentActivity: FragmentActivity = activity ?: return
        fragmentActivity.addMenuProvider(
            FileViewerMenuProvider(),
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
        fragmentActivity.onBackPressedDispatcher.addCallback {
            dispatchEvent(FileViewerViewEvent.BackPressed)
        }
    }

    fun swapLayout() = dispatchEvent(FileViewerViewEvent.SwapLayoutManager)

    fun createFile() = showCreateFileDialog()

    fun uploadFile() = filePickerUpload.launch(FileViewerViewModel.ANY_MIME_TYPE)

    fun signOut() = dispatchEvent(FileViewerViewEvent.SignOut)

    override fun buildState(state: FileViewerViewState) = when (state) {
        FileViewerViewState.Initial -> buildInitialState()
        FileViewerViewState.Loading -> buildLoadingState()
        is FileViewerViewState.Content -> buildContentState(state)
        is FileViewerViewState.SwapLayoutManager -> buildSwapLayoutManagerState()
        FileViewerViewState.Finish -> buildFinishState()
        FileViewerViewState.CheckPermissions -> requestPermissions()
        FileViewerViewState.SignOut -> buildSignOutState()
        is FileViewerViewState.ShowUpdateFilePicker -> launchUpdateFilePicker()
        FileViewerViewState.ShowDownloadExceptionDialog -> showDownloadExceptionDialog()
    }

    private fun showDownloadExceptionDialog() {
        context?.let { context ->
            val downloadExceptionDialogBuilder = AlertDialog.Builder(context)
                .setTitle(getString(R.string.text_download_error_title))
                .setMessage(getString(R.string.text_download_error_message))
                .setPositiveButton(getString(R.string.text_accept)) { dialog, _ -> dialog.dismiss() }

            val downloadExceptionDialog = downloadExceptionDialogBuilder.create().apply {
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
        val files = state.files

        val (emptyFolderVisibility, recyclerVisibility) = if (files.isEmpty()) {
            Pair(View.VISIBLE, View.GONE)
        } else {
            Pair(View.GONE, View.VISIBLE)
        }

        initializeAdapter()

        with(binding) {
            progressBar.visibility = View.GONE
            noContentLayout.visibility = emptyFolderVisibility
            topPanel.visibility = recyclerVisibility
            filesRecyclerView.visibility = recyclerVisibility
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

    override fun onFileClicked(file: OmhFile) {
        dispatchEvent(FileViewerViewEvent.FileClicked(file))
    }

    override fun onDeleteClicked(file: OmhFile) {
        dispatchEvent(FileViewerViewEvent.DeleteFile(file))
    }

    override fun onUpdateClicked(file: OmhFile) {
        dispatchEvent(FileViewerViewEvent.UpdateFileClicked(file))
    }

    private fun launchUpdateFilePicker() {
        filePickerUpdate.launch(FileViewerViewModel.ANY_MIME_TYPE)
    }

    private fun buildFinishState() {
        val activity = activity as? FileViewerFragmentListener
        activity?.finishApplication()
    }

    private fun showCreateFileDialog() {
        val dialogCreateFileView = DialogCreateFileBinding.inflate(layoutInflater)

        configureCreateFileDialogSpinner(dialogCreateFileView)

        context?.let { context ->

            val createFileDialogBuilder = AlertDialog.Builder(context)
                .setTitle(getString(R.string.text_create_file_title))
                .setPositiveButton("Create") { dialog, _ ->
                    configureCreateFilePositiveButtonEvent(dialogCreateFileView, dialog)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
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
        view: DialogCreateFileBinding,
        dialog: DialogInterface
    ) {
        val fileName = view.fileName.text.toString()
        val fileType = viewModel.createFileSelectedType?.mimeType

        if (fileName.isNotBlank() && !fileType.isNullOrEmpty()) {
            dispatchEvent(FileViewerViewEvent.CreateFile(fileName, fileType))
        }

        dialog.dismiss()
    }

    private fun configureCreateFileDialogSpinner(view: DialogCreateFileBinding) {
        val fileTypes = FileViewerViewModel.listOfFileTypes

        context?.let { context ->

            val fileTypesSpinnerAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                fileTypes.map { fileType -> fileType.name }
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            with(view.fileType) {
                adapter = fileTypesSpinnerAdapter
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val fileType = fileTypes[position]
                        viewModel.createFileSelectedType = fileType.omhFileType
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        val fileType = fileTypes[0]
                        viewModel.createFileSelectedType = fileType.omhFileType
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

            val uploadFileDialogBuilder = AlertDialog.Builder(context)
                .setTitle(titleText)
                .setPositiveButton(positiveTextButton) { dialog, _ ->
                    positiveAction(dialog, fileName, uri)
                }
                .setNegativeButton(getString(R.string.text_cancel)) { dialog, _ ->
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
        dialog: DialogInterface,
        fileName: String,
        uri: Uri
    ) {
        context?.let { context ->

            if (fileName.isNotBlank()) {
                dispatchEvent(FileViewerViewEvent.UploadFile(context, uri, fileName))
            }

            dialog.dismiss()
        }
    }

    private fun configureUpdateFilePositiveButtonEvent(
        dialog: DialogInterface,
        fileName: String,
        uri: Uri
    ) {
        context?.let { context ->

            dispatchEvent(FileViewerViewEvent.UpdateFile(context, uri, fileName))
            dialog.dismiss()
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest: Array<String> = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        context?.let { context ->

            val permissionsToAsk: Array<String> =
                permissionsToRequest.filter { permission: String ->
                    ContextCompat.checkSelfPermission(
                        context,
                        permission
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

    inner class FileViewerMenuProvider : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.file_viewer_menu, menu)
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