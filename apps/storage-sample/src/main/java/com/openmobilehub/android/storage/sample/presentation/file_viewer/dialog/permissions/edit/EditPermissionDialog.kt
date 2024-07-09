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

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.sample.databinding.DialogEditPermissionBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.FilePermissionsViewModel
import com.openmobilehub.android.storage.sample.presentation.util.DefaultArrayAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditPermissionDialog : DialogFragment(), AdapterView.OnItemSelectedListener {

    private val viewModel: EditPermissionViewModel by viewModels()
    private val parentViewModel: FilePermissionsViewModel by viewModels({ requireParentFragment() })

    private lateinit var binding: DialogEditPermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DialogEditPermissionBinding.inflate(
            inflater,
            container,
            false
        ).also {
            binding = it
            setupBinding()
        }.root
    }

    private fun setupBinding() {
        viewModel.role = requireNotNull(parentViewModel.state.value.editedPermission?.role)

        binding.roleSpinner.adapter = object : DefaultArrayAdapter<OmhPermissionRole>(
            requireContext(),
            viewModel.roles,
        ) {
            override fun isEnabled(position: Int): Boolean {
                return !viewModel.disabledRoles.contains(viewModel.roles[position])
            }
        }

        binding.roleSpinner.onItemSelectedListener = this
        binding.roleSpinner.setSelection(viewModel.roleIndex)

        binding.saveButton.setOnClickListener {
            parentViewModel.saveEdits(viewModel.role)
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel.roleIndex = position
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        viewModel.role = null
    }

    companion object {
        const val TAG = "EditPermissionDialog"
    }
}
