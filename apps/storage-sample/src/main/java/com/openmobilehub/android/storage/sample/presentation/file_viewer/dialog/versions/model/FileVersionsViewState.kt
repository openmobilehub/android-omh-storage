package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions.model

import com.openmobilehub.android.storage.core.model.OmhFileVersion

data class FileVersionsViewState(
    val versions: List<OmhFileVersion>,
    val isLoading: Boolean,
)
