package com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles

import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FileMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ListFolderResponse

object TestListFolderResponse {

    val testFile1 = FileMetadata(
        tag = "file",
        name = "testFile1.txt",
        path = "/testFile1.txt",
        id = "id:testFile1",
        clientModified = "2023-10-01T12:00:00Z",
        serverModified = "2023-10-01T12:00:00Z",
        size = 1024,
        downloadAble = true,
        rev = "1234567890abcdef",
        hasExplicitSharedMembers = false
    )

    val testFile2 = FileMetadata(
        tag = "file",
        name = "testFile2.txt",
        path = "/testFile2.txt",
        id = "id:testFile2",
        clientModified = "2024-01-01T12:00:00Z",
        serverModified = "2024-01-01T12:00:00Z",
        size = 1025,
        downloadAble = true,
        rev = "1234567890abcdef",
        hasExplicitSharedMembers = false
    )

    val testFileListResponseWithoutNextCursor = ListFolderResponse(
        cursor = "test cursor 1",
        hasMore = false,
        entries = listOf(testFile1)
    )

    val testFileListResponseWithNextCursor = ListFolderResponse(
        cursor = "test cursor 2",
        hasMore = true,
        entries = listOf(testFile1)
    )

    val testFileListResponseWithNextCursor2ndResponse = ListFolderResponse(
        cursor = "test cursor 3",
        hasMore = false,
        entries = listOf(testFile2)
    )
}
