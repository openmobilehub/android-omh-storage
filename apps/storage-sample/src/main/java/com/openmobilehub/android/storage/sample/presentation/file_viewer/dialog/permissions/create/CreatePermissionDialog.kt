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

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogCreatePermissionBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.FilePermissionsViewModel
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.create.model.CreatePermissionsViewAction
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.create.model.PermissionType
import com.openmobilehub.android.storage.sample.presentation.util.DefaultArrayAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePermissionDialog : DialogFragment() {

    private val viewModel: CreatePermissionViewModel by viewModels()
    private val parentViewModel: FilePermissionsViewModel by viewModels({ requireParentFragment() })

    private lateinit var binding: DialogCreatePermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DialogCreatePermissionBinding.inflate(
            inflater,
            container,
            false
        ).also {
            binding = it
            setupBinding()
        }.root
    }

    private fun setupBinding() = with(binding) {
        roleSpinner.adapter = object : DefaultArrayAdapter<OmhPermissionRole>(
            requireContext(),
            viewModel.roles,
        ) {
            override fun isEnabled(position: Int): Boolean {
                return !viewModel.disabledRoles.contains(viewModel.roles[position])
            }
        }
        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.roleIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // ignore, non-nullable
            }
        }

        typeSpinner.adapter = object : DefaultArrayAdapter<PermissionType>(
            requireContext(),
            viewModel.types,
        ) {
            override fun isEnabled(position: Int): Boolean {
                return !viewModel.disabledTypes.contains(viewModel.types[position])
            }
        }

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.typeIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // ignore, non-nullable
            }
        }

        emailAddress.doOnTextChanged { text, _, _, _ ->
            viewModel.emailAddress = text.toString()
        }

        domain.doOnTextChanged { text, _, _, _ ->
            viewModel.domain = text.toString()
        }

        message.doOnTextChanged { text, _, _, _ ->
            viewModel.message = text.toString()
        }

        sendNotificationEmail.setOnCheckedChangeListener { _, isChecked ->
            viewModel.sendNotificationEmail = isChecked
        }

        saveButton.setOnClickListener {
            viewModel.create()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        typeSpinner.setSelection(viewModel.typeIndex)
        roleSpinner.setSelection(viewModel.roleIndex)
        domain.setText(viewModel.domain.orEmpty())
        emailAddress.setText(viewModel.emailAddress.orEmpty())
        message.setText(viewModel.message.orEmpty())
        sendNotificationEmail.isChecked = viewModel.sendNotificationEmail
        roleSpinner.setSelection(viewModel.roleIndex)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.type.collect(::buildTypeState)
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.role.collect(::buildRoleState)
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.action.collect(::handleAction)
            }
        }
    }

    private fun buildTypeState(type: PermissionType) {
        when (type) {
            PermissionType.USER -> buildUserOrGroupState()
            PermissionType.GROUP -> buildUserOrGroupState()
            PermissionType.DOMAIN -> buildDomainState()
            PermissionType.ANYONE -> buildAnyoneState()
        }
    }

    private fun buildUserOrGroupState() = with(binding) {
        domainContainer.isVisible = false
        messageContainer.isVisible = true
        emailAddressContainer.isVisible = true
        sendNotificationEmail.isVisible = true
    }

    private fun buildDomainState() = with(binding) {
        domainContainer.isVisible = true
        messageContainer.isVisible = false
        emailAddressContainer.isVisible = false
        sendNotificationEmail.isVisible = false
    }

    private fun buildAnyoneState() = with(binding) {
        domainContainer.isVisible = false
        messageContainer.isVisible = false
        emailAddressContainer.isVisible = false
        sendNotificationEmail.isVisible = false
    }

    private fun buildRoleState(role: OmhPermissionRole) = with(binding) {
        if (role == OmhPermissionRole.OWNER) {
            sendNotificationEmail.isChecked = true
            sendNotificationEmail.isEnabled = false
        } else {
            sendNotificationEmail.isEnabled = true
        }
    }

    private fun handleAction(action: CreatePermissionsViewAction) {
        when (action) {
            is CreatePermissionsViewAction.CreatePermission -> {
                parentViewModel.create(
                    action.permission,
                    action.sendNotificationEmail,
                    action.emailMessage
                )
                dismiss()
            }

            is CreatePermissionsViewAction.ShowError -> {
                binding.emailAddress.error =
                    if (action.emailAddress)
                        getString(R.string.permission_email_required_error)
                    else null
                binding.domain.error =
                    if (action.domain)
                        getString(R.string.permission_domain_required_error)
                    else null
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    companion object {
        const val TAG = "CreatePermissionDialog"
    }
}
