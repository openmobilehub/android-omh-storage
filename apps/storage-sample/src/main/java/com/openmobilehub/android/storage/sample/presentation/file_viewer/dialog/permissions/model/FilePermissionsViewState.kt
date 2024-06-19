package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.model

import com.openmobilehub.android.storage.core.model.OmhFilePermission

data class FilePermissionsViewState(
    val permissions: List<OmhFilePermission>,
    val isLoading: Boolean
)