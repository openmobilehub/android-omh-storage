package com.openmobilehub.android.storage.core.testdoubles

import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import java.util.Date

val rootFolderList = listOf(
    OmhStorageEntity.OmhFile(
        id = "file1",
        name = "file1.txt",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
        mimeType = "text/plain",
        extension = "txt",
        size = 1
    ),
    OmhStorageEntity.OmhFile(
        id = "file2",
        name = "file2.txt",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
        mimeType = "text/plain",
        extension = "txt",
        size = 2
    ),
    OmhStorageEntity.OmhFile(
        id = "file3",
        name = "file3.txt",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
        mimeType = "text/plain",
        extension = "txt",
        size = 3
    ),
    OmhStorageEntity.OmhFolder(
        id = "folder1",
        name = "folder1",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
    ),
    OmhStorageEntity.OmhFolder(
        id = "folder2",
        name = "folder2",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
    ),
)

val folder1FileList = listOf(
    OmhStorageEntity.OmhFile(
        id = "file4",
        name = "file4.txt",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
        mimeType = "text/plain",
        extension = "txt",
        size = 4
    ),
    OmhStorageEntity.OmhFile(
        id = "file5",
        name = "file5.txt",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
        mimeType = "text/plain",
        extension = "txt",
        size = 5
    ),
)

val folder2FileList = listOf(
    OmhStorageEntity.OmhFolder(
        id = "folder3",
        name = "folder3",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
    ),
)

val folder3FileList = listOf(
    OmhStorageEntity.OmhFolder(
        id = "folder4",
        name = "folder4",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
    ),
)

val folder4FileList = listOf(
    OmhStorageEntity.OmhFolder(
        id = "folder5",
        name = "folder5",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
    ),
)

val folder5FileList = listOf(
    OmhStorageEntity.OmhFolder(
        id = "folder6",
        name = "folder6",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
    ),
)

val folder6FileList = listOf(
    OmhStorageEntity.OmhFile(
        id = "file6",
        name = "file6.txt",
        createdTime = Date(),
        modifiedTime = Date(),
        parentId = null,
        mimeType = "text/plain",
        extension = "txt",
        size = 6
    ),
)
