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

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogFilePermissionBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.FileViewerViewModel
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.create.CreatePermissionDialog
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.edit.EditPermissionDialog
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.model.FilePermissionsViewAction
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.model.FilePermissionsViewState
import com.openmobilehub.android.storage.sample.presentation.util.MarginItemDecoration
import com.openmobilehub.android.storage.sample.presentation.util.displayErrorDialog
import com.openmobilehub.android.storage.sample.presentation.util.displayToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilePermissionsDialog : BottomSheetDialogFragment(), FilePermissionAdapter.ItemListener {

    private val parentViewModel: FileViewerViewModel by viewModels({ requireParentFragment() })
    private val permissionsViewModel: FilePermissionsViewModel by viewModels()

    private lateinit var adapter: FilePermissionAdapter
    private lateinit var binding: DialogFilePermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DialogFilePermissionBinding.inflate(
            inflater,
            container,
            false
        ).also {
            binding = it
            setupBinding()
        }.root
    }

    private fun setupBinding() = with(binding) {
        val file = requireNotNull(parentViewModel.lastFileClicked)
        header.title.text = resources.getString(R.string.text_permissions)
        header.fileName.text = file.name

        permissions.layoutManager = LinearLayoutManager(context)
        permissions.addItemDecoration(
            MarginItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_medium))
        )

        adapter = FilePermissionAdapter(this@FilePermissionsDialog).also {
            permissions.adapter = it
        }

        binding.add.setOnClickListener {
            showCreateView()
        }

        binding.getUrl.setOnClickListener {
            permissionsViewModel.getWebUrl()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val file = requireNotNull(parentViewModel.lastFileClicked)

        permissionsViewModel.getPermissions(file)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsViewModel.state.collect(::buildState)
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsViewModel.action.collect(::handleAction)
            }
        }
    }

    private fun buildState(state: FilePermissionsViewState) {
        adapter.submitList(state.permissions)
        binding.progressBar.isVisible = state.isLoading
    }

    private fun handleAction(action: FilePermissionsViewAction) {
        when (action) {
            is FilePermissionsViewAction.ShowToast -> displayToast(action.message)
            FilePermissionsViewAction.ShowEditView -> showEditView()
            is FilePermissionsViewAction.ShowErrorDialog -> displayErrorDialog(
                action.message,
                action.title
            )

            is FilePermissionsViewAction.CopyUrlToClipboard -> copyUrlToClipboard(action.url)
        }
    }

    override fun onEditClicked(permission: OmhPermission) {
        permissionsViewModel.edit(permission)
    }

    override fun onRemoveClicked(permission: OmhPermission) {
        permissionsViewModel.remove(permission)
    }

    private fun showEditView() {
        EditPermissionDialog().show(
            childFragmentManager, EditPermissionDialog.TAG
        )
    }

    private fun showCreateView() {
        CreatePermissionDialog().show(
            childFragmentManager, CreatePermissionDialog.TAG
        )
    }

    private fun copyUrlToClipboard(url: String) {
        val clipboardManager =
            requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        clipboardManager.setPrimaryClip(
            ClipData.newPlainText(
                getString(R.string.permission_url_to_file),
                url
            )
        )
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            displayToast(R.string.permission_url_copied)
    }
}