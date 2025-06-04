package com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles

import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ExportInfo
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FileMetadata

object TestFileMetadata {

    val testFile: FileMetadata = FileMetadata(
        "file",
        "123456789012345678901234567890ab",
        false,
        "id:testFile1",
        true,
        "testfile.txt",
        "/testfile.txt",
        36,
        "2023-10-01T12:00:00Z",
        "2023-10-01T12:00:00Z",
        null,
        "test file rev"
    )

    val testFileUploadInChunksResult: FileMetadata = FileMetadata(
        "file",
        "123456789012345678901234567890ab",
        false,
        "test file id for multi upload",
        true,
        "test multipart upload.txt",
        "/test multipart upload.txt",
        36,
        "2023-10-01T12:00:00Z",
        "2023-10-01T12:00:00Z",
        null,
        "test file rev"
    )

    val testCreatedFile: FileMetadata = FileMetadata(
        "file",
        "newly created file id",
        false,
        "newly created file id",
        true,
        "test file.txt",
        "/test file.txt",
        0,
        "2023-10-01T12:00:00Z",
        "2023-10-01T12:00:00Z",
        null,
        "test file rev"
    )

    val testCreatedFileWithParent: FileMetadata = FileMetadata(
        "file",
        "newly created file with parent id",
        false,
        "newly created file with parent id",
        true,
        "test file.txt",
        "/test parent folder/test file.txt",
        0,
        "2023-10-01T12:00:00Z",
        "2023-10-01T12:00:00Z",
        null,
        "test file rev"
    )

    val testCreatedPaper: FileMetadata = FileMetadata(
        "file",
        "newly created file id",
        false,
        "newly created file id",
        true,
        "test dropbox.paper",
        "/test dropbox.paper",
        0,
        "2023-10-01T12:00:00Z",
        "2023-10-01T12:00:00Z",
        null,
        "test file rev",
        ExportInfo("html", listOf("markdown", "html"))
    )
}
