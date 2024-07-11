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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.PermissionsAdapterBinding
import java.util.Date

class FilePermissionAdapter(
    private val listener: ItemListener,
) : ListAdapter<OmhPermission, FilePermissionAdapter.PermissionViewHolder>(DiffCallBack()) {

    private class DiffCallBack : DiffUtil.ItemCallback<OmhPermission>() {
        override fun areItemsTheSame(oldItem: OmhPermission, newItem: OmhPermission) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OmhPermission, newItem: OmhPermission) =
            oldItem == newItem
    }

    interface ItemListener {
        fun onEditClicked(permission: OmhPermission)
        fun onRemoveClicked(permission: OmhPermission)
    }

    class PermissionViewHolder(
        private val binding: PermissionsAdapterBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(permission: OmhPermission, listener: ItemListener) {
            with(binding) {
                val context = root.context

                setupDefaultState(context)

                id.value.text = permission.id
                type.value.text = permission.getType(context)
                role.value.text = permission.role.toString()

                when (permission) {
                    is OmhPermission.IdentityPermission -> {
                        setupBindingWithIdentity(permission.identity)
                    }
                }

                buttonEdit.setOnClickListener {
                    listener.onEditClicked(permission)
                }
                buttonRemove.setOnClickListener {
                    listener.onRemoveClicked(permission)
                }
            }
        }

        private fun setupBindingWithIdentity(identity: OmhIdentity) {
            val context = binding.root.context
            when (identity) {
                is OmhIdentity.Anyone -> {
                    // ignore, view already setup
                }

                is OmhIdentity.Domain -> {
                    showDisplayName(identity.displayName)
                    showDomain(identity.domain)
                }

                is OmhIdentity.Group -> {
                    showIdentityId(identity.id)
                    showDisplayName(identity.displayName)
                    showEmail(identity.emailAddress)
                    showExpirationTime(identity.expirationTime)
                    showDeleted(identity.deleted)
                }

                is OmhIdentity.User -> {
                    showIdentityId(identity.id)
                    showDisplayName(identity.displayName)
                    showEmail(identity.emailAddress)
                    showExpirationTime(identity.expirationTime)
                    showDeleted(identity.deleted)
                    showPhoto(context, identity.photoLink)
                    showPendingOwner(identity.pendingOwner)
                }

                is OmhIdentity.Application -> {
                    showIdentityId(identity.id)
                    showDisplayName(identity.displayName)
                    showExpirationTime(identity.expirationTime)
                }

                is OmhIdentity.Device -> {
                    showIdentityId(identity.id)
                    showDisplayName(identity.displayName)
                    showExpirationTime(identity.expirationTime)
                }
            }
        }

        private fun setupDefaultState(context: Context) = with(binding) {
            photo.isVisible = false
            email.root.isVisible = false
            expirationTime.root.isVisible = false
            deleted.root.isVisible = false
            pendingOwner.root.isVisible = false
            domain.root.isVisible = false
            displayName.root.isVisible = false
            identityId.root.isVisible = false

            id.label.text = context.getString(R.string.permission_label_id)
            displayName.label.text = context.getString(R.string.permission_label_display_name)
            type.label.text = context.getString(R.string.permission_label_type)
            role.label.text = context.getString(R.string.permission_label_role)
            photoText.label.text = context.getString(R.string.permission_label_user_photo)
            email.label.text = context.getString(R.string.permission_label_email)
            expirationTime.label.text = context.getString(R.string.permission_label_expiration_time)
            deleted.label.text = context.getString(R.string.permission_label_deleted)
            pendingOwner.label.text = context.getString(R.string.permission_label_pending_owner)
            domain.label.text = context.getString(R.string.permission_label_domain)
            identityId.label.text = context.getString(R.string.permission_label_identity_id)
        }

        private fun showIdentityId(value: String?) = with(binding) {
            identityId.root.isVisible = true
            identityId.value.text = value.toString()
        }

        private fun showDisplayName(value: String?) = with(binding) {
            displayName.root.isVisible = true
            displayName.value.text = value.toString()
        }

        private fun showEmail(value: String?) = with(binding) {
            email.root.isVisible = true
            email.value.text = value.toString()
        }

        private fun showExpirationTime(value: Date?) = with(binding) {
            expirationTime.root.isVisible = true
            expirationTime.value.text = value.toString()
        }

        private fun showDeleted(value: Boolean?) = with(binding) {
            deleted.root.isVisible = true
            deleted.value.text = value.toString()
        }

        private fun showPendingOwner(value: Boolean?) = with(binding) {
            pendingOwner.root.isVisible = true
            pendingOwner.value.text = value.toString()
        }

        private fun showDomain(value: String) = with(binding) {
            domain.root.isVisible = true
            domain.value.text = value
        }

        private fun showPhoto(context: Context, value: String?) = with(binding) {
            photo.isVisible = true
            photoText.value.text = value.toString()

            if (value.isNullOrEmpty()) {
                photoImage.isVisible = false
            }

            photoImage.isVisible = true
            Glide.with(context)
                .asBitmap()
                .load(value)
                .fitCenter()
                .into(photoImage)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = PermissionViewHolder(
        PermissionsAdapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }
}

private fun OmhPermission.getType(context: Context): String = context.getString(
    when (this) {
        is OmhPermission.IdentityPermission -> {
            getType()
        }
    }
)

@StringRes
private fun OmhPermission.IdentityPermission.getType(): Int {
    return when (this.identity) {
        is OmhIdentity.Anyone -> R.string.permission_type_anyone
        is OmhIdentity.Domain -> R.string.permission_type_domain
        is OmhIdentity.Group -> R.string.permission_type_group
        is OmhIdentity.User -> R.string.permission_type_user
        is OmhIdentity.Application -> R.string.permission_type_application
        is OmhIdentity.Device -> R.string.permission_type_device
    }
}