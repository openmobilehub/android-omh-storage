package com.openmobilehub.android.storage.plugin.googledrive.nongms.testdoubles

import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileRemoteResponse
import retrofit2.Response

internal val testFolderRsx = FileRemoteResponse(
    "id of folder /RSX",
    "RSX",
    TEST_FIRST_MAY_2024_RFC_3339,
    TEST_FIRST_JUNE_2024_RFC_3339,
    listOf(GoogleDriveNonGmsConstants.ROOT_FOLDER),
    GoogleDriveNonGmsConstants.FOLDER_MIME_TYPE,
    "",
    0
)

internal val testFolder1 = FileRemoteResponse(
    "id of folder /RSX/1",
    "1",
    TEST_FIRST_MAY_2024_RFC_3339,
    TEST_FIRST_JUNE_2024_RFC_3339,
    listOf("id of folder /RSX"),
    GoogleDriveNonGmsConstants.FOLDER_MIME_TYPE,
    "",
    0
)

internal val testFolder2 = FileRemoteResponse(
    "id of folder /RSX/1/2",
    "2",
    TEST_FIRST_MAY_2024_RFC_3339,
    TEST_FIRST_JUNE_2024_RFC_3339,
    listOf("id of folder /RSX/1"),
    GoogleDriveNonGmsConstants.FOLDER_MIME_TYPE,
    "",
    0
)

internal val testFolder3 = FileRemoteResponse(
    "id of folder /RSX/1/2/3",
    "3",
    TEST_FIRST_MAY_2024_RFC_3339,
    TEST_FIRST_JUNE_2024_RFC_3339,
    listOf("id of folder /RSX/1/2"),
    GoogleDriveNonGmsConstants.FOLDER_MIME_TYPE,
    "",
    0
)

internal val testFileJpg = FileRemoteResponse(
    "id of file /RSX/1/2/3/testfile.jpg",
    "testfile.jpg",
    TEST_FIRST_MAY_2024_RFC_3339,
    TEST_FIRST_JUNE_2024_RFC_3339,
    listOf("id of folder /RSX/1/2/3"),
    "image/jpeg",
    "jpg",
    12345
)

internal val testQueryRootFolder = Response.success(
    FileListRemoteResponse(listOf(testFolderRsx))
)

internal val testQueryFolderRsx = Response.success(
    FileListRemoteResponse(listOf(testFolder1))
)

internal val testQueryFolder1 = Response.success(
    FileListRemoteResponse(listOf(testFolder2))
)

internal val testQueryFolder2 = Response.success(
    FileListRemoteResponse(listOf(testFolder3))
)

internal val testQueryFolder3 = Response.success(
    FileListRemoteResponse(listOf(testFileJpg))
)
