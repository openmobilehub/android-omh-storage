package com.openmobilehub.android.storage.plugin.dropbox.testdoubles

import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import java.util.Date

const val TEST_FILE_ID = "123"
const val TEST_FILE_NAME = "fileName.txt"
val TEST_FILE_MODIFIED_TIME = TEST_FIRST_JUNE_2024_RFC_3339.fromRFC3339StringToDate()
const val TEST_FILE_PARENT_ID = "parentId"
const val TEST_FILE_MIME_TYPE = "test/mime-type"
const val TEST_FILE_EXTENSION = "txt"

val testOmhFile: OmhStorageEntity.OmhFile = OmhStorageEntity.OmhFile(
    TEST_FILE_ID,
    TEST_FILE_NAME,
    TEST_FILE_MODIFIED_TIME,
    TEST_FILE_PARENT_ID,
    TEST_FILE_MIME_TYPE,
    TEST_FILE_EXTENSION
)

const val TEST_FOLDER_ID = "456"
const val TEST_FOLDER_NAME = "folder"
val TEST_FOLDER_MODIFIED_TIME: Date? = null
const val TEST_FOLDER_PARENT_ID = "parentFolderId"

val testOmhFolder = OmhStorageEntity.OmhFolder(
    TEST_FOLDER_ID,
    TEST_FOLDER_NAME,
    TEST_FOLDER_MODIFIED_TIME,
    TEST_FOLDER_PARENT_ID,
)
