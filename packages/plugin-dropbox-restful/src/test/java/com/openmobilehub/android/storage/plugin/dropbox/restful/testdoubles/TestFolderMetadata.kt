package com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles

import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FolderMetadata

object TestFolderMetadata {

    val testParentFolder: FolderMetadata = FolderMetadata(
        "folder",
        "id:test folder id",
        "test parent folder",
        "/test parent folder"
    )

    val testFolder: FolderMetadata = FolderMetadata(
        "folder",
        "id:newly created folder id",
        "newly created folder name",
        "/new folder",
        sharedFolderId = "shared folder id",
    )
}
