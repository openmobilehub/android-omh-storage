package com.openmobilehub.android.storage.plugin.onedrive.testdoubles

import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import java.util.Date

const val TEST_FILE_ID = "123"
const val TEST_FILE_NAME = "fileName.txt"
val TEST_FILE_CREATED_TIME = Date(TEST_FIRST_MAY_2024_MILLIS)
val TEST_FILE_MODIFIED_TIME = Date(TEST_FIRST_JUNE_2024_MILLIS)
const val TEST_FILE_PARENT_ID = "parentId"
const val TEST_FILE_MIME_TYPE = "test/mime-type"
const val TEST_FILE_SIZE = 10

val testOmhFile = OmhStorageEntity.OmhStorageFile(
    TEST_FILE_ID,
    TEST_FILE_NAME,
    TEST_FILE_CREATED_TIME,
    TEST_FILE_MODIFIED_TIME,
    TEST_FILE_PARENT_ID,
    TEST_FILE_MIME_TYPE,
    TEST_FILE_SIZE
)

const val TEST_FOLDER_ID = "456"
const val TEST_FOLDER_NAME = "folder"
val TEST_FOLDER_CREATED_TIME = null // Folders do not have created time
val TEST_FOLDER_MODIFIED_TIME = null // Folders do not have modified time
const val TEST_FOLDER_PARENT_ID = "parentFolderId"

val testOmhFolder = OmhStorageEntity.OmhStorageFolder(
    TEST_FOLDER_ID,
    TEST_FOLDER_NAME,
    TEST_FOLDER_CREATED_TIME,
    TEST_FOLDER_MODIFIED_TIME,
    TEST_FOLDER_PARENT_ID,
)
