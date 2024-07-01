package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.metadata.model

import com.openmobilehub.android.storage.core.model.OmhStorageMetadata

data class FileMetadataViewState(
    val metadata: OmhStorageMetadata?,
    val isLoading: Boolean,
    val isOriginalMetadataShown: Boolean
)