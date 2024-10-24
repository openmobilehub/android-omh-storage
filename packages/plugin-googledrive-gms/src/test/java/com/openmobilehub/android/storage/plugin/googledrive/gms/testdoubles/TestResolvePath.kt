package com.openmobilehub.android.storage.plugin.googledrive.gms.testdoubles

import com.google.api.client.util.DateTime
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants
import io.mockk.every
import io.mockk.mockk

val testQueryRootFolder = mockk<FileList>(relaxed = true).also { result ->
    every { result.files } returns listOf(testFolderRsx())
}

val testQueryFolderRsx = mockk<FileList>(relaxed = true).also { result ->
    every { result.files } returns listOf(testFolder1())
}

val testQueryFolder1 = mockk<FileList>(relaxed = true).also { result ->
    every { result.files } returns listOf(testFolder2())
}

val testQueryFolder2 = mockk<FileList>(relaxed = true).also { result ->
    every { result.files } returns listOf(testFolder3())
}

val testQueryFolder3 = mockk<FileList>(relaxed = true).also { result ->
    every { result.files } returns listOf(testFileJpg())
}

fun testFolderRsx(): File = mockk<File>().also { file ->
    every { file.id } returns "id of folder /RSX"
    every { file.name } returns "RSX"
    every { file.createdTime } returns DateTime(TEST_FILE_CREATED_TIME)
    every { file.modifiedTime } returns DateTime(TEST_FILE_MODIFIED_TIME)
    every { file.parents } returns listOf(GoogleDriveGmsConstants.ROOT_FOLDER)
    every { file.mimeType } returns GoogleDriveGmsConstants.FOLDER_MIME_TYPE
    every { file.size } returns 0
}

fun testFolder1(): File = mockk<File>().also { file ->
    every { file.id } returns "id of folder /RSX/1"
    every { file.name } returns "1"
    every { file.createdTime } returns DateTime(TEST_FILE_CREATED_TIME)
    every { file.modifiedTime } returns DateTime(TEST_FILE_MODIFIED_TIME)
    every { file.parents } returns listOf("id of folder /RSX")
    every { file.mimeType } returns GoogleDriveGmsConstants.FOLDER_MIME_TYPE
    every { file.size } returns 0
}

fun testFolder2(): File = mockk<File>().also { file ->
    every { file.id } returns "id of folder /RSX/1/2"
    every { file.name } returns "2"
    every { file.createdTime } returns DateTime(TEST_FILE_CREATED_TIME)
    every { file.modifiedTime } returns DateTime(TEST_FILE_MODIFIED_TIME)
    every { file.parents } returns listOf("id of folder /RSX/1")
    every { file.mimeType } returns GoogleDriveGmsConstants.FOLDER_MIME_TYPE
    every { file.size } returns 0
}

fun testFolder3(): File = mockk<File>().also { file ->
    every { file.id } returns "id of folder /RSX/1/2/3"
    every { file.name } returns "3"
    every { file.createdTime } returns DateTime(TEST_FILE_CREATED_TIME)
    every { file.modifiedTime } returns DateTime(TEST_FILE_MODIFIED_TIME)
    every { file.parents } returns listOf("id of folder /RSX/1/2")
    every { file.mimeType } returns GoogleDriveGmsConstants.FOLDER_MIME_TYPE
    every { file.size } returns 0
}

fun testFileJpg(): File = mockk<File>(relaxed = true).also { file ->
    every { file.id } returns "id of file /RSX/1/2/3/testfile.jpg"
    every { file.name } returns "testfile.jpg"
    every { file.createdTime } returns DateTime(TEST_FILE_CREATED_TIME)
    every { file.modifiedTime } returns DateTime(TEST_FILE_MODIFIED_TIME)
    every { file.parents } returns listOf("id of folder /RSX/1/2/3")
    every { file.mimeType } returns "image/jpeg"
    every { file.size } returns 12345
}
