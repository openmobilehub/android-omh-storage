package com.openmobilehub.android.storage.plugin.onedrive.testdoubles

import com.openmobilehub.android.storage.core.model.OmhStorageEntity

const val TEST_FILE_MIME_TYPE = "test/mime-type"
const val TEST_FILE_ID = "123"
const val TEST_FILE_NAME = "fileName.txt"
const val TEST_FILE_MODIFIED_TIME = TEST_FIRST_JUNE_2024_RFC_3339
const val TEST_FILE_PARENT_ID = "parentId"

val testOmhStorageEntity = OmhStorageEntity(
    TEST_FILE_MIME_TYPE,
    TEST_FILE_ID,
    TEST_FILE_NAME,
    TEST_FILE_MODIFIED_TIME,
    TEST_FILE_PARENT_ID,
)

const val TEST_FOLDER_MIME_TYPE = "application/vnd.openmobilehub.folder"
const val TEST_FOLDER_ID = "456"
const val TEST_FOLDER_NAME = "folder"
const val TEST_FOLDER_MODIFIED_TIME = TEST_FIRST_JUNE_2024_RFC_3339
const val TEST_FOLDER_PARENT_ID = "parentFolderId"

val testOmhFolder = OmhStorageEntity(
    TEST_FOLDER_MIME_TYPE,
    TEST_FOLDER_ID,
    TEST_FOLDER_NAME,
    TEST_FOLDER_MODIFIED_TIME,
    TEST_FOLDER_PARENT_ID,
)
