/*
 * Copyright 2023 Open Mobile Hub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openmobilehub.android.storage.plugin.dropbox.testdoubles

import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import java.util.Date

const val TEST_FILE_ID = "123"
const val TEST_FILE_NAME = "fileName.txt"
val TEST_FILE_CREATED_TIME = null
val TEST_FILE_MODIFIED_TIME = TEST_FIRST_JUNE_2024_RFC_3339.fromRFC3339StringToDate()!!
const val TEST_FILE_PARENT_ID = "parentId"
const val TEST_FILE_MIME_TYPE = "test/mime-type"
const val TEST_FILE_EXTENSION = "txt"
const val TEST_FILE_SIZE: Long = 10
const val TEST_FILE_PATH = "/$TEST_FILE_NAME"

val testOmhFile: OmhStorageEntity.OmhFile = OmhStorageEntity.OmhFile(
    TEST_FILE_ID,
    TEST_FILE_NAME,
    TEST_FILE_CREATED_TIME,
    TEST_FILE_MODIFIED_TIME,
    TEST_FILE_PARENT_ID,
    TEST_FILE_MIME_TYPE,
    TEST_FILE_EXTENSION,
    TEST_FILE_SIZE.toInt()
)

const val TEST_FOLDER_ID = "456"
const val TEST_FOLDER_NAME = "folder"
val TEST_FOLDER_CREATED_TIME: Date? = null
val TEST_FOLDER_MODIFIED_TIME: Date? = null
const val TEST_FOLDER_PARENT_ID = "parentFolderId"

val testOmhFolder = OmhStorageEntity.OmhFolder(
    TEST_FOLDER_ID,
    TEST_FOLDER_NAME,
    TEST_FOLDER_CREATED_TIME,
    TEST_FOLDER_MODIFIED_TIME,
    TEST_FOLDER_PARENT_ID,
)
