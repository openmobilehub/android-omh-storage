package com.openmobilehub.android.storage.sample.presentation.file_viewer.model

sealed class FileViewerAction {
    object ShowMoreOptions : FileViewerAction()
    object ShowFilePermissions : FileViewerAction()
    object ShowFileVersions : FileViewerAction()
    object ShowFileMetadata : FileViewerAction()
}