package com.openmobilehub.android.storage.plugin.onedrive.restful.testdoubles

import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.DriveItem

@Suppress("LongParameterList")
object TestDriveItem {
    fun file(
        id: String = "file-id",
        name: String = "test-file.txt",
        size: Long = 123,
        mimeType: String = "text/plain",
        parentId: String = "PARENT",
        driveId: String = "DRIVE",
        parentPath: String = "/drive/root:/parent",
        webUrl: String = "https://web/${'$'}id"
    ): DriveItem =
        DriveItem(
            id = id,
            name = name,
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-02T00:00:00Z",
            size = size,
            fileInfo = DriveItem.FileInfo(mimeType = mimeType),
            folderInfo = null,
            fileDownloadUrl = "https://download/${'$'}id",
            parentReference = DriveItem.ParentReference(
                id = parentId,
                driveId = driveId,
                path = parentPath
            ),
            webUrl = webUrl
        )

    fun folder(
        id: String = "folder-id",
        name: String = "test-folder",
        childrenCount: Long = 0,
        parentId: String = "PARENT",
        driveId: String = "DRIVE",
        parentPath: String = "/drive/root:/parent",
        webUrl: String = "https://web/${'$'}id"
    ): DriveItem =
        DriveItem(
            id = id,
            name = name,
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-02T00:00:00Z",
            size = 0,
            fileInfo = null,
            folderInfo = DriveItem.FolderInfo(childrenCount = childrenCount),
            fileDownloadUrl = null,
            parentReference = DriveItem.ParentReference(
                id = parentId,
                driveId = driveId,
                path = parentPath
            ),
            webUrl = webUrl
        )
}
