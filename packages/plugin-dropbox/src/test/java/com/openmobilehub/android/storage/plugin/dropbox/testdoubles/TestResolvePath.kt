package com.openmobilehub.android.storage.plugin.dropbox.testdoubles

import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.ListFolderResult
import io.mockk.every
import io.mockk.mockk

val testQueryRootFolder = mockk<ListFolderResult>(relaxed = true).also { result ->
    every { result.entries } returns listOf(testFolderRsx())
}

val testQueryFolderRsx = mockk<ListFolderResult>(relaxed = true).also { result ->
    every { result.entries } returns listOf(testFolder1())
}

val testQueryFolder1 = mockk<ListFolderResult>(relaxed = true).also { result ->
    every { result.entries } returns listOf(testFolder2())
}

val testQueryFolder2 = mockk<ListFolderResult>(relaxed = true).also { result ->
    every { result.entries } returns listOf(testFolder3())
}

val testQueryFolder3 = mockk<ListFolderResult>(relaxed = true).also { result ->
    every { result.entries } returns listOf(testFileJpg())
}

fun testFolderRsx(): FolderMetadata = mockk<FolderMetadata>().also { folder ->
    every { folder.id } returns "id of folder /RSX"
    every { folder.name } returns "RSX"
    every { folder.parentSharedFolderId } returns ""
}

fun testFolder1(): FolderMetadata = mockk<FolderMetadata>().also { folder ->
    every { folder.id } returns "id of folder /RSX/1"
    every { folder.name } returns "1"
    every { folder.parentSharedFolderId } returns "id of folder /RSX"
}

fun testFolder2(): FolderMetadata = mockk<FolderMetadata>().also { folder ->
    every { folder.id } returns "id of folder /RSX/1/2"
    every { folder.name } returns "2"
    every { folder.parentSharedFolderId } returns "id of folder /RSX/1"
}

fun testFolder3(): FolderMetadata = mockk<FolderMetadata>().also { folder ->
    every { folder.id } returns "id of folder /RSX/1/2/3"
    every { folder.name } returns "3"
    every { folder.parentSharedFolderId } returns "id of folder /RSX/1/2"
}

fun testFileJpg(): FileMetadata = FileMetadata(
    "testfile.jpg",
    "id of file /RSX/1/2/3/testfile.jpg",
    TEST_FILE_MODIFIED_TIME,
    TEST_FILE_MODIFIED_TIME,
    "000000000000000000000",
    12345L
)
