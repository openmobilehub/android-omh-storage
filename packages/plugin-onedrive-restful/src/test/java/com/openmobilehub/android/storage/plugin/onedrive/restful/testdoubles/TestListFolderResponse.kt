package com.openmobilehub.android.storage.plugin.onedrive.restful.testdoubles

import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.DriveItem
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.ListFolderResponse

object TestListFolderResponse {
    // Root listing single page
    val testFile1 = DriveItem(
        id = "file1",
        name = "File 1",
        createdAt = "2023-01-01T00:00:00Z",
        updatedAt = "2023-01-02T00:00:00Z",
        size = 123,
        fileInfo = DriveItem.FileInfo(mimeType = "text/plain"),
        folderInfo = null,
        fileDownloadUrl = "https://download/file1",
        parentReference = DriveItem.ParentReference(
            id = "PARENT",
            driveId = "DRIVE",
            path = "/drive/root:/parent"
        ),
        webUrl = "https://web/file1"
    )

    val testFolder1 = DriveItem(
        id = "folder1",
        name = "Folder 1",
        createdAt = "2023-01-01T00:00:00Z",
        updatedAt = "2023-01-02T00:00:00Z",
        size = 0,
        fileInfo = null,
        folderInfo = DriveItem.FolderInfo(childrenCount = 0),
        fileDownloadUrl = null,
        parentReference = DriveItem.ParentReference(
            id = "PARENT",
            driveId = "DRIVE",
            path = "/drive/root:/parent"
        ),
        webUrl = "https://web/folder1"
    )

    val rootSinglePage = ListFolderResponse(
        entries = listOf(testFile1, testFolder1),
        nextLink = null
    )

    // Paginated listing
    val page1File = DriveItem(
        id = "f1",
        name = "P1-File",
        createdAt = "2023-01-01T00:00:00Z",
        updatedAt = "2023-01-02T00:00:00Z",
        size = 10,
        fileInfo = DriveItem.FileInfo(mimeType = "text/plain"),
        folderInfo = null,
        fileDownloadUrl = "https://download/f1",
        parentReference = DriveItem.ParentReference(
            id = "PARENT",
            driveId = "DRIVE",
            path = "/drive/root:/parent"
        ),
        webUrl = "https://web/f1"
    )

    val page2Folder = DriveItem(
        id = "d2",
        name = "P2-Folder",
        createdAt = "2023-01-01T00:00:00Z",
        updatedAt = "2023-01-02T00:00:00Z",
        size = 0,
        fileInfo = null,
        folderInfo = DriveItem.FolderInfo(childrenCount = 0),
        fileDownloadUrl = null,
        parentReference = DriveItem.ParentReference(
            id = "PARENT",
            driveId = "DRIVE",
            path = "/drive/root:/parent"
        ),
        webUrl = "https://web/d2"
    )

    val paginatedFirstPage = ListFolderResponse(
        entries = listOf(page1File),
        nextLink = "https://graph/children?%24skiptoken=abc"
    )

    val paginatedSecondPage = ListFolderResponse(
        entries = listOf(page2Folder),
        nextLink = null
    )
}
