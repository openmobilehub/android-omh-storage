package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions.model

import com.openmobilehub.android.storage.core.model.OmhFileRevision

data class FileVersionsViewState(
    val revisions: List<OmhFileRevision>,
    val isLoading: Boolean,
)
